package compression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class LZW {
    private static String p;
    private static String c;
    private static String fileName = "db/musicas.db";
    private static int version = 1;

    /**
     * Compresses the music file using the LZW algorithm
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void compress() throws FileNotFoundException, IOException {
        System.out.println("Sequência código do arquivo de músicas comprimido: ");

        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo para compressão não foi encontrado!");
        }
        String compressedFileName = "compression/musicaLZWCompressao" + version++ + ".lzw";

        try (RandomAccessFile music = new RandomAccessFile(new File(fileName), "r")) {
            // inicializa dicionario
            Map<String, Integer> dictionary = initializeDictionary();
            p = ""; // o caractere atual começa vazio
            int input = music.read(); // le primeiro byte de musica

            try (RandomAccessFile compressedFile = new RandomAccessFile(new File(compressedFileName), "rw")) {
                while (input != -1) { // enquanto tiver mais caracteres na sequencia de entrada
                    c = p + (char) input; // c recebe o caractere atual mais o proximo da sequencia de entrada
                    if (dictionary.containsKey(c)) {
                        p = c;
                    } else {
                        compressedFile.writeInt(dictionary.get(p)); // escreve p na sequencia codificada
                        System.out.print(dictionary.get(p) + " ");
                        dictionary.put(c, dictionary.size()); // adiciona c no dicionario
                        p = String.valueOf((char) input); // p recebe o caractere do arquivo de musica
                    }
                    input = music.read();
                }
                if (!p.equals("")) {
                    compressedFile.writeInt(dictionary.get(p));// quando acaba os caracteres de entrada coloca p no arq
                                                               // codificado
                }

            } catch (IOException e) {
                System.err.println("Ocorreu um erro de E/S: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de E/S: " + e.getMessage());
        }
    }

    /**
     * Uncompresses the compressed file
     *
     * @param desiredVersion The desired version of the compressed file to
     *                       uncompress.
     */
    public static void uncompress(int desiredVersion) {
        String cw, pw;
        char c;
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo para compressão não foi encontrado!");
        }
        String compressedFileName = "compression/musicaLZWCompressao" + desiredVersion + ".lzw";

        try (RandomAccessFile compFile = new RandomAccessFile(new File(compressedFileName), "r")) {
            // inicializa dicionario
            Map<String, Integer> dictionary = initializeDictionary();
            int input = compFile.readInt(); // le primeiro byte do arq compactado
            cw = String.valueOf((char) input); // primeiro caractere do arquivo

            try (RandomAccessFile music = new RandomAccessFile(new File(fileName), "rw")) {
                music.writeUTF(cw); // escrevendo o primeiro caractere no arquivo de dados
                while (compFile.getFilePointer() < compFile.length()) {
                    pw = cw;
                    input = compFile.readInt();
                    cw = String.valueOf((char) input); // prox caractere do arq compactado

                    if (dictionary.containsKey(cw)) {
                        // se esse caractere tiver no dicionario vai escreve-lo no arquivo de dados
                        music.writeUTF(cw);
                        p = pw;
                        c = cw.charAt(0);
                        dictionary.put(p + c, dictionary.size()); // adiciona no dicionario a junção do caractere atual
                                                                  // com o primeiro caractere do codigo seguinte
                    } else {
                        p = pw;
                        c = pw.charAt(0);
                        music.writeUTF(p + c); // se n, escreve no arquivo o caractere atual mais o primeiro caractere
                        dictionary.put(p + c, dictionary.size());
                    }
                }

            } catch (IOException e) {
                System.err.println("Ocorreu um erro de E/S: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ocorreu um erro de E/S: " + e.getMessage());
        }
    }

    /**
     * Initializes the dictionary with the 256 possible values.
     *
     * @return The initialized dictionary.
     */
    private static Map<String, Integer> initializeDictionary() {
        // Inicializando o dicionário com os 256 valores possíveis
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }
        return dictionary;
    }
}
