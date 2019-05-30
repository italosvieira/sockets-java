import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Cliente {
    private static String usuario;
    private static volatile Boolean isSocketOpen = Boolean.TRUE;
    private static final String MENSAGEM_DESCONECTADO = "Desconectando da aplicação por inatividade de 60 segundos.";
    private static final String MENSAGEM_SAIDA_GRUPO = "Saindo do grupo e finalizando a aplicação.";
    private static final String SAIR = "sair";
    private static final String DOIS_PONTOS = ":";
    private static final String PONTO_E_VIRGULA = ";";
    private static final String LIST_USERS = "_listUsers";
    private static final String LIST_USERS_KEY = "_listUsers;";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        inicializarUsuario(scanner);
        InetSocketAddress inetSocketAddress = inicializarInetSocketAdress(args);
        MulticastSocket multicastSocket = inicializarMulticastSocket(inetSocketAddress);

        listarUsuariosNoGrupo(inetSocketAddress);

        new Thread(() -> receberMensagem(multicastSocket, inetSocketAddress)).start();

        Logger.info("Conectado no chat. Pode começar a mandar mensagens.");

        while (true) {
            ScheduledFuture scheduledFuture = Executors.newSingleThreadScheduledExecutor().schedule(() -> sairDoGrupo(multicastSocket, inetSocketAddress, MENSAGEM_DESCONECTADO), 60, TimeUnit.SECONDS);
            String mensagem = scanner.nextLine();
            scheduledFuture.cancel(true);

            if (SAIR.equalsIgnoreCase(mensagem)) {
                sairDoGrupo(multicastSocket, inetSocketAddress, MENSAGEM_SAIDA_GRUPO);
            } else {
                enviarMensagem(multicastSocket, inetSocketAddress, usuario + DOIS_PONTOS + mensagem);
            }
        }
    }

    private static void inicializarUsuario(Scanner scanner) {
        Logger.infoSameLine("Digite o seu nome: ");
        usuario = scanner.nextLine().trim();
    }

    private static InetSocketAddress inicializarInetSocketAdress(String[] args) {
        try {
            int portaDestino = 6789;
            InetAddress ipGrupoDestino = InetAddress.getByName("224.225.226.227");

            if (args.length == 2) {
                ipGrupoDestino = InetAddress.getByName(args[0]);
                portaDestino = Integer.parseInt(args[1]);
            } else if(args.length == 1) {
                ipGrupoDestino = InetAddress.getByName(args[0]);
            }

            return new InetSocketAddress(ipGrupoDestino, portaDestino);
        } catch (Exception e) {
            Logger.error("Erro ao inicializar inet socket adress.", e);
            System.exit(1);
        }

        return null;
    }

    private static MulticastSocket inicializarMulticastSocket(InetSocketAddress inetSocketAddress) {
        try {
            MulticastSocket multicastSocket = new MulticastSocket(inetSocketAddress.getPort());
            multicastSocket.joinGroup(inetSocketAddress.getAddress());
            return multicastSocket;
        } catch (Exception e) {
            Logger.error("Erro ao inicializar inet socket adress.", e);
            System.exit(1);
        }

        return null;
    }

    private static void receberMensagem(MulticastSocket multicastSocket, InetSocketAddress inetSocketAddress) {
        while (isSocketOpen) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                multicastSocket.receive(datagramPacket);
                String mensagem = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);

                if (LIST_USERS.equals(mensagem)) {
                    enviarMensagem(multicastSocket, inetSocketAddress, LIST_USERS_KEY + usuario);
                    continue;
                }

                try {
                    if (mensagem.split(DOIS_PONTOS)[0].equals(usuario) || mensagem.split(PONTO_E_VIRGULA)[0].equals(LIST_USERS)) {
                        continue;
                    }
                } catch (Exception ignored) {}

                Logger.info(mensagem);
            } catch (Exception ignored) {}
        }
    }

    private static void enviarMensagem(MulticastSocket multicastSocket, InetSocketAddress inetSocketAddress, String mensagem) {
        try {
            multicastSocket.send(new DatagramPacket(mensagem.getBytes(), mensagem.getBytes().length, inetSocketAddress.getAddress(), inetSocketAddress.getPort()));
        } catch (Exception e) {
            Logger.error("Erro ao enviar mensagem. Mensagem: " + mensagem, e);
            System.exit(1);
        }
    }

    private static void sairDoGrupo(MulticastSocket multicastSocket, InetSocketAddress inetSocketAddress, String mensagemMotivoSaida) {
        try {
            Logger.info(mensagemMotivoSaida);
            isSocketOpen = Boolean.FALSE;
            enviarMensagem(multicastSocket, inetSocketAddress, usuario + ": Saiu do grupo.");
            multicastSocket.leaveGroup(inetSocketAddress.getAddress());
            multicastSocket.close();
            System.exit(0);
        } catch (Exception e) {
            Logger.error("Erro ao sair do grupo.", e);
            System.exit(1);
        }
    }

    private static void listarUsuariosNoGrupo(InetSocketAddress inetSocketAddress) {
        Logger.info("Fazendo requisição de usuários no grupo.");

        Set<String> usuariosNoGrupo = new TreeSet<>();
        MulticastSocket multicastSocket = inicializarMulticastSocket(inetSocketAddress);

        Thread thread = new Thread(() -> receberMensagemUsuariosNoGrupo(multicastSocket, usuariosNoGrupo));
        thread.start();
        enviarMensagem(multicastSocket, inetSocketAddress, LIST_USERS);

        try {
            TimeUnit.SECONDS.sleep(12);
        } catch (Exception e) {
            Logger.error("Erro ao parar a thread main.", e);
        }

        try {
            multicastSocket.leaveGroup(inetSocketAddress.getAddress());
            multicastSocket.close();
        } catch (Exception ignored) {}

        thread.stop();

        if (usuariosNoGrupo.isEmpty()) {
            Logger.info("Não há usuários conectados no momento.");
        } else {
            Logger.info("Usuários conectados no grupo: " + String.join(", ", usuariosNoGrupo));
        }
    }

    private static void receberMensagemUsuariosNoGrupo(MulticastSocket multicastSocket, Set<String> usuariosNoGrupo) {
        Instant start = Instant.now();

        while (Duration.between(start, Instant.now()).toMillis() <= 10000) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                multicastSocket.receive(datagramPacket);
                String mensagem = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);

                try {
                    String[] m = mensagem.split(PONTO_E_VIRGULA);
                    if (LIST_USERS.equals(m[0])) {
                        usuariosNoGrupo.add(m[1]);
                    }
                } catch (Exception ignored) {}
            } catch (Exception e) {
                Logger.error("Erro ao receber mensagem com os usuários do grupo.", e);
            }
        }
    }
}