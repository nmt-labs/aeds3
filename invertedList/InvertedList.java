package invertedList;

import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.ArrayList;

import java.io.*;

public class InvertedList {
    private String fileNames = "invertedList" + File.separator + "db" + File.separator + "invertedListNames.db",
            fileArtists = "invertedList" + File.separator + "db" + File.separator + "invertedListArtists.db";
    private RandomAccessFile file;

    public InvertedList() {
        try {
            boolean checkFileNames = (new File(fileNames)).exists();
            boolean checkFileArtists = (new File(fileArtists)).exists();
            if (!checkFileNames && !checkFileArtists) {
                try {
                    file = new RandomAccessFile(fileNames, "rw");
                    file = new RandomAccessFile(fileArtists, "rw");
                    file.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method creates the inverted list
     * 
     * @param word
     * @param id
     * @param file
     * @throws IOException
     * @throws ParseException
     */
    public void createInvertedList(String word, int id, String file) throws IOException, ParseException {
        // tira palavras inuteis
        word = treatWords(word);
        // contar caracteres das palavras
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == ' ') {
                count++;
            }
        }

        // separar palavras
        String words[] = new String[count];
        words = word.split(" ");
        try (RandomAccessFile openFile = new RandomAccessFile(file, "rw")) {
            for (int i = 0; i < words.length; i++) {
                // verifica se palavra já existe na lista
                if (contains(words[i], file) == true) {
                    long pos = positionToInsert(words[i], file);
                    // Se a posição encontrada não for o final do arquivo, move o ponteiro para essa
                    // posição e escreve o id
                    if (pos != openFile.length()) {
                        openFile.seek(pos);
                        openFile.writeByte(id);
                        // Caso contrário, move o ponteiro para a posição atual e escreve a palavra, o
                        // id e valores para indicar final do registro
                    } else {
                        openFile.seek(pos);
                        openFile.writeUTF(words[i]);
                        openFile.writeByte(id);
                        openFile.writeByte(-1);
                        openFile.writeByte(-1);
                        openFile.writeByte(-1);
                        openFile.writeByte(-1);
                        openFile.writeLong(-1);
                    }
                    // Se a palavra não existe no arquivo, move o ponteiro para o final do arquivo e
                    // escreve a palavra, o id e valores para indicar final do registro
                } else {
                    openFile.seek(openFile.length());
                    openFile.writeUTF(words[i]);
                    openFile.writeByte(id);
                    openFile.writeByte(-1);
                    openFile.writeByte(-1);
                    openFile.writeByte(-1);
                    openFile.writeByte(-1);
                    openFile.writeLong(-1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reads a word and checks if it is present in the inverted list. If
     * the word is present, it returns the ids where the word appears
     * 
     * @param word -> searched word
     * @param file -> file to read
     * @return
     */
    public ArrayList<Byte> readInvertedList(String word, String file) {
        ArrayList<Byte> ids = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == ' ') {
                count++;
            }
        }
        String words[] = new String[count];
        words = word.split(" ");

        try (RandomAccessFile openFile = new RandomAccessFile(file, "rw")) {
            String wordsList;
            byte id;
            long pos;
            for (int i = 0; i < words.length; i++) {
                openFile.seek(0); // move ponteiro para o inicio do arquivo
                while (openFile.getFilePointer() < openFile.length()) {
                    wordsList = openFile.readUTF(); // le nome/artista da lista invertida e guarda na variavel
                    if (words[i].compareTo(wordsList) == 0) { // se encontrar palavra procurada na lista
                        pos = openFile.getFilePointer();
                        if (openFile.readByte() != -1) {
                            openFile.seek(pos); // move o ponteiro de volta para a posição salva
                            id = openFile.readByte();
                            if (idsArray(ids, id) == false) { // se o id não estiver na lista ids
                                ids.add(id); // adiciona o id à lista de ids
                            }
                        }
                        pos = openFile.getFilePointer();
                        if (openFile.readByte() != -1) {
                            openFile.seek(pos);
                            id = openFile.readByte();
                            if (idsArray(ids, id) == false) {
                                ids.add(id);
                            }
                        }
                        pos = openFile.getFilePointer();
                        if (openFile.readByte() != -1) {
                            openFile.seek(pos);
                            id = openFile.readByte();
                            if (idsArray(ids, id) == false) {
                                ids.add(id);
                            }
                        }
                        pos = openFile.getFilePointer();
                        if (openFile.readByte() != -1) {
                            openFile.seek(pos);
                            id = openFile.readByte();
                            if (idsArray(ids, id) == false) {
                                ids.add(id);
                            }
                        }
                        pos = openFile.getFilePointer();
                        if (openFile.readByte() != -1) {
                            openFile.seek(pos);
                            id = openFile.readByte();
                            if (idsArray(ids, id) == false) {
                                ids.add(id);
                            }
                        }
                        // se tiver mais registros move o ponteiro pra essa posicao
                        pos = openFile.getFilePointer();
                        if (openFile.readLong() != -1) {
                            openFile.seek(pos);
                        }
                        // se a palavra procurada não for encontrada na lista invertida
                    } else {
                        openFile.readByte();
                        openFile.readByte();
                        openFile.readByte();
                        openFile.readByte();
                        openFile.readByte();
                        openFile.readLong();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * This method removes all words from the list corresponding to the ID of the
     * deleted song
     * 
     * @param id
     * @param file
     */
    public void deleteInvertedList(byte id, String file) {
        try (RandomAccessFile openFile = new RandomAccessFile(file, "rw")) {

            long pos;

            while (openFile.getFilePointer() < openFile.length()) {
                openFile.readUTF(); // ler nome/artista

                // verifica se o id procurado está na lista, se tiver, grava o valor -1 no
                // arquivo na posição atual do ponteiro, o que remove o ID do arquivo
                pos = openFile.getFilePointer();
                if (openFile.readByte() == id) {
                    openFile.seek(pos);
                    openFile.writeByte(-1);
                }

                pos = openFile.getFilePointer();
                if (openFile.readByte() == id) {
                    openFile.seek(pos);
                    openFile.writeByte(-1);
                }

                pos = openFile.getFilePointer();
                if (openFile.readByte() == id) {
                    openFile.seek(pos);
                    openFile.writeByte(-1);
                }

                pos = openFile.getFilePointer();
                if (openFile.readByte() == id) {
                    openFile.seek(pos);
                    openFile.writeByte(-1);
                }

                pos = openFile.getFilePointer();
                if (openFile.readByte() == id) {
                    openFile.seek(pos);
                    openFile.writeByte(-1);
                }

                openFile.readLong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method updates the list if the name or artist is changed in the database
     * 
     * @param word
     * @param id
     * @param file
     */
    public void updateInvertedList(String word, byte id, String file) {
        try {
            // deleta registo do arquivo e cria um novo alterado
            deleteInvertedList(id, file);
            createInvertedList(word, id, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verify if the word to be inserted already exists in the file
     * 
     * @param word
     * @param file
     * @return
     */
    public boolean contains(String word, String file) {
        try (RandomAccessFile openFile = new RandomAccessFile(file, "rw")) {
            String fileWord;
            openFile.seek(0);
            while (openFile.getFilePointer() < openFile.length()) {
                fileWord = openFile.readUTF();
                openFile.readByte();
                openFile.readByte();
                openFile.readByte();
                openFile.readByte();
                openFile.readByte();
                openFile.readLong();
                if (word.compareTo(fileWord) == 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Remove useless words
     * 
     * @param word
     * @return
     */
    public String treatWords(String word) {
        String[] allWords = word.split(" ");
        int count = 0;
        for (int i = 0; i < allWords.length; i++) {
            if (allWords[i].length() > 2)
                count++;
        }
        String[] usefullWords = new String[count];
        int count2 = 0;
        for (int j = 0; j < allWords.length; j++) {
            if (allWords[j].length() > 2) {
                usefullWords[count2] = allWords[j];
                count2++;
            }
        }
        return String.join(" ", usefullWords);
    }

    /**
     * Find the position in the file to insert new words, checking for duplicates
     * 
     * @param word
     * @param file
     * @return
     */
    public long positionToInsert(String word, String file) {
        try (RandomAccessFile openFile = new RandomAccessFile(file, "rw")) {
            String fileWord;
            long posToInsert = openFile.getFilePointer();
            openFile.seek(0);
            while (openFile.getFilePointer() < openFile.length()) {
                fileWord = openFile.readUTF();
                if (word.compareTo(fileWord) == 0) {
                    posToInsert = openFile.getFilePointer();
                    if (openFile.readByte() == -1) {
                        return posToInsert;
                    }
                    posToInsert = openFile.getFilePointer();
                    if (openFile.readByte() == -1) {
                        return posToInsert;
                    }
                    posToInsert = openFile.getFilePointer();
                    if (openFile.readByte() == -1) {
                        return posToInsert;
                    }
                    posToInsert = openFile.getFilePointer();
                    if (openFile.readByte() == -1) {
                        return posToInsert;
                    }
                    posToInsert = openFile.getFilePointer();
                    if (openFile.readByte() == -1) {
                        return posToInsert;
                    }
                    posToInsert = openFile.getFilePointer();
                    if (openFile.readByte() == -1) {
                        openFile.seek(posToInsert);
                        openFile.writeLong(openFile.length());
                        openFile.seek(openFile.length());
                        return openFile.getFilePointer();
                    }

                } else {
                    openFile.readByte();
                    openFile.readByte();
                    openFile.readByte();
                    openFile.readByte();
                    openFile.readLong();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * This method checks if the ID is present in the list
     * 
     * @param ids
     * @param id
     * @return
     */
    public boolean idsArray(ArrayList<Byte> ids, byte id) {
        for (Byte j : ids) {
            if (j == id) {
                return true;
            } else {
                break;
            }
        }
        return false;
    }

}
