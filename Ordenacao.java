import java.io.File;
import java.io.RandomAccessFile;

public class Ordenacao {

  private String nomeDoArquivo = "musicas.db";
  private RandomAccessFile arquivo;

  public Ordenacao(){
    try {
      boolean verificaArquivo = (new File(nomeDoArquivo)).exists();
      if (!verificaArquivo) System.out.println("Arquivo não existe para ordenação");
    } catch(Exception e){System.out.println(e.getMessage());}
  }

  public boolean intercalacaoComum(int op){

  }
  
  public boolean intercalacaoVariavel(int op){
    
  }
  
  public boolean intercalacaoSelecao(int op){
    
  }
}
