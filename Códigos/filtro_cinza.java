// Importa as bibliotecas necessárias
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

// Classe principal
public class filtro_cinza {

    /** Função para converter uma seção da imagem para escala de cinza
     * Parâmetros:
     *     imagem: objeto da imagem original
     *     inicio: coordenada x inicial da seção
     *     fim: coordenada x final da seção
     *     altura: altura da imagem
     * Retorna: nova imagem em escala de cinza */
    public static BufferedImage converterParaEscalaDeCinza(BufferedImage imagem, int inicio, int fim, int altura) {

        // Calcula a largura subtraindo o fim pelo início
        int largura = fim - inicio;
        // Cria uma nova imagem com o mesmo tamanho da seção
        BufferedImage novaImagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

        // Percorre cada pixel da seção da imagem original
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                // Obtém os valores RGB do pixel
                int rgb = imagem.getRGB(inicio + x, y);

                // Extrai os componentes de cor
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                // Calcula o valor em escala de cinza usando a fórmula de luminosidade
                int valorCinza = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                // Define o pixel na nova imagem com o valor em escala de cinza
                int novoRGB = (valorCinza << 16) | (valorCinza << 8) | valorCinza;

                // Insere os novos valores RGB na nova imagem
                novaImagem.setRGB(x, y, novoRGB);
            }
        }
        // Retorna a nova imagem em escala de cinza
        return novaImagem;
    }

    // Função principal para processar a imagem
    public static void main(String[] args) {

        // Verifica se os argumentos de entrada e saída foram fornecidos. Se não, exibe uma mensagem de uso e encerra o programa.
        if (args.length != 2) {
            System.out.println("Uso: java ProcessadorImagem <imagem_de_entrada> <imagem_de_saida>");
            System.exit(1);
        }

        // Caminhos dos arquivos de entrada e saída
        String caminhoEntrada = args[0];
        String caminhoSaida = args[1];

        // Cria o objeto da imagem original
        BufferedImage imagemOriginal;

        // Tenta carregar a imagem do arquivo. Se não for do formato RGB ou se falhar, exibe uma mensagem de erro e encerra o programa.
        try {
            imagemOriginal = ImageIO.read(new File(caminhoEntrada));
            if (imagemOriginal == null) {
                throw new IOException("Imagem não está no formato RGB");
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem: " + e.getMessage());
            return;
        }

        // ================= Processamento Sequencial =================
        System.out.println("Processando a imagem '" + caminhoEntrada + "' de forma sequencial...");

        // Inicia a medição do tempo
        long inicio = System.currentTimeMillis();

        // Converte a imagem para escala de cinza e atribui o resultado a uma nova imagem
        BufferedImage imagemProcessada = converterParaEscalaDeCinza(imagemOriginal, 0, imagemOriginal.getWidth(), imagemOriginal.getHeight());

        // Finaliza a medição do tempo
        long fim = System.currentTimeMillis();

        // Calcula o tempo de execução
        long tempoExecucao = fim - inicio;

        // Tenta salvar a imagem processada no arquivo de saída. Se falhar, exibe uma mensagem de erro.
        try {
            ImageIO.write(imagemProcessada, "jpg", new File(caminhoSaida));

            // Informa que a imagem foi salva com sucesso
            System.out.println("Imagem convertida com sucesso e salva em '" + caminhoSaida + "'");
        } catch (IOException e) {
            System.out.println("Erro ao salvar a imagem: " + e.getMessage());
        }

        // Exibe o tempo de execução em milissegundos
        System.out.printf("Tempo de execução (sequencial): %.2f ms\n\n", (double)tempoExecucao);

        // ================= Multithreading =================
        System.out.println("Processando a imagem '" + caminhoEntrada + "' com multithreading...");

        // Define o número de threads
        int numThreads = 6;

        // Cria a imagem processada para multithreading, com o mesmo tamanho da original
        BufferedImage imagemProcessada2 = new BufferedImage(imagemOriginal.getWidth(), imagemOriginal.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        // Divide a imagem em partes pela largura e cria threads para processar cada parte
        int largura = imagemOriginal.getWidth();
        int intervalo = largura / numThreads;

        // Array para armazenar as threads e os resultados
        Thread[] threads = new Thread[numThreads];
        BufferedImage[] resultados = new BufferedImage[numThreads];

        // Inicia a medição do tempo
        inicio = System.currentTimeMillis();

        // Cria, armazena e inicia cada thread
        for (int i = 0; i < numThreads; i++) {
            final int idx = i;
            final int inicioParte = i * intervalo;
            final int fimParte = (i == numThreads - 1) ? largura : (i + 1) * intervalo;

            threads[i] = new Thread(() -> {
                resultados[idx] = converterParaEscalaDeCinza(imagemOriginal, inicioParte, fimParte, imagemOriginal.getHeight());
            });
            threads[i].start();
        }

        // Tenta sincronizar as threads. Caso alguma thread seja interrompida, exibe a pilha de erros.
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Junta os pedaços processados
        Graphics g = imagemProcessada2.getGraphics();
        for (int j = 0; j < numThreads; j++) {
            g.drawImage(resultados[j], j * intervalo, 0, null);
        }
        g.dispose();

        // Finaliza a medição do tempo
        fim = System.currentTimeMillis();

        // Calcula o tempo de execução
        tempoExecucao = fim - inicio;

        // Modifica o nome do arquivo de saída para indicar que é a versão multithread e o armazena em caminhoMultithread
        String caminhoMultithread = caminhoSaida.replace(".jpg", "_multithread.jpg");

        // Tenta salvar a imagem processada no arquivo de saída. Se falhar, exibe uma mensagem de erro.
        try {
            ImageIO.write(imagemProcessada2, "jpg", new File(caminhoMultithread));
            // Informa que a imagem foi salva com sucesso
            System.out.println("Imagem convertida com sucesso e salva em '" + caminhoMultithread + "'");
        } catch (IOException e) {
            System.out.println("Erro ao salvar a imagem: " + e.getMessage());
        }

        // Exibe o tempo de execução em milissegundos
        System.out.printf("Tempo de execução (multithreading): %.2f ms\n", (double)tempoExecucao);
    }
}
