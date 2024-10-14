import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bienvenido al simulador de memoria virtual con threads");

        try {
            System.out.println("Ingrese el nombre del archivo BMP:");
            String archivoImagen = "caso2-parrots_mod.bmp";

            System.out.println("Ingrese el tamaño de página:");
            int tamanioPagina = 10;

            Imagen imagen = new Imagen(archivoImagen); 
            Proceso proceso = new Proceso(imagen, tamanioPagina);

            char[] cadena = new char[imagen.leerLongitud()];

            while(true){
                System.out.println("Seleccione una opción:");
                System.out.println("1. Generar el archivo con las referencias");
                System.out.println("2. Calcular los datos de la simulación (hits, misses, etc.)");
                int opcion = scanner.nextInt();

                if (opcion == 1) {
                    // Opción 1: Generar el archivo de referencias
                    System.out.println("Generando el archivo de referencias...");
                    proceso.recuperarMensaje(cadena);
                    System.out.println("Archivo de referencias generado como 'referencias.txt'.");
                } else if (opcion == 2) {
                    // Opción 2: Calcular los datos de la simulación (hits, misses, etc.)
                    System.out.println("Ingrese el número de marcos de página:");
                    int marcosPagina = scanner.nextInt();

                    // Crear el administrador de memoria y el simulador
                    AdministradorMemoria adminMemoria = new AdministradorMemoria(marcosPagina);
                    SimuladorMemoria simulador = new SimuladorMemoria(adminMemoria, proceso);

                    // Iniciar la simulación con threads
                    System.out.println("Iniciando simulación...");
                    simulador.iniciarSimulacionConThreads("referencias.txt");
                    simulador.generarResultados(); // Mostrar resultados de la simulación
                } else {
                    System.out.println("Opción no válida. Por favor, seleccione 1 o 2.");
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
