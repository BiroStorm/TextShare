package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Gestisce il singolo file dato il path in input (senza ".txt")
 * e usa il ReadWriteLock per gestire i problemi del Reader&Writer.
 * 
 * Ogni operazione (lettura, scrittura) comporta la riapertura del
 * file, così da chiudere il canale (BufferedWriter/Reader) sempre.
 */
public class FileManager {

    private File file;
    private ReentrantReadWriteLock lock;
    private BufferedWriter bw;
    private Lock blockingLock;

    /**
     * In caso il file non esista o il path porta ad una directory
     * ritorna un FileNotFoundException.
     * 
     * @param filePath
     * @throws FileNotFoundException
     */
    public FileManager(String filePath) throws FileNotFoundException {
        File f = new File(filePath);

        if (!f.exists() || f.isDirectory()) {
            // Se è una directory o il file non esiste lanciamo l'eccezione.
            throw new FileNotFoundException("Il file non esiste o è una directory! File: " + filePath);
        }
        this.file = f;
        this.lock = new ReentrantReadWriteLock();
        this.blockingLock = new ReentrantLock();
    }

    /**
     * Apre una "Sessione" in Lettura
     * Per la chiusura della Sessione usare CloseReadSession();
     * 
     * @throws IOException
     * @see {@link #CloseReadSession()}
     */
    public String OpenReadSession() throws IOException, FileNotFoundException {

        // Continua se il Lock in lettura è disponibile:
        this.lock.readLock().lock();
        // Inizio Sessione di Lettura
        BufferedReader br = new BufferedReader(new FileReader(file));
        String testo = "";
        while (br.ready()) {
            testo += br.readLine() + "\n";
        }

        // Visto che nel Progetto non viene implementata nessuna azione oltre
        // a quella della chiusura, possiamo fare la chiusura dello stream
        // direttamente in questo metodo (a differenza della scrittura)
        br.close();
        return testo;

        /*
         * sarebbe opportuno rilasciare infondo il Lock, ma lo si
         * delega a CloseReadSession() visto che deve essere il Client
         * a dire quando vuole uscire dalla sessione di lettura.
         * 
         * Quindi non è solo un "Stampa a video il contenuto del file"!
         */
    }

    /**
     * Chiusura della Sessione in Lettura.
     */
    public void CloseReadSession() {
        this.lock.readLock().unlock();
    }

    /**
     * Apre una "Sessione" in Scrittura.
     * Per chiudere la sessione usare CloseWriteSession();
     * 
     * @throws IOException
     * @see {@link #CloseWriteSession()}
     */
    public void OpenWriteSession() throws IOException, FileNotFoundException {

        this.blockingLock.lock();
        // START CRITICAL SECTION

        // Prendiamo il Lock in scrittura.
        this.lock.writeLock().lock();

        if (!this.file.exists())
            throw new FileNotFoundException(
                    "Il File non risulta più accessibile. Controllare che non sia stato cancellato o spostato!");

        // END CRITICAL SECTION
        this.blockingLock.unlock();
        this.bw = new BufferedWriter(new FileWriter(file, true));
    }

    /**
     * Scrive la Stringa in input sul file.
     * Solamente se il Thread ha già acquisito il Lock in Scrittura.
     * 
     * @param line
     * @throws IOException
     */
    public void Write(String line) throws IOException {

        if (this.lock.writeLock().isHeldByCurrentThread()) {
            if (line.isEmpty()) {
                this.bw.newLine();
            } else {
                this.bw.write(line + "\n");
            }
            this.bw.flush();
        } else {
            // Se arriva qua significa che un Thread ha provato a scrivere
            // Senza avere prima acquisito il Lock in scrittura!
            throw new IOException("Vietato modificare il file se non si ha il Lock in scrittura!");
        }
    }

    /**
     * Elimina l'ultima riga del file.
     * 
     * @throws IOException
     */
    public boolean deleteLastRow() throws IOException {
        if (this.lock.writeLock().isHeldByCurrentThread()) {
            this.bw.close();
            // leggo il file
            BufferedReader br = new BufferedReader(new FileReader(file));
            String testo = "";
            String lastrow = "";
            while (br.ready()) {
                testo += lastrow;
                lastrow = br.readLine() + "\n";
            }
            br.close();
            this.bw = new BufferedWriter(new FileWriter(file));
            this.bw.write(testo);
            this.bw.flush();
            return true;
        } else {
            throw new IOException("Vietato modificare il file se non si ha il Lock in scrittura!");
        }
    }

    /**
     * Chiusura di una Sessione in Scrittura.
     * Viene chiuso pure lo stream del BufferedWriter.
     * 
     * @throws IOException
     */
    public void CloseWriteSession() throws IOException {
        this.lock.writeLock().unlock();
        // è sempre buona norma chiudere il buffered quando si finisce.
        bw.close();
    }

    /**
     * Ritorna il numero di Thread che hanno il Lock in lettura.
     * 
     * @return
     */
    public int getReadingUsers() {
        return this.lock.getReadLockCount();
    }

    /**
     * Ritorna true se un Thread ha in possesso il Lock in scrittura
     * 
     * @return
     */
    public boolean isSomeoneWriting() {
        return this.lock.isWriteLocked();
    }

    /**
     * Ritorna un instanza del File associato a questo FileManager
     * 
     * @return File
     */
    public File getFile() {

        return this.file;
    }

    public boolean isSafeHandling() {
        return this.blockingLock.tryLock();
    }

    public void unLockBlockingLock() {
        this.blockingLock.unlock();
    }

}
