import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SimuladorMemoria {
    private AdministradorMemoria adminMemoria;
    private Proceso proceso;
    private volatile boolean stopThreads = false; 

    public SimuladorMemoria(AdministradorMemoria adminMemoria, Proceso proceso) {
        this.adminMemoria = adminMemoria;
        this.proceso = proceso;
    }

    public void iniciarSimulacionConThreads(String archivoReferencias) throws IOException {
        Thread threadActualizarEstado = new Thread(() -> {
            try {
                while (!stopThreads) {
                    Thread.sleep(1000); // 1 milisegundo en lugar de pulsos de reloj
                    adminMemoria.actualizarEstado(); // Actualiza tabla de páginas
                }
            } catch (InterruptedException e) {
                System.out.println("Thread de actualización de estado interrumpido");
            }
        });

        Thread threadActualizarBits = new Thread(() -> {
            try {
                while (!stopThreads) {
                    Thread.sleep(2000); // 2 milisegundos para actualizar los bits de referencia
                    adminMemoria.actualizarBitsReferencia(); // Actualiza bits de referencia
                }
            } catch (InterruptedException e) {
                System.out.println("Thread de actualización de bits interrumpido");
            }
        });

        threadActualizarEstado.start();
        threadActualizarBits.start();

        // Simular acceso a las páginas basado en las referencias del archivo
        BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias));
        String linea;
        while ((linea = reader.readLine()) != null) {
            if (linea.startsWith("Referencia:")) {
                int numeroPagina = Integer.parseInt(linea.split(":")[1].trim());
                adminMemoria.simularAcceso(numeroPagina);
            }
        }
        reader.close();

        // Detener los threads después de la simulación
        stopThreads = true;
        threadActualizarEstado.interrupt();
        threadActualizarBits.interrupt();
    }

    public void generarResultados() {
        System.out.println("Número de fallos de página: " + adminMemoria.getFallosPagina());
        System.out.println("Número de hits: " + adminMemoria.getHits());
    }
}
