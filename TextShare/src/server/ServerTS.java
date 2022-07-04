package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * TSServer si occupa solo di accettare nuove connessioni, lascia la gestione a
 * thread creati appositamente
 * per il client appena connesso (ClientHandler); per gestire i comandi di info
 * e quit, crea un apposito thread (ServerHandler)
 */
public class ServerTS {

    public static void main(String args[]) {

        if (args.length < 2) {
            System.err.println("Errore, avviare nel seguente modo: java TSServer <path> <port>");
            return;
        }
        String dirPath = args[0];
        int port = Integer.parseInt(args[1]);// tra 1024 e 65535

        // check del path
        File directory = new File(dirPath);
        if (!(directory.exists()) || !(directory.isDirectory())) {
            System.err.println("Il path inserito non esiste");
            return;
        }

        try {
            ServerSocket listener = new ServerSocket(port); // si mette in ascolto creando un'istanza di ServerSocekt,
                                                            // puo' sollevare IOException
            System.out.println("Benvenuto/a in TextShare! \n\n"
                    + "Il Server è attivo. Puoi eseguire uno dei seguenti comandi:\n"
                    + "- \"info\": ottieni informazioni sul numero di file gestiti, client attualmente connessi in lettura e"
                    + "client attualmente connessi in scrittura\n"
                    + "- \"quit\": disconnette eventuali client connessi e chiude il server");

            DirectoryManager dirManager = new DirectoryManager(directory);
            ArrayList<Socket> socketList = new ArrayList<>();

            Thread serverHandlerThread = new Thread(new ServerHandlerTS(listener, socketList, dirManager));
            serverHandlerThread.start();

            // Ciclo di vita del thread principale del server, non fa altro che accettare
            // nuove connessioni
            System.out.println("In ascolto...");
            while (true) {
                Socket s = listener.accept(); // Puo' sollevare IOException

                // Lasica la gestione a un thread dedicato
                Thread clientHandlerThread = new Thread(new ClientHandlerTS(s, socketList, dirManager));
                socketList.add(s);// aggiunge il socket appena creato alla lista di socket da chiudere in caso di
                                  // quit (vedi ServerHandler)
                clientHandlerThread.start();
                // Si rimette in ascolto di altre richieste di connessione

            }

        } catch (IOException e) {
            /*
             * In caso di chiusura del ServerSocket "listener" da parte di ServerHandler,
             * listener.accept() e' ancora in esecuzione ma,
             * dato che viene chiuso inaspettatamente, solleva l'eccezione. La gestiamo
             * semplicemente chiudendo l'app.
             * NB: anche la creazione di listener può sollevare la stessa
             * eccezione, anche in questo caso chiudiamo l'app
             * perchè senza ServerSocket non possiamo fare nulla.
             */
            return;
        }

    }
}
