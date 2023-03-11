/*
 * ainda em desenvolvimento
 */
package externalsort;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import musica.Musica;

public class Selection {
  private String fileName = "db" + File.separator + "musicas.db", fileTemp = "db" + File.separator + "fileTemp" + File.separator +"outputTemp", typeTemp = ".db";
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
    logs = new Musica[arraySize]; // array para criar o heap
    heapsort(index);

    // while (!isAvaliable()) { // enquanto o arquivo nao termina
    //   logs = new Musica[blockSize];
    //   readLogs(); // preenche array
    //   sortArray(logs); // ordena bloco em memoria primaria
    //   for (int i = 0; i < logs.length; i++) { // escreve no arquivo temporario
    //     if (logs[i] != null) {
    //       tempOutput[index].writeChar(' ');
    //       tempOutput[index].writeInt(logs[i].toByteArray().length);
    //       tempOutput[index].write(logs[i].toByteArray());
    //     }
    //   }
    //   index = (index + 1) % qntFiles; // seleciona o proximo arquivo a armazenar o bloco
    // }
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

    while (!(numTmpPrim == 1 && numTmpSec == 0 || numTmpPrim == 0 && numTmpSec == 1)) { // enquanto existir apenas um arquivo para leitura -> os outros arquivos estao vazios
      mergeFiles(indexInsertion);
      numTmpPrim = filesToRead();
      if (numTmpPrim == 0) {
        toggleTempFiles();
        // blockSize = blockSize * qntFiles;
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
  
  // /**
  //  * Read and set items in an array in primary memory
  //  * @throws Exception
  //  */
  // private void readLogs() throws Exception {

  //   try {
  //     for (int i = 0; i < arraySize; i++) {
  //       if (!isAvaliable())
  //       logs[i] = readMusic(file);
  //       else
  //         i = arraySize;
  //     }
  //   } catch (Exception e) {
  //     System.err.println("Erro ao ler registros e salvar internamente");
  //     e.printStackTrace();
  //   }
  // }
  
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
    Musica wroteMusic = new Musica();
    int smallestValueIndex, wroteMusicId = 0; // index do menor valor
    
    // iniciando o vetor de musicas -> armazena a primeira musica de cada arquivo no vetor
    // iniciando contador -> nenhuma musica ainda foi colocada no arquivo de escrita
    for (int i = 0; i < qntFiles; i++) {
      if (filePos[i] < tempInput[i].length()){
        compareMusic[i] = readMusicMerge(tempInput[i], filePos[i]);
      }

      filePos[i] = tempInput[i].getFilePointer(); // armazena a posicao do o ponteiro dos arquivos de entrada
      availableFiles[i] = true;
    }

    while (isFilesAvailables() && !isAllFilesAllRead()) { // enquanto ainda existe bloco de algum arquivo para a leitura -> algum elemento do vetor de contador e difernete do tamanho do bloco
      smallestValueIndex = firstAvailableFileToMerge(); // recebe menor index do bloco ainda valido
      filePos[wroteMusicId] = tempInput[wroteMusicId].getFilePointer();
      // encontra a menor musica do vetor
      for (int i = 0; i < compareMusic.length; i++) {
        if (availableFiles[i] == true && filePos[i] < tempInput[i].length()){ // pula o arquivo que ja teve seu bloco todo lido
          if (compareMusic[i].getId() <= compareMusic[smallestValueIndex].getId()) smallestValueIndex = i;
        }
      }
      // colocar menor valor no arquivo de escrita
      tempOutput[index].writeChar(' ');
      tempOutput[index].writeInt(compareMusic[smallestValueIndex].toByteArray().length);
      tempOutput[index].write(compareMusic[smallestValueIndex].toByteArray());
      //System.out.println("MUSICA QUE FOI ESCRITA NO ARQUIVO"+index+": "+compareMusic[smallestValueIndex].getId()+" "+compareMusic[smallestValueIndex].getName()); 
      wroteMusic = compareMusic[smallestValueIndex].clone();
      wroteMusicId = smallestValueIndex;
      
      
      // se nao chegou no fim do arquivo, le a proxima musica
      if(filePos[smallestValueIndex] < tempInput[smallestValueIndex].length()){ 
        compareMusic[smallestValueIndex] = readMusicMerge(tempInput[smallestValueIndex], filePos[smallestValueIndex]); // le proxima musica do arquivo inserido
        // se musica lida for menor que a musica escrita, desconsidera o arquivo
        if(wroteMusic.getId() > compareMusic[smallestValueIndex].getId()) availableFiles[smallestValueIndex] = false;
      }
    }
  }

  /**
   * Verify if theres at least one file available to merge
   * @return
   * @throws IOException
   */
  private boolean isFilesAvailables() throws IOException {
    int counter = 0;
    for(int i = 0; i < availableFiles.length; i++){
      if (availableFiles[i] == false) counter++;
    }
    // se todos os arquivos forem false
    if (counter >= availableFiles.length) return false;
    else return true;
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
    for(int i = 0; i < availableFiles.length; i++){
      if (availableFiles[i] == true) return i;
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

  //  --- Heapsort ---

  // public void swap(int i, int j) {
  //   Musica temp = logs[i].clone();
  //   int weightTemp = weight[i];
  //   logs[i] = logs[j].clone();
  //   weight[i] = weight[j];
  //   logs[j] = temp.clone();
  //   weight[j] = weightTemp;

  // }


  // public void construir(int tamHeap){
  //   for(int i = tamHeap; i > 1 && (logs[i].getId() > logs[i/2].getId()); i /= 2){
  //     swap(i, i/2);
  //   }
  // }


  // public void reconstruir(int tamHeap){
  //   int i = 1;
  //   while(i <= (tamHeap/2)){
  //     int filho = getMaiorFilho(i, tamHeap);
  //     if(logs[i].getId() > logs[i/2].getId() && weight[i] > weight[i/2]){
  //       swap(i, filho);
  //       i = filho;
  //     }else{
  //       i = tamHeap;
  //     }
  //   }
  // }

  // public int getMaiorFilho(int i, int tamHeap) { // acho que aqui tem que ser MENOR FILHO
  //   int filho;
  //   if (2*i == tamHeap || logs[2*i].getId() > logs[2*i+1].getId()){
  //     filho = 2*i;
  //   } else if(logs[2*i].getId() == logs[2*i+1].getId()){
  //     if(2*i == tamHeap || logs[2*i].getName().compareTo(logs[2*i+1].getName()) > 0){ 
  //       filho = 2*i;
  //     }else{
  //       filho = 2*i + 1;
  //     }
  //   } else {
  //     filho = 2*i + 1;
  //   }
  //   return filho;
  // }

  // public void heapsort(int index) throws Exception{
  //   Musica wroteMusic = new Musica();
  //   weight = new int[logs.length];
  //   int currentWeight = 0;
  //   //Alterar o vetor ignorando a posicao zero
  //   Musica[] tmp = new Musica[logs.length+1]; // arraySize + 1 ?
  //   for(int i = 0; i < logs.length; i++){
  //       tmp[i+1] = logs[i].clone();
  //       weight[i] = currentWeight;
  //   }
  //   logs = tmp;

  //   //Contrucao do heap
  //   for(int tamHeap = 2; tamHeap <= logs.length; tamHeap++){
  //       construir(tamHeap);
  //   }

  //   while (isAvaliable()) { // enquanto ainda tem registros para serem lidos no arquivo principal
  //     // Ordenar o heap
  //     int tamHeap = logs.length;
  //     while(tamHeap > 1){
  //         swap(1, tamHeap--);
  //         reconstruir(tamHeap);
  //     }

  //     // se o peso atual for diferente do peso do menor registro, tocar arquivo de output
  //     if (currentWeight != weight[0]) index = (index + 1) % qntFiles;
  //     // como o heap foi reordenado, agora o currentWeight sera o mesmo do 1o item do array
  //     currentWeight = weight[0];

  //     // Escrever o menor elemento
  //     tempOutput[index].writeChar(' ');
  //     tempOutput[index].writeInt(logs[0].toByteArray().length);
  //     tempOutput[index].write(logs[0].toByteArray());
  //     wroteMusic = logs[0].clone();

  //     logs[0] = readMusic(file);

  //     // se a musica lida for menor que a escrita, adiciona o peso
  //     if (logs[0].getId() < wroteMusic.getId()) weight[0]++;
  //   }

  //   // escreve o resto dos registros que estao no logs
  //   for (int i = 1; i < logs.length; i++) {
  //     tempOutput[index].writeChar(' ');
  //     tempOutput[index].writeInt(logs[i].toByteArray().length);
  //     tempOutput[index].write(logs[i].toByteArray());
  //   }
  // }
  // //  --- Heapsort ---
  public void heapsort(int[] array) {
    vetor = array;          // vetor global recebe o vetor passado como parâmetro
    tam = vetor.length - 1;     // o tamanho deste vetor é armazenado em 'tam', que também é global
    
    // Chama a função para construir um Max-Heap
    constroiHeap();
    
    // Assim que a Max-Heap foi criada, o processo de ordenação pode começar.
    // Através desse loop que a troca do valor do topo com o valor da última posição da Heap é feita
    for (int i = tam; i > 0; i--) {
        troca(0, tam);      // Troca a posição
        tam -= 1;           // Diminui 'tam' para não alterar a posição do maior valor nas próximas iterações
        maxHeapifica(0);    // Como existe um valor menor no topo, é necessário heapificar novamente a árvore inteira
    }
}

// Função que constrói o Max-Heap
private void constroiHeap() {
    // Como o último nível da árvore não tem filhos, a construção se inicia no último elemento da penúltima.
    // Esse elemento se encontra bem no meio do vetor, ou seja, tam/2:
    int meio = (int) (tam/2);
    
    // Para cada elemento do penúltimo nível, chama o maxHeapifica, ou seja
    // encontra o maior elemento e coloca como pai
    for (int i = meio - 1; i >= 0; i--) {
        maxHeapifica(i);
    }
}

  private void troca(int i, int j) {
    Musica temp = logs[i].clone();
      int weightTemp = weight[i];
      logs[i] = logs[j].clone();
      weight[i] = weight[j];
      logs[j] = temp.clone();
      weight[j] = weightTemp;
  }

// Função maxHeapifica
// Essa função é o core do algoritmo.
// Ela faz a comparação entre os valores de um Heap e ao encontrar o maior, o coloca como pai da Heap.
// Isso faz com que qualquer Heap se torne um Max-Heap.
private void maxHeapifica(int pai, int tam) {
    int maior = pai,            // O maior elemento é o pai, até que se prove o contrário.
        esquerda = 2 * pai + 1,     // Pega a posição do filho da esquerda
        direita = 2 * pai + 2;  // e a do filho da direita.
    
    // Se o filho da esquerda existe, ou seja, se ele está dentro do intervalo verificável do array E
    // Se este filho é maior que o pai (que no momento é o 'maior')
    if (esquerda <= tam && logs[esquerda].getId() > logs[maior].getId() && weight[esquerda] >= weight[maior])
        maior = esquerda;
    
    // Se o filho da direita existe, ou seja, se ele está dentro do intervalo verificável do array E
    // Se este filho é maior que o 'maior' (que no momento pode ser o 'pai' ou o 'esquerda')
    if (direita <= tam && logs[direita].getId() > logs[maior].getId() && weight[direita] >= weight[maior])
        maior = direita;
    
    // Se ao chegar até aqui o 'pai' deixou de ser o 'maior' valor
    if (maior != pai) {
        troca(pai, maior);      // Faz a troca de posições
        maxHeapifica(maior, tam);    // Continua heapificando com o valor que foi trocado
    }
}
}
