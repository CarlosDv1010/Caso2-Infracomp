import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bienvenido al simulador de memoria virtual con threads");

        try {
            System.out.println("Ingrese el nombre del archivo BMP:");
            String archivoImagen = "caso2-parrots_mod.bmp"; // Ejemplo

            System.out.println("Ingrese el tamaño de página:");
            int tamanioPagina = 64; // Ejemplo

            Imagen imagen = new Imagen(archivoImagen); 
            Proceso proceso = new Proceso(imagen, tamanioPagina);
            imagen.inicializarProceso(proceso); 

            char[] cadena = new char[imagen.leerLongitud()];
            proceso.recuperarMensaje(cadena); // Recuperar el mensaje y generar referencias

            System.out.println("Ingrese el número de marcos de página:");
            int marcosPagina = 8; // Ejemplo. Un marco de pagina contiene una pagina virtual

            // Crear el administrador de memoria y el simulador
            AdministradorMemoria adminMemoria = new AdministradorMemoria(marcosPagina);
            SimuladorMemoria simulador = new SimuladorMemoria(adminMemoria, proceso);

            // Iniciar la simulación con threads
            simulador.iniciarSimulacionConThreads("referencias.txt");
            simulador.generarResultados(); // Mostrar resultados de la simulación

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
