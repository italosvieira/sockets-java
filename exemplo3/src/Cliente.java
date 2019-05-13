import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Cliente {
    public static void main(String[] args) {
        DatagramSocket datagramSocket = inicializarSocket();
        DatagramPacket mensagemEnvio = inicializarMensagemEnvio(args);

        enviarMensagem(mensagemEnvio, datagramSocket);
        receberResposta(datagramSocket);

        datagramSocket.close();
    }

    private static DatagramSocket inicializarSocket() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(3000);
            return datagramSocket;
        } catch (Exception e) {
            Logger.error("Erro ao inicializar socket do cliente.", e);
            System.exit(1);
        }

        return null;
    }

    private static DatagramPacket inicializarMensagemEnvio(String[] args) {
        try {
            byte[] mensagem = "1;5;VVFFV;2;4;VVVV;3;5;FFVFF;4;3;FFF;5;5;VVFVF".getBytes();
            InetAddress enderecoDestino = InetAddress.getByName("localhost");
            int portaDestino = 6789;

            if (args.length == 3) {
                mensagem = args[0].getBytes();
                enderecoDestino = InetAddress.getByName(args[1]);
                portaDestino = Integer.valueOf(args[2]);
            } else if (args.length == 2) {
                mensagem = args[0].getBytes();
                enderecoDestino = InetAddress.getByName(args[1]);
            } else if(args.length == 1) {
                mensagem = args[0].getBytes();
            }

            return new DatagramPacket(mensagem, mensagem.length, enderecoDestino, portaDestino);
        } catch (Exception e) {
            Logger.error("Erro ao inicializar mensagem para enviar ao servidor.", e);
            System.exit(1);
        }

        return null;
    }

    private static void enviarMensagem(DatagramPacket mensagem, DatagramSocket datagramSocket) {
        try {
            Logger.info("Enviando mensagem para o servidor." + obterClienteComoString(mensagem));
            datagramSocket.send(mensagem);
        } catch (Exception e) {
            Logger.error("Erro ao enviar mensagem para o servidor." + obterClienteComoString(mensagem), e);
        }
    }

    private static void receberResposta(DatagramSocket datagramSocket) {
        try {
            DatagramPacket pctVem = new DatagramPacket(new byte[1024], 1024);
            datagramSocket.receive(pctVem);
            Logger.info("Sucesso ao receber mensagem de resposta do servidor. Mensagem: " + new String(pctVem.getData()));
        } catch (Exception e) {
            Logger.error("Erro ao receber resposta do servidor.", e);
        }
    }

    private static String obterClienteComoString(DatagramPacket mensagem) {
        return " Endereço: " + mensagem.getAddress() + ". Porta: " + mensagem.getPort() + ". Mensagem: " + new String(mensagem.getData());
    }
}