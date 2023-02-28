package ExternalSort;

import java.io.RandomAccessFile;

public class IntercalacaoComum {
  private String nomeArquivo;
  private RandomAccessFile arquivo;
  private int qntArquivos, tamBloco;

  public IntercalacaoComum(String nomeArquivo, int qntArquivos, int tamBloco){
    this.nomeArquivo = nomeArquivo;
    this.qntArquivos = qntArquivos;
    this.tamBloco = tamBloco;
  }

  public void ordenar(){
    // distribui os registros nos arquivos temporarios
    distribuir();
    // intercala os arquivos temporarios
    intercalar();

    System.out.println("Arquivo ordenado!");
  }
}
