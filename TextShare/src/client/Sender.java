package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Sender implements Runnable {

    private Socket s;

    public Sender(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            Scanner fromConsole = new Scanner(System.in);
            PrintWriter toOther = new PrintWriter(s.getOutputStream(), true);
            while (true) {
                String message = fromConsole.nextLine();
                if (Thread.interrupted()) {
                    toOther.println("quit");
                    break;
                }
                // questo blocco deve essere messo in un Syncronized perchè deve essere eseguito
                // in maniera "atomica" rispetto al Receiver, che dovrà fare un Notify() SOLO
                // dopo che questo thread sarà stato messo in wait.
                // Visto che il Receiver ha l'istanza del Thread e NON della oggetto, bisogna
                // che usiamo il Thread come Monitor Object.
                synchronized (Thread.currentThread()) {
                    toOther.println(message);
                    if (message.startsWith("read ")) {
                        Thread.currentThread().wait();
                    } else if (message.equals("quit")) {
                        break;
                    }
                }
            }

            // Prima di terminare
            fromConsole.close();
            System.out.println("Closed (sender)");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
