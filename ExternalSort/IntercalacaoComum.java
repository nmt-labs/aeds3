package ExternalSort;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class IntercalacaoComum {
  private String nomeArquivo;
  private RandomAccessFile arquivo;
  private int qntArquivos, tamBloco, ultimoId;
  private RandomAccessFile[] saidaTemporaria;
  private ArrayList<Musica> registros;

  public IntercalacaoComum(String nomeArquivo, int qntArquivos, int tamBloco){
    this.nomeArquivo = nomeArquivo;
    this.qntArquivos = qntArquivos;
    this.tamBloco = tamBloco;
  }

  public void ordenar() throws IOException{
    // distribui os registros nos arquivos temporarios
    distribuir();
    // intercala os arquivos temporarios
    intercalar();

    System.out.println("Arquivo ordenado!");
  }

  private void distribuir() throws IOException{
    // ler o id do inicio do arquivo
    this.ultimoId = arquivo.readInt();

    iniciarSaidaTemps(); // apenas cria os arquivos temporarios
    registros = new ArrayList<>(registroMemoria); // array para ordenar os arquivos na memoria primaria

    while (!isAvaliable()) { // enquanto o arquivo nao termina
      registros.clear();
      lerRegistros();
      Collections.sort(registros);
      for (Conta item : registros) {
          saidaTemporaria[indexInsercao].writeChar(' ');
          saidaTemporaria[indexInsercao].writeInt(item.toByteArray().length);
          saidaTemporaria[indexInsercao].write(item.toByteArray());
      }
      indexInsercao = (indexInsercao + 1) % totalArquivos;
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
            saidaTemporaria[i] = new RandomAccessFile("saidaTemp" + i + ".db", "rw");
        }
    } catch (IOException e) {
        System.err.println("Falha ao iniciar arquivos temporários");
        e.printStackTrace();
    }
  }
}
