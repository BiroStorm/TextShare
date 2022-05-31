package textshare;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * ServerHandler prende come parametri il ServerSocket, la lista contenente i Socket per la connessione con i client
 * e l'istanza di TextManager che contiene le informazioni utili al comando info.
 * Con il comando quit viene chiuso il ServerSocekt e, uno ad uno, tutti i socket delle connessioni ancora aperte con i client.
 */
public class ServerHandlerTS implements Runnable {

	ServerSocket listener;
	ArrayList<Socket> socketList = new ArrayList<>();
	//TextManager textManager;
	
	public ServerHandlerTS (ServerSocket listener, ArrayList<Socket> socketList/*, TextManager textManager*/) {
		this.listener = listener;
		this.socketList = socketList;
	}
	
	@Override
	public void run() {
		
		Scanner userInput = new Scanner(System.in); //Scanner per leggere i comandi da terminale
		
		while (true) {
			String command = userInput.nextLine();
		
			//TODO: completare comando "info" stampando le informazioni vere e proprie
			if (command.equalsIgnoreCase("info")) {
				System.out.println("- Numero di file gestiti: \n"
						+ "- Numero di client attualmente connessi in lettura: \n"
						+ "- Numero di client attualmente connessi in scrittura: ");
			} else if (command.equalsIgnoreCase("quit")) {
				try {
					listener.close();//il server non accetta più connessioni ma i ClientHandler continuano a funzionare.
					System.out.println("ServerSocket chiuso, il server non accetta più connessioni");
					for (Socket s : socketList) {
						s.close();//chiudo tutti i socket lato server cosi' da interrompere tutti i ClientHandler ancora attivi
						System.out.println("Chiudo socket lato server, disconnetto relativo client");
					}
				} catch (IOException e) {
					System.err.println("Errore nella chiusura del server");
				}
						
				//chiudo lo scanner e interrompo l'applicazione
				userInput.close();
				System.out.println("SERVER CHIUSO");
				return;
			} else {
				System.out.println("Comando sconosciuto");
			}
		}
		
	}
}
