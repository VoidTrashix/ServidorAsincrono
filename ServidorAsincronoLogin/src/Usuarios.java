
import java.io.PrintWriter;

public class Usuarios {
    private String Nombre;
    private String Contraseña;
    private PrintWriter Escritor;
    private String Bloqueados;

    public Usuarios(String Nombre, String Contraseña, PrintWriter Escritor, String Bloqueados) {
        this.Nombre = Nombre;
        this.Contraseña = Contraseña;
        this.Escritor = Escritor;
        this.Bloqueados = Bloqueados;
    }

    public String getContraseña() {
        return Contraseña;
    }

    public void setContraseña(String Contraseña) {
        this.Contraseña = Contraseña;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String Nombre) {
        this.Nombre = Nombre;
    }

    public PrintWriter getEscritor() {
        return Escritor;
    }

    public void setEscritor(PrintWriter Escritor) {
        this.Escritor = Escritor;
    }

    public String getBloqueados() {
        return Bloqueados;
    }

    public void setBloqueados(String Bloqueados) {
        this.Bloqueados = Bloqueados;
    }
}
