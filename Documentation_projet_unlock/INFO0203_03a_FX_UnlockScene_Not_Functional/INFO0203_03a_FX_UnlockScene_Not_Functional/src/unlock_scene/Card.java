package unlock_scene;

import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Card  extends ImageView {
    private static final String  IMAGE_DIRECTORY = "images";
    private static final String  IMAGE_PREFIX    = "Unlock_MDF_Carte_";
    private static final String  IMAGE_SUFFIX    = ".png";
    private static final char    SEPARATOR_CHAR  = File.separatorChar;
    private static final Integer IMAGE_HEIGHT    = 500;
    
    private final int number;
    private double dragX,dragY;
    
    public Card(int number) {     
        
        this.number=number;
        
        String chemin  = IMAGE_DIRECTORY;
        chemin        += SEPARATOR_CHAR;
        chemin        += IMAGE_PREFIX;
        chemin        += this.number;
        chemin        += IMAGE_SUFFIX;
        
        File file = new File(chemin);
        System.out.println(file.toURI().toString());
        setImage(new Image(file.toURI().toString()));
        setPreserveRatio(true);
        setFitHeight(IMAGE_HEIGHT);
        
        setOnMousePressed(e->handleMousePressed(e));
        setOnMouseDragged(e->handleMouseDragged(e));
    }
    
    protected void handleMousePressed(MouseEvent e)
    {
        this.dragX= e.getScreenX() - getTranslateX();
        this.dragY= e.getScreenY() - getTranslateY();
    }
    
    protected void handleMouseDragged(MouseEvent e)
    {
        translateXProperty().set(e.getScreenX()-dragX);
        translateYProperty().set(e.getScreenY()-dragY);
    }
    
    public int getNumber() {
        return number;
    }
}
