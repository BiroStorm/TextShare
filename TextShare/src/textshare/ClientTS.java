package textshare;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientTS {
    public static void main (String args[]) {

        //All'avvio del client passare indirizzo e porta del server a cui connettersi
        if (args.length < 2) {
            System.err.println("Errore, avviare nel seguente modo: java TSClient <host> <port>");
            return;
        }
        //estraiamo i due argomenti da linea di comando
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            //Ci connettiamo all'indirizzo e alla porta forniti
            Socket s = new Socket(host, port);
            System.out.println("Connessione avvenuta con successo\n");
            System.out.println("Benvenuto/a in TextShare!\n"
            		+ "Puoi eseguire uno dei seguenti comandi:\n"
            		+ "- \"list\": ottieni una lista di tutti i file presenti sul server\n"
            		+ "- \"create\": seguito da <nome>.txt, crea un nuovo file chiamato <nome>\n"
            		+ "- \"read\": seguito da <nome>.txt, apre il file in modalità lettura\n"
            		+ "   - \":close\": se sei in modalità lettura, chiude la sessione\n"
            		+ "- \"edit\": seguito da <nome>.txt, apre il file in modalità scrittura\n"
            		+ "   - \":backspace\": se sei in modalità scrittura, elimina l'ultima riga del file\n"
            		+ "   - \":close\": se sei in modalità scrittura, chiude la sessione\n"
            		+ "   - ogni comando che non inizia con \":\" viene interpretato come una riga di testo che viene aggiunta in coda al file");

            Scanner fromServer = new Scanner(s.getInputStream()); //Wrapper per ricevere dal server
            PrintWriter toServer = new PrintWriter(s.getOutputStream(), true); //Wrapper per inviare al server

            Scanner userInput = new Scanner(System.in); //Lettura da terminale

            //Ciclo di vita del client
            while (true) {
                String request = userInput.nextLine(); //Lettura della richiesta dell'utente
                toServer.println(request); //inoltro della richista al server
                if (request.equals("quit")) {
                    //Se l'utente scrive quit, interrompe il ciclo
                    break;
                } else {
                	String response = fromServer.nextLine(); //Lettura della risposta del server
                	System.out.println(response); //stampa della risposta
                }
            }

            //Al termine del ciclo di vita, viene chiusa la connessione e lo scanner
            s.close();
            userInput.close();
            System.out.println("Disconessione avvenuta con successo");

        } catch (IOException e) {
            System.err.println("Errore I/O");
        }
        
    }
}