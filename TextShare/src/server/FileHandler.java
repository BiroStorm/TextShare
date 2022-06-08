package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import error.IncorrectFileException;

/**
 * Gestisce il singolo file dato il path in input (senza ".txt")
 * e usa il ReadWriteLock per gestire i problemi del Reader&Writer.
 * 
 * Ogni operazione (lettura, scrittura) comporta la riapertura del
 * file, così da chiudere il canale (BufferedWriter/Reader) sempre.
 */
public class FileHandler {

    /**
     * In caso il file non esista o il path porta ad una directory
     * ritorna un IncorrectFileException.
     * 
     * @param filePath
     * @throws IncorrectFileException
     */
    private File file;
    private ReentrantReadWriteLock lock;
    private BufferedWriter bw;

    public FileHandler(String filePath) throws IncorrectFileException {
        File f = new File(filePath + ".txt");
        if (!f.exists() || f.isDirectory()) {
            // Se è una directory o il file non esiste lanciamo l'eccezione.
            throw new IncorrectFileException("Il file non esiste o è una directory! File: " + filePath);
        }
        this.file = f;
        this.lock = new ReentrantReadWriteLock();

    }

    /**
     * Apre una "Sessione" in Lettura
     * Per la chiusura della Sessione usare CloseReadSession();
     * 
     * @throws IOException
     * @see {@link #CloseReadSession()}
     */
    public void OpenReadSession() throws IOException {
        // Continua se il Lock in lettura è disponibile:
        this.lock.readLock().lock();
        // Sessione di Lettura:
        BufferedReader br = new BufferedReader(new FileReader(file));

        // TODO: Completare la lettura del file.

        // Visto che nel Progetto non viene implementata nessuna azione oltre
        // a quella della chiusura, possiamo fare la chiusura dello stream
        // direttamente in questo metodo (a differenza della scrittura)
        br.close();

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
    public void OpenWriteSession() throws IOException {
        // Prendiamo il Lock in scrittura.
        this.lock.writeLock().lock();
        this.bw = new BufferedWriter(new FileWriter(file));
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
                bw.write(line);
            }
            this.bw.flush();
        } else {
            // Se arriva qua significa che un Thread ha provato a scrivere
            // Senza avere prima acquisito il Lock in scrittura!
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
}
