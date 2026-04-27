package test_threads;

/**
 * Class which only writes integer from 0 to a maximum value
 * 
 * @author J.-C. BOISSON
 * @version 1.1
 */
public class WriteIntegers implements Runnable {

    private static final int DEFAULT_VALUE=10;
    
    private final int maximum;
    
    /**
     * Constructor of the corresponding object
     * 
     * @param maximum The chosen maximum value
     */    
    public WriteIntegers(int maximum) {
            
        if(maximum < 0)
            maximum=-maximum;
            
        if(maximum == 0)
            maximum=DEFAULT_VALUE;
            
        this.maximum=maximum;
    }
        
    @Override
    public void run() {
         
        int counter = 0;
            
        while(counter<maximum) {
            if((counter+1)%10==0) {
                System.out.println();
            }
            System.out.print(counter+" ");
            counter++;
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