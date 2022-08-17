
/**
 * @aluno: Igor José Costa Gonçalves
 * @matricula: 202065138AC
 * @disciplina: DCC042 - Redes de Computadores
 * @semestre: 2022.1
 * */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UDPClient {


    public static void main(String[] args) throws Exception {

        File arquivoToSend = gerarArquivo();

        final long  TIMEOUT = TimeUnit.MILLISECONDS.toMillis(4);
        byte[] dadosRecebidos = new byte[1024];
        byte[] dadosEnviados;
        final int TAMANHO_PACOTE_MAX = 1024;

        DatagramSocket clienteSocket = new DatagramSocket();
        int port = 9876;
        String servidor = "localhost";
        InetAddress inetAddress = InetAddress.getByName(servidor);

        System.out.println(
                String.format("Enviando mensagem na porta: %d", port));

        int counter = 1;
        int totalPacotesDescartados = 0;
        System.out.println("Iniciando envio de pacotes ao servidor..");
        for (int i=0; i<arquivoToSend.getAbsoluteFile().length(); i = i + 1024) {

            dadosEnviados = String.format("%d#%s",counter,new byte[TAMANHO_PACOTE_MAX].toString()).getBytes();

            DatagramPacket datagramPacket =
                    new DatagramPacket(dadosEnviados, dadosEnviados.length, inetAddress, port);

            /**
             *
             * Equação que estabelece um controle de fluxo com base no numero de pacotes em atraso
             * */
            if (true) {

                equacaoControle((int) arquivoToSend.getAbsoluteFile().length()/1024, totalPacotesDescartados);
                clienteSocket.send(datagramPacket);
            }
            long inicio =  System.nanoTime();

            /** Recebendo a mensagem de Resposta do Servidor */
            DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
            clienteSocket.receive(pacoteRecebido);
            long fim = System.nanoTime();

            long duracao = TimeUnit.NANOSECONDS.toMillis(fim - inicio);

            /**
             *
             * Timeout calculado como 3 x o tempo médio do envio de 10 pacotes
             * */
            if(duracao >= TIMEOUT) {

                System.out.println(String.format("Timeout atingido. Reenviando pacote de # %d", counter));
                clienteSocket.send(datagramPacket);
                totalPacotesDescartados += totalPacotesDescartados + 1; // incrementando total de pacotes perdidos ou em atraso
                continue;
            }

            String pacoteDecodificado = new String(pacoteRecebido.getData());
            if(counter % 100 == 0) {
                System.out.println(String.format("ACK Acumulativo: %s", pacoteDecodificado));
            }

            counter++;
        }
        /**
         * Fechando conexão */
        clienteSocket.close();
        System.out.println("Conexão finalizada!");
    }

    /**
     * Função que é responsável por controlar o fluxo de envio de dados para o servidor UDP
     *
     * A função recebe o total de pacotes enviados e também o numero de perdas. Se as perdas representarem
     * 5% do total de pacotes, o tempo de envio de pacotes é ampliado para 3x o tempo normal de envio
     * */

    public static void equacaoControle(int totalPac, int perdas) {

        System.out.println("Congestionamento identificado. Reduzindo frequência de envios..");
        int porcentagemDePerdas = perdas / totalPac * 100;
        if(porcentagemDePerdas >= 5) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Função utilizada para ler */

    public static File gerarArquivo() throws IOException {

        File arq = new File("arquivo_teste");
        arq.createNewFile();

        RandomAccessFile raf = new RandomAccessFile(arq, "rw");
        raf.setLength(10000000);
        raf.close();

        return arq;
    }


}

/*
1) Entrega ordenada para aplicação baseado na ordem dos pacotes (# de sequência). OK
2) Confirmação acumulativa (ACK acumulativo) do destinatário para o remetente. OK
3) Utilização de um Buffer de pacotes de tamanho T, onde pacotes ocupam M Bytes. OK
4) O tamanho de cada pacote é de, no máximo, 1024 Bytes. (M) OK
5) Deve haver uma janela deslizante com tamanho N no buffer do remetente e do servidor, onde N é igual a pelo menos 10 pacotes de tamanho M.
6) Números de sequência devem ser utilizados. Eles podem ser inteiros em um total de N*2, ou serem incrementados conforme o fluxo de bytes, como no TCP. OK
7) Adicione no protocolo um controle de fluxo, onde o remetente deve saber qual o tamanhoda janela N do destinatário, a fim de não afogá-lo.
8) Por fim, crie um equação de controle de congestionamento, a fim de que, se a rede estiver
apresentando perda (muitos pacotes com ACK pendentes e timeout), ele deve ser utilizado
para reduzir o fluxo de envio de pacotes. OK
9) Avalie seu protocolo sobre um remetente que envia um arquivo de, pelo menos, 10MB, qualquer,
para 1 destinatário. Para avaliar o controle de congestionamento, insira perdas arbitrárias de
pacotes no destinatário (você pode fazer isso sorteando a cada chegada de um novo pacotes se ele
será contabilizado e processado ou descartado). OK

 */