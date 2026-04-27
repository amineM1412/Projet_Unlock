package secret_message;

/**
 * Class which is able to read an instance of a SecretMessage object from a binary file thanks to a (de)serialization process
 *
 * @author J.-C. BOISSON 03/2020
 * 
 * @version 1.3 03/2025
 */
public class ReadSecretMessage {
    
    /**
     * Default constructor (only detailed for avoiding warning in documentation generation) */
    private ReadSecretMessage() {}
    
    /**
     * Main procedure<br>
     * usage : ReadSecretMessage fileName<br>
     * fileName It gives the path to the message encoded by WriteSecretMessage
     *
     * @param args The command line parameters, only one parameter is needed.*/
    public static void main(String[] args) {
	if(args.length!=1) {
	    System.err.println("Usage : ReadSecretMessage fileName ");
	    System.err.println("        fileName must contain the message encoded by WriteSecretMessage");
	    System.exit(10);
	}

	java.io.ObjectInputStream in=null;
	SecretMessage message=null;
	
	try {
	    in=new java.io.ObjectInputStream(new java.io.FileInputStream(args[0]));
	    message=(SecretMessage)in.readObject();
	    System.err.println(message);
    	} catch(java.io.StreamCorruptedException sce) {
	    System.err.println("The stream header is incorrect during the opening of the stream or the object reading.");
	    System.err.println(sce);
	    System.exit(20);
	} catch(java.lang.SecurityException se) {
	    System.err.println("An untrusted subclass illegally overrides security-sensitive methods.");
	    System.err.println(se);
	    System.exit(30);
	} catch(java.lang.NullPointerException npe) {
	    System.err.println("The FileInputStream is null.");
	    System.err.println(npe);
	    System.exit(40);
	} catch(java.lang.ClassNotFoundException cne) {
	    System.err.println("Class "+SecretMessage.class.getName()+" of the serialized object cannot be found.");
	    System.err.println(cne);
	    System.exit(50);
	} catch(java.io.InvalidClassException ice) {
	    System.err.println("Something is wrong with a class used by serialization.");
	    System.err.println(ice);
	    System.exit(60);
	} catch(java.io.OptionalDataException ode) {
	    System.err.println("Primitive data was found in the stream instead of objects.");
	    System.err.println(ode);
	    System.exit(70);
	} catch(java.io.IOException ioe) {
	    System.err.println("an I/O error occurs while reading stream header.");
	    System.err.println(ioe);
	    System.exit(80);
	} finally {
	    if(in != null) {
		try {
		    in.close();
		} catch(java.io.IOException ioe) {
		    System.err.println("There is an I/O error ...");
		    System.err.println(ioe);
		    System.exit(90);
		}
	    }
	}
  }
}
