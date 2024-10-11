public class Pagina {
    private int numero;
    private boolean bitReferencia; // Bit de referencia (si fue usada recientemente)
    private boolean enRAM; // Si está en RAM o no
    private long timestamp; // Marca de tiempo para reemplazo

    public Pagina(int numero) {
        this.numero = numero;
        this.bitReferencia = false;
        this.enRAM = false;
        this.timestamp = System.currentTimeMillis(); // Marca de tiempo inicial
    }

    public int getNumero() {
        return numero;
    }

    public boolean isBitReferencia() {
        return bitReferencia;
    }

    public void setBitReferencia(boolean bitReferencia) {
        this.bitReferencia = bitReferencia;
    }

    public boolean isEnRAM() {
        return enRAM;
    }

    public void setEnRAM(boolean enRAM) {
        this.enRAM = enRAM;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void actualizarTimestamp() {
        this.timestamp = System.currentTimeMillis(); // Actualiza tiempo de uso
    }
}
