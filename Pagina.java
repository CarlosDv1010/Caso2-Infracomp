public class Pagina {
    private boolean enMemoria;  // Si la página está en memoria o no
    private boolean bitR;       // Bit de referencia
    private boolean bitM;       // Bit de modificación
    private int paginaVirtual;  // Número de página virtual

    public Pagina() {
        this.enMemoria = false;
        this.bitR = false;
        this.bitM = false;
        this.paginaVirtual = -1;
    }

    public synchronized boolean isEnMemoria() {
        return enMemoria;
    }

    public synchronized void setEnMemoria(boolean enMemoria) {
        this.enMemoria = enMemoria;
    }

    public synchronized boolean isBitR() {
        return bitR;
    }

    public synchronized void setBitR(boolean bitR) {
        this.bitR = bitR;
    }

    public synchronized boolean isBitM() {
        return bitM;
    }

    public synchronized void setBitM(boolean bitM) {
        this.bitM = bitM;
    }

    public synchronized int getPaginaVirtual() {
        return paginaVirtual;
    }

    public synchronized boolean correspondeAPaginaVirtual(int paginaVirtual) {
        return this.paginaVirtual == paginaVirtual;
    }

    public synchronized void cargar(int paginaVirtual) {
        this.paginaVirtual = paginaVirtual;
        this.enMemoria = true;
        this.bitR = true;  // Al cargar la página, el bit de referencia se marca
        this.bitM = false; // Al inicio no está modificada
    }

    @Override
    public synchronized String toString() {
        return "Pagina{" +
                "enMemoria=" + enMemoria +
                ", bitR=" + bitR +
                ", bitM=" + bitM +
                ", paginaVirtual=" + paginaVirtual +
                '}';
    }
}
