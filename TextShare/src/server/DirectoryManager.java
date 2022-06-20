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
            // si può cancellare in sicurezza perchè nessuno sta scrivendo o leggendo
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
        /*
         * Nota: Cosa succede se 2 thread tentano di rinominare 2 file diversi
         * con uno stesso nome, nessuno stesso istante?
         * Esempio: FileA --> testo.txt, al contempo FileB --> testo.txt...
         * Oppure: Utente A crea file testo.txt, nel mentre un utente B rinomina il file
         * in testo.txt
         */
        File oldName = new File(directory.getPath(), filename);
        File newName = new File(directory.getPath(), splittedCom[2]);
        if (oldName.renameTo(newName)) {
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
        if (concurrentHM.put(filePath, fh) != null) {
            // Nota: Se arriva qua --> C'è stato qualche errore, perchè è stata fatta una
            // sostituzione invece che un inserimento, ovvero, ci stava già un FileHandler
            // con quel valore.
        }
        return fh;
    }
}
