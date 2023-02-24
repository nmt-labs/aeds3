/*
 * ainda precisa ser testado
 */
import java.util.ArrayList;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.DataOutputStream;

public class Musica {
  protected String name, key;
  protected ArrayList<String> artists;
  protected int id, duration_ms, explicit;
  protected float tempo;
  protected Date release_date;

  // constructors
  public Musica (){
    this.name = this.key = null;
    this.artists = new ArrayList<String>();
    this.id = this.duration_ms = this.explicit = -1;
    this.tempo = -1;
    this.release_date = null;
  }

  public Musica (int id, String key, String name, ArrayList<String> artists, int duration_ms, int explicit, float tempo, Date release_date) {
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
  public void setName(String name) { this.name = name; }
  public void setArtists(ArrayList<String> artists) { this.artists = artists; }
  public void setDuration_ms(int duration_ms) { this.duration_ms = duration_ms; }
  public void setExplicit(int explicit) { this.explicit = explicit; }
  public void setTempo(float tempo) { this.tempo = tempo; }
  public void setRelease_date(Date release_date) { this.release_date = release_date; }

  // gets
  public int getId() { return id; }
  public String getKey() { return key; }
  public String getName() { return name; }
  public ArrayList<String> getArtists() { return artists; }
  public int getDuration_ms() { return duration_ms; }
  public int getExplicit() { return explicit; }
  public float getTempo() { return tempo; }
  public Date getRelease_date() { return release_date; }

  // ----------------------------------- fim constructor -----------------------------------

  // transformar um objeto em um byte array
  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    // converter data para string
    DateFormat date_format = new SimpleDateFormat("yyyy-mm-dd");
    String release_date_string = date_format.format(release_date);

    dos.write(id);
    dos.writeUTF(key);
    dos.writeUTF(name);
    for(String artist : artists){
      dos.writeUTF(artist);
    }
    dos.write(duration_ms);
    dos.write(explicit);
    dos.writeFloat(tempo);
    dos.writeUTF(release_date_string);
    return baos.toByteArray();
  }

  // transforma um byte array em objeto
  public void fromByteArray(byte[] ba) throws IOException, ParseException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);

    id = dis.read();
    key = dis.readUTF();
    name = dis.readUTF();

    while (dis.available() > 0) {
      artists.add(dis.readUTF());
    }

    duration_ms = dis.read();
    explicit = dis.read();
    tempo = dis.readFloat();

    // converter string para data
    String release_date_string = dis.readUTF();
    release_date = new SimpleDateFormat("yyyy-MM-dd").parse(release_date_string);
  }

  public String toString(){
    return 
    "\nId: " + this.id +
    "\nKey: " + this.key +
    "\nNome: " + this.name +
    "\nArtistas: " + this.artists +
    "\nDuração: " + String.format( "%03d:%02d", duration_ms / 3600000, ( duration_ms / 60000 ) % 60 ) + 
    "\nExplicito: " + ((explicit == 1) ? 'S' : 'N') + 
    "\nData de lançamento: " + release_date + 
    "\nSonoridade: " + tempo;
  }
}