package secret_message;

/**
 * Class which is able to save several messages and implements java.io.Serializable 
 *
 * @author J.-C. BOISSON 03/2020
 * 
 * @version 1.4 03/2026 */
public class SecretMessage implements java.io.Serializable {

    /** Indicate the current version of the class*/
    private static final long serialVersionUID = 11111111L;

    /** A random int value in [0,1024[*/
    private int specialValue;

    /** An array of String corresponding to the user messages*/
    private java.util.ArrayList<String> messages;

    /** An other random int value in [0,1024[ which cannot be serialized due to the <b>transient</b> keyword */ 
    private transient int specialValueT; 

    /**
     * Classical constructor by initialization */
    public SecretMessage() {
	messages        =  new java.util.ArrayList<String>();
	specialValue    = (int)(Math.random()*1024);
	specialValueT   = (int)(Math.random()*1024);
    }

    /**
     * Procedure to save a new message
     *
     * @param message The new message to save */
    public void addMessage(String message) {
	  messages.add(message);
    }
    
    /**
     * Specialized version of toString function that prints the value of all the internal attributes.
     *
     * @return String version of the SecretMessage Object.*/
    @Override
    public String toString() {
	  
      String secretMessage = "=============================";
	  secretMessage       += "\nThe special    value  is "+specialValue;
	  secretMessage       += "\nThe special    valueT is "+specialValueT;
	  secretMessage       += "\nThe secret message    is ";
	
	  java.util.Iterator<String> it=messages.iterator();
	  while(it.hasNext()) {
	      secretMessage+="\n\t"+it.next();
	  }
	
	  secretMessage       +="\n=============================";
	
	  return secretMessage;
    }
}
