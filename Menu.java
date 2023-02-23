import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class Menu {
  public static Scanner scan = new Scanner(System.in);
  public static void main(String[] args) throws ParseException {
    int op = -1;

    System.out.println("-------------------------------------------------------------");
    System.out.println("|                      SPOTIFY SEARCH                       |");
    System.out.println("|                  Músicas de 1921 a 2020                   |");
    System.out.println("-------------------------------------------------------------");

    while (op != 0){
      System.out.println("\nEscolha uma opção:");
      System.out.println("1- Adicionar uma música");
      System.out.println("2- Procurar uma música");
      System.out.println("3- Alterar uma música");
      System.out.println("4- Excluir uma música");
      System.out.println("0- Sair");
      System.out.println("Digite a opção: ");
      op = scan.nextInt();

      if (op != 0)
          menu(op);
      else
       System.out.println("\nFim do programa");
    }
  }

  private static void menu(int op) throws ParseException {
    switch(op){
      case 1:
      // create
        menuCreate();

        break;
      case 2:
      // read
        break;
      case 3:
      // update
        break;
      case 4:
      // delete
        break;
    }
  }

  private static Musica menuCreate() throws ParseException {
    
    String id, name, artistsString, dataString;
    ArrayList<String> artists = new ArrayList<String>();
    int duration_ms, explicit;
    float loudness;
    Date release_date;

    // criar gerador de id
    // tem que buscar o ultimo id e criar um novo a partir dele
    id = "12345ABCDE"; // apenas para teste

    System.out.println("Nome da música: ");
    name = scan.nextLine();

    System.out.println("Artistas (separados por virgula): ");
    artistsString = scan.nextLine();
    String[] artistsSeparado = artistsString.split(",");
    for(int i = 0; i < artistsSeparado.length; i++){
      artists.add(scan.nextLine());
    }

    System.out.println("Duração: ");
    duration_ms = scan.nextInt();

    System.out.println("É explicita? (S ou N): ");
    explicit = scan.nextLine() == "S" ? 1 : 0;

    System.out.println("Sonoridade: ");
    loudness = scan.nextFloat();

    System.out.println("Data de lançamento (dd/mm/aaaa): ");
    dataString = scan.nextLine();
    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
    release_date = formato.parse(dataString);

    Musica musica = new Musica(id, name, artists, duration_ms, explicit, loudness, release_date);
    return musica;
  }
}
