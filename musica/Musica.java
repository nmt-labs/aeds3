package musica;
import java.util.ArrayList;
import java.util.Date;

import cryptography.BlockCipher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.DataOutputStream;

public class Musica {
  protected String name, key;
  protected ArrayList<String> artists;
  protected int id, duration_ms, explicit;
  protected double tempo;
  protected Date release_date;
  private static final BlockCipher CIPHER = new BlockCipher();

  // constructors
  public Musica (){
    this.name = this.key = null;
    this.artists = new ArrayList<String>();
    this.id = this.duration_ms = this.explicit = -1;
    this.tempo = -1;
    this.release_date = null;
  }

  public Musica (int id, String key, String name, ArrayList<String> artists, int duration_ms, int explicit, double tempo, Date release_date) {
    this.id = id;
    this.key = key;
    this.name = name;
    this.artists = artists;
    this.duration_ms = duration_ms;
    this.explicit = explicit;
    this.tempo = tempo;
    this.release_date = release_date;
  }

  // sets
  public void setId(int id) { this.id = id; }
  public void setKey(String key) { this.key = key; }
  public void setName(String name) { this.name = CIPHER.crypt(name); } // criptografia
  public void setArtists(ArrayList<String> artists) { this.artists = artists; }
  public void setDuration_ms(int duration_ms) { this.duration_ms = duration_ms; }
  public void setExplicit(int explicit) { this.explicit = explicit; }
  public void setTempo(double tempo) { this.tempo = tempo; }
  public void setRelease_date(Date release_date) { this.release_date = release_date; }

  // gets
  public int getId() { return id; }
  public String getKey() { return key; }
  public String getName() { return CIPHER.decrypt(name); } // criptografia
  public ArrayList<String> getArtists() { return artists; }
  public int getDuration_ms() { return duration_ms; }
  public int getExplicit() { return explicit; }
  public double getTempo() { return tempo; }
  public Date getRelease_date() { return release_date; }

  // ----------------------------------- fim constructor -----------------------------------

  // transformar um objeto em um byte array
  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    // converter data para string
    DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
    String release_date_string = date_format.format(this.release_date);

    dos.writeInt(this.id);
    dos.writeUTF(this.key);
    dos.writeUTF(this.name);
    dos.writeInt(this.duration_ms);
    dos.writeInt(this.explicit);
    dos.writeInt(artists.size());
    for(String artist : artists){
      dos.writeInt(artist.getBytes(Charset.forName("UTF-8")).length); //tamanho da string atual
      dos.writeUTF(artist);
    }
    dos.writeUTF(release_date_string);
    dos.writeDouble(this.tempo);
    return baos.toByteArray();
  }

  // transforma um byte array em objeto
  public void fromByteArray(byte[] ba) throws IOException, ParseException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);
    artists.clear(); // limpar o array

    this.id = dis.readInt();
    this.key = dis.readUTF(); 
    this.name = dis.readUTF(); 
    this.duration_ms = dis.readInt();
    this.explicit = dis.readInt();
    
    int artists_length = dis.readInt();
    for(int i = 0; i < artists_length; i++) {
      dis.readInt();
      this.artists.add(dis.readUTF());
    }
    
    // converter string para data
    String release_date_string = dis.readUTF();
    this.release_date = new SimpleDateFormat("yyyy-MM-dd").parse(release_date_string);
    
    this.tempo = dis.readDouble();
  }

  public String toString(){
    DateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
    String release_date_string = date_format.format(this.release_date);
    
    return 
    "\nId: " + this.id +
    "\nKey: " + this.key +
    "\nNome: " + this.name +
    "\nArtistas: " + this.artists +
    "\nDuração: " + this.duration_ms + " ms" + 
    "\nExplicito: " + ((explicit == 1) ? "SIM" : "NAO") + 
    "\nData de lançamento: " + release_date_string + 
    "\nTempo: " + tempo;
  }

  public Musica clone() {

    Musica cloned = new Musica();

    cloned.id = this.id;
    cloned.key = this.key;
    cloned.name = this.name;
    cloned.artists = this.artists;
    cloned.duration_ms = this.duration_ms;
    cloned.explicit = this.explicit;
    cloned.tempo = this.tempo;
    cloned.release_date = this.release_date;

    return cloned;
  }
}
