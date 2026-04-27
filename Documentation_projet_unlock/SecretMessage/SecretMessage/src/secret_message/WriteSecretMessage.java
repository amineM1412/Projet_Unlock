package secret_message;

/**
 * Class which is able to write an instance of a SecretMessage object thanks to a serialization process
 *
 * @author J.-C. BOISSON 03/2020
 * 
 * @version 1.3 03/2025
 */
public class WriteSecretMessage {

    /**
     * Default constructor (only detailed for avoiding warning in documentation generation) */
    private WriteSecretMessage() {}
        
    /**
     * Main procedure<br>
     * usage : WriteSecretMessage fileName value1 value2 ... <br>
     * fileName is the path to the file that will be created<br>
     * value1, value2, ... are the messages to put in the objet Secret Message
     *
     * @param args The command line parameters */
    public static void main(String[] args) {

	if(args.length<2) {
	    System.err.println("Usage : WriteSecretMessage fileName value1 value2 ... ");
	    System.err.println("        value1, value2, ... are going to be saved into the file \"fileName\"");
	    return;
	}

	SecretMessage secret=new SecretMessage();

	for(int i=1;i<args.length;i++)
	    secret.addMessage(args[i]);

	System.out.println(secret);
	
	java.io.ObjectOutputStream out=null;

	try {
	    out=new java.io.ObjectOutputStream(new java.io.FileOutputStream(args[0]));
	} catch(java.io.IOException ioe) {
	    System.err.println("An I/O error occurs while writing stream header");
	    System.err.println(ioe);
	    System.exit(10);
	} catch(java.lang.SecurityException se) {
	    System.err.println("An untrusted subclass illegally overrides security-sensitive methods");
	    System.err.println(se);
	    System.exit(20);
	} catch(java.lang.NullPointerException npe) {
	    System.err.println("The FileOutputStream is null");
	    System.err.println(npe);
	    System.exit(30);
	}
	
	try {
	    out.writeObject(secret);
	} catch(java.io.InvalidClassException ice) {
	    System.err.println("Something is wrong with "+secret.getClass().getSimpleName()+" class during serialization");
	    System.err.println(ice);
	    System.exit(40);
	} catch(java.io.NotSerializableException nse) {
	    System.err.println("Some object to be serialized does not implement the java.io.Serializable interface");
	    System.err.println(nse);
	    System.exit(50);
	} catch(java.io.IOException ioe) {
	    System.err.println("Any exception thrown by the underlying OutputStream.");
	    System.err.println(ioe);
	    System.exit(60);
	} finally {
	    try {
		out.flush();
		out.close();
	    } catch(java.io.IOException ioe) {
		System.err.println("There is an I/O error during flush or close process.");
		System.err.println(ioe);
		System.exit(70);
	    } 	
	}
  }
}
