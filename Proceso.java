import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Proceso {
    private List<Pagina> paginas; 
    private int tamanioPagina; 
    private Imagen imagen; 
    private List<Integer> referencias; 
    private char[] cadena; 

    public Proceso(Imagen imagen, int tamanioPagina) {
        this.imagen = imagen;
        this.tamanioPagina = tamanioPagina;
        this.paginas = new ArrayList<>();
        this.referencias = new ArrayList<>();
    }

    public void recuperarMensaje(char[] cadena) throws IOException {
        int longitud = imagen.leerLongitud();
        this.cadena = imagen.recuperar(cadena, longitud); 

        guardarReferenciasEnArchivo("referencias.txt", imagen.getAncho(), imagen.getAlto(), referencias.size(), (referencias.size() / tamanioPagina));
    }

    private void guardarReferenciasEnArchivo(String nombreArchivo, int anchoImagen, int altoImagen, int totalBytes, int paginasNecesarias) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo));
        
        writer.write("P=" + tamanioPagina + "\n");
        writer.write("NF=" + altoImagen + "\n");
        writer.write("NC=" + anchoImagen + "\n");
        writer.write("NR=" + referencias.size() + "\n");
        writer.write("NP=" + paginasNecesarias + "\n");
        
        for (int numBytes = 0; numBytes < totalBytes; numBytes++) {
            int fila = numBytes / (anchoImagen * 3); 
            int col = (numBytes % (anchoImagen * 3)) / 3; 
            int componente = numBytes % 3; 
            String componenteRGB = (componente == 0) ? "R" : (componente == 1) ? "G" : "B";
            
            int numeroPagina = numBytes / tamanioPagina;
            int desplazamiento = numBytes % tamanioPagina;
            
            writer.write(String.format("Imagen[%d][%d].%s,%d,%d,R\n", fila, col, componenteRGB, numeroPagina, desplazamiento));
        }
        for (int posCaracter = 0; posCaracter < cadena.length; posCaracter++) {
            for (int i = 0; i < 8; i++) { 
                int numBytes = 16 + (posCaracter * 8) + i; 
    
                int numeroPagina = numBytes / tamanioPagina;
                int desplazamiento = numBytes % tamanioPagina;
    
                writer.write(String.format("Mensaje[%d],%d,%d,W\n", posCaracter, numeroPagina, desplazamiento));
            }
        }
        
        writer.close();
        System.out.println("Archivo de referencias generado: " + nombreArchivo);
    }
    

    public List<Pagina> getPaginas() {
        return paginas;
    }

    public List<Integer> getReferencias() {
        return referencias;
    }

    public int getTamanioPagina() {
        return tamanioPagina;
    }

    public void setCadena(char[] cadena) {
        this.cadena = cadena;
    }
}
