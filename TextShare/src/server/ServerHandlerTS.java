package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	DirectoryManager dirManager;;
	
	int totalReadingUsers = 0;
	int totalWritingUsers = 0;
	
	public ServerHandlerTS (ServerSocket listener, ArrayList<Socket> socketList, DirectoryManager dirManager) {
		this.listener = listener;
		this.socketList = socketList;
		this.dirManager = dirManager;
	}
	
	@Override
	public void run() {
		
		Scanner userInput = new Scanner(System.in); //Scanner per leggere i comandi da terminale
		
		while (true) {
			String command = userInput.nextLine();
		
			//TODO: completare comando "info" stampando le informazioni vere e proprie
			if (command.equalsIgnoreCase("info")) {
				/*
				 * FIXME: stesso problema di list in ClientHandlerTS, i file non hanno associato un loro FileHandler
				 */
				// conto il numero di client connessi in lettura e in scrittura
				File[] filesList = this.dirManager.getDirectory().listFiles();
        		for (File f : filesList) {
        			FileHandler fh = dirManager.getCHM().get(f.getPath());
        			this.totalReadingUsers += fh.getReadingUsers();
        			if (fh.getUserIsWriting())
        				this.totalWritingUsers += 1;
        		}
        		// stampo le informazioni
				System.out.println("- Numero di file gestiti: " + filesList.length + "\n"
						+ "- Numero di client attualmente connessi in lettura: " + this.totalReadingUsers + "\n"
						+ "- Numero di client attualmente connessi in scrittura: "  + this.totalWritingUsers);
				// setto a 0 cos� che al prossimo comando "list" non si vadano a sommare due volte gli stessi file.
				this.totalReadingUsers = 0;
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
