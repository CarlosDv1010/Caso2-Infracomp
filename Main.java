import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.io.BufferedReader;
import java.io.FileReader;


public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bienvenido al simulador de memoria virtual con threads");


            System.out.println("Ingrese el nombre del archivo BMP:");
            String archivoImagen = "caso2-parrots_mod.bmp";

            System.out.println("Ingrese el tamaño de página:");
            int tamanioPagina = scanner.nextInt();

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
                    try {
                    	long startTime = System.nanoTime();
                        CyclicBarrier barrier = new CyclicBarrier(2);
                        Opcion2 ram = new Opcion2(marcosPagina);
                        ProcesoOpcion2 proceso2 = new ProcesoOpcion2(new BufferedReader(new FileReader("referencias.txt")), ram, barrier);
                        Thread procesoThread = new Thread(proceso2);
                        Thread relojThread = new Thread(() -> proceso2.reloj());
                        procesoThread.start();
                        relojThread.start();
                        procesoThread.join();
                        relojThread.join();
                        long endTime = System.nanoTime() - startTime;
                        System.out.println("Número de fallas de página: " + ram.getNumFallosPagina());
                        System.out.println("Número de hits: " + ram.getNumHits());
                        System.out.println("Tiempo de ejecucción: " + endTime/1e6 + " ms");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("\nNo se encontró el archivo");
                    }



                    System.out.println("Simulación completada.");
                } else {
                    System.out.println("Opción no válida. Por favor, seleccione 1 o 2.");
                }
            }
        
    }


}
