import java.io.*;

public class Imagen {
    private byte[] header = new byte[54];
    private byte[][][] imagen; 
    private int alto, ancho; 
    private int padding; 
    private String nombre; 
    private int hits;
    private int misses;

    public Imagen(String input) {
        this.nombre = input;
        leerImagen();
    }


    private void leerImagen() {
        try {
            FileInputStream fis = new FileInputStream(nombre);
            fis.read(header);
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) |
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) |
                    ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);
            System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            imagen = new byte[alto][ancho][3];
            int rowSizeSinPadding = ancho * 3;
            padding = (4 - (rowSizeSinPadding % 4)) % 4;

            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    fis.read(pixel);
                    imagen[i][j][0] = pixel[0]; 
                    imagen[i][j][1] = pixel[1];
                    imagen[i][j][2] = pixel[2]; 
                }
                fis.skip(padding);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void esconder(char[] mensaje, int longitud) {
        int contador = 0;
        escribirBits(contador, longitud, 16); 
        contador = 2; 
        for (int i = 0; i < longitud; i++) {
            byte elByte = (byte) mensaje[i];
            escribirBits(contador, elByte, 8); 
            contador++;
            if (i % 1000 == 0) System.out.println("Van " + i + " caracteres de " + longitud);
        }
    }

    private void escribirBits(int contador, int valor, int numbits) {
        int bytesPorFila = ancho * 3; 
        for (int i = 0; i < numbits; i++) {
            int fila = (8 * contador + i) / bytesPorFila;
            int col = ((8 * contador + i) % bytesPorFila) / 3;
            int color = ((8 * contador + i) % bytesPorFila) % 3;
            int mascara = (valor >> i) & 1; 
            imagen[fila][col][color] = (byte) ((imagen[fila][col][color] & 0xFE) | mascara);
        }
    }

    public char[] recuperar(char[] cadena, int longitud, BufferedWriter writer, int tamanioPagina, int inicial) throws IOException {
        int bytesFila = ancho * 3;
        int desplazamientoImagen = 0;
    
        int paginaActualMensaje = inicial;
        int desplazamientoMensaje = 0;
    
        for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
            cadena[posCaracter] = 0;
            writer.write(String.format("Mensaje[%d],%d,%d,W\n", posCaracter, paginaActualMensaje, desplazamientoMensaje));
    
            for (int i = 0; i < 8; i++) {
                int numBytes = 16 + (posCaracter * 8) + i;
                int fila = numBytes / bytesFila;
                int col = numBytes % (bytesFila) / 3;
                int componente = (numBytes % bytesFila) % 3;
                String componenteRGB = (componente == 0) ? "R" : (componente == 1) ? "G" : "B";
    
                int numeroPaginaImagen = numBytes / tamanioPagina;
    
                writer.write(String.format("Imagen[%d][%d].%s,%d,%d,R\n", fila, col, componenteRGB, numeroPaginaImagen, desplazamientoImagen));
    
                cadena[posCaracter] |= (imagen[fila][col][componente] & 1) << i;
    
                writer.write(String.format("Mensaje[%d],%d,%d,W\n", posCaracter, paginaActualMensaje, desplazamientoMensaje));
    
                // Actualizar desplazamiento de la imagen
                desplazamientoImagen++;
                if (desplazamientoImagen == tamanioPagina) {
                    desplazamientoImagen = 0;
                }
            }
    
            // Después de 8 iteraciones, actualizar el desplazamiento del mensaje
            desplazamientoMensaje++;
            if (desplazamientoMensaje == tamanioPagina) {
                paginaActualMensaje++;
                desplazamientoMensaje = 0;
            }
        }
    
        return cadena;
    }
    
    

    public void escribirImagen(String output) {
        byte pad = 0;
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(header);
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    pixel[0] = imagen[i][j][0];
                    pixel[1] = imagen[i][j][1];
                    pixel[2] = imagen[i][j][2];
                    fos.write(pixel);
                }
                for (int k = 0; k < padding; k++) fos.write(pad);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int leerLongitudReferencias(BufferedWriter writer, int tamanioPagina) throws IOException {
        int longitud = 0;
        int paginaActual = 0;
        int desplazamiento = 0;
    
        for (int i = 0; i < 16; i++) {
            int col = (i % (ancho * 3)) / 3;
            int componente = (i % (ancho * 3)) % 3;
            String componenteRGB = (componente == 0) ? "R" : (componente == 1) ? "G" : "B";
    
            longitud |= (imagen[0][col][componente] & 1) << i;
    
            // Escribir la referencia del byte leído con el componente adecuado
            writer.write(String.format("Imagen[0][%d].%s,%d,%d,R\n", col, componenteRGB, paginaActual, desplazamiento));
    
            // Actualizar desplazamiento y página
            desplazamiento++;
            if (desplazamiento == tamanioPagina) {
                paginaActual++;
                desplazamiento = 0;
            }
        }
    
        return longitud;
    }

    public int leerLongitud(BufferedWriter writer, int tamanioPagina) throws IOException {
        int longitud = 0;
    
        for (int i = 0; i < 16; i++) {
            int col = (i % (ancho * 3)) / 3;
            int componente = (i % (ancho * 3)) % 3;
    
            longitud |= (imagen[0][col][componente] & 1) << i;
        }
    
        return longitud;
    }

    public void incrementarHit() {
        hits++;
    }

    public void incrementarMiss() {
        misses++;
    }

    public void mostrarEstadisticas() {
        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
    }

    public int getAncho() {
        return ancho;
    }

    public int getAlto() {
        return alto;
    }

    public byte[][][] getImagen() {
        return imagen;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }
}
