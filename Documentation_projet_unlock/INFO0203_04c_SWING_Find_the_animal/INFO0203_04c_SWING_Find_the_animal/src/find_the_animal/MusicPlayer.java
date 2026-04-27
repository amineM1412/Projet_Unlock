package find_the_animal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MusicPlayer implements Runnable {

        private final String song;
        
        public MusicPlayer(String song) {
            this.song=song;
        }
        
        @Override
        public void run() {
            try{
                FileInputStream fis = new FileInputStream(song);
                Player playMP3 = new Player(fis);
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