import java.util.Date;
import java.util.ArrayList;

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
}