import java.util.Scanner;

public class Menu {
  public static Scanner scan = new Scanner(System.in);
  public static void main(String[] args) {
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

  private static void menu(int op) {
    switch(op){
      case 1:
      // create
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
}
