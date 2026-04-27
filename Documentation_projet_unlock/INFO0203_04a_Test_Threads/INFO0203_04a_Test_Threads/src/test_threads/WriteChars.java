package test_threads;

/**
 * Class which only writes char from A to Z according to a maximum of input
 * 
 * @author J.-C. BOISSON
 * @version 1.1
 */
public class WriteChars implements Runnable {

    private static final int DEFAULT_VALUE=10;
    
    private final int maximum;
    
    /**
     * Constructor of the corresponding object
     * 
     * @param maximum The maximum number of printed chars
     */        
    public WriteChars(int maximum) {
        
        if(maximum < 0)
            maximum=-maximum;
            
        if(maximum == 0)
            maximum=DEFAULT_VALUE;
            
        this.maximum=maximum;
    }
        
    @Override
    public void run() {
            
        int value = 65;
        int counter = 0;
            
        while(counter<maximum) {
            if((counter+1)%10==0) {
                System.out.println();
            }
            System.out.print((char)value+" ");
            counter++;
            value++;
            value%=90;
            try {
                 // each sleep is between 200 and 2000 ms
                Thread.sleep((long) ((Math.random()*1500)+200));
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
                   
        }
        System.out.println("==> ["+this.getClass().getSimpleName()+" finished] <==\n");
    }
     
}