import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Cliente {

    private static volatile Boolean isSocketOpen = Boolean.TRUE;
    private static String usuario;
    private static final String MENSAGEM_DESCONECTADO = "Desconectando da aplicação por inatividade de 60 segundos.";
    private static final String MENSAGEM_SAIDA_GRUPO = "Saindo do grupo e finalizando a aplicação.";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        inicializarUsuario(scanner);

        InetSocketAddress inetSocketAddress = inicializarInetSocketAdress(args);
        MulticastSocket multicastSocket = inicializarMulticastSocket(inetSocketAddress);

        new Thread(() -> receberMensagem(multicastSocket)).start();

        Logger.info("Conectado no chat. Pode começar a mandar mensagens.");

        while (true) {
            //TODO colocar como 60 segundos
            ScheduledFuture v = Executors.newSingleThreadScheduledExecutor().schedule(() -> sairDoGrupo(multicastSocket, inetSocketAddress, MENSAGEM_DESCONECTADO), 60, TimeUnit.SECONDS);
            String mensagem = scanner.nextLine();
            v.cancel(true);

            if ("sair".equalsIgnoreCase(mensagem)) {
                sairDoGrupo(multicastSocket, inetSocketAddress, MENSAGEM_SAIDA_GRUPO);
                System.exit(0);
            } else {
                enviarMensagem(multicastSocket, inetSocketAddress, usuario + ":" + mensagem);
            }
        }
    }

    private static void inicializarUsuario(Scanner scanner) {
        Logger.infoSameLine("Digite o seu nome:");
        usuario = scanner.nextLine();
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

    private static void receberMensagem(MulticastSocket multicastSocket) {
        while (isSocketOpen) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                multicastSocket.receive(datagramPacket);
                String mensagem = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);

                if (!mensagem.substring(0, usuario.length()).equals(usuario)) {
                    Logger.info(mensagem);
                }
            } catch (Exception e) {
                Logger.error("Erro ao receber mensagem.", e);
                System.exit(1);
            }
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
            enviarMensagem(multicastSocket, inetSocketAddress, usuario + ":Saindo do Grupo!");
            multicastSocket.leaveGroup(inetSocketAddress.getAddress());
            multicastSocket.close();
            System.exit(0);
        } catch (Exception e) {
            Logger.error("Erro ao sair do grupo.", e);
            System.exit(1);
        }
    }
}