import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ProcesoOpcion2 extends Thread {
    private Opcion2 ram;
    private CyclicBarrier barrier;
    private BufferedReader archivo;

    public ProcesoOpcion2(BufferedReader archivo, Opcion2 ram, CyclicBarrier barrier) throws IOException {
        this.archivo = archivo;
        this.ram = ram;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = archivo.readLine()) != null) {
                if (line.startsWith("NP=")) {
                    int numPaginasVirtuales = Integer.parseInt(line.split("=")[1]);
                    ram.setTablaPaginas(numPaginasVirtuales);
                } else if (line.startsWith("M") || line.startsWith("F") || line.startsWith("R")) {
                    String[] parts = line.split(",");
                    int numPaginaVirtual = Integer.parseInt(parts[1]);
                    boolean isWrite = parts[3].trim().equals("W");
                    ram.getPagina(numPaginaVirtual, isWrite);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            archivo.close();
            ram.terminar();
            barrier.await();
        } catch (IOException | InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public void reloj() {
        try {
            while (ram.continuar()) {
                Thread.sleep(4);
                ram.envejecer();
            }
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}