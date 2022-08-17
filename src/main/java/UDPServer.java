
/**
 * @aluno: Igor José Costa Gonçalves
 * @matricula: 202065138AC
 * @disciplina: DCC042 - Redes de Computadores
 * @periodo: 2022.1
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {


    public static void main(String[] args) throws IOException {

        /**
         * Criando os Buffers para Dados recebidos e Enviados
         *
         * Capacidade máxima do buffer de até 100 x 1024 (tamanho máximo de um pacote)
         *
         * Tamanho máximo da janela deslizante (tamWindow)
         * */
        byte[] bufferRecebidos = new byte[102400000];
        byte[] bufferEnviados;
        int porta = 9876;

        DatagramSocket serverSocket = new DatagramSocket(porta);

        long capacidadeBuffer = 0;
        while (true) {

            /** Recebendo os dados*/
            DatagramPacket dadosRecebidos = new DatagramPacket(bufferRecebidos, bufferRecebidos.length);
            System.out.println(String.format("Ouvindo a porta: %d", porta));

            serverSocket.receive(dadosRecebidos);
            /** Se a função retornar zero, o pacote atual será descartado */

            /** Convertendo os dados para String */
            String mensagemConvertida = new String(dadosRecebidos.getData());
            capacidadeBuffer += dadosRecebidos.getData().length;


            InetAddress address = dadosRecebidos.getAddress();
            int port = dadosRecebidos.getPort();

            String capitalizedSentence = String.format(
                    "%s#ACK%d", mensagemConvertida.split("#")[0], capacidadeBuffer);
            bufferEnviados = capitalizedSentence.getBytes();

            DatagramPacket datagramSend =
                    new DatagramPacket(bufferEnviados, bufferEnviados.length, address, port);

            try {
                System.out.println(
                        String.format("Enviando Mensagem: %s", capitalizedSentence));
                if(funcaoDescarteAleatorio())
                    Thread.sleep(5);
                serverSocket.send(datagramSend);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    /**
     * Função responsável por gerar um número 0 ou 1 para decidir se irá descartar o pacote
     * recebi ou não*/
    public static boolean funcaoDescarteAleatorio() {
        int resultado = (int) ((Math.random() * (2 - 0)) + 0);
        return resultado == 1;
    }
}
