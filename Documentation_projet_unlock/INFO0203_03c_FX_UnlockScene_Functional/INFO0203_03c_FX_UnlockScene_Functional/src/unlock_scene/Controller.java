package unlock_scene;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;

import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class Controller {

    @FXML
    private Pane panel;
    
    private final ArrayList<Integer> cards;
    
    private Timeline checkFoundCards;
    
    public Controller() {
        cards = new ArrayList<>();
    }
    
    public void initialize() {
        Card card= new Card(65);
        
        cards.add(card.getNumber());
        
        panel.getChildren().add(card);
        
        checkFoundCards=new Timeline(new KeyFrame(Duration.millis(1000), event ->checkCards()));
        checkFoundCards.setCycleCount(Animation.INDEFINITE);
        checkFoundCards.play();        
    }
    
    /**
     * Procedure that checks if new cards have been discovered
 If necessary, the procedure update the Pane panel.
     */
    private void checkCards() {
        
        //System.out.println("unlock_scenes.Controller.checkCards()");
        
        ArrayList<Integer> new_cards = new ArrayList<>();
        
        for (Iterator<Node> it_node = panel.getChildren().iterator(); it_node.hasNext();) {
            Node card = it_node.next();
            for (Iterator<Integer> it_idCard = ((Card)card).getCartesTrouvees().iterator(); it_idCard.hasNext();) {
                java.lang.Integer numero = it_idCard.next();
                if(!cards.contains(numero)) {
                    new_cards.add(numero);
                }
            }
        }
        
        for(Integer numero: new_cards) {
            panel.getChildren().add(new Card(numero));
            cards.add(numero);
        }
    }
}