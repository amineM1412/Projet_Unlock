package sound_player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * Class to ease the use of the JLayer library to play music (mp3)
 * 
 * @author J.-C. BOISSON
 * @version 1.0
 */
public class MusicPlayer implements Runnable {

        private final String song;
        
        public MusicPlayer(String song) {
            this.song=song;
        }
        
        @Override
        public void run() {
            try{
                FileInputStream fis = new FileInputStream(song);
                Player playMP3      = new Player(fis);
                playMP3.play();
            } catch(FileNotFoundException fnfe){
                System.out.println("The file "+song+" can not be found.");
                System.out.println(fnfe.getMessage());
            } catch(JavaLayerException jle) {
                System.out.println("The file "+song+" can not be played in the current system configuration.");
                System.out.println(jle.getMessage());
            }
        }       
    }