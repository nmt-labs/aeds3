package extendiblehash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import bplustree.Key;
import musica.Musica;

public class ExtendibleHash {

  private RandomAccessFile pointersEnd;
  private RandomAccessFile bucketsList;
  private String hasher = "db" + File.separator + "hasher.db", buckets = "db" + File.separator + "buckets.db";
  private int buckSize;
  private int p;
  private final int BuckByteSize; // tamanho do espaco para todos os buckets em bytes
  private final int INT_SIZE = (Integer.BYTES + Long.BYTES); // id + pos
  private final int INT_SIZE_2 = (2 * Integer.BYTES);
  private final int CONST_MATH = 4;

  private String fileName = "db" + File.separator + "musicas.db";
  private RandomAccessFile file;

  public RandomAccessFile getPointersEnd() {return pointersEnd;}
  public void setPointersEnd(RandomAccessFile pointersEnd) {this.pointersEnd = pointersEnd;}
  public RandomAccessFile getBucketsList() {return bucketsList;}
  public void setBucketsList(RandomAccessFile bucketsList) {this.bucketsList = bucketsList;}
  public int getbuckSize() {return buckSize;}
  public void setbuckSize(int buckSize) {this.buckSize = buckSize;}
  public int getP() {return p;}
  public void setP(int p) {this.p = p;}
  public int getBuckByteSize() {return BuckByteSize;}
  public int getINT_SIZE() {return INT_SIZE;}
  public int getINT_SIZE_2() {return INT_SIZE_2;}
  public int getCONST_MATH() {return CONST_MATH;}

  
  public ExtendibleHash(RandomAccessFile pointersEnd, RandomAccessFile bucketsList, int buckSize, int p, int BuckByteSize) throws FileNotFoundException {
    this.pointersEnd = pointersEnd;
    this.bucketsList = bucketsList;
    this.buckSize = buckSize;
    this.p = p;
    this.BuckByteSize = BuckByteSize;
    this.file = new RandomAccessFile(fileName, "rw");
  }

  public ExtendibleHash(int buckSize) throws IOException, Exception {
    System.out.println("Access HASH");
    this.file = new RandomAccessFile(fileName, "rw");
    this.buckSize = buckSize; // 8
    this.p = 2;
    pointersEnd = new RandomAccessFile(hasher, "rw");
    bucketsList = new RandomAccessFile(buckets, "rw");
    // definindo bytes do bucket
    BuckByteSize = buckSize * INT_SIZE + INT_SIZE_2;
    if (!isAvaliable()) {
      // cria novo hash
      System.out.println("Criando novo HASH");
      startHash();
    } else {
      // le qual o valor atual de p
      pointersEnd.seek(0);
      this.p = pointersEnd.readInt();
      System.out.println("Sistema de HASH pronto para operar");
    }
  }

  /**
   * Iniciando hash e inserindo valores iniciais para o bucket e posições
   * 
   * @throws IOException
   */
  public void startHash() throws IOException {
    p = 2;
    pointersEnd.setLength((int) Math.pow(2, p) * Long.BYTES);
    bucketsList.setLength(BuckByteSize * (int) Math.pow(2, p));
    bucketsList.seek(0);
    pointersEnd.seek(0);
    pointersEnd.writeInt(p);
    int value = (int) Math.pow(2, p);
    createData(value);
  }

  /**
   * Função para dar start nas linhas dos bucket
   * 
   * @param value valor de repetição de linhas
   * @throws IOException
   */
  private void createData(int value) throws IOException {
    System.out.println("Valor de repetição: " + value);
    for (int i = 0; i < value; i++) {
      pointersEnd.writeLong(bucketsList.getFilePointer());
      bucketsList.writeInt(p); // valor de p
      bucketsList.writeInt(0); // quantidade de itens no bucket
      for (int j = 0; j < buckSize; j++) { // inicia todos os valores com -1 ja que a lista esta vazia
        bucketsList.writeInt(-1);
        bucketsList.writeLong(-1);
      }
    }
  }

  /**
   * Função que verifica se o o hash foi criado e esta funcionando no momento para
   * receber novos dados e consultas
   * 
   * @return
   * @throws IOException
   */
  public boolean isAvaliable() throws IOException {
    return (pointersEnd.length() > 0 && bucketsList.length() > 0);
  }

  /**
   * O método que adiciona um novo elemento no hash já criado
   * Função para carga inicial
   * 
   * @param index   index do elemento
   * @param pointer ptr para o elemento
   */
  public void add() throws Exception {
    Key key = new Key();
    file.readInt();
    while (!isAvaliable()) {
      key = readKey(file);
      add(key.getId(), key.getPointer());
    }
  }

  /**
   * Leitura de chave
   * 
   * @param file
   * @return
   * @throws Exception
   */
  private Key readKey(RandomAccessFile file) throws Exception {
    Musica reg = null;
    char lapide = file.readChar();
    int sizeReg = file.readInt();
    byte[] bytearray = new byte[sizeReg];
    file.read(bytearray);
    Key key = new Key();
    if (lapide != '*') {
      long pointer = file.getFilePointer();
      reg = new Musica();
      reg.fromByteArray(bytearray);
      int id = reg.getId();
      key = new Key(id, pointer);
    }
    return key;
  }
  
  /**
   * Adição no bucket disponivel
   * 
   * @param qtd
   * @param index
   * @param pointer
   * @throws IOException
   */
  private void addBucket(int qtd, int index, long pointer) throws IOException {
      bucketsList.seek(bucketsList.getFilePointer() - Integer.BYTES);
      bucketsList.writeInt(qtd + 1);
      for (int i = 0; i < buckSize; i++) {
          int chave = bucketsList.readInt();
          long ptrWaiter = bucketsList.readLong();
          ptrWaiter = ptrWaiter + 0;
          if (chave == -1) {
              bucketsList.seek(bucketsList.getFilePointer() - (INT_SIZE));
              bucketsList.writeInt(index);
              bucketsList.writeLong(pointer);
              i = buckSize;
          }
      }
  }

  /**
   * O método que adiciona um novo elemento no hash já criado
   * Função responsavel por reparticionar um bucket e realocar caso o mesmo esteja
   * cheio
   * 
   * @param index   index do elemento
   * @param pointer ptr para o elemento
   */
  public void add(int index, long pointer) {
    try {
      System.out.println("INDEX ALVO: " + index);
      long posHash = (index % (int) Math.pow(2, p)) * 8 + CONST_MATH; // encontrar posicao do item na tabela hash
      System.out.println("Hash pos conta: " + posHash);
      pointersEnd.seek(posHash);
      long posBkt = pointersEnd.readLong();
      bucketsList.seek(posBkt);
      int pLoc = bucketsList.readInt(); // valor de p do bucket
      int qtd = bucketsList.readInt(); // le quantidade de itens no bucket
      if (qtd < buckSize) {
        // se tiver espaço no bucket
        addBucket(qtd, index, pointer);
      } else {
        // bucket cheio com P igual
        if (pLoc == p) {
          p++;
          pointersEnd.setLength((int) Math.pow(2, p) * Long.BYTES);
          pointersEnd.seek(0);
          pointersEnd.writeInt(p);
          int value = (int) Math.pow(2, p);
          for (int i = 0; i < value / 2; i++) {
            pointersEnd.writeLong(i * BuckByteSize);
          }
          for (int i = 0; i < value / 2; i++) {
            pointersEnd.writeLong(i * BuckByteSize);
          }
          bucketsList.setLength(bucketsList.length() + BuckByteSize);
          posHash = (index % (int) Math.pow(2, p - 1)) * 8 + CONST_MATH + ((int) Math.pow(2, p - 1) * Long.BYTES);
          pointersEnd.seek(posHash);
          pointersEnd.writeLong(bucketsList.length() - BuckByteSize);
          int[] keyStore = new int[buckSize];
          long[] ptrStore = new long[buckSize];
          bucketsList.seek(posBkt);
          bucketsList.writeInt(pLoc + 1);
          bucketsList.writeInt(0);
          for (int i = 0; i < buckSize; i++) {
            keyStore[i] = bucketsList.readInt();
            ptrStore[i] = bucketsList.readLong();
            bucketsList.seek(bucketsList.getFilePointer() - (INT_SIZE));
            bucketsList.writeInt(-1);
            bucketsList.writeLong(-1);
          }
          bucketsList.seek(bucketsList.length() - BuckByteSize);
          bucketsList.writeInt(pLoc + 1);
          bucketsList.writeInt(0);
          for (int i = 0; i < buckSize; i++) {
            bucketsList.writeInt(-1);
            bucketsList.writeLong(-1);
          }
          for (int i = 0; i < buckSize; i++) {
            if (keyStore[i] != -1) {
              add(keyStore[i], ptrStore[i]);
            }
          }
          add(index, pointer);
        } else {
          // Bucket cheio e p diferente
          bucketsList.setLength(bucketsList.length() + BuckByteSize);
          posHash = (index % (int) Math.pow(2, p - 1)) * 8 + CONST_MATH + ((int) Math.pow(2, p - 1) * Long.BYTES);
          pointersEnd.seek(posHash);
          pointersEnd.writeLong(bucketsList.length() - BuckByteSize);
          int[] keyStore = new int[buckSize];
          long[] ptrStore = new long[buckSize];
          bucketsList.seek(posBkt);
          bucketsList.writeInt(pLoc + 1);
          bucketsList.writeInt(0);
          for (int i = 0; i < buckSize; i++) {
            keyStore[i] = bucketsList.readInt();
            ptrStore[i] = bucketsList.readLong();
            bucketsList.seek(bucketsList.getFilePointer() - (INT_SIZE));
            bucketsList.writeInt(-1);
            bucketsList.writeLong(-1);
          }
          bucketsList.seek(bucketsList.length() - BuckByteSize);
          bucketsList.writeInt(pLoc + 1);
          bucketsList.writeInt(0);
          for (int i = 0; i < buckSize; i++) {
            bucketsList.writeInt(-1);
            bucketsList.writeLong(-1);
          }
          for (int i = 0; i < buckSize; i++) {
            if (keyStore[i] != -1) {
              add(keyStore[i], ptrStore[i]);
            }
          }
          add(index, pointer);
        }
      }
    } catch (Exception e) {
      System.err.println("Falha ao inserir no HASH\nerro: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * FUnção para atualizar um registro de dentro do hash automaticamente após sua
   * atualização dentro da DAO
   * 
   * @param index   indice do objeto de conta
   * @param novoEnd novo endereço dentro do arquivo de contas
   */
  public void atualizar(int index, long novoEnd) {
      try {
          int posHash = (index % (int) Math.pow(2, p)) * 8 + CONST_MATH;
          pointersEnd.seek(posHash);
          long posBkt = pointersEnd.readLong();
          bucketsList.seek(posBkt);
          bucketsList.readInt();
          doMoving(novoEnd, index);
      } catch (Exception e) {
          System.err.println("Falha ao alterar no HASH\nerro: " + e.getMessage());
          e.printStackTrace();
      }
  }

  /**
   * Realiza a mudança de ponteiros dentro do Hash
   * 
   * @param novoEnd endereço alvo que foi recem alterado
   * @param index   indice da conta
   * @throws IOException
   */
  private void doMoving(long novoEnd, int index) throws IOException {
      int qtd = bucketsList.readInt();
      for (int i = 0; i < qtd; i++) {
          int chave = bucketsList.readInt();
          long ptrWaiter = bucketsList.readLong();
          ptrWaiter = ptrWaiter + 0;
          if (chave == index) {
              bucketsList.seek(bucketsList.getFilePointer() - Long.BYTES);
              bucketsList.writeLong(novoEnd);
              i = qtd;
          }
      }
  }

  /**
   * Função para remover contas do hash quando uma mesma for deletada ou quando
   * houver força bruta para deletar
   * 
   * @param index da conta alvo para deletar
   * @return referente ao endereço de remoção
   */
  public long remover(int index) {
      long pointer = -1;
      try {
          int posHash = (index % (int) Math.pow(2, p)) * 8 + CONST_MATH;
          pointersEnd.seek(posHash);
          long posBkt = pointersEnd.readLong();
          bucketsList.seek(posBkt);
          bucketsList.readInt();
          int qtd = bucketsList.readInt();
          int lidos = 0;
          for (int i = 0; i < qtd; i++) {
              int chave = bucketsList.readInt();
              long ptr = bucketsList.readLong();
              lidos++;
              if (chave == index) {
                  pointer = ptr;
                  for (int j = 0; j < buckSize - lidos; j++) {
                      int tmpChave = bucketsList.readInt();
                      long tmpPonteiro = bucketsList.readLong();
                      bucketsList.seek(bucketsList.getFilePointer() - 2 * (INT_SIZE));
                      bucketsList.writeInt(tmpChave);
                      bucketsList.writeLong(tmpPonteiro);
                      bucketsList.skipBytes(INT_SIZE);
                  }
                  i = qtd;
              }
          }
      } catch (Exception e) {
          System.err.println("Falha ao remover do HASH\nerro: " + e.getMessage());
          e.printStackTrace();
      }
      return pointer;
  }

  /**
   * Função para localizar indice dentro do hash e retornar seu endereço dentro do
   * arquivo de dados do sistema
   * 
   * @param index para localizar baseado em objeto de Conta
   * @return Endereço relativo ao arquivo de dados de contas
   */
  public long localizar(int index) {
      long pointer = -1;
      try {
          int posHash = (index % (int) Math.pow(2, p)) * 8 + CONST_MATH;
          pointersEnd.seek(posHash);
          long posBkt = pointersEnd.readLong();
          bucketsList.seek(posBkt);
          bucketsList.readInt();
          int qtd = bucketsList.readInt();
          for (int i = 0; i < qtd; i++) {
              int chave = bucketsList.readInt();
              long ptr = bucketsList.readLong();
              if (chave == index) {
                  pointer = ptr;
                  i = qtd;
              }
          }
      } catch (Exception e) {
          System.err.println("Falha ao recuperar dado no HASH\nerro: " + e.getMessage());
          e.printStackTrace();
      }
      return pointer;
  }

  @Override
  public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((pointersEnd == null) ? 0 : pointersEnd.hashCode());
      result = prime * result + ((bucketsList == null) ? 0 : bucketsList.hashCode());
      result = prime * result + buckSize;
      result = prime * result + p;
      result = prime * result + BuckByteSize;
      result = prime * result + INT_SIZE;
      result = prime * result + INT_SIZE_2;
      result = prime * result + CONST_MATH;
      return result;
  }

  @Override
  public boolean equals(Object obj) {
      if (this == obj)
          return true;
      if (obj == null)
          return false;
      if (getClass() != obj.getClass())
          return false;
      ExtendibleHash other = (ExtendibleHash) obj;
      if (pointersEnd == null) {
          if (other.pointersEnd != null)
              return false;
      } else if (!pointersEnd.equals(other.pointersEnd))
          return false;
      if (bucketsList == null) {
          if (other.bucketsList != null)
              return false;
      } else if (!bucketsList.equals(other.bucketsList))
          return false;
      if (buckSize != other.buckSize)
          return false;
      if (p != other.p)
          return false;
      if (BuckByteSize != other.BuckByteSize)
          return false;
      if (INT_SIZE != other.INT_SIZE)
          return false;
      if (INT_SIZE_2 != other.INT_SIZE_2)
          return false;
      if (CONST_MATH != other.CONST_MATH)
          return false;
      return true;
  }

  @Override
  public String toString() {
      return "HashConta [pointersEnd=" + pointersEnd + ", bucketsList=" + bucketsList + ", buckSize=" + buckSize
              + ", p=" + p + ", BuckByteSize=" + BuckByteSize + ", INT_SIZE=" + INT_SIZE + ", INT_SIZE_2=" + INT_SIZE_2
              + ", CONST_MATH=" + CONST_MATH + "]";
  }
}
