import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    public static void main(String[] args) {
        DatagramSocket datagramSocket = inicializarSocket(args);
        AtomicInteger numeroDeClientesAtivos = new AtomicInteger(0);

        Logger.info("Servidor iniciado e aguardando novos clientes.");

        while (true) {
            DatagramPacket mensagem = receberMensagem(datagramSocket, numeroDeClientesAtivos);

            if (mensagem != null) {
                new Thread(() -> enviarMensagem(datagramSocket, mensagem, numeroDeClientesAtivos)).start();
            }
        }
    }

    private static DatagramSocket inicializarSocket(String[] args) {
        try {
            return new DatagramSocket(Arrays.stream(args).mapToInt(Integer::valueOf).findFirst().orElse(6789));
        } catch (Exception e) {
            Logger.error("Erro ao inicializar socket do servidor.", e);
            System.exit(1);
            return null;
        }
    }

    private static DatagramPacket receberMensagem(DatagramSocket datagramSocket, AtomicInteger numeroDeClientesAtivos) {
        DatagramPacket mensagemRecebida = new DatagramPacket(new byte[1024], 1024);

        try {
            datagramSocket.receive(mensagemRecebida);
            Logger.info("Mensagem recebida com sucesso." + obterClienteComoString(mensagemRecebida));
            incrementarClientesAtivos(numeroDeClientesAtivos);
            return mensagemRecebida;
        } catch (Exception e) {
            Logger.error("Erro ao receber mensagem de cliente." + obterClienteComoString(mensagemRecebida), e);
            return null;
        }
    }

    private static void enviarMensagem(DatagramSocket datagramSocket, DatagramPacket mensagem, AtomicInteger numeroDeClientesAtivos) {
        String cliente = obterClienteComoString(mensagem);

        sleepThread(mensagem);

        try {
            Logger.info("Enviando mensagem de resposta para o cliente." + cliente);
            datagramSocket.send(new DatagramPacket(mensagem.getData(), mensagem.getLength(), mensagem.getAddress(), mensagem.getPort()));
            Logger.info("Mensagem de resposta enviada ao cliente com sucesso." + cliente);
        } catch (Exception e) {
            Logger.error("Erro ao enviar mensagem para o cliente." + cliente, e);
        }

        decrementarClientesAtivos(numeroDeClientesAtivos);
    }

    private static String obterClienteComoString(DatagramPacket mensagem) {
        return " Endereço: " + mensagem.getAddress() + ". Porta: " + mensagem.getPort() + ". Mensagem: " + new String(mensagem.getData());
    }

    private static void incrementarClientesAtivos(AtomicInteger numeroDeClientesAtivos) {
        Logger.info("Número de clientes ativos = " + numeroDeClientesAtivos.incrementAndGet());
    }

    private static void decrementarClientesAtivos(AtomicInteger numeroDeClientesAtivos) {
        Logger.info("Número de clientes ativos = " + numeroDeClientesAtivos.decrementAndGet());
    }

    private static void sleepThread(DatagramPacket mensagem) {
        String str = new String(mensagem.getData(), mensagem.getOffset(), mensagem.getLength(), StandardCharsets.UTF_8);

        if (str.equalsIgnoreCase("Sleep")) {
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (InterruptedException e) {
                Logger.error("Erro ao pausar thread.", e);
            }
        }
    }
}