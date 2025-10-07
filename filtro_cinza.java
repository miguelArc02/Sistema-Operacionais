import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;

public class filtro_cinza {

    // Função que converte parte da imagem para escala de cinza
    public static BufferedImage converterParaEscalaDeCinza(BufferedImage imagem, int inicio, int fim, int altura) {
        int largura = fim - inicio;
        BufferedImage novaImagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int rgb = imagem.getRGB(inicio + x, y);

                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                int valorCinza = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                int novoRGB = (valorCinza << 16) | (valorCinza << 8) | valorCinza;

                novaImagem.setRGB(x, y, novoRGB);
            }
        }
        return novaImagem;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Uso: java ProcessadorImagem <imagem_de_entrada> <imagem_de_saida>");
            System.exit(1);
        }

        String caminhoEntrada = args[0];
        String caminhoSaida = args[1];

        BufferedImage imagemOriginal;
        try {
            imagemOriginal = ImageIO.read(new File(caminhoEntrada));
            if (imagemOriginal == null) {
                throw new IOException("Imagem não está no formato RGB");
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar a imagem: " + e.getMessage());
            return;
        }

        System.out.println("Processando a imagem '" + caminhoEntrada + "' de forma sequencial...");
        long inicio = System.currentTimeMillis();

        BufferedImage imagemProcessada = converterParaEscalaDeCinza(imagemOriginal, 0, imagemOriginal.getWidth(), imagemOriginal.getHeight());

        long fim = System.currentTimeMillis();
        long tempoExecucao = fim - inicio;

        try {
            ImageIO.write(imagemProcessada, "jpg", new File(caminhoSaida));
            System.out.println("Imagem convertida com sucesso e salva em '" + caminhoSaida + "'");
        } catch (IOException e) {
            System.out.println("Erro ao salvar a imagem: " + e.getMessage());
        }

        System.out.printf("Tempo de execução (sequencial): %.2f ms\n\n", (double)tempoExecucao);

        // ================= Multithreading =================
        System.out.println("Processando a imagem '" + caminhoEntrada + "' com multithreading...");

        int numThreads = 6;
        BufferedImage imagemProcessada2 = new BufferedImage(imagemOriginal.getWidth(), imagemOriginal.getHeight(), BufferedImage.TYPE_INT_RGB);
        int largura = imagemOriginal.getWidth();
        int intervalo = largura / numThreads;

        Thread[] threads = new Thread[numThreads];
        BufferedImage[] resultados = new BufferedImage[numThreads];

        inicio = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            final int idx = i;
            final int inicioParte = i * intervalo;
            final int fimParte = (i == numThreads - 1) ? largura : (i + 1) * intervalo;

            threads[i] = new Thread(() -> {
                resultados[idx] = converterParaEscalaDeCinza(imagemOriginal, inicioParte, fimParte, imagemOriginal.getHeight());
            });
            threads[i].start();
        }

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

        fim = System.currentTimeMillis();
        tempoExecucao = fim - inicio;

        String caminhoMultithread = caminhoSaida.replace(".jpg", "_multithread.jpg");
        try {
            ImageIO.write(imagemProcessada2, "jpg", new File(caminhoMultithread));
            System.out.println("Imagem convertida com sucesso e salva em '" + caminhoMultithread + "'");
        } catch (IOException e) {
            System.out.println("Erro ao salvar a imagem: " + e.getMessage());
        }

        System.out.printf("Tempo de execução (multithreading): %.2f ms\n", (double)tempoExecucao);
    }
}
