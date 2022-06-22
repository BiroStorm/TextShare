package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Contenitore dei metodi per gestire le operazioni sulla Directory.
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
            return true;
        } else {
            return false;
        }
    }

    synchronized public boolean delete(String filename) throws FileNotFoundException, FileOccupiedException {
        File f = new File(directory.getPath(), filename);
        if (!f.exists())
            throw new FileNotFoundException("Il File " + filename + " non esiste!");
        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" + filename);
        if (fh == null) {
            // si può cancellare in sicurezza perchè nessuno sta scrivendo o leggendo
            if (f.delete())
                return true;
            return false;
        }
        // Esisteva il FileHandler nel CHM, bisogna controllare i lettori e scrittori:
        if (fh.getisUserWriting() || fh.getReadingUsers() > 0)
            throw new FileOccupiedException(filename);
        concurrentHM.remove(f.getPath());
        if (f.delete())
            return true;
        // ritorna falso quando se il file è aperto (in scrittura o lettura!)
        return false;
    }

    synchronized public boolean rename(String fileName, String newFileName)
            throws FileNotFoundException, FileOccupiedException {

        File oldFile = new File(directory.getPath(), fileName);
        File newFile = new File(directory.getPath(), newFileName);

        if (!oldFile.exists())
            throw new FileNotFoundException("Il File " + fileName + " non esiste!");
        if (newFile.exists()) return false;
        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" + fileName);
        if (fh != null) {
            if (fh.getisUserWriting() || fh.getReadingUsers() > 0)
                throw new FileOccupiedException(fileName);
        }

        if (oldFile.renameTo(newFile)) {
            concurrentHM.remove(oldFile.getPath());
            return true;
        } else {
            return false;
        }
    }

    synchronized public FileHandler getFileHandler(String filename) throws FileNotFoundException {

        FileHandler fh = concurrentHM.get(directory.getPath() + "\\" + filename);

        if (fh == null) {
            fh = new FileHandler(directory.getPath() + "\\" + filename);
            concurrentHM.put(directory.getPath() + "\\" + filename, fh);
        }
        return fh;
    }
}
