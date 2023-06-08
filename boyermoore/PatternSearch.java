package boyermoore;

import musica.Musica;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.function.BiFunction;

public class PatternSearch {
    private final RandomAccessFile src;
    private static final int MAX_MEM_MUSICA = 100;
    private static final int FILE_HEADER_SIZE = 4;

    /**
     * Pattern Search class wrapper constructor.
     * 
     * @param srcFile the file containing the registers to be read.
     * @throws FileNotFoundException I/O errors caused by RandomAccessFile.
     */
    public PatternSearch(File srcFile) throws FileNotFoundException {
        src = new RandomAccessFile(srcFile, "r");
    }

    /**
     * Wrapper function to read file, convert registers to string
     * then call the desired pattern searching method.
     * 
     * @param pattern  The string pattern to be searched in the file.
     * @param searcher The desired pattern searching function to be used. -> Booyer Moore in this case
     * @throws Exception I/O errors caused by RandomAccessFile.
     * 
     * BiFuncion -> 1st argument = String, 2nd argument = String, return = PatternSearchInfo
     */
    public void search(String pattern, BiFunction<String, String, PatternSearchInfo> searcher) throws Exception {
        src.seek(FILE_HEADER_SIZE);
        Musica[] regs = getMusicas(); // put data in a string to read in primary memory
        PatternSearchInfo info = new PatternSearchInfo();
        while (regs[0] != null) {
            for (int i = 0; regs[i] != null;) {
              // applies find() function from BoyerMoore class
                info.sum(searcher.apply(pattern, regs[i++].toString())); 
            }
            regs = getMusicas();
        }
        System.out.println("O padrão '" + pattern + "' foi encontrado " + info.getFoundCount() + " vezes no arquivo.");
        System.out.println("Total de comparações (Boyer Moore): " + info.getNumComp());
    }

    /**
     * Reads up to MAX_MEM_CONTA valid Conta instances from the file.
     * 
     * @return Array of contas read from file.
     * @throws Exception I/O errors caused by RandomAccessFile.
     */
    private Musica[] getMusicas() throws Exception {
        Musica[] regs = new Musica[MAX_MEM_MUSICA];
        byte[] ba = null;
        for (int i = 0; i < MAX_MEM_MUSICA && src.getFilePointer() < src.length();) {
            if (src.readChar() != '*') {
                ba = new byte[src.readInt()];
                src.read(ba);
                regs[i] = new Musica();
                regs[i].fromByteArray(ba);
                i++;
            } else {
                src.skipBytes(src.readInt());
            }
        }
        return regs;
    }
}
