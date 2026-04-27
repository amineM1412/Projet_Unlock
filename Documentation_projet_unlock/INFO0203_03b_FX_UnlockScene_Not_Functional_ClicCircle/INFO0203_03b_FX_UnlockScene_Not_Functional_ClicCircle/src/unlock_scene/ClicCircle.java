package unlock_scene;

public class ClicCircle {
    
    private static final Integer RADIUS=18;
    
    private final int number;
    private final int x_center_location;
    private final int y_center_location;
    
    public ClicCircle(int number, int x_center_location, int y_center_location) {
        this.number=number;
        this.x_center_location=x_center_location;
        this.y_center_location=y_center_location;
    }
    
    public Integer getNumber() {
        return number;
    }
    
    public boolean isInside(double x,double y) {
        
       return Math.sqrt(Math.pow((x-x_center_location), 2)+
                        Math.pow((y-y_center_location), 2))
                        <= RADIUS;
    }
    
}
