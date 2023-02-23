/*
 * & -> comando a ser criado
 */

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
    Musica musica = new Musica();
    String nome;
    
    switch(op){
      case 1:
      // create
        musica = menuCreate();
        // & inserir musica na base de dados &
        break;
      case 2:
      // read
        System.out.println("Digite o nome da música:");
        nome = scan.nextLine();
        // & buscar &
        if (musica != null) {
            System.out.println("Música:\n" + musica.toString());
        } else {
            System.out.println("Nenhuma música localizada");
        }
        break;
      case 3:
      // update
        musica = menuUpdate();
        // & alterar musica na base de dados &
        break;
      case 4:
      // delete
        System.out.println("Digite o nome da música:");
        nome = scan.nextLine();
        // & deletar &
        if (deletado) {
            System.out.println("Deletado com sucesso!");
        } else {
            System.err.println("Música não encontrada");
        }
        break;
    }
  }

  private static Musica menuCreate() throws ParseException {
    
    String id, name, artistsString, dataString;
    ArrayList<String> artists = new ArrayList<String>();
    int duration_ms, explicit;
    float loudness;
    Date release_date;

    // & criar gerador de id &
    // buscar o ultimo id e criar um novo a partir dele
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

  private static Musica menuUpdate() throws ParseException {
    
    String id, name, artistsString, dataString;
    ArrayList<String> artists = new ArrayList<String>();
    int duration_ms, explicit, op;
    float loudness;
    Date release_date;

    Musica musica;

    System.out.println("Insira o nome da música para alteração: ");
    name = scan.nextLine();
    // & musica = busca &;
    // & if achar executar menu abaixo &
    musica = new Musica(); //apenas para teste

    System.out.println("Selecione o que será alterado: ");
    System.out.println("1- Nome");
    System.out.println("2- Artistas");
    System.out.println("3- Duração");
    System.out.println("4- Explicita");
    System.out.println("5- Sonoridade");
    System.out.println("6- Data de lançamento");
    System.out.println("Insira sua opção: ");
    op = scan.nextInt();

    switch(op){
      case 1:
        System.out.println("Insira novo nome: ");
        name = scan.nextLine();

        musica.setName(name);
        break;
      case 2:
        System.out.println("Insira novo(s) artistas (separados por vírgula): ");
        artistsString = scan.nextLine();
        String[] artistsSeparado = artistsString.split(",");
        for(int i = 0; i < artistsSeparado.length; i++){
          artists.add(scan.nextLine());
        }

        musica.setArtists(artists);
        break;
      case 3:
        System.out.println("Insira nova duração: ");
        duration_ms = scan.nextInt();

        musica.setDuration_ms(duration_ms);
        break;
      case 4:
        System.out.println("Insira se é explicita (S ou N): ");
        explicit = scan.nextLine() == "S" ? 1 : 0;

        musica.setExplicit(explicit);
        break;
      case 5:
        System.out.println("Insira nova sonoridade: ");
        loudness = scan.nextFloat();

        musica.setLoudness(loudness);
        break;
      case 6:
        System.out.println("Insira nova data de lançamento (dd/mm/aaaa): ");
        dataString = scan.nextLine();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        release_date = formato.parse(dataString);

        musica.setRelease_date(release_date);
        break;
    }

    // & else -> musica nao encontrada &
    return musica;
  }
}
