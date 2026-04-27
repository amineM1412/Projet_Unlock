package unlock_scene;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class Controller {

    @FXML
    private Pane panel;
    
    public void initialize() {
        Card initial_card = new Card(65);  
        panel.getChildren().add(initial_card);     
    }
}