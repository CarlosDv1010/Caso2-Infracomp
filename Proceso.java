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
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("referencias.txt"))) {

            int longitud = 5069;
            int nr = longitud * 17 + 16;
            int np = (int) Math.ceil((double) (imagen.getAncho() * imagen.getAlto() * 3) / tamanioPagina) + (int) Math.ceil((double) longitud / tamanioPagina);
            int inicial = (int) Math.ceil((double) (imagen.getAncho() * imagen.getAlto() * 3) / tamanioPagina);

            // Escribir los encabezados del archivo
            writer.write("P=" + tamanioPagina + "\n");
            writer.write("NF=" + imagen.getAlto() + "\n");
            writer.write("NC=" + imagen.getAncho() + "\n");
            writer.write("NR=" + nr + "\n");
            writer.write("NP=" + np + "\n");
            imagen.leerLongitudReferencias(writer, np);
            // Recuperar el mensaje y generar referencias adicionales
            this.cadena = imagen.recuperar(cadena, 5069, writer, tamanioPagina, inicial);
        }
    
        System.out.println("Archivo de referencias generado.");
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
