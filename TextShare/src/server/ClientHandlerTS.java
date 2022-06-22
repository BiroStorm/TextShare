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

                try {
                    if (commandType.equalsIgnoreCase("list")) {
                        commandList(toClient);
                    } else if (commandType.equalsIgnoreCase("create")) {
                        boolean created = dirManager.createNewFile(fileName);
                        if (created == true) {
                            toClient.println("File creato correttamente");
                        } else {
                            toClient.println("File già esistente: il file non è stato creato");
                        }
                    } else if (commandType.equalsIgnoreCase("read")) {

                        this.readSession(fileName, fromClient, toClient);

                    } else if (commandType.equalsIgnoreCase("edit")) {

                        this.editSession(fileName, fromClient, toClient);

                    } else if (commandType.equalsIgnoreCase("rename")) {
                        if(splittedCom.length != 3){
                            toClient.println("Comando incompleto!\nIl comando è: rename  file1  file2");
                            continue;
                        }
                        if (dirManager.rename(fileName, splittedCom[2])) {
                            toClient.println("Il file è stato rinominato correttamente");
                        } else {
                            toClient.println("Il file " + splittedCom[2] + " esiste già!");
                        }

                    } else if (commandType.equalsIgnoreCase("delete")) {
                        if(splittedCom.length != 2){
                            toClient.println("Comando incompleto!\nIl comando è: delete nomefile");
                            continue;
                        }
                        if (dirManager.delete(fileName)) {
                            toClient.println("Il file è stato eliminato correttamente.");
                        } else {
                            toClient.println(
                                    "Non è stato possibile eliminare il file.\nQualcuno potrebbe star leggendo o scrivendo il file!");
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
                } catch (FileNotFoundException fe) {
                    toClient.println(fe.getMessage());
                } catch (FileOccupiedException e) {
                    toClient.println(e.getMessage());
                } catch (IOException e) {
                    System.out.println("Errore IOException" + e.getMessage());
                    e.printStackTrace();
                    toClient.println(e.getMessage());
                    ;
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
            toClient.println("Nome File \t Ultima Modifica \t Lettura \t Scrittura");
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

    private void readSession(String filename, Scanner input, PrintWriter output) {
        try {
            FileHandler fh = dirManager.getFileHandler(filename);
            output.println("Attesa inizio Sessione di Scrittura...");
            try {
                String testo = fh.OpenReadSession();
                output.println("Avviata Sessione di Lettura per il file " + filename);
                output.println(testo);
                output.flush();
                output.println(this.IDENTIFIER + "101");
                output.println("[Per uscire dalla modalità lettura inviare :close]");
                while (!input.nextLine().equalsIgnoreCase(":close")) {
                    // do nothing...
                    output.println("[Per uscire dalla modalità lettura inviare :close]");
                }
            } catch (FileNotFoundException e) {
                output.println("Errore: " + e.getMessage());
                output.println(this.IDENTIFIER + "101"); // unlock del terminale in scrittura.
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

    private void editSession(String filename, Scanner input, PrintWriter output) {
        try {
            FileHandler fh = dirManager.getFileHandler(filename);
            output.println("Attesa inizio Sessione di Scrittura...");
            try {
                fh.OpenWriteSession();
                output.println("Avviata Sessione di Scrittura per il file " + filename);
                output.println("[Per uscire dalla modalità scrittura inviare :close]");
                output.println("[Per eliminare l'ultima riga del file inviare :backspace]");

                while (true) {
                    String linea = input.nextLine();
                    // non si usa switch perchè è necessario fare Break;
                    if (linea.equalsIgnoreCase(":backspace")) {
                        // Rimuove l'ultima riga del file:
                        if(fh.deleteLastRow()){
                            output.println("Ultima riga eliminata correttamente.");
                        }
                    } else if (linea.equalsIgnoreCase(":close")) {
                        break;
                    } else {
                        // Nuova riga su File
                        fh.Write(linea);
                    }
                }
            } catch (FileNotFoundException e) {
                // System.out.println("Errore nella Lettura di un file: " + e.getMessage());
                output.println("Errore: " + e.getMessage());
                output.println(this.IDENTIFIER + "101"); // unlock del terminale in scrittura.
            } catch (IOException e) {
                output.println(e.getMessage());
            } finally {
                fh.CloseWriteSession();
                output.println("Sessione di scrittura Terminata.");
            }

        } catch (FileNotFoundException e) {
            output.println(e.getMessage());
        } catch (IOException e) {
            output.println(e.getMessage());
        }
    }

}
