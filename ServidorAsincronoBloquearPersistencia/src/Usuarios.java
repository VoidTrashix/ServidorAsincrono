
import java.io.PrintWriter;
public class Usuarios {
    private String Nombre;
    private PrintWriter Escritor;
    private String Bloqueados;

    public Usuarios(String Nombre, PrintWriter Escritor, String Bloqueados) {
        this.Nombre = Nombre;
        this.Escritor = Escritor;
        this.Bloqueados = Bloqueados;
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
