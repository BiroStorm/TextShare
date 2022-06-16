package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/*
 * ClientTS implementa la logica del client. Prende come argomenti host e porta, si connette creando un socket lato client.
 * Invia i comandi al server (vedi ClientHandlerTS), riceve la risposta e la stampa su terminale.
 * Con il comando quit chiude il suo socket e, dato che invia anche questo comando al server (ClientHandlerTS),
 * quest'ultimo saprà di dover anch'esso chiudere il socket lato server.
 * NB: quando il server viene chiuso, tutti i client rimasti connessi vengono disconnessi ma il processo client non viene terminato;
 * quando verrà inserito un comando dall'utente client, allora il comando non verrà accettato perchà il server e' chiuso e l'applicazione verra' terminata
 */

public class ClientTS {
    public static void main(String args[]) {

        /*
         * For debug reason:
         */

        String host = "localhost";
        int port = 3500;

        /*
         * // All'avvio del client passare indirizzo e porta del server a cui
         * connettersi
         * if (args.length < 2) {
         * System.err.
         * println("Errore, avviare nel seguente modo: java TSClient <host> <port>");
         * return;
         * }
         * 
         * // estraiamo i due argomenti da linea di comando
         * String host = args[0];
         * int port = Integer.parseInt(args[1]);
         */
        try {
            // Ci connettiamo all'indirizzo e alla porta forniti
            Socket s = new Socket(host, port);
            System.out.println("Benvenuto/a in TextShare!\n"
                    + "Puoi eseguire uno dei seguenti comandi:\n"
                    + "- \"list\": ottieni una lista di tutti i file presenti sul server\n"
                    + "- \"create\": seguito da <nome>.txt, crea un nuovo file chiamato <nome>\n"
                    + "- \"read\": seguito da <nome>.txt, apre il file in modalità lettura\n"
                    + "   - \":close\": se sei in modalità lettura, chiude la sessione\n"
                    + "- \"edit\": seguito da <nome>.txt, apre il file in modalità scrittura\n"
                    + "   - \":backspace\": se sei in modalità scrittura, elimina l'ultima riga del file\n"
                    + "   - \":close\": se sei in modalità scrittura, chiude la sessione\n"
                    + "   - ogni comando che non inizia con \":\" viene interpretato come una riga di testo che viene aggiunta in coda al file\n"
                    + "- \"rename\": seguito da <nome_file_da _rinominare>.txt e da <nuovo_nome>.txt, rinomina il file\n"
                    + "-\"delete\": seguito da <nome_file_da_eliminare>.txt, elimita tale file\n"
                    + "-\"quit\": arresta il client\n"
                    + "\nInserire un comando:");

            Scanner fromServer = new Scanner(s.getInputStream()); // Wrapper per ricevere dal server
            PrintWriter toServer = new PrintWriter(s.getOutputStream(), true); // Wrapper per inviare al server

            Scanner userInput = new Scanner(System.in); // Scanner per leggere i comandi da terminale

            // Ciclo di vita del client
            while (true) {
                String request = userInput.nextLine(); // Lettura della richiesta dell'utente
                toServer.println(request); // inoltro della richista al server

                // TODO-Nota: Se si è in Sessione scrittura o Lettura, non deve valere questo
                // if!
                if (request.equalsIgnoreCase("quit")) {
                    // Se l'utente scrive quit, interrompe il ciclo
                    break;
                } else {
                    // TODO: Delegare ad un altro Thread l'input da lato Server, così
                    // da non rendere bloccante il thread principale.
                    // Visto che hashNextLine ritorna sempre true, rimanendo in attesa di un input.
                    // while (fromServer.hasNextLine()) {
                    String response = fromServer.nextLine(); // Lettura della risposta del server
                    System.out.println(response); // stampa della risposta
                    // }
                }
            }

            // Al termine del ciclo di vita, viene chiusa la connessione e lo scanner
            System.out.println("Sto chiudendo il socket lato client");
            s.close();
            userInput.close();
            System.out.println("Disconessione avvenuta con successo");

        } catch (IOException e) {
            System.err.println(
                    "Errore durante operazione I/O: connessione al server non riuscita. Controllare indirizzo e porta");
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            /*
             * Questa eccezione verra' sollevata nel momento in cui La lettura della
             * richiesta dell'utente
             * non sarà possibile per via della chiusura del socket da parte dell'altro
             * thread (vedi ServerHandler)
             */
            System.out.println(
                    "Comando non accettato, ii server e' stato chiuso.\n\nChiusura client in corso...\nClient chiuso");// Viene
                                                                                                                       // sollevata
                                                                                                                       // dalla
                                                                                                                       // riga
                                                                                                                       // 34
            return;
        }

    }
}