package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/*
 * ServerHandler prende come parametri il ServerSocket, la lista contenente i Socket per la connessione con i client
 * e l'istanza di TextManager che contiene le informazioni utili al comando info.
 * Con il comando quit viene chiuso il ServerSocekt e, uno ad uno, tutti i socket delle connessioni ancora aperte con i client.
 */
public class ServerHandlerTS implements Runnable {

	ServerSocket listener;
	ArrayList<Socket> socketList = new ArrayList<>();
	DirectoryManager dirManager;

	public ServerHandlerTS(ServerSocket listener, ArrayList<Socket> socketList, DirectoryManager dirManager) {
		this.listener = listener;
		this.socketList = socketList;
		this.dirManager = dirManager;
	}

	@Override
	public void run() {

		Scanner userInput = new Scanner(System.in); // Scanner per leggere i comandi da terminale

		while (true) {
			String command = userInput.nextLine();

			if (command.equalsIgnoreCase("info")) {

				int totalReadingUsers = 0;
				int totalWritingUsers = 0;

				File[] filesList = this.dirManager.getDirectory().listFiles();
				int nonFiles = 0;
				for (File f : filesList) {
					// Se è una Directory skip.
					if (f.isDirectory()){
						nonFiles++;
						continue;
					}
					// se il file e' in HashMap conto il numero di client connessi in lettura e in
					// scrittura, se il file non e' in HM allora significa 0 lettori e 0 scrittori.
					ConcurrentHashMap<String, FileManager> CHM = dirManager.getCHM();
					if (CHM.containsKey(f.getPath())) {
						FileManager fm = CHM.get(f.getPath());
						totalReadingUsers += fm.getReadingUsers();
						totalWritingUsers += fm.isSomeoneWriting() ? 1 : 0;
					}
				}
				// stampo le informazioni
				System.out.println("- Numero di file gestiti: " + (filesList.length - nonFiles) + "\n"
						+ "- Numero di client attualmente connessi in lettura: " + totalReadingUsers + "\n"
						+ "- Numero di client attualmente connessi in scrittura: " + totalWritingUsers);

			} else if (command.equalsIgnoreCase("quit")) {

				try {
					listener.close();// il server non accetta più connessioni ma i ClientHandler continuano a
										// funzionare.
					System.out.println("ServerSocket chiuso, il server non accetta più connessioni");
					for (Socket s : socketList) {
						s.close();// chiudo tutti i socket lato server cosi' da interrompere tutti i ClientHandler
									// ancora attivi
						System.out.println("Chiudo socket lato server, disconnetto relativo client");
					}
				} catch (IOException e) {
					System.err.println("Errore nella chiusura del server");
				}

				// chiudo lo scanner e interrompo l'applicazione
				userInput.close();
				System.out.println("SERVER CHIUSO");
				return;

			} else {
				System.out.println("Comando sconosciuto");
			}
		}

	}
}
