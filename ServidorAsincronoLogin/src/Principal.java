
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Principal {

   public static ArrayList<Usuarios> perfiles= new ArrayList<>();
   
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
        private String usuario;
        private String nombre;
        private String contraseña;
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
                    usuario= in.nextLine();
                    nombre= usuario.substring(0,usuario.indexOf(";"));
                    contraseña= usuario.substring(usuario.indexOf(";")+1);
                    if(usuario==null || usuario.toLowerCase().startsWith("quit") || usuario.length()<4){
                        continue;
                    }
                    synchronized(perfiles){
                        if(!perfiles.contains(usuario)){
                            if(LeerArchivo().contains(nombre)){
                                if(LeerArchivo().contains(contraseña)){
                                out.println("NAMEACCEPTED " + usuario);
                            for(Usuarios usuarios : perfiles){
                                usuarios.getEscritor().println("MESSAGE " + nombre + " se ha unido");
                            }
                            perfiles.add(new Usuarios(nombre, contraseña, out, LeerBloqueados(nombre)));
                            System.out.println("Usuario: " + nombre + " - Bloqueados: " + LeerBloqueados(nombre));
                            EscribirUsuario();
                            break;
                            }
                            }else{
                                out.println("NAMEACCEPTED " + usuario);
                            for(Usuarios usuarios : perfiles){
                                usuarios.getEscritor().println("MESSAGE " + nombre + " se ha unido");
                            }
                            perfiles.add(new Usuarios(nombre, contraseña, out, LeerBloqueados(nombre)));
                            System.out.println("Usuario: " + nombre + " - Bloqueados: " + LeerBloqueados(nombre));
                            EscribirUsuario();
                            break;
                            }
                        }
                    }
                }
                
                while (true) {                    
                    String input= in.nextLine();
                    String bloqueados= "";
                    for(Usuarios usuarios : perfiles){
                        if(usuarios.getNombre().equals(nombre))
                            bloqueados= usuarios.getBloqueados();
                    }
                    if(input.startsWith("/")){
                    if(input.toLowerCase().startsWith("/quit")){
                        return;
                    }else if(input.toLowerCase().startsWith("/bloquear")){
                        String bloqueado= input.substring(10);
                        if(bloqueado.equals(nombre)){
                            for(Usuarios usuarios : perfiles){
                                if(usuarios.getNombre().equals(nombre))
                                    usuarios.getEscritor().println("No puedes bloquearte a ti mismo");
                            }
                            continue;
                        }
                        for(Usuarios usuarios : perfiles){
                            if(usuarios.getNombre().equals(nombre)){
                                System.out.println(nombre + " bloqueó a " + bloqueado);
                                if(usuarios.getBloqueados().isEmpty()){
                                    usuarios.setBloqueados(bloqueado + "]");
                                }else{
                                    usuarios.setBloqueados(usuarios.getBloqueados() + bloqueado + "↨");
                                }
                                EscribirBloqueo(usuarios.getNombre(), usuarios.getBloqueados());
                            }
                        }
                    }else if(input.toLowerCase().startsWith("/desbloquear")){
                        String desbloquear= input.substring(13);
                        for(Usuarios usuarios : perfiles){
                            if(usuarios.getNombre().equals(nombre)){
                                if(usuarios.getBloqueados().contains(desbloquear)){
                                    System.out.println(nombre + " desbloqueó a " + desbloquear);
                                    usuarios.setBloqueados(usuarios.getBloqueados().replace(desbloquear + "]", ""));
                                    EscribirBloqueo(usuarios.getNombre(), usuarios.getBloqueados());
                                }
                            }
                        }
                    }else{
                        try{
                        int separador= input.substring(1).indexOf(" ");
                        String destinatario =  input.substring(1, separador + 1);
                        String mensaje =  input.substring(1).substring(separador + 1);
                        
                        if(destinatario.equals(nombre)){
                            for(Usuarios usuarios : perfiles){
                                if(usuarios.getNombre().equals(nombre))
                                    usuarios.getEscritor().println("No puedes enviarte a ti mismo");
                            }
                            continue;
                        }
                        
                        for(Usuarios usuarios : perfiles){
                            if(!bloqueados.contains(usuarios.getNombre())){
                            if(usuarios.getNombre().equals(destinatario)){
                                System.out.println("[" + usuarios.getNombre() + "] - [" + destinatario + "]");
                                usuarios.getEscritor().println("MESSAGE " + nombre + " ---> " + destinatario + " (" + getFecha() + " - " + getHora() + ") : " + mensaje);
                            }
                            if(usuarios.getNombre().equals(nombre)){
                                System.out.println("[" + usuarios.getNombre() + "] - [" + destinatario + "]");
                                usuarios.getEscritor().println("MESSAGE " + nombre + " ---> " + destinatario + ": " + mensaje);
                            }
                            }
                        }
                        }catch (Exception e){System.err.println(e);}
                    }
                }else{
                        for(Usuarios usuarios : perfiles){
                            if(!bloqueados.contains(usuarios.getNombre())){
                             usuarios.getEscritor().println("MESSAGE " + nombre + " (" + getFecha() + " - " + getHora() + ") : " + input);
                            }
                        }
                    }
                }
                
            } catch (Exception e){
                System.err.println(e);
            } finally {
                if(out!=null && nombre!=null){
                    System.err.println(nombre + " se ha salido");
                    perfiles.remove(nombre);
                    for(Usuarios usuarios : perfiles){
                        usuarios.getEscritor().println("MESSAGE " + nombre + " se ha ido");
                    }
                }
                try{ socket.close(); } catch (IOException e){}
            }
        }
    }
    
    public static String LeerBloqueados(String Nombre){
        String texto= "";
        String bloqueados= "";
        File file= new File("Login.txt");
        if(file.exists()){
            FileReader fr = null;
            BufferedReader br = null;
      try {
         fr = new FileReader (file);
         br = new BufferedReader(fr);
         String linea;
         while((linea=br.readLine())!=null){texto= texto + linea;}
         
	 StringTokenizer tokens=new StringTokenizer(texto, ":;");
         while(tokens.hasMoreTokens()){
            String nombre=tokens.nextToken();
            String contraseña=tokens.nextToken();
            String Bloqueados=tokens.nextToken();
            if(Bloqueados.equals("-"))
            Bloqueados= "";
            if(Nombre.equals(nombre))
            bloqueados= Bloqueados;
        }
      }catch(Exception e){ e.printStackTrace(); }finally{ try{ if( null != fr ){   fr.close(); } }catch (Exception e2){ e2.printStackTrace(); }}
        }else{
            System.out.println("No se encontró el archivo especificado");
        }
        return bloqueados;
    }
    
   public static String LeerNombres() throws IOException{
        String texto= "";
        String Nombres= "";
        File file= new File("Login.txt");
        if(file.exists()){
            FileReader fr = null;
            BufferedReader br = null;
      try {
         fr = new FileReader (file);
         br = new BufferedReader(fr);
         String linea;
         while((linea=br.readLine())!=null){texto= texto + linea;}
	 StringTokenizer tokens=new StringTokenizer(texto, ":;");
        while(tokens.hasMoreTokens()){
            String nombre=tokens.nextToken();
            String contraseña=tokens.nextToken();
            String Bloqueados=tokens.nextToken();
            Nombres= Nombres + nombre + "]";
        }
      }catch(Exception e){ e.printStackTrace(); }finally{ try{ if( null != fr ){   fr.close(); } }catch (Exception e2){ e2.printStackTrace(); }}
        }else{
            file.createNewFile();
        }
        return Nombres;
    }
   
   public static String LeerArchivo() throws IOException{
        String texto= "";
        File file= new File("Login.txt");
        if(file.exists()){
            FileReader fr = null;
            BufferedReader br = null;
      try {
         fr = new FileReader (file);
         br = new BufferedReader(fr);
         String linea;
         while((linea=br.readLine())!=null){texto= texto + linea;}
      }catch(Exception e){ e.printStackTrace(); }finally{ try{ if( null != fr ){   fr.close(); } }catch (Exception e2){ e2.printStackTrace(); }}
        }else{
            file.createNewFile();
        }
        return texto;
    }
   
   public static ArrayList<Usuarios> LeerUsuarios() throws IOException{
        ArrayList<Usuarios> lista= new ArrayList<>();
        String texto= "";
        File file= new File("Login.txt");
        if(file.exists()){
            FileReader fr = null;
            BufferedReader br = null;
      try {
         fr = new FileReader (file);
         br = new BufferedReader(fr);
         String linea;
         while((linea=br.readLine())!=null){texto= texto + linea;}
         StringTokenizer tokens=new StringTokenizer(texto, ":;");
        while(tokens.hasMoreTokens()){
            String nombre=tokens.nextToken();
            String contraseña=tokens.nextToken();
            String Bloqueados=tokens.nextToken();
            lista.add(new Usuarios(nombre, contraseña, null, Bloqueados));
        }
      }catch(Exception e){ e.printStackTrace(); }finally{ try{ if( null != fr ){   fr.close(); } }catch (Exception e2){ e2.printStackTrace(); }}
        }else{
            file.createNewFile();
        }
        return lista;
    }
    
    public static void EscribirUsuario() throws IOException{
        String nombres= LeerNombres();
        String cadena= "";
        BufferedWriter bw = null;
        FileWriter fw = null;
        try{
        File file = new File("Login.txt");
        // Si el archivo no existe, se crea!
        if (!file.exists()) {
            file.createNewFile();
        }
        fw = new FileWriter(file.getAbsoluteFile(), true);
        bw = new BufferedWriter(fw);
        for(Usuarios usuario : perfiles){
            if(!nombres.contains(usuario.getNombre())){
                if(usuario.getBloqueados().isEmpty()){
                    cadena= cadena + usuario.getNombre() + ":" + usuario.getContraseña() + ":" + "-" + ";";
                }else{
                cadena= cadena + usuario.getNombre() + ":" + usuario.getContraseña() + ":" + usuario.getBloqueados() + ";";
                }
            }
        }
        bw.write(cadena);
    } catch (IOException e) {
        e.printStackTrace();
    } finally {try {
            if (bw != null)
                bw.close();
            if (fw != null)
                fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    }
    
    public static void EscribirBloqueo(String Nombre, String Bloqueo) throws IOException{
        System.out.println("Escribiendo Bloqueo");
        String texto= "";
        String perfilestxt= LeerArchivo();
        if(Bloqueo.isEmpty())
            Bloqueo= "-";
        ArrayList<Usuarios> usuarios= new ArrayList<>();
        StringTokenizer tokens=new StringTokenizer(perfilestxt, ":;");
        while(tokens.hasMoreTokens()){
            String nombre=tokens.nextToken();
            String contraseña=tokens.nextToken();
            String Bloqueados=tokens.nextToken();
            System.out.println("Nombre: " + nombre + " - Bloqueos: " + Bloqueados);
            usuarios.add(new Usuarios(nombre, contraseña, null, Bloqueados));
        }
        BufferedWriter bw = null;
        FileWriter fw = null;
        try{
        File file = new File("Login.txt");
        // Si el archivo no existe, se crea!
        if (!file.exists()) {
            file.createNewFile();
        }
        fw = new FileWriter(file.getAbsoluteFile());
        bw = new BufferedWriter(fw);
        for(Usuarios usuario : usuarios){
            if(usuario.getNombre().equals(Nombre)){
                texto= texto + usuario.getNombre() + ":" + usuario.getContraseña() + ":" + Bloqueo + ";";
            }else{
            if(usuario.getBloqueados().isEmpty()){
                texto= texto + usuario.getNombre() + ":" + usuario.getContraseña() + ":-" + ";";
            }else{
           texto= texto + usuario.getNombre() + ":" + usuario.getContraseña() + ":" + usuario.getBloqueados() + ";";
            }
            }
        }
        bw.write(texto);
    } catch (IOException e) {
        e.printStackTrace();
    } finally {try {
            if (bw != null)
                bw.close();
            if (fw != null)
                fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    }
    
    public static String getHora() {
        Calendar cal = Calendar.getInstance();
        Date date=cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        String formattedDate=dateFormat.format(date);
        return formattedDate;
    }

    public static String getFecha(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = df.format(c);
        return formattedDate;
    }
    
}
