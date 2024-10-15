import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.io.BufferedReader;
import java.io.FileReader;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bienvenido al simulador de memoria virtual con threads");

        // Solicitar los datos de entrada
        System.out.println("Ingrese el nombre del archivo BMP:");
        String archivoImagen = "caso2-parrots_mod.bmp";  // Puedes cambiar esta línea para ingresar el archivo por consola

        System.out.println("Ingrese el tamaño de página:");
        int tamanioPagina = scanner.nextInt();

        Imagen imagen = new Imagen(archivoImagen); 
        Proceso proceso = new Proceso(imagen, tamanioPagina);

        char[] cadena = new char[imagen.leerLongitud()];

        while (true) {
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
                try {
                    long startTime = System.nanoTime();  // Iniciar el cronómetro

                    // Crear la barrera cíclica para sincronizar los hilos
                    CyclicBarrier barrier = new CyclicBarrier(2);

                    // Crear el simulador de memoria con el número de marcos de página y el archivo de referencias
                    BufferedReader archivoReferencias = new BufferedReader(new FileReader("referencias.txt"));
                    SimuladorMemoria simulador = new SimuladorMemoria(marcosPagina, archivoReferencias, barrier);

                    // Crear e iniciar los hilos
                    Thread procesoThread = new Thread(simulador);
                    Thread relojThread = new Thread(() -> simulador.ejecutarReloj());

                    procesoThread.start();
                    relojThread.start();

                    // Esperar a que ambos hilos terminen
                    procesoThread.join();
                    relojThread.join();

                    long endTime = System.nanoTime() - startTime;  // Calcular tiempo de ejecución

                    // Mostrar los resultados de la simulación
                    System.out.println("Número de fallas de página: " + simulador.getNumeroFallosPagina());
                    System.out.println("Número de hits: " + simulador.getNumeroHits());
                    System.out.println("Tiempo de ejecución: " + endTime / 1e6 + " ms");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("\nNo se encontró el archivo de referencias.");
                }
                System.out.println("Simulación completada.");
            } else {
                System.out.println("Opción no válida. Por favor, seleccione 1 o 2.");
            }
        }
    }
}
