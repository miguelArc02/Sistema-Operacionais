#Importa as bibliotecas necessárias
import sys
import time
import threading
from PIL import Image

""" Função para converter uma seção da imagem para escala de cinza 
    Parâmetros:
        imagem: objeto da imagem original
        inicio: coordenada x inicial da seção
        fim: coordenada x final da seção
        altura: altura da imagem
    Retorna: nova imagem em escala de cinza """
def converter_para_escala_de_cinza(imagem, inicio: int, fim: int, altura: int):

    # Calcula a largura subtraindo o fim pelo início
    largura = fim - inicio

    # Cria uma nova imagem com o mesmo tamanho da seção
    nova_imagem = Image.new("RGB", (largura, altura))

    # Percorre cada pixel da seção da imagem original
    for x in range(largura):
        for y in range(altura):
            # Obtém os valores RGB do pixel
            r, g, b = imagem.getpixel((inicio + x, y))

            # Calcula o valor em escala de cinza usando a fórmula de luminosidade
            valor_cinza = int(0.299 * r + 0.587 * g + 0.114 * b)

            # Define o pixel na nova imagem com o valor em escala de cinza
            nova_imagem.putpixel((x, y), (valor_cinza, valor_cinza, valor_cinza))

    # Retorna a nova imagem em escala de cinza
    return nova_imagem

# Função principal para processar a imagem
def main():
    # Verifica os argumentos da linha de comando. Caso sejam inválidos, exibe a mensagem de uso e encerra o programa.
    if len(sys.argv) != 3:
        print("Uso: python processador_sequencial.py <imagem_de_entrada> <imagem_de_saida>")
        sys.exit(1)

    # Lê os caminhos dos arquivos de entrada e saída a partir dos argumentos da linha de comando
    caminho_entrada = sys.argv[1]
    caminho_saida = sys.argv[2]

    # Tenta abrir a imagem de entrada. Se não abrir a imagem ou se o processo falhar, exibe uma mensagem de erro e encerra o programa.
    try:
        imagem_original = Image.open(caminho_entrada)
        if imagem_original.mode != 'RGB':
            raise Exception("Desculpe, imagem não está no formato RGB")
    except FileNotFoundError:
        print(f"Erro: O arquivo '{caminho_entrada}' não foi encontrado.")
        sys.exit(1)

    # ================= Processamento Sequencial =================
    print(f"Processando a imagem '{caminho_entrada}' de forma sequencial...")
    
    # Inicia a medição do tempo
    inicio = time.time()

    # Converte a imagem para escala de cinza e atribui à variável imagem_processada
    imagem_processada = converter_para_escala_de_cinza(imagem_original, 0, imagem_original.width, imagem_original.height)
    
    # Finaliza a medição do tempo
    fim = time.time()

    # Calcula o tempo de execução em milissegundos
    tempo_execucao_ms = (fim - inicio) * 1000

    # Salva a imagem processada no caminho de saída especificado
    imagem_processada.save(caminho_saida)

    # Exibe mensagens de sucesso e o tempo de execução
    print(f"Imagem convertida com sucesso e salva em '{caminho_saida}'")
    print(f"Tempo de execução (sequencial): {tempo_execucao_ms:.2f} ms\n")

    # ================= Processamento da imagem com multithreading =================
    print(f"Processando a imagem '{caminho_entrada}' com multithreading...")

    # Inicia a medição do tempo
    inicio = time.time()

    # Define o número de threads
    num_threads = 6

    # Cria uma nova imagem para armazenar o resultado
    imagem_processada2 = Image.new("RGB", imagem_original.size)
    
    # Divide a imagem em partes, pela largura
    largura = imagem_original.width
    intervalo = largura // num_threads

    # Função auxiliar para processar cada parte da imagem em uma thread e armazenar o resultado na lista resultados
    resultados = [None] * num_threads
    def worker(idx, inicio, fim):
        resultados[idx] = converter_para_escala_de_cinza(imagem_original, inicio, fim, imagem_original.height)

    # Lista para armazenar as threads
    threads = []

    # Cria, armazena e inicia as threads
    for i in range(num_threads):
        inicio_parte = i * intervalo
        fim_parte = (i + 1) * intervalo if i < (num_threads - 1) else largura
        thread = threading.Thread(target=worker, args=(i, inicio_parte, fim_parte))
        threads.append(thread)
        thread.start()

    # Sincroniza as threads
    for thread in threads:
        thread.join()

    # Combina os resultados das threads na imagem final
    for j in range(num_threads):
        imagem_processada2.paste(resultados[j], (j * intervalo, 0))

    # Finaliza a medição do tempo
    fim = time.time()
    
    # Calcula o tempo de execução em milissegundos
    tempo_execucao_ms = (fim - inicio) * 1000

    # Modifica o nome do arquivo para indicar que foi processada com multithreading
    caminho_multithread = caminho_saida.replace(".jpg", "_multithread.jpg")

    # Salva a imagem processada no caminho de saída especificado
    imagem_processada2.save(caminho_multithread)
    
    # Exibe mensagens de sucesso e o tempo de execução
    print(f"Imagem convertida com sucesso e salva em '{caminho_multithread}'")
    print(f"Tempo de execução (sequencial): {tempo_execucao_ms:.2f} ms")

if __name__ == "__main__":
    main()