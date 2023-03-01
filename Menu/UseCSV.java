package Menu;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Musica.Crud;
import Musica.Musica;

class UseCSVReader {
  private String arq;
  private String line;

  public UseCSVReader(){
    arq = "spotifydb.csv";
    line = "";
  }

  public void readArq() throws Exception{

    String name, key;
    ArrayList<String> artists;
    int id, duration_ms, explicit;
    double tempo = 8.5754E+15;
    Date release_date;
      
    FileReader fr = new FileReader(this.arq);
    BufferedReader br = new BufferedReader(fr);

    line = br.readLine();
    while(line != null){

        String[] field = line.split(";");
        id = Integer.parseInt(field[0]);
        key = field[1];
        name = field[2];
        duration_ms = Integer.parseInt(field[3]);
        explicit = Integer.parseInt(field[4]);
        
        // arraylist
        String artists_string = field[5];
        artists_string = artists_string.replace("[", "");
        artists_string = artists_string.replace("]", "");
        artists_string = artists_string.replace("'", "");
        String[] artists_field = artists_string.split(",");
        artists = new ArrayList<String>();
        for(int i = 0; i < artists_field.length; i++){
          artists.add(artists_field[i]);
        }
        
        // tratar data -> os que tiverem apenas ano, colocar -01-01 no final
        String release_date_string = field[6]; 
        if (release_date_string.contains("/")){
          SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
          release_date = formato.parse(release_date_string);
        }
        else {
          if (release_date_string.length() == 4) release_date_string += "-01-01";
          if (release_date_string.length() == 7) release_date_string += "-01";

          SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
          release_date = formato.parse(release_date_string);
        }
        
        String tempo_string = field[7].replace(',', '.');
        tempo = Double.parseDouble(tempo_string);

        Crud crud = new Crud();
        Musica musica = new Musica(id, key, name, artists, duration_ms, explicit, tempo, release_date);
        crud.create(musica);
        // System.out.println(musica.toString());
        line = br.readLine();
    }
    br.close();
  }
}

public class UseCSV {
  public static void main(String[] args) throws Exception {
    UseCSVReader csv = new UseCSVReader();
    csv.readArq();
  }
}
