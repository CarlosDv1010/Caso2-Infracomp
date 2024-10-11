import java.util.*;

public class AdministradorMemoria {
    private int marcosEnRAM;
    private Queue<Pagina> paginasRAM;
    private int fallosPagina;
    private int hits;
    private Map<Integer, Pagina> tablaPaginas;
    private final Object lock = new Object();

    public AdministradorMemoria(int marcosEnRAM) {
        this.marcosEnRAM = marcosEnRAM;
        this.paginasRAM = new LinkedList<>();
        this.tablaPaginas = new HashMap<>();
        this.fallosPagina = 0;
        this.hits = 0;
    }

    public void simularAcceso(int numeroPagina) {
        synchronized (lock) {
            Pagina pagina = tablaPaginas.get(numeroPagina);
            if (pagina != null && pagina.isEnRAM()) {
                hits++;
                pagina.actualizarTimestamp();
                pagina.setBitReferencia(true);
            } else {
                fallosPagina++;
                reemplazarPagina(numeroPagina);
            }
        }
    }

    private void reemplazarPagina(int numeroPagina) {
        Pagina nuevaPagina = new Pagina(numeroPagina);
        if (paginasRAM.size() >= marcosEnRAM) {
            Pagina paginaAReemplazar = seleccionarPaginaAReemplazar();
            tablaPaginas.remove(paginaAReemplazar.getNumero());
            paginasRAM.remove(paginaAReemplazar);
        }
        nuevaPagina.setEnRAM(true);
        nuevaPagina.setBitReferencia(true);
        nuevaPagina.actualizarTimestamp();
        paginasRAM.add(nuevaPagina);
        tablaPaginas.put(numeroPagina, nuevaPagina);
    }

    private Pagina seleccionarPaginaAReemplazar() {
        for (Pagina pagina : paginasRAM) {
            if (!pagina.isBitReferencia()) {
                return pagina;
            }
            pagina.setBitReferencia(false);
        }
        return paginasRAM.peek();
    }

    public int getFallosPagina() {
        return fallosPagina;
    }

    public int getHits() {
        return hits;
    }

    public void actualizarBitsReferencia() {
        synchronized (lock) {
            for (Pagina pagina : paginasRAM) {
                pagina.setBitReferencia(false); // Reinicia el bit de referencia
            }
        }
    }

    // Método llamado por el primer thread que actualiza la tabla de páginas
    public void actualizarEstado() {
        synchronized (lock) {
            for (Pagina pagina : paginasRAM) {
                pagina.actualizarTimestamp(); // Actualiza la marca temporal de las páginas
            }
        }
    }
}
