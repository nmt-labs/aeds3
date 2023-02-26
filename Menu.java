/*
 * & -> comando a ser criado
 */

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.*;

public class Menu {
  public static Scanner scan = new Scanner(System.in);
  public static void main(String[] args) throws Exception {
    int op = -1;

    System.out.println("-------------------------------------------------------------");
    System.out.println("|                      SPOTIFY DATASET                      |");
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

  private static void menu(int op) throws Exception {
    Musica musica = new Musica();
    Crud crud = new Crud();
    int id;
    
    switch(op){
      case 1:
      // create
        musica = menuCreate();
        crud.create(musica);
        break;
      case 2:
      // read
        System.out.println("Digite o ID da música:");
        id = scan.nextInt();
        musica = crud.read(id);
        if (musica != null) {
            System.out.println(musica.toString());
        } else {
            System.out.println("Nenhuma música localizada");
        }
        break;
      // case 3:
      // // update
      //   musica = menuUpdate();
      //   // & alterar musica na base de dados &
      //   break;
      // case 4:
      // // delete
      //   System.out.println("Digite o ID da música:");
      //   id = scan.nextInt();
      //   // & deletar &
      //   if (deletado) {
      //       System.out.println("Deletado com sucesso!");
      //   } else {
      //       System.err.println("Música não encontrada");
      //   }
      //   break;
    }
  }

  private static Musica menuCreate() throws ParseException {
    
    String name, key, artistsString, dataString;
    ArrayList<String> artists = new ArrayList<String>();
    int id, duration_ms, explicit;
    float tempo;
    Date release_date;

    id = ultimoId();

    key = geradorKey();

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
    tempo = scan.nextFloat();

    System.out.println("Data de lançamento (dd/mm/aaaa): ");
    dataString = scan.nextLine();
    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
    release_date = formato.parse(dataString);

    Musica musica = new Musica(id, key, name, artists, duration_ms, explicit, tempo, release_date);
    return musica;
  }

  // encontra o ultimo id e cria um novo
  private static int ultimoId() {
    RandomAccessFile arquivo;
    int ultimoId;
    try {
      arquivo = new RandomAccessFile("musicas.db", "rw");
      ultimoId = arquivo.readInt();
      arquivo.close();
      ultimoId++;

      return ultimoId;
    } catch (IOException e) {
      System.out.println("Criando novo arquivo");

      return 0;
    }
  }

  /*
   * Código retirado de: https://acervolima.com/gerar-string-aleatoria-de-determinado-tamanho-em-java/
   */
  private static String geradorKey(){
    int n = 23; 
    // length is bounded by 256 Character
    byte[] array = new byte[256];
    new Random().nextBytes(array);

    String randomString = new String(array, Charset.forName("UTF-8"));

    // Create a StringBuffer to store the result
    StringBuffer r = new StringBuffer();

    // remove all spacial char
    String  AlphaNumericString = randomString.replaceAll("[^A-Za-z0-9]", "");

    // Append first 20 alphanumeric characters
    // from the generated random String into the result
    for (int k = 0; k < AlphaNumericString.length(); k++) {

        if (Character.isLetter(AlphaNumericString.charAt(k)) && (n > 0) || Character.isDigit(AlphaNumericString.charAt(k)) && (n > 0)) {
            r.append(AlphaNumericString.charAt(k));
            n--;
        }
    }

    // return the resultant string
    return r.toString();
  }

  // private static Musica menuUpdate() throws ParseException {
    
  //   String name, key, artistsString, dataString;
  //   ArrayList<String> artists = new ArrayList<String>();
  //   int id, duration_ms, explicit, op;
  //   float tempo;
  //   Date release_date;

  //   Musica musica;

  //   System.out.println("Insira o id da música para alteração: ");
  //   id = scan.nextInt();
  //   // & musica = busca &;
  //   // & if achar executar menu abaixo &
  //   musica = new Musica(); //apenas para teste

  //   System.out.println("Selecione o que será alterado: ");
  //   System.out.println("1- Nome");
  //   System.out.println("2- Artistas");
  //   System.out.println("3- Duração");
  //   System.out.println("4- Explicita");
  //   System.out.println("5- Tempo");
  //   System.out.println("6- Data de lançamento");
  //   System.out.println("Insira sua opção: ");
  //   op = scan.nextInt();

  //   switch(op){
  //     case 1:
  //       System.out.println("Insira novo nome: ");
  //       name = scan.nextLine();

  //       musica.setName(name);
  //       break;
  //     case 2:
  //       System.out.println("Insira novo(s) artistas (separados por vírgula): ");
  //       artistsString = scan.nextLine();
  //       String[] artistsSeparado = artistsString.split(",");
  //       for(int i = 0; i < artistsSeparado.length; i++){
  //         artists.add(scan.nextLine());
  //       }

  //       musica.setArtists(artists);
  //       break;
  //     case 3:
  //       System.out.println("Insira nova duração: ");
  //       duration_ms = scan.nextInt();

  //       musica.setDuration_ms(duration_ms);
  //       break;
  //     case 4:
  //       System.out.println("Insira se é explicita (S ou N): ");
  //       explicit = scan.nextLine() == "S" ? 1 : 0;

  //       musica.setExplicit(explicit);
  //       break;
  //     case 5:
  //       System.out.println("Insira novo tempo: ");
  //       tempo = scan.nextFloat();

  //       musica.setTempo(tempo);
  //       break;
  //     case 6:
  //       System.out.println("Insira nova data de lançamento (dd/mm/aaaa): ");
  //       dataString = scan.nextLine();
  //       SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
  //       release_date = formato.parse(dataString);

  //       musica.setRelease_date(release_date);
  //       break;
  //   }

  //   // & else -> musica nao encontrada &
  //   return musica;
  // }
}
