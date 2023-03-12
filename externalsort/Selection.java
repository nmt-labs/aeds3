package externalsort;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import musica.Musica;

public class Selection {
  private String fileName = "db" + File.separator + "musicas.db",
      fileTemp = "db" + File.separator + "fileTemp" + File.separator + "outputTemp", typeTemp = ".db";
  private RandomAccessFile file;
  private int qntFiles, arraySize, lastId, numPrimRead, numPrimWrite, numTmpPrim, numTmpSec;
  private RandomAccessFile[] tempOutput, tempInput;
  private File tempFile;
  private Musica[] logs;
  private long[] remainingBytesTmp, filePos; // posicao do ponteiro em cada arquivo temporario
  private boolean[] availableFiles;
  private int[] weight;

  public Selection(int qntFiles, int arraySize) throws FileNotFoundException {
    file = new RandomAccessFile(fileName, "rw");
    this.qntFiles = qntFiles;
    this.arraySize = arraySize;
    this.tempInput = new RandomAccessFile[qntFiles];
    this.tempOutput = new RandomAccessFile[qntFiles];
    this.filePos = new long[qntFiles];
    this.availableFiles = new boolean[qntFiles];
    this.remainingBytesTmp = new long[qntFiles];
    this.numTmpPrim = -1;
    this.numTmpSec = -1;
  }

  /**
   * Selection sort
   * @throws Exception
   */
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
   * 
   * @throws Exception
   */
  private void distribute() throws Exception {
    // ler o id do inicio do arquivo
    file.seek(0);
    this.lastId = file.readInt();
    numPrimRead = 0;

    startTemp(); // apenas cria os arquivos temporarios
    int index = 0;
    logs = new Musica[arraySize]; // array para criar o heap
    weight = new int[arraySize];
    readLogs();
    heapsort(index);

    closeTemp();
    file.close();
  }

  /**
   * Intercalation of temp files to ordenate
   * 
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

    while (!(numTmpPrim == 1 && numTmpSec == 0 || numTmpPrim == 0 && numTmpSec == 1)) { // enquanto existir apenas um
                                                                                        // arquivo para leitura -> os
                                                                                        // outros arquivos estao vazios
      mergeFiles(indexInsertion);
      numTmpPrim = filesToRead();
      if (numTmpPrim == 0) {
        toggleTempFiles();
        numTmpSec = filesToRead();
      }
      indexInsertion = (indexInsertion + 1) % qntFiles;
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
    // copia do arquivo temporario pro arquivo final
    copyFile(fileTempFinal, sortedFile);

    // deletar arquivos temporarios
    deleteTempFiles();
  }

  // -------------------------------------- utilitarios

  /**
   * Verify if exists more data to read
   * 
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
        if (!tempFile.exists())
          tempFile.createNewFile();
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
    for (int i = 0; i < qntFiles * 2; i++) {
      tempFile = new File(fileTemp + i + typeTemp);
      if (tempFile.exists())
        tempFile.delete();
    }
  }

  /**
   * Read and set items in an array in primary memory
   * 
   * @throws Exception
   */
  private void readLogs() throws Exception {

    try {
      for (int i = 0; i < arraySize; i++) {
        if (!isAvaliable()) {
          logs[i] = readMusic(file);
          weight[0] = 0;
        } else
          i = arraySize;
      }
    } catch (Exception e) {
      System.err.println("Erro ao ler registros e salvar internamente");
      e.printStackTrace();
    }
  }

  /**
   * Function that returns the number of files to read internally
   * 
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
   * Toggle temp files
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
   * Merge logs from n files in one file
   * 
   * @param index index do arquivo que sera escrito
   * @throws Exception caso erro
   */
  private void mergeFiles(int index) throws Exception {
    Musica[] compareMusic = new Musica[qntFiles];
    Musica wroteMusic = new Musica();
    int smallestValueIndex, wroteMusicId = 0; // index do menor valor

    // iniciando o vetor de musicas -> armazena a primeira musica de cada arquivo no
    // vetor
    // iniciando contador -> nenhuma musica ainda foi colocada no arquivo de escrita
    for (int i = 0; i < qntFiles; i++) {
      if (filePos[i] < tempInput[i].length()) {
        compareMusic[i] = readMusicMerge(tempInput[i], filePos[i]);
      }

      filePos[i] = tempInput[i].getFilePointer(); // armazena a posicao do o ponteiro dos arquivos de entrada
      availableFiles[i] = true;
    }

    while (isFilesAvailables() && !isAllFilesAllRead()) { // enquanto ainda existe bloco de algum arquivo para a leitura
                                                          // -> algum elemento do vetor de contador e difernete do
                                                          // tamanho do bloco

      smallestValueIndex = firstAvailableFileToMerge(); // recebe menor index do bloco ainda valido
      filePos[wroteMusicId] = tempInput[wroteMusicId].getFilePointer();
      // encontra a menor musica do vetor
      for (int i = 0; i < compareMusic.length; i++) {
        if (availableFiles[i] == true && filePos[i] < tempInput[i].length()) { // pula o arquivo que ja teve seu bloco
                                                                               // todo lido
          if (compareMusic[i].getId() <= compareMusic[smallestValueIndex].getId())
            smallestValueIndex = i;
        }
      }
      // colocar menor valor no arquivo de escrita
      tempOutput[index].writeChar(' ');
      tempOutput[index].writeInt(compareMusic[smallestValueIndex].toByteArray().length);
      tempOutput[index].write(compareMusic[smallestValueIndex].toByteArray());

      wroteMusic = compareMusic[smallestValueIndex].clone();
      wroteMusicId = smallestValueIndex;

      // se nao chegou no fim do arquivo, le a proxima musica
      if (filePos[smallestValueIndex] < tempInput[smallestValueIndex].length()) {
        // le proxima musica do arquivo inserido
        compareMusic[smallestValueIndex] = readMusicMerge(tempInput[smallestValueIndex], filePos[smallestValueIndex]);
        // se musica lida for menor que a musica escrita, desconsidera o arquivo
        if (wroteMusic.getId() > compareMusic[smallestValueIndex].getId())
          availableFiles[smallestValueIndex] = false;
      }
    }
  }

  /**
   * Verify if theres at least one file available to merge
   * 
   * @return
   * @throws IOException
   */
  private boolean isFilesAvailables() throws IOException {
    int counter = 0;
    for (int i = 0; i < availableFiles.length; i++) {
      if (availableFiles[i] == false)
        counter++;
    }
    // se todos os arquivos forem false
    if (counter >= availableFiles.length)
      return false;
    else
      return true;
  }

  /**
   * Verify if all temp files are read
   * @return
   * @throws IOException
   */
  private boolean isAllFilesAllRead() throws IOException {
    boolean[] verify = new boolean[qntFiles];

    for (int i = 0; i < verify.length; i++) {
      if (filePos[i] >= tempInput[i].length())
        verify[i] = true;
    }
    for (int i = 0; i < verify.length; i++) {
      if (verify[i] == false)
        return false;
    }
    return true;
  }

  /**
   * Find first file available to merge
   * @return
   */
  private int firstAvailableFileToMerge() {
    for (int i = 0; i < availableFiles.length; i++) {
      if (availableFiles[i] == true)
        return i;
    }
    return -1;
  }

  /**
   * Read Music from file in a specific position
   * 
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
   * 
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
    return reg;
  }

  /**
   * Copy file
   * 
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

  // --- Heapsort ---
  public void heapsort(int index) throws Exception {
    Musica wroteMusic = new Musica();
    weight = new int[logs.length];
    int currentWeight = 0;
    int size;

    while (!isAvaliable()) { // enquanto ainda tem registros para serem lidos no arquivo principal
      size = logs.length - 1;
      buildHeap(size);

      // ordenar heap
      for (int i = size; i > 0; i--) {
        swap(0, size);
        size -= 1;
        heapify(0, size);
      }

      // se o peso atual for diferente do peso do menor registro, tocar arquivo de output
      if (currentWeight != weight[0])
        index = (index + 1) % qntFiles;
      // como o heap foi reordenado, agora o currentWeight sera o mesmo do 1o item do array
      currentWeight = weight[0];

      // Escrever o menor elemento
      tempOutput[index].writeChar(' ');
      tempOutput[index].writeInt(logs[0].toByteArray().length);
      tempOutput[index].write(logs[0].toByteArray());
      wroteMusic = logs[0].clone();

      logs[0] = readMusic(file);

      // se a musica lida for menor que a escrita, adiciona o peso
      if (logs[0].getId() < wroteMusic.getId())
        weight[0]++;
    }

    size = logs.length - 1;
    // ultima ordenacao
    for (int i = size; i > 0; i--) {
      swap(0, size);
      size -= 1;
      heapify(0, size);
    }

    // escreve o resto dos registros que estao no logs
    for (int i = 0; i < logs.length; i++) {
      tempOutput[index].writeChar(' ');
      tempOutput[index].writeInt(logs[i].toByteArray().length);
      tempOutput[index].write(logs[i].toByteArray());

      wroteMusic = logs[i].clone();
    }
  }

  /**
   * Build heap
   * @param size
   */
  private void buildHeap(int size) {
    int half = (int) (size / 2);

    for (int i = half - 1; i >= 0; i--) {
      heapify(i, size);
    }
  }

  /**
   * Swap items from array
   * @param i
   * @param j
   */
  private void swap(int i, int j) {
    Musica temp = logs[i].clone();
    int weightTemp = weight[i];
    logs[i] = logs[j].clone();
    weight[i] = weight[j];
    logs[j] = temp.clone();
    weight[j] = weightTemp;
  }

  /**
   * Order heap
   * @param parent
   * @param size
   */
  private void heapify(int parent, int size) {
    int biggest = parent,
        left = 2 * parent + 1,
        right = 2 * parent + 2;

    if (left <= size && logs[left].getId() > logs[biggest].getId() && weight[left] >= weight[biggest])
      biggest = left;

    if (right <= size && logs[right].getId() > logs[biggest].getId() && weight[right] >= weight[biggest])
      biggest = right;

    if (biggest != parent) {
      swap(parent, biggest); 
      heapify(biggest, size);
    }
  }
}
