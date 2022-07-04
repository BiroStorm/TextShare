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
    private ConcurrentHashMap<String, FileManager> concurrentHM;

    public DirectoryManager(File directory) {
        this.directory = directory;
        this.concurrentHM = new ConcurrentHashMap<String, FileManager>();
    }

    public File getDirectory() {
        return this.directory;
    }

    public ConcurrentHashMap<String, FileManager> getCHM() {
        return this.concurrentHM;
    }

    public boolean createNewFile(String Filename) throws IOException {

        File f = new File(directory.getPath(), Filename);
        // il metodo createNewFile controlla già di per se se il file esiste o meno.
        return f.createNewFile();
    }

    synchronized public boolean delete(String filename) throws FileNotFoundException, FileOccupiedException {
        File f = new File(directory.getPath(), filename);
        if (!f.exists())
            throw new FileNotFoundException("Il File " + filename + " non esiste!");
        FileManager fm = concurrentHM.get(directory.getPath() + "\\" + filename);
        if (fm == null) {
            // si può cancellare in sicurezza perchè nessuno sta scrivendo o leggendo
            return f.delete();
        }

        if (fm.isSafeHandling()) {
            // se riesce ad acquisire il lock per il critical section
            try {
                if (fm.isSomeoneWriting() || fm.getReadingUsers() > 0)
                    throw new FileOccupiedException(filename);
                concurrentHM.remove(f.getPath());
                // ritorna falso quando se il file è aperto (in scrittura o lettura!)
                return f.delete();
            } finally {
                // in ogni caso unlocka il lock della critical section
                fm.unLockBlockingLock();
            }
        }
        // se arriva qua, significa che durante la Race Condition, un Thread è entrato
        // in scrittura
        return false;
    }

    synchronized public boolean rename(String fileName, String newFileName)
            throws FileNotFoundException, FileOccupiedException {

        File oldFile = new File(directory.getPath(), fileName);
        File newFile = new File(directory.getPath(), newFileName);

        if (!oldFile.exists())
            throw new FileNotFoundException("Il File " + fileName + " non esiste!");
        if (newFile.exists())
            throw new FileOccupiedException("Esiste già un file di nome " + newFileName);
        FileManager fm = concurrentHM.get(directory.getPath() + "\\" + fileName);
        if (fm == null) {
            return oldFile.renameTo(newFile);
        }
        // start critical section 
        if (fm.isSafeHandling()) {
            try {
                if (fm.isSomeoneWriting() || fm.getReadingUsers() > 0)
                    throw new FileOccupiedException(
                            "Sembra che un Client sia connesso in Scrittura o Lettura sul file " + fileName);
                if (oldFile.renameTo(newFile)) {
                    concurrentHM.remove(oldFile.getPath());
                    return true;
                }
                return false;
            } finally {
                fm.unLockBlockingLock();
            }
        }
        // END CRITICAL SECTION
        throw new FileOccupiedException("Sembra che un Client sia connesso in Scrittura!");

    }

    synchronized public FileManager getFileManager(String filename) throws FileNotFoundException {

        FileManager fm = concurrentHM.get(directory.getPath() + "\\" + filename);

        if (fm == null) {
            fm = new FileManager(directory.getPath() + "\\" + filename);
            concurrentHM.put(directory.getPath() + "\\" + filename, fm);
        }
        return fm;
    }
}
