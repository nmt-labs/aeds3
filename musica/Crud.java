package musica;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;

import bplustree.BPlusTree;
import extendiblehash.ExtendibleHash;

public class Crud {
  private String fileName = "db" + File.separator+ "musicas.db";
  private RandomAccessFile file;
  private BPlusTree bplus;
  private ExtendibleHash hash;

  public Crud() throws IOException, Exception {
    bplus = new BPlusTree(8);
    hash = new ExtendibleHash(8);

    try {
      boolean checkFile = (new File(fileName)).exists();
      if (!checkFile){
        try { // força o primeiro registro a ter id 0
          int id = -1;
          file = new RandomAccessFile(fileName, "rw");
          file.writeInt(id);
          file.close();
        } catch (Exception e){System.out.println(e.getMessage() + "erro em criar o id");}
      }
    }catch(Exception e){System.out.println(e.getMessage());}
  }

  public void indice(int id, long pos) throws Exception{
    // inserir arquivo de indice
    bplus.insert();
    hash.add(id, pos);
  }

  // Método de inserção no arquivo
  public void create(Musica musica) throws Exception {
    file = new RandomAccessFile(fileName, "rw");

    byte[] ba = musica.toByteArray(); // converte musica para byte
    file.seek(0); // move ponteiro para o inicio do arquivo
    file.writeInt(musica.getId()); // escreve id da ultima musica no inicio do arquivo
    file.seek(file.length());// mover para o fim do arquivo
    long pos = file.getFilePointer();
    
    // escrever registro
    file.writeChar(' '); // escreve a lápide
    file.writeInt(ba.length); // escreve tamanho do registro
    file.write(ba);
    
    file.close();
    
    // inserir arquivo de indice
    indice(musica.getId(), pos);

    System.out.println("Música adicionada com sucesso! Seu id é " + musica.getId());
  }

  // Método de leitura do arquivo
  public Musica read(int id) throws Exception {
    file = new RandomAccessFile(fileName, "rw");
    byte[] ba;
    int size;
    char lapide;
    Musica musica = new Musica();
    
    file.seek(4); // move ponteiro para o primeiro registro
    while (file.getFilePointer() < file.length()) {
      lapide = file.readChar();
      size = file.readInt();
      ba = new byte[size];
      file.read(ba);
      if (lapide != '*') {
        musica.fromByteArray(ba);
        if (musica.getId() == id)
          return musica;
      }
    }

    file.close(); 
    return null;
  }

  public Musica readBPlus(int id) throws IOException, ParseException {
    file = new RandomAccessFile(fileName, "rw");
    byte[] ba;
    int size;
    Musica musica = new Musica();

    long pos = bplus.search(id);
    if (pos > 0) file.seek(pos);
    else {
      file.close();
      return null;
    }
    file.readChar(); // lapide
    size = file.readInt();
    ba = new byte[size];
    file.read(ba);
    musica.fromByteArray(ba);
    file.close(); 
    return musica;
  }

  public Musica readHash(int id) throws IOException, ParseException {
    file = new RandomAccessFile(fileName, "rw");
    byte[] ba;
    int size;
    Musica musica = new Musica();

    long pos = hash.search(id);
    if (pos > 0) file.seek(pos);
    else {
      file.close();
      return null;
    }
    file.readChar(); // lapide
    size = file.readInt();
    ba = new byte[size];
    file.read(ba);
    musica.fromByteArray(ba);
    file.close(); 
    return musica;
  }

  // Método de alteracao no arquivo
  public boolean update(Musica musica) throws Exception {
    file = new RandomAccessFile(fileName, "rw");
    byte[] ba;
    byte[] newBa;
    int size;
    char lapide;
    long position;
    Musica musicFile = new Musica();

    file.seek(4); // move ponteiro para o primeiro registro
    while (file.getFilePointer() < file.length()) {
      position = file.getFilePointer(); // posicao atual do ponteiro no arquivo
      lapide = file.readChar();
      size = file.readInt();
      ba = new byte[size];
      file.read(ba);
      if (lapide != '*') {
        musicFile.fromByteArray(ba); // le a musica do arquivo
        if (musicFile.getId() == musica.getId()) {
          newBa = musica.toByteArray();
          if (newBa.length <= size) { // se for menor que o registro anterior, sobrescreve
              file.seek(position + 6);
              long pos = file.getFilePointer();
              file.write(newBa);

              file.close();

              // inserir arquivo de indice
              indice(musica.getId(), pos);
              return true;
          } else { // senao, escreve no fim do arquivo e deleta o anterior
              file.seek(file.length());
              long pos = file.getFilePointer();
              file.writeChar(' ');
              file.writeInt(newBa.length);
              file.write(newBa);
              delete(musicFile.getId());

              file.close();

              // inserir arquivo de indice
              indice(musica.getId(), pos);
              return true;
          }
        }
      }
    }
    file.close();
    return false;
  }

  // Método de exclusão do arquivo
  public Musica delete(int id) throws Exception {
    file = new RandomAccessFile(fileName, "rw");
    byte[] ba;
    int size;
    char lapide;
    long position;
    Musica musica = new Musica();

    file.seek(4); // move ponteiro para o primeiro registro
    while (file.getFilePointer() < file.length()) {
      position = file.getFilePointer(); // posicao atual do ponteiro no arquivo
      lapide = file.readChar();
      size = file.readInt();
      ba = new byte[size];
      file.read(ba);
      if (lapide != '*') {
        musica.fromByteArray(ba);
        if (musica.getId() == id){
          file.seek(position); // volta para a posicao inicial do registro
          file.writeChar('*'); // marca a lapide
          file.close();
          return musica;
        }
      }
    }

    file.close();
    return null;
  }

}
