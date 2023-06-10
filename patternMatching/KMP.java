import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class KMP {

    private static final String file = "musicas.db";

    public static void main(String[] args) {
        String pattern = "lo";

        KMP kmp = new KMP();
        try {
            kmp.kmp(pattern);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kmp(String pattern) throws IOException {
        // Se o padrão está vazio
        if (pattern == null || pattern.length() == 0) {
            System.out.println("Padrão encontrado na posição: 0");
            return;
        }

        // Lê o conteúdo do file
        String text = read(file);

        // Se o padrão está vazio ou possui tamanho maior que o texto em que se procura o padrão
        if (text == null || pattern.length() > text.length()) {
            System.out.println("Padrão não encontrado");
            return;
        }

        char[] carac = pattern.toCharArray();

        // prox[i] armazena o índice da próxima melhor correspondência parcial
        int[] prox = new int[pattern.length() + 1];
        for (int i = 1; i < pattern.length(); i++) {
            int j = prox[i + 1];

            while (j > 0 && carac[j] != carac[i]) {
                j = prox[j];
            }

            if (j > 0 || carac[j] == carac[i]) {
                prox[i + 1] = j + 1;
            }
        }

        for (int i = 0, j = 0; i < text.length(); i++) {
            if (j < pattern.length() && text.charAt(i) == pattern.charAt(j)) {
                if (++j == pattern.length()) {
                    System.out.println("Padrão encontrado na posição: " + (i - j + 1));
                }
            } else if (j > 0) {
                j = prox[j];
                i--; // já que i vai ser decrementado na próxima iteração
            }
        }
    }

    public static String read(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
