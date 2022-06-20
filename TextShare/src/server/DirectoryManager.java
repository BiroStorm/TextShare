package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public boolean createNewFile(String Filename) throws IOException {

        File f = new File(directory.getPath(), Filename);
        // il metodo createNewFile controlla già di per se se il file esiste o meno.
        if (f.createNewFile()) {
            this.InsertIntoCHM(f.getPath());
            return true;
        } else {
            return false;
        }

    }

    public boolean delete(String filename) throws FileNotFoundException {
        File f = new File(directory.getPath(), filename);
        if (!f.exists())
            throw new FileNotFoundException("Il File " + filename + " non esiste!");
        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" + filename);
        if (fh == null) {
            // si può cancellare in sicurezza:
            if (f.delete())
                return true;
            return false;
        }
        // Esisteva il FileHandler nel CHM, bisogna controllare i lettori e scrittori:
        if (fh.getisUserWriting() || fh.getReadingUsers() > 0)
            return false;
        concurrentHM.remove(f.getPath());
        return true;

    }

    public boolean rename(String filename, String[] splittedCom) throws FileNotFoundException {

        File oldName = new File(directory.getPath(), filename);
        File newName = new File(directory.getPath(), splittedCom[2]);

        boolean renamed = oldName.renameTo(newName);
        if (renamed == true) {
            concurrentHM.remove(oldName.getPath());
            this.InsertIntoCHM(newName.getPath());
            return true;
        } else {
            return false;
        }
    }

    public FileHandler getFileHandler(String filename) throws FileNotFoundException {
        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" + filename);
        if (fh == null) {
            // non è stato caricato ancora sul CHM, quindi lo carichiamo
            fh = this.InsertIntoCHM(directory.getPath() + "\\" + filename);
        }
        return fh;
    }

    private FileHandler InsertIntoCHM(String filePath) throws FileNotFoundException {
        FileHandler fh = new FileHandler(filePath);
        concurrentHM.put(filePath, fh);
        return fh;
    }
}
