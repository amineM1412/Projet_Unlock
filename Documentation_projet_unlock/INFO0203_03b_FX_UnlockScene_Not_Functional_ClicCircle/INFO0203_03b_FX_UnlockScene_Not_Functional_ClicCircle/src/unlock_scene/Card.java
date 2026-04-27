package unlock_scene;

import java.io.File;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Card  extends ImageView {
    private static final String  IMAGE_DIRECTORY = "images";
    private static final String  IMAGE_PREFIX    = "Unlock_MDF_Carte_";
    private static final String  IMAGE_SUFFIX    = ".png";
    private static final char    SEPARATOR_CHAR  = File.separatorChar;
    private static final Integer IMAGE_HEIGHT    = 500;
    private static final Double  ROTATE_ANGLE    = 45.0;
    
    private final int number;
    private double dragX,dragY;
    
    private final ArrayList<ClicCircle> circles;
    
    public Card(int numero) {
        
        circles = new ArrayList<>();
        
        circles.add(new ClicCircle(18,139,277));
        circles.add(new ClicCircle(59,250,239));        
        
        this.number=numero;
        
        String path  = IMAGE_DIRECTORY;
        path        += SEPARATOR_CHAR;
        path        += IMAGE_PREFIX;
        path        += this.number;
        path        += IMAGE_SUFFIX;
        
        File file = new File(path);
        setImage(new Image(file.toURI().toString()));
        setPreserveRatio(true);
        setFitHeight(IMAGE_HEIGHT);
        
        setOnMousePressed(mouse_event -> handleMousePressed(mouse_event));
        setOnMouseDragged(mouse_event -> handleMouseDragged(mouse_event));
        setOnMouseClicked(mouse_event -> handleMouseClicked(mouse_event));        
    }
    
    public int getNumber() {
        return number;
    }
    
    private void handleMousePressed(MouseEvent event)
    {
        this.dragX= event.getScreenX() - getTranslateX();
        this.dragY= event.getScreenY() - getTranslateY();
    }
    
    private void handleMouseDragged(MouseEvent event)
    {
        translateXProperty().set(event.getScreenX()-this.dragX);
        translateYProperty().set(event.getScreenY()-this.dragY);
    }
    
    private void handleMouseClicked(MouseEvent event) {
        
        switch(event.getButton()) {
            case PRIMARY -> {
                double x = event.getX();
                double y = event.getY();
                System.out.println(x+" "+y);
                for(var cercle : circles) {
                    if(cercle.isInside(x, y)) {
                        System.err.println("Clic dans le cercle "+cercle.getNumber());
                    }
                }
            }
            case SECONDARY -> setRotate(getRotate()+ROTATE_ANGLE);
            
            default -> { 
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setTitle("Mad Guy");
                alert.setContentText("Are you crazy ????");
                alert.show(); 
            }
        }
    }
    
}
