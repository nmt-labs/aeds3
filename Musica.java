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
  protected String id, name;
  protected ArrayList<String> artists;
  protected int duration_ms, explicit;
  protected float loudness;
  protected Date release_date;

  // constructors
  public Musica (){
    this.id = this.name = null;
    this.artists = new ArrayList<String>();
    this.duration_ms = this.explicit = -1;
    this.loudness = -1;
    this.release_date = null;
  }

  public Musica (String id, String name, ArrayList<String> artists, int duration_ms, int explicit, float loudness, Date release_date) {
    this.id = id;
    this.name = name;
    this.artists = artists;
    this.duration_ms = duration_ms;
    this.explicit = explicit;
    this.loudness = loudness;
    this.release_date = release_date;
  }

  // sets
  public void setId(String id) { this.id = id; }
  public void setName(String name) { this.name = name; }
  public void setArtists(ArrayList<String> artists) { this.artists = artists; }
  public void setDuration_ms(int duration_ms) { this.duration_ms = duration_ms; }
  public void setExplicit(int explicit) { this.explicit = explicit; }
  public void setLoudness(float loudness) { this.loudness = loudness; }
  public void setRelease_date(Date release_date) { this.release_date = release_date; }

  // gets
  public String getId() { return id; }
  public String getName() { return name; }
  public ArrayList<String> getArtists() { return artists; }
  public int getDuration_ms() { return duration_ms; }
  public int getExplicit() { return explicit; }
  public float getLoudness() { return loudness; }
  public Date getRelease_date() { return release_date; }

  // ----------------------------------- fim constructor -----------------------------------

  // transformar um objeto em um byte array
  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    // converter data para string
    DateFormat date_format = new SimpleDateFormat("yyyy-mm-dd");
    String release_date_string = date_format.format(release_date);

    dos.writeUTF(id);
    dos.writeUTF(name);
    for(String artist : artists){
      dos.writeUTF(artist);
    }
    dos.write(duration_ms);
    dos.write(explicit);
    dos.writeFloat(loudness);
    dos.writeUTF(release_date_string);
    return baos.toByteArray();
  }

  // transforma um byte array em objeto
  public void fromByteArray(byte[] ba) throws IOException, ParseException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);

    id = dis.readUTF();
    name = dis.readUTF();

    while (dis.available() > 0) {
      artists.add(dis.readUTF());
    }

    duration_ms = dis.read();
    explicit = dis.read();
    loudness = dis.readFloat();

    // converter string para data
    String release_date_string = dis.readUTF();
    release_date = new SimpleDateFormat("yyyy-MM-dd").parse(release_date_string);
  }

  public String toString(){
    return 
    "\nId: " + this.id +
    "\nNome: " + this.name +
    "\nArtistas: " + this.artists +
    "\nDuração: " + String.format( "%03d:%02d", duration_ms / 3600000, ( duration_ms / 60000 ) % 60 ) + 
    "\nExplicito: " + ((explicit == 1) ? 'S' : 'N') + 
    "\nData de lançamento: " + release_date + 
    "\nSonoridade: " + loudness;
  }
}