package find_the_animal;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.util.Iterator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

public class Controller {
    
    private final String SOUND_DIR = "sounds";
    private final char   SEP       = java.io.File.separatorChar;
    private final int    NB_TURNS  = 5;
    
    private final ArrayList<String> paths;
   
    private final RuleManager manager;
    
    @FXML
    private Label score_label;
    private int score;
    
    @FXML
    private Label time_label;
    private int time;
    
    @FXML
    private FlowPane lower_pane;
    
    @FXML
    private Button play_button;

    private final ArrayList<Button> buttons;
    
    private MediaPlayer player;
   
    private Timeline play_timeline;
    private Timeline time_timeline;
    
    public Controller() {
        paths=new ArrayList<>();
        paths.add("01_Cat.mp3");
        paths.add("02_Dog.mp3");
        paths.add("03_Eagle.mp3");
        paths.add("04_Pork.mp3");
        paths.add("05_Ant.mp3");
        
        buttons=new ArrayList<>();
        
        manager=new RuleManager(paths.size());
    
        score=0;
        time=5;
    }
    
    public void initialize() {
        
        play_button.setOnMouseClicked(event -> playSong());
        
        score_label.setText(Integer.toString(score));
        time_label.setText(Integer.toString(time));

        for(var element : lower_pane.getChildren())
            buttons.add((Button)element);
   
        buttons.get(0).setOnMouseClicked(event -> check(0));
        buttons.get(1).setOnMouseClicked(event -> check(1));
        buttons.get(2).setOnMouseClicked(event -> check(2));
        buttons.get(3).setOnMouseClicked(event -> check(3));
        buttons.get(4).setOnMouseClicked(event -> check(4));
        
        for(var bouton : buttons)
            bouton.setDisable(true);
    }
    
    private void play(int choice) {
        
        File file = new File(SOUND_DIR+SEP+paths.get(choice));
        player = new MediaPlayer(new Media(file.toURI().toString()));
        player.play();
        for (Iterator<Button> it = buttons.iterator(); it.hasNext();) {
            javafx.scene.control.Button bouton = it.next();
            bouton.setDisable(false);
        }
    }
    
     private void playSong() {
        score=0;
        score_label.setText(Integer.toString(score));
        play_button.setDisable(true);
        manager.setCurrent();
        play(manager.getCurrent());
        time--;
        time_label.setText(Integer.toString(time));
       
        int play_delay = 5000;
        
        play_timeline = new Timeline(new KeyFrame(Duration.millis(play_delay), event -> {
                    manager.setCurrent();
                    play(manager.getCurrent());
                }));
        play_timeline.setCycleCount(NB_TURNS-1);
        play_timeline.play();
        
        int time_delay = 1000;
        time_timeline = new Timeline(new KeyFrame(Duration.millis(time_delay), event -> {
                time--;
                if(time==-1) {
                    time=4;
                }
                time_label.setText(Integer.toString(time));
                
                }));
        time_timeline.setCycleCount((NB_TURNS+1)*4);
        
        time_timeline.setOnFinished(event -> {
            for(var bouton : buttons) bouton.setDisable(true);
            play_button.setDisable(false);
            time=5;
            time_label.setText(Integer.toString(time));
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setTitle("Results");
            alert.setContentText("Your score is "+score);
            alert.show(); 
            
        });
        time_timeline.play();
     }
    
     private void check(int choice) {
        if(manager.getCurrent()==choice) {
            score++;
            score_label.setText(Integer.toString(score));
        }
        for(var bouton : buttons)
            bouton.setDisable(true);
     }   
}