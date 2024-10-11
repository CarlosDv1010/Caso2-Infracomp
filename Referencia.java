public class Referencia {
    private final int pagina;
    private final long timestamp;

    public Referencia(int pagina) {
        this.pagina = pagina;
        this.timestamp = System.currentTimeMillis(); // Almacena el tiempo de creación
    }

    public int getPagina() {
        return pagina;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
