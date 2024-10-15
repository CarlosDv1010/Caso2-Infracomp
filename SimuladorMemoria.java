import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimuladorMemoria extends Thread {
    private int[] tablaPaginas;
    private int[] tablaPaginasInv;
    private int[] contadorMarcosPaginas;
    private int[] bitsR;
    private int[] bitsM;
    private int fallosPagina = 0;
    private int hits = 0;
    private boolean continuarSimulacion = true;
    private Lock lock = new ReentrantLock();
    private CyclicBarrier barrier;
    private BufferedReader archivo;

    public SimuladorMemoria(int numMarcosPaginas, BufferedReader archivo, CyclicBarrier barrier) throws IOException {
        this.archivo = archivo;
        this.barrier = barrier;

        tablaPaginasInv = new int[numMarcosPaginas];
        contadorMarcosPaginas = new int[numMarcosPaginas];
        bitsR = new int[numMarcosPaginas];  // bit de referencia
        bitsM = new int[numMarcosPaginas];  // bit de modificación

        for (int i = 0; i < numMarcosPaginas; i++) {
            tablaPaginasInv[i] = -1;
            contadorMarcosPaginas[i] = 0;
            bitsR[i] = 0;
            bitsM[i] = 0;
        }
    }

    public void configurarTablaPaginas(int numPaginasVirtuales) {
        lock.lock();
        try {
            tablaPaginas = new int[numPaginasVirtuales];
            for (int i = 0; i < numPaginasVirtuales; i++) {
                tablaPaginas[i] = -1;
            }
        } finally {
            lock.unlock();
        }
    }

    public synchronized void accederPagina(int numPaginaVirtual, boolean isEscritura) {
        int direccionMarcoPagina = tablaPaginas[numPaginaVirtual];
        if (direccionMarcoPagina == -1) {
            manejarFalloPagina(numPaginaVirtual, isEscritura);
        } else {
            manejarHit(direccionMarcoPagina, isEscritura);
        }
    }

    private void manejarFalloPagina(int numPaginaVirtual, boolean isEscritura) {
        fallosPagina++;
    
        // Buscar primero si hay un marco libre disponible
        int marcoLibre = encontrarMarcoLibre();
        int marcoParaAsignar;
    
        if (marcoLibre != -1) {
            // Si encontramos un marco libre, lo usamos
            marcoParaAsignar = marcoLibre;
        } else {
            // Si no hay marcos libres, usamos el algoritmo NRU para encontrar uno para reemplazar
            marcoParaAsignar = encontrarMarcoNRU();
            int paginaVirtualReemplazada = tablaPaginasInv[marcoParaAsignar];
    
            if (paginaVirtualReemplazada != -1) {
                // Invalida la entrada en la tabla de páginas de la página que se reemplaza
                tablaPaginas[paginaVirtualReemplazada] = -1;
            }
        }
    
        // Asignar la nueva página al marco encontrado
        tablaPaginas[numPaginaVirtual] = marcoParaAsignar;
        tablaPaginasInv[marcoParaAsignar] = numPaginaVirtual;
    
        // Restablecer el bit de referencia (R) y modificar el bit de modificación (M) según sea necesario
        bitsR[marcoParaAsignar] = 1;  // Se ha referenciado
        bitsM[marcoParaAsignar] = isEscritura ? 1 : 0;  // Se ha modificado si es una escritura
    }
    
    // Método para buscar un marco libre
    private int encontrarMarcoLibre() {
        for (int i = 0; i < tablaPaginasInv.length; i++) {
            if (tablaPaginasInv[i] == -1) {  // Si el marco está libre
                return i;
            }
        }
        return -1;  // No hay marcos libres
    }
    

    private void manejarHit(int direccionMarcoPagina, boolean isEscritura) {
        hits++;
        // Actualizar los bits R y M
        bitsR[direccionMarcoPagina] = 1;  // La página ha sido referenciada
        if (isEscritura) {
            bitsM[direccionMarcoPagina] = 1;  // Si es una escritura, marcar como modificada
        }
    }

    private int encontrarMarcoNRU() {
        // Clasificación de las páginas en base a los bits R y M
        List<Integer> clase0 = new ArrayList<>();
        List<Integer> clase1 = new ArrayList<>();
        List<Integer> clase2 = new ArrayList<>();
        List<Integer> clase3 = new ArrayList<>();
    
        for (int i = 0; i < tablaPaginasInv.length; i++) {
            if (tablaPaginasInv[i] == -1) {
                continue;  // Si no hay página asignada, ignorar
            }
            if (bitsR[i] == 0 && bitsM[i] == 0) {
                clase0.add(i);  // Clase 0: no referenciada, no modificada
            } else if (bitsR[i] == 0 && bitsM[i] == 1) {
                clase1.add(i);  // Clase 1: no referenciada, modificada
            } else if (bitsR[i] == 1 && bitsM[i] == 0) {
                clase2.add(i);  // Clase 2: referenciada, no modificada
            } else if (bitsR[i] == 1 && bitsM[i] == 1) {
                clase3.add(i);  // Clase 3: referenciada, modificada
            }
        }
    
        // Si todas las clases están vacías, hay un error en la lógica
        if (clase0.isEmpty() && clase1.isEmpty() && clase2.isEmpty() && clase3.isEmpty()) {
            System.out.println("Error: No se encontraron páginas para reemplazar.");
            throw new IllegalStateException("No se encontraron páginas para reemplazar. Verifique la lógica.");
        }
    
        // Seleccionar una página al azar de la clase más baja no vacía
        if (!clase0.isEmpty()) {
            return clase0.get((int) (Math.random() * clase0.size()));
        } else if (!clase1.isEmpty()) {
            return clase1.get((int) (Math.random() * clase1.size()));
        } else if (!clase2.isEmpty()) {
            return clase2.get((int) (Math.random() * clase2.size()));
        } else {
            return clase3.get((int) (Math.random() * clase3.size()));
        }
    }
    
    

    public synchronized void envejecerPaginas() {
        // Simula el envejecimiento de las páginas (resetea los bits R)
        for (int i = 0; i < bitsR.length; i++) {
            bitsR[i] = 0;  // Borrar el bit de referencia periódicamente
        }
    }

    public void finalizarSimulacion() {
        lock.lock();
        try {
            continuarSimulacion = false;
        } finally {
            lock.unlock();
        }
    }

    public int getNumeroFallosPagina() {
        return fallosPagina;
    }

    public int getNumeroHits() {
        return hits;
    }

    public boolean continuarSimulacion() {
        return continuarSimulacion;
    }

    @Override
    public void run() {
        try {
            String linea;
            while ((linea = archivo.readLine()) != null) {
                if (linea.startsWith("NP=")) {
                    int numPaginasVirtuales = Integer.parseInt(linea.split("=")[1]);
                    configurarTablaPaginas(numPaginasVirtuales);
                } else if (linea.startsWith("Imagen") || linea.startsWith("Mensaje")) {
                    String[] partes = linea.split(",");
                    int numPaginaVirtual = Integer.parseInt(partes[1]);
                    boolean isEscritura = partes[3].trim().equals("W");
                    accederPagina(numPaginaVirtual, isEscritura);
                    Thread.sleep(1); // Simula tiempo de acceso a memoria
                }
            }
            archivo.close();
            finalizarSimulacion();
            barrier.await();
        } catch (IOException | InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void ejecutarReloj() {
        try {
            while (continuarSimulacion()) {
                Thread.sleep(20); // Simula el reloj de envejecimiento (20 ms)
                envejecerPaginas();
            }
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
