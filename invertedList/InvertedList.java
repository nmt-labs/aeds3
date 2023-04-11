package invertedList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;

import musica.Musica;

public class InvertedList {
    private String dbMusicas = "db/musicas.db";
    private String fileNames = "invertedList" + File.separator + "db" + File.separator + "invertedListNames.db",
            fileArtists = "invertedList" + File.separator + "db" + File.separator + "invertedListArtirts.db";
    private RandomAccessFile file;

    public InvertedList() {
        try {
            boolean checkFile = (new File(fileNames)).exists();
            if (!checkFile) {
                try {
                    file = new RandomAccessFile(fileNames, "rw");
                    file.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void createInvertedList() throws IOException, ParseException {
        // RandomAccessFile fNames = new RandomAccessFile(fileNames, "rw");
        // fNames.setLength(0); //sempre zerar arquivo
        // fNames.writeLong(-1); // primeiro nome
        RandomAccessFile musicas = new RandomAccessFile(dbMusicas, "r");
        byte[] ba;
        int size;
        long pos;
        char lapide;
        musicas.readInt();
        

        while (musicas.getFilePointer() < musicas.length()) {
            lapide = musicas.readChar();
            size = musicas.readInt();
            pos = musicas.getFilePointer();
            ba = new byte[size];
            musicas.read(ba);
            Musica musica = new Musica();
            musica.fromByteArray(ba);
            if (lapide != '*') {
                treat(musica, pos);
            }

        }
    }

    private void treat(Musica musica, long pos) throws IOException {
        RandomAccessFile indexNames = new RandomAccessFile(fileNames, "rw");
        String[] names = musica.getName().split(" ");
        int id = musica.getId();
        
        // long name;
        // name = indexNames.readLong();

        for(int i = 0; i < names.length; i++) {
           indexNames.seek(pos);
           indexNames.writeUTF(names[i]);
           indexNames.writeByte(id);
           indexNames.writeByte(-1);
           indexNames.writeLong(-1);
        }
        
        // showList();
    }

    public void showList() throws IOException{
        RandomAccessFile indexNames = new RandomAccessFile(fileNames, "rw");
        while(indexNames.getFilePointer() < indexNames.length())
        System.out.println(indexNames.readUTF());
        // System.out.println(indexNames.readByte());
        // System.out.println(indexNames.readByte());
        // System.out.println(indexNames.readLong());

    }
}
