package server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
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
    
    public File getDirectory() {
    	return this.directory;
    }
    
    public ConcurrentHashMap<String, FileHandler> getCHM() {
    	return this.concurrentHM;
    }

    public boolean createNewFile(String Filename) throws Exception {

        File f = new File(directory.getPath(), Filename);
        // il metodo createNewFile controlla già di per se se il file esiste o meno.
        if (f.createNewFile()) {
            this.InsertIntoCHM(f.getPath());
            return true;
        } else {
            return false;
        }

    }

    public FileHandler edit(String fileName) {
        // TODO: Modifica e scrittura del file
        // Controlla che sia presente in concurrentHM
        // Se non è presente allora controlla se il file esiste
        // Se esiste allora crea nuova istanza di FileHandler

        return null;
    }

    public FileHandler read(String filename) throws Exception {
        // TODO: Lettura del file
        // Controlla che sia presente in concurrentHM
        // Se non è presente allora controlla se il file esiste
        // Se esiste allora crea nuova istanza di FileHandler
        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" +"filename");
        
        if(fh == null){
            // non è stato caricato ancora sul CHM, quindi lo carichiamo
            fh = this.InsertIntoCHM(directory.getPath() + "\\" + filename);
        }
        return fh;

    }
    
    /*
    public ArrayList<FileHandler> getFileHandlerList() {
    	// Prende una Collection di FileHandler dalla HashMap
        Collection<FileHandler> values = concurrentHM.values();
  
        // Crea un ArrayList di FileHandler
        ArrayList<FileHandler> fhList = new ArrayList<>(values);
        
        return fhList;
    }
    */

    private FileHandler InsertIntoCHM(String filePath) throws Exception {
        FileHandler fh = new FileHandler(filePath);
        concurrentHM.put(filePath, fh);
        return fh;
    }
}
