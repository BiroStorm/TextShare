package server;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Contenitore dei metodi per gestire le operazioni sui file
 */
public class DirectoryManager {
	
	private File directory;
    // ConcurrentHashMap è un HashMap Thread-Safe, ha un Lock interno.
    private ConcurrentHashMap<String, FileHandler> concurrentHM;

    public DirectoryManager(File directory) {
    	this.directory = directory;
        this.concurrentHM = new ConcurrentHashMap<String, FileHandler>();
    }

    public void create(String Filename) {
        // TODO: Creazione del File
        // Controllo dell'esistenza del File
        // Creazione del File
        // Creazione del FileHandler relativo al file
        // Inserimento del FileHandler nel concurrentHM
    }

    public FileHandler edit(String fileName) {
        // TODO: Modifica e scrittura del file
        // Controlla che sia presente in concurrentHM
        // Se non è presente allora controlla se il file esiste
        // Se esiste allora crea nuova istanza di FileHandler

        return null;
    }

    public FileHandler read(String name) {
        // TODO: Lettura del file
        // Controlla che sia presente in concurrentHM
        // Se non è presente allora controlla se il file esiste
        // Se esiste allora crea nuova istanza di FileHandler

        return null;
    }
}
