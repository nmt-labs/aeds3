/*
 * ainda em desenvolvimento
 */
package externalsort;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import musica.Musica;

public class CommonIntercalation { // C:\Users\natht\Desktop\aeds3\db\arqTemp
  private String fileName = "db" + File.separator + "musicas.db", fileTemp = "db" + File.separator + "fileTemp" + File.separator +"outputTemp", typeTemp = ".db";
  private RandomAccessFile file;
  private int qntFiles, blockSize, lastId, numPrimRead, numPrimWrite, numTmpPrim, numTmpSec;
  private RandomAccessFile[] tempOutput, tempInput;
  private File tempFile;
  private Musica[] logs;
  private long[] remainingBytesTmp, filePos; // posicao do ponteiro em cada arquivo temporario
  private int[] counterMusicToRead;

  public CommonIntercalation(int qntFiles, int blockSize) throws FileNotFoundException {
    file = new RandomAccessFile(fileName, "rw");
    this.qntFiles = qntFiles;
    this.blockSize = blockSize;
    this.tempInput = new RandomAccessFile[qntFiles];
    this.tempOutput = new RandomAccessFile[qntFiles];
    this.filePos = new long[qntFiles];
    this.counterMusicToRead = new int[qntFiles];
    this.remainingBytesTmp = new long[qntFiles];
    this.numTmpPrim = -1;
    this.numTmpSec = -1;
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

  /**
   * Distribute main file in n temporary files
   * @throws Exception
   */
  private void distribute() throws Exception {
    // ler o id do inicio do arquivo
    file.seek(0);
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
          tempOutput[index].writeChar(' ');
          tempOutput[index].writeInt(logs[i].toByteArray().length);
          tempOutput[index].write(logs[i].toByteArray());
        }
      }
      index = (index + 1) % qntFiles; // seleciona o proximo arquivo a armazenar o bloco
    }
    closeTemp();
    file.close();
  }
  
  /**
   * Intercalation of temp files to ordenate
   * @throws Exception
   */
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
    System.out.println(tempInput[0].length() + " " + tempInput[1]);

    while (!(numTmpPrim == 1 && numTmpSec == 0 || numTmpPrim == 0 && numTmpSec == 1)) { // enquanto existir apenas um arquivo para leitura -> os outros arquivos estao vazios
      mergeFiles(indexInsertion);
      System.out.println("numTempPrim: " + numTmpPrim);
      System.out.println("numTempSec: " + numTmpSec);
      numTmpPrim = filesToRead();
      if (numTmpPrim == 0) {
        System.out.println("entrou no if");
        toggleTempFiles();
        blockSize = blockSize * qntFiles;
        numTmpSec = filesToRead();
      }
      indexInsertion = (indexInsertion + 1) % qntFiles;
      System.out.println(numTmpPrim + " " + numTmpSec + " " + indexInsertion);
    }
    for (int i = 0; i < qntFiles; i++) {
      tempInput[i].close();
      tempOutput[i].close();
    }

    int fileNumber = getFileId(indexInsertion);

    File musicaSort = new File("db" + File.separator + "musicaSort.db");
    RandomAccessFile fileTempFinal = new RandomAccessFile(fileTemp + fileNumber + typeTemp, "rw");
    RandomAccessFile sortedFile = new RandomAccessFile(musicaSort, "rw");
    // verifica se arquivo existe
    if (musicaSort.exists()) {
      // exclui se ja existir
      sortedFile.setLength(0);
    }
    //copia do arquivo temporario pro arquivo final
    copyFile(fileTempFinal, sortedFile);

    // deletar arquivos temporarios
    deleteTempFiles();
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
        tempFile = new File(fileTemp + (i + numPrimRead) + typeTemp);
        if (!tempFile.exists()) tempFile.createNewFile();
        tempOutput[i] = new RandomAccessFile(tempFile, "rw");
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
   * Delete temp files
   */
  private void deleteTempFiles() {
    for (int i = 0; i < qntFiles*2; i++) {
      tempFile = new File(fileTemp + i + typeTemp);
      if (tempFile.exists()) tempFile.delete();
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
          if (array[i].getId() < array[j].getId()) swap(array, i, j);
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
      tempOutput[i].setLength(0);
      filePos[i] = 0; // comeca a ler os arquivos (posicao 0)
    }
  }

  /**
   * Get index id from temp file
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
   * Merge logs from n files in one file
   * 
   * @param index index do arquivo que sera escrito
   * @throws Exception caso erro
   */
  private void mergeFiles(int index) throws Exception {
    Musica[] compareMusic = new Musica[qntFiles];
    int smallestValueIndex; // index do menor valor
    
    // iniciando o vetor de musicas -> armazena a primeira musica de cada arquivo no vetor
    // iniciando contador -> nenhuma musica ainda foi colocada no arquivo de escrita
    for (int i = 0; i < qntFiles; i++) {
      if (filePos[i] < tempInput[i].length()){
        compareMusic[i] = readMusicMerge(tempInput[i], filePos[i]);
      }

      filePos[i] = tempInput[i].getFilePointer(); // armazena a posicao do o ponteiro dos arquivos de entrada
      System.out.println("1 FilePos"+i+": "+filePos[i]);
      counterMusicToRead[i] = 0;
    }

    System.out.println("Tamanho arquivo temp"+0+": "+tempInput[0].length());
    System.out.println("Tamanho arquivo temp"+1+": "+tempInput[1].length());

    while (isBlockAvailable() && !isAllFilesAllRead()) {// enquanto ainda existe bloco de algum arquivo para a leitura -> algum elemento do vetor de contador e difernete do tamanho do bloco
      System.out.println("entrou no while");
      smallestValueIndex = firstAvailableFileToMerge(); // recebe menor index do bloco ainda valido
      filePos[smallestValueIndex] = tempInput[smallestValueIndex].getFilePointer();
      // encontra a menor musica do vetor
      for (int i = 0; i < compareMusic.length; i++) {
        if (counterMusicToRead[i] < blockSize && filePos[i] < tempInput[i].length()){ // pula o arquivo que ja teve seu bloco todo lido
          if (compareMusic[i].getId() <= compareMusic[smallestValueIndex].getId()) smallestValueIndex = i;
          //System.out.println(compareMusic[smallestValueIndex].toString());
        }
      }
      // colocar menor valor no arquivo de escrita
      tempOutput[index].writeChar(' ');
      tempOutput[index].writeInt(compareMusic[smallestValueIndex].toByteArray().length);
      tempOutput[index].write(compareMusic[smallestValueIndex].toByteArray());
      System.out.println("MUSICA QUE FOI ESCRITA NO ARQUIVO"+index+": "+compareMusic[smallestValueIndex].getName()); 
      
      counterMusicToRead[smallestValueIndex]++;
      // for(int i = 0; i < counterMusicToRead.length; i++) System.out.println("Contador"+i+": "+counterMusicToRead[i]);
      // se nao chegou no fim do arquivo, le a proxima musica
      if(filePos[smallestValueIndex] < tempInput[smallestValueIndex].length() && counterMusicToRead[smallestValueIndex] < blockSize){ 
        //System.out.println("entrou no ultimo if do mergeFiles");
        compareMusic[smallestValueIndex] = readMusicMerge(tempInput[smallestValueIndex], filePos[smallestValueIndex]); // le proxima musica do arquivo inserido
        //filePos[smallestValueIndex] = tempInput[smallestValueIndex].getFilePointer(); // armazena a posicao do o ponteiro do arquivo da menor musica encontrada  
        //System.out.println(compareMusic[smallestValueIndex].getName());
        //System.out.println(compareMusic[0].getName());
      }
    }
  }

  /**
   * Verify if all the block files were read and written on tempOutput
   * @return
   * @throws IOException
   */
  private boolean isBlockAvailable() throws IOException {
    boolean verify = false;
    for(int i = 0; i < counterMusicToRead.length; i++){
      System.out.println("Arquivo" + i + ": " + counterMusicToRead[i] + " " + blockSize + " " + filePos[i] + " " + tempInput[i].length());
      if (counterMusicToRead[i] < blockSize) verify = true;
    }
    
    return verify;
  }

  private boolean isAllFilesAllRead() throws IOException {
    boolean[] verify = new boolean[qntFiles];

    for (int i = 0; i < verify.length; i++) {
      if (filePos[i] >= tempInput[i].length()) verify[i] = true;
    }
    for (int i = 0; i < verify.length; i++) {
      if (verify[i] == false) return false;
    }
    return true;
  }

  private int firstAvailableFileToMerge(){
    for(int i = 0; i < counterMusicToRead.length; i++){
      if (counterMusicToRead[i] < blockSize) return i;
    }
    return -1;
  }
  
  /**
   * Read Music from file in a specific position
   * @param input
   * @param pos
   * @return
   * @throws Exception
   */
  private Musica readMusicMerge(RandomAccessFile input, long pos) throws Exception {
    input.seek(pos);
    return readMusic(input);
  }
  
  /**
   * Read Music from file
   * @param file
   * @return
   * @throws Exception
   */
  private Musica readMusic(RandomAccessFile file) throws Exception {
    Musica reg = null;
    char lapide = file.readChar();
    int sizeReg = file.readInt();
    byte[] bytearray = new byte[sizeReg];
    file.read(bytearray);
     if (lapide != '*') {
      reg = new Musica();
      reg.fromByteArray(bytearray);
     }
     //System.out.println( reg.toString());
    return reg;
  }

  /**
   * Copy file
   * @param tmp
   * @param target
   * @throws Exception
   */
  private void copyFile(RandomAccessFile tmp, RandomAccessFile target) throws Exception {
    target.writeInt(lastId);
    while (tmp.getFilePointer() != tmp.length()) {
        Musica musica = readMusic(tmp);
        target.writeChar(' ');
        target.writeInt(musica.toByteArray().length);
        target.write(musica.toByteArray());
    }
  }
}
