package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

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
    private DirectoryManager dirManager;
    private final String IDENTIFIER;

    public ClientHandlerTS(Socket socket, ArrayList<Socket> socketList, DirectoryManager dirManager) {
        this.socket = socket;
        this.socketList = socketList;
        this.dirManager = dirManager;

        // genera un identificatore del Client.
        SecureRandom random = new SecureRandom();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        byte[] buffer = new byte[16];
        random.nextBytes(buffer);
        this.IDENTIFIER = encoder.encodeToString(buffer);
    }

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(socket.getInputStream()); // Wrapper per ricevere dal client
            PrintWriter toClient = new PrintWriter(socket.getOutputStream(), true); // Wrapper per inviare al client

            // Invio l'Identificatore al Client come prima comunicazione.
            System.out.println("Client " + this.IDENTIFIER + " in comunicazione...");
            toClient.println(this.IDENTIFIER);

            // Ciclo di vita
            while (true) {
                String command = fromClient.nextLine(); // Lettura della richiesta del client
                String[] splittedCom = command.split(" ");
                String commandType = splittedCom[0];
                String fileName = splittedCom.length > 1 ? splittedCom[1] : "";

                if (commandType.equalsIgnoreCase("list")) {
                    commandList(toClient);
                } else if (commandType.equalsIgnoreCase("create")) {
                    // Nota: Bisogna gestire la concorrenza sull'inserimento e creazione del file.
                    // dati 2 thread, se entrambi procedono con la creazione di un file con lo
                    // stesso nome
                    // anche se, il problema prodotto non è affatto un problema...

                    try {
                        boolean created = dirManager.createNewFile(fileName);
                        if (created == true) {
                            toClient.println("File creato correttamente");
                        } else {
                            toClient.println("File già esistente: il file non è stato creato");
                        }
                    } catch (IOException e) {
                        System.out.println("Errore nella Creazione del file: " + e.getMessage());
                        e.printStackTrace();
                    }

                } else if (commandType.equalsIgnoreCase("read")) {

                    this.gestioneLettura(fileName, fromClient, toClient);

                } else if (commandType.equalsIgnoreCase("edit")) {

                    this.editSession(fileName, fromClient, toClient);

                } else if (commandType.equalsIgnoreCase("rename")) {
                    // TODO: implementare comando rename
                    // usare splittedCom[2] come stringa per il nuovo nome da assegnare al file

                } else if (commandType.equalsIgnoreCase("delete")) {
                    try {
                        if (dirManager.delete(fileName)) {
                            toClient.println("Il file è stato eliminato correttamente.");
                        } else {
                            toClient.println("Per qualche motivo, non è stato possibile eliminare il file.");
                        }
                    } catch (FileNotFoundException fe) {
                        toClient.println(fe.getMessage());
                    }

                } else if (commandType.equalsIgnoreCase("quit")) {
                    // Se il client chiude la connessione, fai lo stesso lato server
                    toClient.println(this.IDENTIFIER + "503");
                    System.out.println("Client " + this.IDENTIFIER + " quitting...");
                    break;
                } else {
                    // In caso di comando sconosciuto, notifica il client
                    toClient.println("Comando sconosciuto");
                }
            }

            // rimuove il socket dalla lista dei socket.
            socketList.remove(this.socket);

            // chiude la connessione
            toClient.close();
            fromClient.close();
            this.socket.close();
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

    private void commandList(PrintWriter toClient) {

        File[] filesList = dirManager.getDirectory().listFiles();
        if (filesList.length == 0) {
            toClient.println("Non c'è nessun file");
        } else {
            DateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm");
            toClient.println("Lista dei file presenti sul server:");
            toClient.println("Nome File \t Ultima Modifica \t Scrittura \t Lettura");
            ConcurrentHashMap<String, FileHandler> CHM = dirManager.getCHM();
            for (File f : filesList) {
                if (f.isDirectory())
                    continue;
                toClient.print(f.getName() + "\t" + sdf.format(f.lastModified()) + "\t");
                // se se il file è in HashMap conto il numero di utenti in lettura/scrittura
                // altrimenti non esistono utenti in lettura/scrittura per quel file.
                if (CHM.containsKey(f.getPath())) {
                    FileHandler fh = CHM.get(f.getPath());

                    toClient.printf("\t%d \t %d\n", fh.getReadingUsers(), fh.getisUserWriting() ? 1 : 0);
                } else {
                    toClient.println("\t0 \t 0");
                }

            }
        }

    }

    private void gestioneLettura(String filename, Scanner input, PrintWriter output) {
        try {
            FileHandler fh = dirManager.read(filename);
            output.println("Attesa inizio Sessione di Scrittura...");
            try {
                String testo = fh.OpenReadSession();
                output.println("Avviata Sessione di Lettura per il file " + filename);
                output.println(testo);
                output.flush();
                output.println(this.IDENTIFIER + "101");
                output.println("\033[3mPer uscire dalla modalità scrittura inviare :close\033[0m");
                while (!input.nextLine().equalsIgnoreCase(":close")) {
                    // do nothing...
                    output.println("\033[3mPer uscire dalla modalità scrittura inviare :close\033[0m");
                }
            } finally {
                // Qualsiasi cosa accada dopo l'incremento (un crash del client)
                // comunque decrementa il counter del numero di client in lettura su questo file
                fh.CloseReadSession();
                output.println("Sessione di scrittura Terminata.");
            }

            /*
             * In questo caso, avviene una lettura completa di tutto il file.
             * Il caso migliore sarebbe quello di stampare riga per riga, o anche
             * tramite un input dell'utente (es. Invio).
             * In tale caso bisognerebbe modificare il OpenReadSession e creare un
             * nuovo metodo in FileHandler, come ad esempio Read() e farlo simile
             * alla procedura di Write.
             */
        } catch (FileNotFoundException e) {
            // System.out.println("Errore nella Lettura di un file: " + e.getMessage());
            output.println("Errore: " + e.getMessage());
            output.println(this.IDENTIFIER + "101"); // unlock del terminale in scrittura.
        } catch (IOException e) {
            output.println("Errore IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // metodo di gestione edit provvisorio
    private void editSession(String filename, Scanner input, PrintWriter output) {
        try {
            FileHandler fh = dirManager.edit(filename);
            output.println("Attesa inizio Sessione di Scrittura...");
            try {
                fh.OpenWriteSession();
                output.println("Avviata Sessione di Scrittura per il file " + filename);
                // serve per chiudere la sessione di scrittura sul file, se l'utente non
                // inserisce close il writer continua a scrivere su file il testo che l'utente
                // digita
                output.println(this.IDENTIFIER + "101");
                output.println("\033[3mPer uscire dalla modalità scrittura inviare :close\033[0m");
                while (!input.nextLine().equalsIgnoreCase(":close")) {
                    String linea = input.nextLine();
                    fh.Write(linea);
                }
            } finally {
                fh.CloseWriteSession();
                output.println("Sessione di scrittura Terminata.");
            }

        } catch (Exception e) {
            output.println("Il file " + filename + " non esiste!");
            output.println(this.IDENTIFIER + "101"); // unlock del terminale in scrittura.
        }
    }
    // metodo di gestione edit provvisorio

}
