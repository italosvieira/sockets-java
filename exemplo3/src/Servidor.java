import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    private static final String PONTO_E_VIRGULA = ";";
    private static final Set<Questao> questoes = new HashSet<>();

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
        try {
            byte[] mensagemResposta = gerarResposta(mensagem);
            Logger.info("Enviando mensagem de resposta para o cliente." + obterClienteComoStringPorByteArray(mensagem, mensagemResposta));
            datagramSocket.send(new DatagramPacket(mensagemResposta, mensagemResposta.length, mensagem.getAddress(), mensagem.getPort()));
            Logger.info("Mensagem de resposta enviada ao cliente com sucesso." + obterClienteComoStringPorByteArray(mensagem, mensagemResposta));
        } catch (Exception e) {
            Logger.error("Erro ao enviar mensagem para o cliente." + obterClienteComoStringPorByteArray(mensagem, "".getBytes()), e);
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

    private static String obterClienteComoStringPorByteArray(DatagramPacket mensagem, byte[] mensagemResposta) {
        return " Endereço: " + mensagem.getAddress() + ". Porta: " + mensagem.getPort() + ". Mensagem: " + new String(mensagemResposta);
    }

    private static byte[] gerarResposta(DatagramPacket datagramPacket) {
        String mensagem = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);

        if ("estatisticas".equalsIgnoreCase(mensagem)) {
            return gerarEstatisticas();
        } else {
            return gerarRespostaCalculada(datagramPacket);
        }
    }

    private static byte[] gerarRespostaCalculada(DatagramPacket datagramPacket) {
        String[] respostas = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8).split(PONTO_E_VIRGULA);
        StringJoiner resposta = new StringJoiner(PONTO_E_VIRGULA);
        Questao questao = new Questao();
        int count = 0;

        for (String s : respostas) {
            if (count == 0) {
                questao = questoes.stream().filter(q -> q.getNumeroQuestao().equals(Integer.valueOf(s))).findFirst().orElse(new Questao());
                questao.setNumeroQuestao(Integer.valueOf(s));
                count++;
            } else if (count == 1) {
                questao.setNumeroAlternativas(Integer.valueOf(s));
                count++;
            } else {
                questao.setNumeroAcertos(Math.toIntExact(s.chars().filter(c -> c == 'V' || c == 'v').count()));
                questao.setNumeroErros(Math.toIntExact(s.chars().filter(c -> c == 'F' || c == 'f').count()));
                questao.incrementarAcertos(questao.getNumeroAcertos());
                questao.incrementarErros(questao.getNumeroErros());

                resposta.add(String.valueOf(questao.getNumeroQuestao()));
                resposta.add(String.valueOf(questao.getNumeroAcertos()));
                resposta.add(String.valueOf(questao.getNumeroErros()));

                questoes.add(questao);

                count = 0;
            }
        }

        return resposta.toString().getBytes();
    }

    private static byte[] gerarEstatisticas() {
        if (questoes.isEmpty()) {
            return "Ainda não existem questões para gerar estatísticas.".getBytes();
        } else {
            StringJoiner resposta = new StringJoiner(System.lineSeparator());
            questoes.forEach(q -> resposta.add(System.lineSeparator() + "Questão " + q.getNumeroQuestao() + ": Acertos = " + q.getNumeroAcertosTotais() + " Erros = " + q.getNumeroErrosTotais()));
            return resposta.toString().getBytes();
        }
    }
}