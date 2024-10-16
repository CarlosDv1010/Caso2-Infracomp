import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Bienvenido al simulador de memoria virtual con threads");

        while (true) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Generar el archivo con las referencias");
            System.out.println("2. Calcular los datos de la simulación (hits, misses, etc.)");
            System.out.println("3. Esconder un mensaje en una imagen");
            System.out.println("4. Recuperar un mensaje de una imagen");
            System.out.println("5. Salir");
            int opcion = scanner.nextInt();

            if (opcion == 1) {
                // Opción 1: Generar el archivo de referencias
                System.out.println("Ingrese el nombre del archivo BMP que contiene el mensaje:");
                String archivoImagen = scanner.next();
                
                System.out.println("Ingrese el tamaño de página:");
                int tamanioPagina = scanner.nextInt();

                // Cargar la imagen y crear el proceso
                Imagen imagen = new Imagen(archivoImagen);
                Proceso proceso = new Proceso(imagen, tamanioPagina);

                // Leer la longitud del mensaje
                char[] cadena = new char[imagen.leerLongitud()];

                System.out.println(imagen.leerLongitud());

                // Generar el archivo de referencias
                System.out.println("Generando el archivo de referencias...");
                proceso.recuperarMensaje(cadena);
                System.out.println("Archivo de referencias generado como 'referencias.txt'.");

            } else if (opcion == 2) {
                // Opción 2: Calcular los datos de la simulación
                System.out.println("Ingrese el número de marcos de página:");
                int marcosPagina = scanner.nextInt();
                try {
                    long startTime = System.nanoTime();

                    CyclicBarrier barrier = new CyclicBarrier(2);

                    BufferedReader archivoReferencias = new BufferedReader(new FileReader("referencias.txt"));
                    SimuladorMemoria simulador = new SimuladorMemoria(marcosPagina, archivoReferencias, barrier);

                    Thread procesoThread = new Thread(simulador);
                    Thread relojThread = new Thread(() -> simulador.ejecutarReloj());

                    procesoThread.start();
                    relojThread.start();

                    procesoThread.join();
                    relojThread.join();

                    long endTime = System.nanoTime() - startTime;

                    long totalMilliseconds = endTime / 1_000_000;
                    long minutes = totalMilliseconds / 60000;
                    long seconds = (totalMilliseconds % 60000) / 1000;
                    long milliseconds = totalMilliseconds % 1000;

                    System.out.println("Número de fallas de página: " + simulador.getNumeroFallosPagina());
                    System.out.println("Número de hits: " + simulador.getNumeroHits());
                    System.out.println("Tiempo de ejecución: " + minutes + " minutos, " + seconds + " segundos, " + milliseconds + " milisegundos.");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("\nNo se encontró el archivo de referencias.");
                }
                System.out.println("Simulación completada.");
                
            } else if (opcion == 3) {
                // Opción 3: Esconder un mensaje en una imagen
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                try {
                    // Solicitar el archivo BMP que contiene la imagen
                    System.out.println("Ingrese el nombre del archivo BMP que contiene la imagen:");
                    String ruta = br.readLine();
                    Imagen_2 imagen = new Imagen_2(ruta);

                    // Solicitar el archivo que contiene el mensaje a esconder
                    System.out.println("Ingrese el nombre del archivo que contiene el mensaje a esconder:");
                    String rutaMensaje = br.readLine();

                    // Leer el mensaje desde el archivo de texto
                    StringBuilder mensaje = new StringBuilder();
                    try (BufferedReader brMensaje = new BufferedReader(new FileReader(rutaMensaje))) {
                        String linea;
                        while ((linea = brMensaje.readLine()) != null) {
                            mensaje.append(linea);
                            mensaje.append("\n");
                        }
                    }

                    char[] mensajeCharArray = mensaje.toString().toCharArray();
                    int longitudMensaje = mensajeCharArray.length;

                    // Esconder el mensaje en la imagen
                    imagen.esconder(mensajeCharArray, longitudMensaje);

                    // Guardar la imagen modificada en un nuevo archivo BMP
                    System.out.println("Ingrese el nombre del archivo de salida BMP:");
                    String salidaImagen = br.readLine();
                    imagen.escribirImagen(salidaImagen);

                    System.out.println("Mensaje escondido en la imagen guardada como: " + salidaImagen);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (opcion == 4) {
                // Opción 4: Recuperar un mensaje escondido en una imagen
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                try {
                    // Solicitar el archivo BMP que contiene la imagen con el mensaje escondido
                    System.out.println("Ingrese el nombre del archivo BMP que contiene el mensaje escondido:");
                    String ruta = br.readLine();
                    Imagen_2 imagen = new Imagen_2(ruta);

                    // Leer la longitud del mensaje escondido
                    int longitud = imagen.leerLongitud();

                    // Recuperar el mensaje de la imagen
                    char[] mensajeRecuperado = new char[longitud];
                    imagen.recuperar(mensajeRecuperado, longitud);

                    // Mostrar el mensaje recuperado en la consola
                    System.out.println("Mensaje recuperado:");
                    System.out.println(new String(mensajeRecuperado));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (opcion == 5) {
                // Salir del programa
                System.out.println("Saliendo del programa.");
                break;
            } else {
                System.out.println("Opción no válida. Por favor, seleccione 1, 2, 3, 4 o 5.");
            }
        }
    }
}