import java.util.ArrayList;
import java.util.List;

public class Reporte {
    private List<String> datosSimulacion;

    public Reporte() {
        this.datosSimulacion = new ArrayList<>();
    }

    public void agregarDato(String dato) {
        datosSimulacion.add(dato);
    }

    public void generarTablaResultados() {
        System.out.println("Tabla de Resultados:");
        for (String dato : datosSimulacion) {
            System.out.println(dato);
        }
    }

    public void generarGraficas() {
        // Aquí podrías usar alguna biblioteca como JFreeChart para generar gráficas
        // Esto es solo un placeholder para la lógica de graficar
        System.out.println("Generando gráficos (pendiente de implementación)...");
    }
}
