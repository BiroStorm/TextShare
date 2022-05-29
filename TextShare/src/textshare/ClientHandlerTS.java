package textshare;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

//ClientHandler gestisce la connessione con il client
public class ClientHandlerTS implements Runnable {

    private Socket s;
    private TextManager textManager;

    public ClientHandlerTS(Socket s, TextManager textManager) {
        this.s = s;
        this.textManager = textManager;
    } 

    @Override
    public void run() {
        try {
            Scanner fromClient = new Scanner(s.getInputStream()); //Canale di ricezione dal client
            PrintWriter toClient = new PrintWriter(s.getOutputStream(), true); //Canale di invio verso il client

            //Ciclo di vita
            while (true) {
                String command = fromClient.nextLine(); //Lettura della richiesta del client

                //TODO: comandi client
                if (command.equalsIgnoreCase("quit")) {
                	break;
                } else {
                	System.out.println("Comando sconosciuto");
                }
            }

            //chiude la connessione
            s.close();
            System.out.println("Client disconnesso");

        } catch (IOException e) {
            System.err.println("Error during I/O operation:");
            e.printStackTrace();
        }
        
    }
    
}
