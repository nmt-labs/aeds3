/*
 * ainda em desenvolvimento
 */
package externalsort;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import musica.Musica;

public class IntercalacaoComum {
  private String nomeArquivo = "db/musicas.db", fileTemp = "db/arqTemp/saidaTemp", tipoTemp = ".db";
  private RandomAccessFile arquivo;
  private int qntArquivos, tamBloco, ultimoId, numPrimRead, numPrimWrite, numTmpPrim, numTmpSec;
  private RandomAccessFile[] saidaTemporaria, entradaTemporaria;
  private Musica[] registros;
  private long[] bytesRestantesTmp;
  private int[] posArq;

  public IntercalacaoComum(int qntArquivos, int tamBloco) throws FileNotFoundException {
    arquivo = new RandomAccessFile(nomeArquivo, "rw");
    this.qntArquivos = qntArquivos;
    this.tamBloco = tamBloco;
    this.entradaTemporaria = new RandomAccessFile[qntArquivos];
    this.saidaTemporaria = new RandomAccessFile[qntArquivos];
  }

  public void ordenar() throws Exception {
    // distribui os registros nos arquivos temporarios
    System.out.println("Distribuindo arquivo em arquivos temporarios...");
    distribuir();
    // intercala os arquivos temporarios
    System.out.println("Intercalando arquivos temporarios...");
    intercalar();

    System.out.println("Arquivo ordenado!");
  }

  private void distribuir() throws Exception {
    // ler o id do inicio do arquivo
    this.ultimoId = arquivo.readInt();
    numPrimRead = 0;

    iniciarSaidaTemps(); // apenas cria os arquivos temporarios
    int index = 0;
    registros = new Musica[tamBloco]; // array para ordenar os arquivos na memoria primaria

    while (!isAvaliable()) { // enquanto o arquivo nao termina
      registros = new Musica[tamBloco];
      lerRegistros();
      System.out.println(registros[1].toString());
      // sort(registros); // ordena bloco em memoria primaria
      for (Musica musica : registros) { // escreve no arquivo temporario
        saidaTemporaria[index].seek(0);
        saidaTemporaria[index].writeChar(' ');
        saidaTemporaria[index].writeInt(musica.toByteArray().length);
        saidaTemporaria[index].write(musica.toByteArray());
        leituraTeste(saidaTemporaria[index]);
      }
      index = (index + 1) % qntArquivos; // seleciona o proximo arquivo a armazenar o bloco
    }
    finalizarSaidaTmp();
    arquivo.close();
  }

  private Musica lerMusica(RandomAccessFile entrada, int pos) throws Exception {
    entrada.seek(pos);
    return readMusica(entrada);
  }

  private void leituraTeste(RandomAccessFile arq) throws Exception {
    Musica teste = readMusica(arq);
    System.out.println(teste.toString());
  }

  private void intercalar() throws Exception {
    int indexInsercao = 0;
    numPrimRead = 0;
    numPrimWrite = qntArquivos;
    for (int i = 0; i < qntArquivos; i++) {
      // vetores que contem arquivos que contem os registros
      entradaTemporaria[i] = new RandomAccessFile(fileTemp + (i + numPrimRead) + tipoTemp, "rw");
      saidaTemporaria[i] = new RandomAccessFile(fileTemp + (i + numPrimWrite) + tipoTemp, "rw");
      posArq[i] = 0; // comeca a ler os arquivos (posicao 0)
    }
    while (!(numTmpPrim == 1 && numTmpSec == 0 || numTmpPrim == 0 && numTmpSec == 1)) {
      mesclarRegistros(indexInsercao, posArq);
      numTmpPrim = numeroArqsLer();
      if (numTmpPrim == 0) {
        alternarTmpFiles();
        tamBloco = tamBloco * qntArquivos;
        numTmpSec = numeroArqsLer();
      }
      indexInsercao = (indexInsercao + 1) % qntArquivos;
    }
    for (int i = 0; i < qntArquivos; i++) {
      entradaTemporaria[i].close();
      saidaTemporaria[i].close();
    }
    int numArq = getIdentificadorArq(indexInsercao);

    // ...
  }

  // -------------------------------------- utilitarios
  // ----------------------------------------
  /*
   * metodo para verificar se existem dados para leitura
   */
  private boolean isAvaliable() throws Exception {
    return arquivo.getFilePointer() == arquivo.length();
  }

  /*
   * metodo para criar os arquivos temporarios
   */
  private void iniciarSaidaTemps() {
    try {
      for (int i = 0; i < qntArquivos; i++) {
        saidaTemporaria[i] = new RandomAccessFile(fileTemp + (i + numPrimRead) + tipoTemp, "rw");
      }
    } catch (IOException e) {
      System.err.println("Falha ao iniciar arquivos temporários");
      e.printStackTrace();
    }
  }

  /*
   * metodo para fechar os arquivos temporarios
   */
  private void finalizarSaidaTmp() {
    try {
      for (int i = 0; i < qntArquivos; i++) {
        saidaTemporaria[i].close();
      }
    } catch (IOException e) {
      System.err.println("Falha ao finalizar conexão com arquivos temporários");
      e.printStackTrace();
    }
  }

  /*
   * metodo para ler e armazenar registros na array da memoria primaria
   */
  private void lerRegistros() throws Exception {
    try {
      for (int i = 0; i < tamBloco; i++) {
        if (!isAvaliable())
          registros[i] = readMusica(arquivo);
        else
          i = tamBloco;
      }
    } catch (Exception e) {
      System.err.println("Erro ao ler registros e salvar internamente");
      e.printStackTrace();
    }
  }

  /*
   * metodo para ler registro no arquivo principal (parecido com o do crud)
   */
  private Musica readMusica(RandomAccessFile arq) throws Exception {
    Musica reg = null;
    char lapide = arq.readChar();
    int sizeReg = arq.readInt();
    byte[] bytearray = new byte[sizeReg];
    arq.read(bytearray);
    // if (lapide != '*') {
    reg = new Musica();
    reg.fromByteArray(bytearray);
    // }
    return reg;
  }

  /**
   * Função que retorna um numero de arquivos para ler internamente
   * 
   * @return total de arquivos disponiveis para ler
   */
  private int numeroArqsLer() {
    int totFiles = 0;
    try {
      for (int i = 0; i < qntArquivos; i++) {
        bytesRestantesTmp[i] = entradaTemporaria[i].length() - entradaTemporaria[i].getFilePointer();
        if (bytesRestantesTmp[i] > 0) {
          totFiles++;
        }
      }
    } catch (IOException e) {
      System.err.println("Erro ao verificar os dados de bytes nas temps");
      e.printStackTrace();
    }
    return totFiles;
  }

  /**
   * Alternar entre arquivos temporarios que estão sendo ordenados internamente
   * 
   * @throws Exception
   */
  private void alternarTmpFiles() throws Exception {
    for (int i = 0; i < qntArquivos; i++) {
      entradaTemporaria[i].close();
      saidaTemporaria[i].close();
    }
    numPrimRead = numPrimRead == 0 ? qntArquivos : 0;
    numPrimWrite = numPrimWrite == 0 ? qntArquivos : 0;
    for (int i = 0; i < qntArquivos; i++) {
      entradaTemporaria[i] = new RandomAccessFile(fileTemp + (i + numPrimRead) + tipoTemp, "rw");
      saidaTemporaria[i] = new RandomAccessFile(fileTemp + (i + numPrimWrite) + tipoTemp, "rw");
    }
  }

  /**
   * Função que pega um indentificador do indice de um arquivo temporário para
   * acessar arquivos dentro de um array ou dentro de funções
   * 
   * @param index parametro de indice do arquivo
   * @return
   */
  private int getIdentificadorArq(int index) {
    if (index == 0) {
      return ((qntArquivos - 1) + numPrimRead);
    } else {
      return ((index - 1) + numPrimRead);
    }
  }

  /**
   * Testando
   * 
   * @param index  parametro
   * @param posArq vetor com posicoes do arquivo
   * @throws Exception caso erro
   */
  private void mesclarRegistros(int index, int[] posArq) throws Exception {
    Musica[] musicasComparar = new Musica[qntArquivos];

    // iniciando o vetor de musicas
    for (int i = 0; i < qntArquivos; i++) {
      musicasComparar[i] = lerMusica(entradaTemporaria[i], 0);
    }

  }
}
