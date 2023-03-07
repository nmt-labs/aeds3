/*
 * ainda em desenvolvimento
 */
package externalsort;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import musica.Musica;

public class CommonIntercalation {
  private String fileName = "musicas.db", fileTemp = "arqTemp/saidaTemp", typeTemp = ".db";
  private RandomAccessFile file;
  private int qntFiles, blockSize, lastId, numPrimRead, numPrimWrite, numTmpPrim, numTmpSec;
  private RandomAccessFile[] tempOutput, tempInput;
  private Musica[] logs;
  private long[] remainingBytesTmp;
  private int[] filePos;

  public CommonIntercalation(int qntFiles, int blockSize) throws FileNotFoundException {
    file = new RandomAccessFile(fileName, "rw");
    this.qntFiles = qntFiles;
    this.blockSize = blockSize;
    this.tempInput = new RandomAccessFile[qntFiles];
    this.tempOutput = new RandomAccessFile[qntFiles];
  }

  public void sort() throws Exception {
    // distribui os registros nos arquivos temporarios
    System.out.println("Distribuindo arquivo em arquivos temporarios...");
    distribute();
    // intercala os arquivos temporarios
    System.out.println("Intercalando arquivos temporarios...");
    intercalate();

    System.out.println("Arquivo ordenado!");
  }

  private void distribute() throws Exception {
    // ler o id do inicio do arquivo
    this.lastId = file.readInt();
    numPrimRead = 0;

    startTemp(); // apenas cria os arquivos temporarios
    int index = 0;
    logs = new Musica[blockSize]; // array para ordenar os arquivos na memoria primaria

    while (!isAvaliable()) { // enquanto o arquivo nao termina
      logs = new Musica[blockSize];
      readLogs();
      sortArray(logs); // ordena bloco em memoria primaria
      for (int i = 0; i < logs.length; i++) { // escreve no arquivo temporario
        if (logs[i] != null) {
          // tempOutput[index].seek(0);
          tempOutput[index].writeChar(' ');
          tempOutput[index].writeInt(logs[i].toByteArray().length);
          tempOutput[index].write(logs[i].toByteArray());
        }
        System.out.println(i);
        //leituraTeste(tempOutput[index]);
      }
      index = (index + 1) % qntFiles; // seleciona o proximo arquivo a armazenar o bloco
    }
    closeTemp();
    file.close();
  }

  // private void leituraTeste(RandomAccessFile arq) throws Exception {
  //   Musica teste = readMusica(arq);
  //   System.out.println(teste.toString());
  // }
  
  private void intercalate() throws Exception {
    int indexInsertion = 0;
    numPrimRead = 0;
    numPrimWrite = qntFiles;
    for (int i = 0; i < qntFiles; i++) {
      // vetores que contem arquivos que contem os registros
      tempInput[i] = new RandomAccessFile(fileTemp + (i + numPrimRead) + typeTemp, "rw");
      tempOutput[i] = new RandomAccessFile(fileTemp + (i + numPrimWrite) + typeTemp, "rw");
      filePos[i] = 0; // comeca a ler os arquivos (posicao 0)
    }
    while (!(numTmpPrim == 1 && numTmpSec == 0 || numTmpPrim == 0 && numTmpSec == 1)) {
      mergeLogs(indexInsertion, filePos);
      numTmpPrim = filesToRead();
      if (numTmpPrim == 0) {
        toggleTempFiles();
        blockSize = blockSize * qntFiles;
        numTmpSec = filesToRead();
      }
      indexInsertion = (indexInsertion + 1) % qntFiles;
    }
    for (int i = 0; i < qntFiles; i++) {
      tempInput[i].close();
      tempOutput[i].close();
    }
    int fileNumber = getFileId(indexInsertion);

    // ...
  }
  
  // -------------------------------------- utilitarios
  
  /**
   * Verify if exists more data to read
   * @return boolean
   * @throws Exception
   */
  private boolean isAvaliable() throws Exception {
    return file.getFilePointer() == file.length();
  }
  
  /**
   * Create temp files
   */
  private void startTemp() {
    try {
      for (int i = 0; i < qntFiles; i++) {
        tempOutput[i] = new RandomAccessFile(fileTemp + (i + numPrimRead) + typeTemp, "rw");
      }
    } catch (IOException e) {
      System.err.println("Falha ao iniciar arquivos temporários");
      e.printStackTrace();
    }
  }

  /**
   * Close temp files
   */
  private void closeTemp() {
    try {
      for (int i = 0; i < qntFiles; i++) {
        tempOutput[i].close();
      }
    } catch (IOException e) {
      System.err.println("Falha ao finalizar conexão com arquivos temporários");
      e.printStackTrace();
    }
  }
  
  /**
   * Swap two items fron an array
   * @param array Array of Musica
   * @param i position to swap
   * @param j position to swap
   */
  public void swap(Musica[] array, int i, int j) {
    Musica temp = array[i].clone();
    array[i] = array[j].clone();
    array[j] = temp.clone();
  }

  /**
   * Selection sort by name
   * @param array Array of Musica
   */
  public void sortArray(Musica[] array){ // by name
    for (int i = (array.length - 1); i > 0; i--) {
      if (array[i] != null) { // caso o array tenha espacos vazios
        for (int j = 0; j < i; j++) {
          if (array[i].getName().compareTo(array[j].getName()) < 0) swap(array, i, j);
        }
      }
    }
  }
  
  /**
   * Read and set items in an array in primary memory
   * @throws Exception
   */
  private void readLogs() throws Exception {
    try {
      for (int i = 0; i < blockSize; i++) {
        if (!isAvaliable())
        logs[i] = readMusic(file);
        else
          i = blockSize;
      }
    } catch (Exception e) {
      System.err.println("Erro ao ler registros e salvar internamente");
      e.printStackTrace();
    }
  }

  /*
   * metodo para ler registro no arquivo principal (parecido com o do crud)
   */
  private Musica readMusic(RandomAccessFile arq) throws Exception {
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
   * Function that returns the number of files to read internally
   * @return total de arquivos disponiveis para ler
   */
  private int filesToRead() {
    int totFiles = 0;
    try {
      for (int i = 0; i < qntFiles; i++) {
        remainingBytesTmp[i] = tempInput[i].length() - tempInput[i].getFilePointer();
        if (remainingBytesTmp[i] > 0) {
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
  private void toggleTempFiles() throws Exception {
    for (int i = 0; i < qntFiles; i++) {
      tempInput[i].close();
      tempOutput[i].close();
    }
    numPrimRead = numPrimRead == 0 ? qntFiles : 0;
    numPrimWrite = numPrimWrite == 0 ? qntFiles : 0;
    for (int i = 0; i < qntFiles; i++) {
      tempInput[i] = new RandomAccessFile(fileTemp + (i + numPrimRead) + typeTemp, "rw");
      tempOutput[i] = new RandomAccessFile(fileTemp + (i + numPrimWrite) + typeTemp, "rw");
    }
  }

  /**
   * Função que pega um indentificador do indice de um arquivo temporário para
   * acessar arquivos dentro de um array ou dentro de funções
   * 
   * @param index parametro de indice do arquivo
   * @return
   */
  private int getFileId(int index) {
    if (index == 0) {
      return ((qntFiles - 1) + numPrimRead);
    } else {
      return ((index - 1) + numPrimRead);
    }
  }

  /**
   * Merge logs from n files and ordenate
   * 
   * @param index  parametro
   * @param filePos vetor com posicoes do arquivo
   * @throws Exception caso erro
   */
  private void mergeLogs(int index, int[] filePos) throws Exception {
    Musica[] compareMusic = new Musica[qntFiles];

    // iniciando o vetor de musicas
    for (int i = 0; i < qntFiles; i++) {
      compareMusic[i] = readMusicMerge(tempInput[i], 0);
    }

  }

  private Musica readMusicMerge(RandomAccessFile entrada, int pos) throws Exception {
    entrada.seek(pos);
    return readMusic(entrada);
  }
}
