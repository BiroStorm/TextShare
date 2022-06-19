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

    public int delete(String filename) {
		File f = new File(directory.getPath(), filename);
    	
    	if (!f.exists()) {
    		return 0;
		}  else {
			if (f.delete()) {
				if (concurrentHM.containsKey(f.getPath())) {
					concurrentHM.remove(f.getPath());
				}

				return 2;
			} else {
				return 1;
			}
		}
	}

    public FileHandler edit(String fileName) throws Exception{
        // TODO: Modifica e scrittura del file
        // Controlla che sia presente in concurrentHM
        // Se non è presente allora controlla se il file esiste
        // Se esiste allora crea nuova istanza di FileHandler
        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" +"fileName");
        if(fh == null){
            // controlla se il file esiste
            File f = new File(directory.getPath(), fileName);
            if (!f.exists()) {
                // non è stato caricato ancora sul CHM, quindi lo carichiamo
                fh = this.InsertIntoCHM(directory.getPath() + "\\" + fileName);
            } else {
                //il file non esiste
                return null;
            }

        }
        return fh;
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

    private FileHandler InsertIntoCHM(String filePath) throws Exception {
        FileHandler fh = new FileHandler(filePath);
        concurrentHM.put(filePath, fh);
        return fh;
    }
}
