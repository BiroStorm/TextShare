package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * ClientHandler implementa la gestione della connessione con il suo client, '
 * il "server vero e proprio".
 * Prende il clientSocket per gestire la connessione con il client,
 * prende il TextManager che contiene i metodi per soddisfare le richieste del
 * client,
 * prende la lista di Socket cosi' da rimuovere il socket che gestisce dalla
 * socketList nel caso in cui il client richieda la disconnessione.
 */
public class ClientHandlerTS implements Runnable {

    private Socket socket;
    private ArrayList<Socket> socketList = new ArrayList<>();
    private TextManager textManager;

    public ClientHandlerTS(Socket socket, ArrayList<Socket> socketList, TextManager textManager) {
        this.socket = socket;
        this.socketList = socketList;
        this.textManager = textManager;
    }

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(socket.getInputStream()); // Wrapper per ricevere dal client
            PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true); // Wrapper per inviare al client

            // Ciclo di vita
            while (true) {
                String command = fromClient.nextLine(); // Lettura della richiesta del client

                if (command.equalsIgnoreCase("quit")) {
                    // Se il client chiude la connessione, fai lo stesso lato server
                    System.out.println("Sto chiudendo il socket lato server");
                    break;
                } else if (command.equalsIgnoreCase("edit")) {
                    // TODO: Modalità Scrittura

                } else if (command.equalsIgnoreCase("read")) {
                    // TODO: Modalità Lettura

                } else if (command.equalsIgnoreCase("create")) {
                    // TODO: Creazione di un File di testo

                } else {
                    // In caso di comando sconosciuto, notifica il client
                    toClient.println("Comando sconosciuto");
                }
            }

            // rimuove il socket dalla lista dei socket.
            for (Socket s : socketList) {
                if (this.socket.equals(s)) {
                    socketList.remove(s);
                    break;
                }
            }

            // chiude la connessione
            this.socket.close();
            System.out.println("Client disconnesso");

        } catch (IOException e) {
            System.err.println("Errore durante operazione I/O");
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            /*
             * Viene sollevata una NoSuchElementException quando il socket viene chiuso,
             * perche' fromClient.nextLine(), riga 28, non puo' piu' leggere
             * Visto che il socket e' chiuso, il ClientHandler puo' terminare
             */
            return;
        }

    }

}
