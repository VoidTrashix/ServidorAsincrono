
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Principal {

   private static Map<String, PrintWriter> perfiles= new HashMap<>();
   
    public static void main(String[] args) throws Exception {
        System.err.println("El servidor del chat está iniciando...");
        ExecutorService pool= Executors.newFixedThreadPool(500);
        try(ServerSocket listener= new ServerSocket(59001)){
            while(true){
                pool.execute(new Handler(listener.accept()));
            }
        }catch (Exception e){}
    }
    
    private static class Handler implements Runnable{
        private String nombre;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        
        public Handler(Socket socket){
            this.socket= socket;
        }
        
        public void run(){
            try{
                in= new Scanner(socket.getInputStream());
                out= new PrintWriter(socket.getOutputStream(), true);
                
                while (true) {                    
                    out.println("SUBMITNAME");
                    nombre= in.nextLine();
                    if(nombre==null || nombre.toLowerCase().startsWith("quit") || nombre.length()<4){
                        continue;
                    }
                    synchronized(perfiles){
                        if(!perfiles.containsKey(nombre)){
                                                        
                            out.println("NAMEACCEPTED " + nombre);
                                         
                            for(PrintWriter writer : perfiles.values()){
                                writer.println("MESSAGE " + nombre + " se ha unido");
                            }
                            
                            perfiles.put(nombre, out);
                            break;
                        }
                    }
                }
                
                while (true) {                    
                    String input= in.nextLine();
                    if(input.startsWith("/")){
                    if(input.toLowerCase().startsWith("/quit")){
                        return;
                    }else{
                        try{
                        int separador= input.substring(1).indexOf(" ");
                        String destinatario =  input.substring(1, separador + 1);
                        String mensaje =  input.substring(1).substring(separador + 1);
                                               
                        perfiles.get(destinatario).println("MESSAGE " + nombre + " ---> " + destinatario + ": " + mensaje);
                        perfiles.get(nombre).println("MESSAGE " + nombre + " ---> " + destinatario + ": " + mensaje);
                        }catch (Exception e){System.err.println(e);}
                    }
                }else{
                        for(PrintWriter writer : perfiles.values()){
                            writer.println("MESSAGE " + nombre + ": " + input);
                        }
                    }
                }
                
            } catch (Exception e){
                System.err.println(e);
            } finally {
                if(out!=null && nombre!=null){
                    System.err.println(nombre + " se ha salido");
                    perfiles.remove(nombre, out);
                    for(PrintWriter writer : perfiles.values()){
                        writer.println("MESSAGE " + nombre + " se ha ido");
                    }
                }
                try{ socket.close(); } catch (IOException e){}
            }
        }
    }
    
}
