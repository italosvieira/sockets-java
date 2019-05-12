import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class Servidor {
    public static void main(String[] args) {
        DatagramSocket datagramSocket = inicializarSocket(args);
        enviarMensagem(datagramSocket, receberMensagem(datagramSocket));
        datagramSocket.close();
    }

    private static DatagramSocket inicializarSocket(String[] args) {
        try {
            return new DatagramSocket(Arrays.stream(args).mapToInt(Integer::valueOf).findFirst().orElse(6789));
        } catch (Exception e) {
            Logger.error("Erro ao inicializar socket do servidor.", e);
            System.exit(1);
        }

        return null;
    }

    private static DatagramPacket receberMensagem(DatagramSocket datagramSocket) {
        Logger.info("Servidor iniciado e aguardando novos clientes.");

        DatagramPacket mensagemRecebida = new DatagramPacket(new byte[1024], 1024);

        try {
            datagramSocket.receive(mensagemRecebida);
            Logger.info("Mensagem recebida com sucesso." + obterClienteComoString(mensagemRecebida));
        } catch (Exception e) {
            Logger.error("Erro ao receber mensagem de cliente." + obterClienteComoString(mensagemRecebida), e);
            System.exit(1);
        }

        return mensagemRecebida;
    }

    private static void enviarMensagem(DatagramSocket datagramSocket, DatagramPacket mensagem) {
        String cliente = obterClienteComoString(mensagem);

        try {
            Logger.info("Enviando mensagem de resposta para o cliente." + cliente);
            datagramSocket.send(new DatagramPacket(mensagem.getData(), mensagem.getLength(), mensagem.getAddress(), mensagem.getPort()));
            Logger.info("Mensagem de resposta enviada ao cliente com sucesso." + cliente);
        } catch (Exception e) {
            Logger.error("Erro ao enviar mensagem para o cliente." + cliente, e);
        }
    }

    private static String obterClienteComoString(DatagramPacket mensagem) {
        return " Endereço: " + mensagem.getAddress() + ". Porta: " + mensagem.getPort() + ". Mensagem: " + new String(mensagem.getData());
    }
}