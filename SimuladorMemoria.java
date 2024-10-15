import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimuladorMemoria extends Thread {
    private int[] tablaPaginas;
    private int[] tablaPaginasInv;
    private int[] contadorMarcosPaginas;
    private int[] marcosPaginaReferenciados;
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
        marcosPaginaReferenciados = new int[numMarcosPaginas];

        for (int i = 0; i < numMarcosPaginas; i++) {
            tablaPaginasInv[i] = -1;
            contadorMarcosPaginas[i] = 0;
            marcosPaginaReferenciados[i] = 0;
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
            manejarFalloPagina(numPaginaVirtual);
        } else {
            manejarHit(direccionMarcoPagina);
        }
    }

    private void manejarFalloPagina(int numPaginaVirtual) {
        fallosPagina++;
        int marcoMenosUsado = encontrarMarcoMenosUsado();
        tablaPaginas[numPaginaVirtual] = marcoMenosUsado;
        int paginaVirtualReemplazada = tablaPaginasInv[marcoMenosUsado];
        if (paginaVirtualReemplazada != -1) {
            tablaPaginas[paginaVirtualReemplazada] = -1;
        }
        tablaPaginasInv[marcoMenosUsado] = numPaginaVirtual;
        contadorMarcosPaginas[marcoMenosUsado] = 0b01000000000000000000000000000000;
    }

    private void manejarHit(int direccionMarcoPagina) {
        hits++;
        contadorMarcosPaginas[direccionMarcoPagina] |= 0b01000000000000000000000000000000;
    }

    private int encontrarMarcoMenosUsado() {
        int indexMenosUsado = 0;
        int valMenosUsado = contadorMarcosPaginas[0];
        for (int i = 1; i < contadorMarcosPaginas.length; i++) {
            if (contadorMarcosPaginas[i] < valMenosUsado) {
                valMenosUsado = contadorMarcosPaginas[i];
                indexMenosUsado = i;
            }
        }
        return indexMenosUsado;
    }

    public synchronized void envejecerPaginas() {
        for (int i = 0; i < marcosPaginaReferenciados.length; i++) {
            contadorMarcosPaginas[i] >>= 1;
            if (marcosPaginaReferenciados[i] == 1) {
                contadorMarcosPaginas[i] |= 0b01000000000000000000000000000000;
            }
            marcosPaginaReferenciados[i] = 0;
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
                Thread.sleep(2); // Simula el reloj de envejecimiento
                envejecerPaginas();
            }
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
