package server;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import error.IncorrectFileException;

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

    public boolean create(String Filename) throws IOException, IncorrectFileException {

    	File f = new File(Filename);
    	if ((f.exists()) && (f.isFile())) {
    		//System.out.println("Test: File già esistente");
        	return false;
        } else {
        	f.createNewFile();
        	FileHandler fh = new FileHandler(directory.getAbsolutePath()+Filename);
        	concurrentHM.put(f.getAbsolutePath(), fh);
        	//System.out.println("Test: File creato");
        	return true;
        }
    	
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
