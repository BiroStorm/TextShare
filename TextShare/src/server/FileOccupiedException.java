package server;

/*
 * Exception riguardante un file che è in Lettura o Scrittura
 * e quindi non può essere rinominato, cancellato.
 */
public class FileOccupiedException extends Exception {
    public FileOccupiedException(String filename){
        super("C'è qualcuno che sta scrivendo o leggendo il file " + filename);
    }
}
