package kmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class KMP {

    private String file = "db" + File.separator + "musicas.db";
    int comp = 0;
    int find = 0;

    /**
     * This is a kmp pattern search method
     * @param pattern
     * @throws IOException
    */
    public void kmp(String pattern) throws IOException {
        // Se o padrão está vazio
        if (pattern == null || pattern.length() == 0) {
            System.out.println("Digite um padrão válido!");
            return;
        }

        // Lê o conteúdo do arquivo
        String text = readFile(file);

        // Se o padrão está vazio ou possui tamanho maior que o texto em que se procura
        // o padrão
        if (text == null || pattern.length() > text.length()) {
            System.out.println("Padrão não encontrado");
            return;
        }

        char[] carac = pattern.toCharArray();

        // prefix[i] armazena o índice da próxima melhor correspondência parcial
        int[] prefix = new int[pattern.length() + 1];
        for (int i = 1; i < pattern.length(); i++) {
            int j = prefix[i + 1];

            while (j > 0 && carac[j] != carac[i]) {
                j = prefix[j];
                comp++;
            }

            if (j > 0 || carac[j] == carac[i]) {
                prefix[i + 1] = j + 1;
                comp++;
            }
            comp++;
        }

        for (int i = 0, j = 0; i < text.length(); i++) {
            if (j < pattern.length() && text.charAt(i) == pattern.charAt(j)) {
                if (++j == pattern.length()) {
                    // System.out.println("Padrão encontrado na posição: " + (i - j + 1));
                    find++;
                    comp++;
                }
                comp++;
            } else if (j > 0) {
                j = prefix[j];
                i--; // já que i vai ser decrementado na próxima iteração
                comp++;
            }
            comp++;
        }
        System.out.println("O padrão '" + pattern + "' foi encontrado " + find + " vezes no arquivo.");
        System.out.println("Total de comparações (KMP): " + comp);

    }

    /**
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String readFile(String fileName) throws IOException {
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
