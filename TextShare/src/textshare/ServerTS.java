package textshare;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/** TSServer accetta nuove connessioni e lascia la gestione a thread creati appositamente
 * per il client appena connesso; per gestire i comandi di info e quit, crea un apposito thread
 */
public class ServerTS {
    
    public static void main (String args []) {
        if (args.length < 2) {
            System.err.println("Errore, avviare nel seguente modo: java TSServer <path> <port>");
            return;
        }
        String path = args[0];
        int port = Integer.parseInt(args[1]);

        //check del path
        File f = new File(path);
        if (!(f.exists()) || !(f.isDirectory())) {
        	System.err.println("Il path inserito non esiste");
        	return;
        }
        
        try {
            ServerSocket listener = new ServerSocket(port); //si mette in ascolto sulla porta creando un'istanza di ServerSocekt

            TextManager textManager = new TextManager(); //gestore dei file di testo con tutti i metodi
            
            Scanner userInput = new Scanner(System.in); //Lettura dell'input da terminale
            
            //Ciclo di vita del thread principale del server
            while (true) {
                System.out.println("In ascolto...");
                Socket s = listener.accept(); //Connessione al client con creazione apposito Socket
                System.out.println("Connesso");

                //Lasica la gestione a un thread dedicato
                Thread clientHandlerThread = new Thread(new ClientHandlerTS(s, textManager));
                clientHandlerThread.start();
                //Si rimette in ascolto di altre connessioni
                
                //TODO: info e quit, nuovo thread
            }
            
            //interruzione del server e conseguente chiusura del ServerSocket 
            //listener.close();
            //userInput.close();

        } catch (IOException e) {
            System.err.println("Error during I/O operation:");
            e.printStackTrace();
        }
    }
}
