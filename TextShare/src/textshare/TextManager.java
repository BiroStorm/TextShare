package textshare;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.locks.ReentrantLock;
//import java.util.Map;
//import java.util.HashMap;
import java.util.Scanner;
import java.io.File;  
import java.io.FileNotFoundException;
/*
 * Contenitore dei metodi per gestire le operazioni sui file
 */
public class TextManager {

    /*questo serve per implementare un lock per ogni file e sarà sufficente generare 
    un lock nel momento in cui viene richiesto l'editing di un file */
    // Map<FileWriter, ReentrantLock> map = new HashMap<FileWriter, ReentrantLock>(); 
    
    ReentrantLock lockEdit = new ReentrantLock();
    
    public void create(String Filename){
        try {
            FileWriter file = new FileWriter(Filename + ".txt");
            BufferedWriter output = new BufferedWriter(file);
            output.close();
            System.out.println("File creato con successo!");
          }
      
          catch (Exception e) {
            e.getStackTrace();
          }
    }

      //questo metodo ha concorrenza, un solo utente per volta può editare.
      public boolean edit(String name, String data){
        if(! lockEdit.isLocked()){
            lockEdit.lock(); //il thread acquisisce il lock
            try {
                FileWriter file = new FileWriter("output.txt");
                BufferedWriter output = new BufferedWriter(file);
                output.write(data);
                // Closes the writer
                output.close();
            }catch (Exception e) {
                e.getStackTrace();
            }finally{
                lockEdit.unlock(); //il thread rilascia il lock
            }
            return true;
        }else{
            System.out.print("Devi attendere che un lock si liberi");
            return false;
        }
    }

    public String read(String name){
        if(lockEdit.isLocked()){
            return "False";
        }else if(! lockEdit.isLocked()) {
            try {
                File myObj = new File(name + ".txt");
                Scanner myReader = new Scanner(myObj);
                String data = "";
                while (myReader.hasNextLine()) {
                  data =  data + "\n" + myReader.nextLine();
                }
                myReader.close();
                return data;
              } catch (FileNotFoundException e) {
                return "An error occurred.";
              }
            }
        return "";
    }

    //ritorna un boleano a seconda se il thread che lo richiede è il possessore del lock o meno
    public boolean owner(){
        return lockEdit.isHeldByCurrentThread();
    }


}
