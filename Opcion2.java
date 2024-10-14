import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Opcion2 {
    private int[] tablaPaginas;
    private int[] tablaPaginasInv;
    private int[] contadorMarcosPaginas;
    private int[] marcosPaginaReferenciados;
    private int fallosPagina = 0;
    private int hits = 0;
    private boolean continuar = true;
    private Lock lock = new ReentrantLock();

    public Opcion2(int numMarcosPaginas) {
        tablaPaginasInv = new int[numMarcosPaginas];
        contadorMarcosPaginas = new int[numMarcosPaginas];
        marcosPaginaReferenciados = new int[numMarcosPaginas];

        for (int i = 0; i < numMarcosPaginas; i++) {
            tablaPaginasInv[i] = -1;
            contadorMarcosPaginas[i] = 0;
            marcosPaginaReferenciados[i] = 0;
        }
    }

    public void setTablaPaginas(int numPaginasVirtuales) {
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

    public synchronized void getPagina(int numPaginaVirtual, boolean isWrite) {
        int direccionMarcoPagina = tablaPaginas[numPaginaVirtual];
        if (direccionMarcoPagina == -1) {
            fallosPagina++;
            int marcoMenosUsado = getMarcoMenosUsado();
            tablaPaginas[numPaginaVirtual] = marcoMenosUsado;
            int paginaVirtualBorrada = tablaPaginasInv[marcoMenosUsado];
            if (paginaVirtualBorrada != -1) {
                tablaPaginas[paginaVirtualBorrada] = -1;
            }
            tablaPaginasInv[marcoMenosUsado] = numPaginaVirtual;
            contadorMarcosPaginas[marcoMenosUsado] = 0b01000000000000000000000000000000;
        } else {
            hits++;
            contadorMarcosPaginas[direccionMarcoPagina] |= 0b01000000000000000000000000000000;
        }
    }

    private int getMarcoMenosUsado() {
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

    public synchronized void envejecer() {
        for (int i = 0; i < marcosPaginaReferenciados.length; i++) {
            contadorMarcosPaginas[i] >>= 1;
            if (marcosPaginaReferenciados[i] == 1) {
                contadorMarcosPaginas[i] = contadorMarcosPaginas[i] | 0b01000000000000000000000000000000;
            }
            marcosPaginaReferenciados[i] = 0;
        }
    }

    public void terminar() {
        lock.lock();
        try {
            continuar = false;
        } finally {
            lock.unlock();
        }
    }

    public int getNumFallosPagina() {
        return fallosPagina;
    }

    public int getNumHits() {
        return hits;
    }

    public boolean continuar() {
        return continuar;
    }
}