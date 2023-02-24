import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UseCSV {
  private String arq;
  private String line;

  public UseCSV(){
    arq = "spotifydb.csv";
    line = "";
  }

  public void readArq() throws IOException, ParseException{

    String name, key;
    ArrayList<String> artists;
    int id, duration_ms, explicit;
    float tempo;
    Date release_date;
      
    FileReader fr = new FileReader(this.arq);
    BufferedReader br = new BufferedReader(fr);

    line = br.readLine();
    while(line != null){

        String[] field = line.split(";");
        id = Integer.parseInt(field[0]);
        key = field[1];
        name = field[2];

        // arraylist
        String artists_string = field[3];
        artists_string = artists_string.replace("[", "");
        artists_string = artists_string.replace("]", "");
        artists_string = artists_string.replace("'", "");
        String[] artists_field = artists_string.split(",");
        artists = new ArrayList<String>();
        for(int i = 0; i < artists_field.length; i++){
          artists.add(artists_field[i]);
        }

        duration_ms = Integer.parseInt(field[4]);
        explicit = Integer.parseInt(field[5]);
        tempo = Float.parseFloat(field[6]);

        // tratar data -> os que tiverem apenas ano, colocar -01-01 no final
        String release_date_string = field[7]; 
        if (release_date_string.length() == 4) release_date_string += "-01-01";
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        release_date = formato.parse(release_date_string);


        Musica musica = new Musica(id, key, name, artists, duration_ms, explicit, tempo, release_date);
        // & insere no db &
    }
    br.close();
  }
}
