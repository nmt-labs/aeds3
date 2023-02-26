import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Crud {

    private String nomeDoArquivo = "musicas.db";
    private RandomAccessFile arquivo;

    public Crud() {
        boolean verificaArquivo = (new File(nomeDoArquivo)).exists();
        if (!verificaArquivo) { // talvez criar o arquivo caso ele nao exista
            System.out.print("Arquivo não existe!");
        }
    }

    // Método de inserção no arquivo
    public void create(Musica musica) throws Exception {
        try {
            arquivo = new RandomAccessFile(nomeDoArquivo, "rw");

            arquivo.seek(0); // move ponteiro para o inicio do arquivo
            // int ultimoId = file.readInt();// le ultimo id
            // int novoId = ultimoId + 1;// incrementa id
            arquivo.seek(0);// mover ponteiro para o inicio do arquivo
            // file.writeInt(novoId);// escrever objeto.id

            // criar registro

            arquivo.seek(arquivo.length());// mover para o fim do arquivo

            // escrever registro
            // musica.setId(novoId);
            byte[] ba = musica.toByteArray();
            arquivo.writeByte(' '); // escreve a lápide
            arquivo.writeInt(ba.length);
            arquivo.writeShort((short) ba.length);
            arquivo.write(ba);

            System.out.println("Música adicionada com sucesso! Seu id é " + musica.getId());
        } catch (IOException e) {
            System.out.println("Erro ao adicionar música!");
        }

    }

    // Método de leitura do arquivo
    public Musica read(int id) throws Exception {
        arquivo = new RandomAccessFile(nomeDoArquivo, "rw");
        byte[] ba;
        short tamanho = arquivo.readShort();
        Musica musica = new Musica();

        arquivo.seek(4); // move ponteiro para o primeiro registro
        while (arquivo.getFilePointer() != arquivo.length()) {
            arquivo.seek(arquivo.getFilePointer() + 1);
            ba = new byte[tamanho];
            arquivo.read(ba);
            if (arquivo.readByte() != '*') {
                musica.fromByteArray(ba);
                if (musica.getId() == id)
                    return musica;
            }
        }
        return null;
    }

}
