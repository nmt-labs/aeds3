/*
 * ainda em desenvolvimento
 */
package ExternalSort;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import Musica.Musica;

public class IntercalacaoComum {
  private String nomeArquivo = "db/musicas.db";
  private RandomAccessFile arquivo;
  private int qntArquivos, tamBloco, ultimoId, numPrimRead;
  private RandomAccessFile[] saidaTemporaria, entradaTemporaria;
  private List<Musica> registros;

  public IntercalacaoComum(int qntArquivos, int tamBloco) throws FileNotFoundException{
    arquivo = new RandomAccessFile(nomeArquivo, "rw");
    this.qntArquivos = qntArquivos;
    this.tamBloco = tamBloco;
    this.entradaTemporaria = new RandomAccessFile[qntArquivos];
    this.saidaTemporaria = new RandomAccessFile[qntArquivos];
  }

  public void ordenar() throws Exception{
    // distribui os registros nos arquivos temporarios
    System.out.println("Distribuindo arquivo em arquivos temporarios...");
    distribuir();
    // intercala os arquivos temporarios
    System.out.println("Intercalando arquivos temporarios...");
    intercalar();

    System.out.println("Arquivo ordenado!");
  }

  private void distribuir() throws Exception{
    // ler o id do inicio do arquivo
    this.ultimoId = arquivo.readInt();
    numPrimRead = 0;

    iniciarSaidaTemps(); // apenas cria os arquivos temporarios
    int index = 0;
    registros = new ArrayList<>(tamBloco); // array para ordenar os arquivos na memoria primaria

    while (!isAvaliable()) { // enquanto o arquivo nao termina
      registros.clear();
      lerRegistros();
      Collections.sort(registros, Comparator.comparing(Musica::getName)); // ordena bloco em memoria primaria
      for (Musica musica : registros) { // escreve no arquivo temporario
          saidaTemporaria[index].writeChar(' ');
          saidaTemporaria[index].writeInt(musica.toByteArray().length);
          saidaTemporaria[index].write(musica.toByteArray());
      }
      index = (index + 1) % qntArquivos; // seleciona o proximo arquivo a armazenar o bloco
    }
    finalizarSaidaTmp();
    arquivo.close();

  }

  private void intercalar(){

  }
  
  // -------------------------------------- utilitarios ----------------------------------------
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
        saidaTemporaria[i] = new RandomAccessFile("db/arqTemp/saidaTemp" + (i + numPrimRead) + ".db", "rw");
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
                registros.add(readMusica(arquivo));
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
    if (lapide != '*') {
        reg = new Musica();
        reg.fromByteArray(bytearray);
    }
    return reg;
  }
}
