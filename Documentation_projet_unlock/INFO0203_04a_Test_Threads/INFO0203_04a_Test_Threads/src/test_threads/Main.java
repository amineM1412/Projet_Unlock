package test_threads;

/**
 * Main class for testing Thread launching
 * 
 * @author J.-C. BOISSON
 * @version 1.1
 */
public class Main {

    /**
     * Classical main procedure
     * 
     * @param args command line arguments ==> not used here 
     */
    public static void main(String[] args) {
        
        Thread charWriter = new Thread(new WriteChars   (20));
        Thread intWriter  = new Thread(new WriteIntegers(20));
        
        charWriter.start();
        intWriter.start();
    }
    
}
