import sys
import time
import threading
from PIL import Image

def converter_para_escala_de_cinza(imagem, inicio: int, fim: int, altura: int):
    """
    Aplica o filtro de escala de cinza em uma imagem.
    """
    largura = fim - inicio
    nova_imagem = Image.new("RGB", (largura, altura))
    for x in range(largura):
        for y in range(altura):
            r, g, b = imagem.getpixel((inicio + x, y))
            valor_cinza = int(0.299 * r + 0.587 * g + 0.114 * b)
            nova_imagem.putpixel((x, y), (valor_cinza, valor_cinza, valor_cinza))
    return nova_imagem

def main():
    """
    Função principal que gerencia a execução do script.
    """
    # Verifica se os argumentos de linha de comando foram fornecidos corretamente
    if len(sys.argv) != 3:
        print("Uso: python processador_sequencial.py <imagem_de_entrada> <imagem_de_saida>")
        sys.exit(1)

    # Pega os caminhos dos arquivos a partir dos argumentos
    caminho_entrada = sys.argv[1]
    caminho_saida = sys.argv[2]

    try:
        # Tenta abrir a imagem de entrada
        imagem_original = Image.open(caminho_entrada)
        if imagem_original.mode != 'RGB':
            raise Exception("Desculpe, imagem não está no formato RGB")
    except FileNotFoundError:
        print(f"Erro: O arquivo '{caminho_entrada}' não foi encontrado.")
        sys.exit(1)

    print(f"Processando a imagem '{caminho_entrada}' de forma sequencial...")
    
    # --- Medição de Tempo ---
    # Marca o tempo de início do processamento
    inicio = time.time()

    # Chama a função que executa a conversão
    imagem_processada = converter_para_escala_de_cinza(imagem_original, 0, imagem_original.width, imagem_original.height)
    
    fim = time.time()

    # Marca o tempo de fim do processamento
    inicio2 = time.time()
    print(f"Processando a imagem '{caminho_entrada}' com multithreading...")

    imagem_processada2 = Image.new("RGB", imagem_original.size)
    largura = imagem_original.width
    intervalo = largura // 4

    resultados = [None] * 4
    def worker(idx, inicio, fim):
        resultados[idx] = converter_para_escala_de_cinza(imagem_original, inicio, fim, imagem_original.height)

    threads = []
    for i in range(4):
        inicio_parte = i * intervalo
        fim_parte = (i + 1) * intervalo if i < 3 else largura
        thread = threading.Thread(target=worker, args=(i, inicio_parte, fim_parte))
        threads.append(thread)
        thread.start()

    for thread in threads:
        thread.join()

    for j in range(4):
        imagem_processada2.paste(resultados[j], (j * intervalo, 0))

    fim2 = time.time()

    # Salva a imagem resultante no caminho especificado
    imagem_processada.save(caminho_saida)
    caminho_multithread = caminho_saida.replace(".jpg", "_multithread.jpg")
    imagem_processada2.save(caminho_multithread)
    # Calcula e exibe o tempo total de execução em milissegundos
    tempo_execucao_ms = (fim - inicio) * 1000
    tempo_execucao_ms2 = (fim2 - inicio2) * 1000
    
    print(f"Imagem convertida com sucesso e salva em '{caminho_saida}'")
    print(f"Tempo de execução (sequencial): {tempo_execucao_ms:.2f} ms\n")
    print(f"Imagem convertida com sucesso e salva em '{caminho_multithread}'")
    print(f"Tempo de execução (sequencial): {tempo_execucao_ms2:.2f} ms")

# Ponto de entrada do script: verifica se ele está sendo executado diretamente
if __name__ == "__main__":
    main()