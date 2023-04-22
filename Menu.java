import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

import externalsort.CommonIntercalation;
import externalsort.Selection;
import externalsort.VariableIntercalation;
import musica.Crud;
import invertedList.InvertedList;
import musica.Musica;

import java.util.*;
import java.io.File;
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

        while (op != 0) {
            System.out.println("\nEscolha uma opção:");
            System.out.println("1- Adicionar uma música");
            System.out.println("2- Procurar uma música");
            System.out.println("3- Alterar uma música");
            System.out.println("4- Excluir uma música");
            System.out.println("5- Ordenar");
            System.out.println("6- Procurar com B Plus Tree");
            System.out.println("7- Procurar com Extendible Hash");
            System.out.println("8- Buscar na lista invertida");
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
        InvertedList il = new InvertedList();
        String word;

        int id;

        switch (op) {
            case 1:
                // create
                musica = menuCreate();
                System.out.println(musica.toString());
                crud.create(musica);

                il.createInvertedList(musica.getName(), musica.getId(),
                        "invertedList" + File.separator + "db" + File.separator + "invertedListNames.db");
                il.createInvertedList(separateArtists(musica.getArtists()), musica.getId(),
                        "invertedList" + File.separator + "db" + File.separator + "invertedListArtists.db");
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
            case 3:
                // update
                musica = menuUpdate();
                if (crud.update(musica)) {
                    System.out.println("Música atualizada com sucesso!");
                } else {
                    System.out.println("Erro ao atualizar");
                }
                break;
            case 4:
                // delete
                System.out.println("Digite o ID da música:");
                id = scan.nextInt();
                byte idByte = (byte) id;
                musica = crud.delete(id);
                if (musica != null) {
                    System.out.println("Música de id " + musica.getId() + " deletada com sucesso!");
                } else {
                    System.err.println("Música não encontrada");
                }
                il.deleteInvertedList(idByte,
                        "invertedList" + File.separator + "db" + File.separator + "invertedListNames.db");
                il.deleteInvertedList(idByte,
                        "invertedList" + File.separator + "db" + File.separator + "invertedListArtists.db");
                break;
            case 5:
                int caminhos, bloco, heap;
                // criar subpasta fileTemp
                File fileTemp = new File("db" + File.separator + "fileTemp");
                if (!fileTemp.exists())
                    fileTemp.mkdirs();

                // ordenar
                System.out.println("1 - Intercalação balanceada comum");
                System.out.println("2 - Intercalação balanceada com blocos de tamanho variável");
                System.out.println("3 - Intercalação balanceada com seleção por substituição");
                op = scan.nextInt();

                switch (op) {
                    case 1:
                        // comum
                        System.out.println("Digite a quantidade de caminhos: ");
                        caminhos = scan.nextInt();
                        System.out.println("Digite o tamanho do bloco: ");
                        bloco = scan.nextInt();
                        CommonIntercalation commonInt = new CommonIntercalation(caminhos, bloco); // ordena por nome
                        commonInt.sort();
                        break;
                    case 2:
                        System.out.println("Digite a quantidade de caminhos: ");
                        caminhos = scan.nextInt();
                        System.out.println("Digite o tamanho do bloco: ");
                        bloco = scan.nextInt();
                        VariableIntercalation variableInt = new VariableIntercalation(caminhos, bloco); // ordena por
                                                                                                        // nome
                        variableInt.sort();
                        break;
                    case 3:
                        System.out.println("Digite a quantidade de caminhos: ");
                        caminhos = scan.nextInt();
                        System.out.println("Digite o tamanho do heap: ");
                        heap = scan.nextInt();
                        Selection selection = new Selection(caminhos, heap); // ordena por nome
                        selection.sort();
                        break;
                    default:
                        System.out.println("Comando inválido");
                        break;
                }
                break;

            case 6:
                System.out.println("Digite o ID da música:");
                id = scan.nextInt();
                musica = crud.readBPlus(id);
                if (musica != null) {
                    System.out.println(musica.toString());
                } else {
                    System.out.println("Nenhuma música localizada");
                }
                break;

            case 7:
                System.out.println("Digite o ID da música:");
                id = scan.nextInt();
                musica = crud.readHash(id);
                if (musica != null) {
                    System.out.println(musica.toString());
                } else {
                    System.out.println("Nenhuma música localizada");
                }
                break;
            case 8:
                System.out.println(
                        "Digite o termo que deseja procurar na lista invertida, seja por nome ou por artista: ");
                scan.nextLine();
                word = scan.nextLine();
                menuSearchInvertedList(word);
                break;
            default:
                System.out.println("Comando inválido");
                break;
        }
    }

    private static Musica menuCreate() throws ParseException {

        String name, key, artistsString, dataString;
        ArrayList<String> artists = new ArrayList<String>();
        int id, duration_ms, explicit;
        double tempo;
        Date release_date;

        id = lastId();

        key = keyGen();

        scan.nextLine(); // descarta proxima entrada (problema do nextInt)
        System.out.println("Nome da música: ");
        name = scan.nextLine();

        System.out.println("Artistas (separados por virgula): ");
        artistsString = scan.nextLine();
        String[] separateArtists = artistsString.split(",");
        for (int i = 0; i < separateArtists.length; i++) {
            artists.add(separateArtists[i]);
        }

        System.out.println("Duração (ms): ");
        duration_ms = scan.nextInt();

        scan.nextLine(); // descarta proxima entrada (problema do nextInt)
        System.out.println("É explicita? (SIM ou NAO): ");
        String line = scan.nextLine();
        explicit = (line.equals("SIM")) ? 1 : 0;

        System.out.println("Tempo: ");
        tempo = scan.nextDouble();

        scan.nextLine(); // descarta proxima entrada (problema do nextInt)
        System.out.println("Data de lançamento (dd/mm/aaaa): ");
        dataString = scan.nextLine();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        release_date = formato.parse(dataString);

        Musica musica = new Musica(id, key, name, artists, duration_ms, explicit, tempo, release_date);
        return musica;
    }

    // encontra o ultimo id e cria um novo
    private static int lastId() {
        RandomAccessFile file;
        int lastId;
        try {
            file = new RandomAccessFile("db" + File.separator + "musicas.db", "rw");
            lastId = file.readInt();
            file.close();
            lastId++;

            return lastId;
        } catch (IOException e) {
            System.out.println("Criando novo arquivo");

            return 0;
        }
    }

    /*
     * Código retirado de:
     * https://acervolima.com/gerar-string-aleatoria-de-determinado-tamanho-em-java/
     */
    private static String keyGen() {
        int n = 22;
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // remove all spacial char
        String AlphaNumericString = randomString.replaceAll("[^A-Za-z0-9]", "");

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < AlphaNumericString.length(); k++) {

            if (Character.isLetter(AlphaNumericString.charAt(k)) && (n > 0)
                    || Character.isDigit(AlphaNumericString.charAt(k)) && (n > 0)) {
                r.append(AlphaNumericString.charAt(k));
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }

    private static Musica menuUpdate() throws Exception {

        String name, artistsString, dataString;
        ArrayList<String> artists = new ArrayList<String>();
        int id, duration_ms, explicit, op;
        float tempo;
        Date release_date;

        Crud crud = new Crud();
        Musica musica;
        InvertedList il = new InvertedList();

        System.out.println("Insira o id da música para alteração: ");
        id = scan.nextInt();
        byte idByte = (byte) id;
        musica = crud.read(id);

        if (musica != null) {
            System.out.println("Selecione o que será alterado: ");
            System.out.println("1- Nome");
            System.out.println("2- Artistas");
            System.out.println("3- Duração");
            System.out.println("4- Explicita");
            System.out.println("5- Tempo");
            System.out.println("6- Data de lançamento");
            System.out.println("Insira sua opção: ");
            op = scan.nextInt();
            scan.nextLine(); // erro do nextInt()

            switch (op) {
                case 1:
                    System.out.println("Insira novo nome: ");
                    name = scan.nextLine();

                    musica.setName(name);
                    il.updateInvertedList(name, idByte,
                            "invertedList" + File.separator + "db" + File.separator + "invertedListNames.db");

                    break;
                case 2:
                    System.out.println("Insira novo(s) artistas (separados por vírgula): ");
                    artistsString = scan.nextLine();
                    String[] separateArtists = artistsString.split(",");
                    for (int i = 0; i < separateArtists.length; i++) {
                        artists.add(separateArtists[i]);
                    }

                    musica.setArtists(artists);
                    il.updateInvertedList(separateArtists(artists), idByte,
                            "invertedList" + File.separator + "db" + File.separator + "invertedListArtists.db");

                    break;
                case 3:
                    System.out.println("Insira nova duração: ");
                    duration_ms = scan.nextInt();

                    musica.setDuration_ms(duration_ms);
                    break;
                case 4:
                    System.out.println("É explicita? (SIM ou NAO): ");
                    String linha = scan.nextLine();
                    explicit = (linha.equals("SIM")) ? 1 : 0;

                    musica.setExplicit(explicit);
                    break;
                case 5:
                    System.out.println("Insira novo tempo: ");
                    tempo = scan.nextFloat();

                    musica.setTempo(tempo);
                    break;
                case 6:
                    System.out.println("Insira nova data de lançamento (dd/mm/aaaa): ");
                    dataString = scan.nextLine();
                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                    release_date = formato.parse(dataString);

                    musica.setRelease_date(release_date);
                    break;
            }

            return musica;
        } else {
            System.out.println("Musica nao encontrada");
            return null;
        }
    }

    public static void menuSearchInvertedList(String word) throws Exception {
        InvertedList iList = new InvertedList();
        Musica music = new Musica();
        Crud crud_ = new Crud();

        ArrayList<Byte> idsNames = iList.readInvertedList(word,
                "invertedList" + File.separator + "db" + File.separator + "invertedListNames.db");
        ArrayList<Byte> idsArtists = iList.readInvertedList(word,
                "invertedList" + File.separator + "db" + File.separator + "invertedListArtists.db");
        System.out.println("Musicas localizadas:");
        for (int i = 0; i < idsNames.size(); i++) {
            music = crud_.read(idsNames.get(i));
            if (music != null) {
                System.out.println(music.toString());
            } else {
                System.out.println("Nenhuma música localizada com esse nome");
            }
        }
        for (int i = 0; i < idsArtists.size(); i++) {
            music = crud_.read(idsArtists.get(i));
            if (music != null) {
                System.out.println(music.toString());
            } else {
                System.out.println("Nenhuma música localizada com esse artista");
            }
        }
    }

    public static String separateArtists(ArrayList<String> artists) {
        String artistsString = String.join(" ", artists);
        return artistsString;
    }
}
