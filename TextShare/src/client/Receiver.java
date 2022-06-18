package client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

//Receiver implementa la logica di ricezione di messaggi dall'interlocutore e la loro stampa su terminale
public class Receiver implements Runnable {

    private Socket s;
    private Thread senderThread;
    private String Identifier;

    public Receiver(Socket s, Thread t) {
        this.s = s;
        this.senderThread = t;
    }

    @Override
    public void run() {
        try {
            Scanner fromOther = new Scanner(s.getInputStream());

            // Il primo messaggio in arrivo dal Server dovrebbe essere l'Identificatore.
            this.Identifier = fromOther.nextLine();

            while (fromOther.hasNext()) {

                String message = fromOther.nextLine();
                /*
                 * Simulazione di un Ritardo di ricezione:
                 * 
                 * try {
                 * Thread.sleep(1000); // 1 secondo
                 * } catch (Exception ex) {
                 * System.err.println(ex.getMessage());
                 * }
                 */

                // Controllo di eventuali Codici stabiliti.
                // BUG: In uno sfortunato evento in cui il testo di un file contiene uno di
                // questi codici, fa scattare questo if e quindi causa un bug.
                if (message.startsWith(this.Identifier)) {
                    if (message.endsWith("101")) {
                        // per ricevere questo codice, significa che il Client ha fatto un read
                        // quindi attende di ricevere tutto il testo del file.
                        synchronized (senderThread) {
                            // per entrare in questo pezzo di codice il Thread deve acquisire il Lock
                            // dell'istanza senderThread, che era stato inizialmente preso quando l'utente
                            // ha inviato un comando e poi rilasciato quando è andato in wait().

                            // nota: Notify() non funziona visto che, nonostante sia un Thread, al suo
                            // interno ci sono dei sottoThread.
                            senderThread.notifyAll();
                        }
                    }else if(message.endsWith("503")){
                        // Chiusura Connessione Client - Server
                        break;
                    }

                } else {
                    System.out.println(message);
                }
            }
            fromOther.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            // Qualunque cosa succeda, interrompere il senderThread.
            senderThread.interrupt();
            System.out.println("Connessione col Server è stato chiuso!");
            System.out.println("Chiusura del Receiver.");
            System.out.println("Invia qualcosa per chiudere il Sender:");
        }
    }

}
