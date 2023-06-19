package cryptography.cesar;

public class Cesar {

  public static int DEFAULT_KEY;

  public Cesar() {
    DEFAULT_KEY = 3;
  }

  public Cesar(int key) {
    DEFAULT_KEY = key;
  }

  public String crypt (String base) {
    String end = "";
    char ascii;
    char x, y;
    int key = DEFAULT_KEY;

    while (key >= 26) { //key tem que ter o tamanho do alfabeto
      key = key - 26;
    }

    for (int i = 0; i < base.length(); i++) {
      //Tratamento Letras minusculas  
      if (base.charAt(i) >= 97 && base.charAt(i) <= 122) {//letrans minusculas de acordo com a tabela ASCII
        if ((int) (base.charAt(i) + key) > 122) {
          x = (char) (base.charAt(i) + key);
          y = (char) (x - 122);
          ascii = (char) (96 + y);
          System.out.print(ascii + " ");
        } else {
          ascii = (char) (base.charAt(i) + key);
          end += ascii;
        }
      }
      //Tratamento Letras mausculas
      if (base.charAt(i) >= 65 && base.charAt(i) <= 90) {
        if (base.charAt(i) + key > 90) {
          x = (char) (base.charAt(i) + key);
          y = (char) (x - 90);
          ascii = (char) (64 + y);
          System.out.print(ascii + " ");
        } else {
          ascii = (char) (base.charAt(i) + key);
          end += ascii;
        }
      }
    }
    return end;
  }

  public String decrypt (String base) {
    String end = "";
    char ascii;
    char x, y;
    int key = DEFAULT_KEY;

    while (key >= 26) { //key tem que ter o tamanho do alfabeto
      key = key - 26;
    }

    for (int i = 0; i < base.length(); i++) {
      //Tratamento Letras minusculas  
      if (base.charAt(i) >= 97 && base.charAt(i) <= 122) {//letrans minusculas de acordo com a tabela ASCII
        if ((int) (base.charAt(i) - key) > 122) {
          x = (char) (base.charAt(i) - key);
          y = (char) (x - 122);
          ascii = (char) (96 + y);
          System.out.print(ascii + " ");
        } else {
          ascii = (char) (base.charAt(i) - key);
          end += ascii;
        }
      }
      //Tratamento Letras mausculas
      if (base.charAt(i) >= 65 && base.charAt(i) <= 90) {
        if (base.charAt(i) - key > 90) {
          x = (char) (base.charAt(i) - key);
          y = (char) (x - 90);
          ascii = (char) (64 + y);
          System.out.print(ascii + " ");
        } else {
          ascii = (char) (base.charAt(i) - key);
          end += ascii;
        }
      }
    }
    return end;
  }
}
