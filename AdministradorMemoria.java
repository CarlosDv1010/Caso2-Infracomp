import java.util.*;

public class AdministradorMemoria {
    private int marcosEnRAM; // Número de marcos en la RAM
    private Queue<Pagina> paginasRAM; // Cola de páginas en la RAM
    private int fallosPagina; // Contador de fallos de página
    private int hits; // Contador de hits
    private Map<Integer, Pagina> tablaPaginas; // Mapa de páginas en memoria
    private final Object lock = new Object(); // Lock para sincronización

    public AdministradorMemoria(int marcosEnRAM) {
        this.marcosEnRAM = marcosEnRAM;
        this.paginasRAM = new LinkedList<>();
        this.tablaPaginas = new HashMap<>();
        this.fallosPagina = 0;
        this.hits = 0;
    }

    // Método para simular el acceso a una página
    public void simularAcceso(int numeroPagina) {
        synchronized (lock) {
            Pagina pagina = tablaPaginas.get(numeroPagina);

            if (pagina != null && pagina.isEnRAM()) {
                // Hit: La página está en RAM
                hits++;
                pagina.setBitReferencia(true); // Página accedida recientemente
                pagina.actualizarTimestamp();
            } else {
                // Miss: La página no está en RAM
                fallosPagina++;
                reemplazarPagina(numeroPagina); // Reemplazar página según NRU
            }
        }
    }

    // Reemplazo de página basado en el algoritmo NRU
    private void reemplazarPagina(int numeroPagina) {
        Pagina nuevaPagina = new Pagina(numeroPagina);
        
        // Si no hay espacio en la RAM, seleccionar página para reemplazar
        if (paginasRAM.size() >= marcosEnRAM) {
            Pagina paginaAReemplazar = seleccionarPaginaAReemplazar(); // Usar NRU
            if (paginaAReemplazar != null) {
                // Sacar la página a reemplazar de la RAM
                tablaPaginas.remove(paginaAReemplazar.getNumero());
                paginasRAM.remove(paginaAReemplazar);
            }
        }

        // Agregar la nueva página a la RAM
        nuevaPagina.setEnRAM(true);
        nuevaPagina.setBitReferencia(true); // Inicialmente, se considera usada
        nuevaPagina.actualizarTimestamp();
        paginasRAM.add(nuevaPagina);
        tablaPaginas.put(numeroPagina, nuevaPagina);
    }

    // Selección de página para reemplazo usando NRU
    private Pagina seleccionarPaginaAReemplazar() {
        Pagina paginaAReemplazar = null;
        
        // Primera pasada: encontrar una página con bit de referencia 0 (no usada recientemente)
        for (Pagina pagina : paginasRAM) {
            if (!pagina.isBitReferencia()) {
                paginaAReemplazar = pagina;
                break;
            }
        }

        // Si no hay páginas con bit de referencia 0, seleccionar la primera página en la cola
        if (paginaAReemplazar == null) {
            paginaAReemplazar = paginasRAM.peek(); // Devuelve la primera página
            if (paginaAReemplazar != null) {
                paginaAReemplazar.setBitReferencia(false); // Reiniciar el bit de referencia
            }
        }

        return paginaAReemplazar;
    }

    // Actualizar los bits de referencia periódicamente (cada 2 milisegundos)
    public void actualizarBitsReferencia() {
        synchronized (lock) {
            for (Pagina pagina : paginasRAM) {
                pagina.setBitReferencia(false); // Reinicia el bit de referencia
            }
        }
    }

    public int getFallosPagina() {
        return fallosPagina;
    }

    public int getHits() {
        return hits;
    }

    // Método para actualizar el estado de las páginas (puede incluir timestamps, etc.)
    public void actualizarEstado() {
        synchronized (lock) {
            for (Pagina pagina : paginasRAM) {
                pagina.actualizarTimestamp(); // Actualiza la marca temporal de las páginas
            }
        }
    }
}
