import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Imagen_2 {
    byte[] header = new byte[54];
    byte[][][] imagen;
    int alto, ancho; // en píxeles
    int padding;
    String nombre;

    /**
     * Método para crear una matriz imagen a partir de un archivo BMP.
     * El formato debe ser BMP de 24 bits de bit depth.
     * @param input: nombre del archivo BMP.
     */
    public Imagen_2(String input) {
        nombre = new String(input);
        try {
            FileInputStream fis = new FileInputStream(nombre);
            fis.read(header);
            
            // Extraer el ancho y alto de la imagen desde la cabecera almacenados en little endian.
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) |
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) |
                   ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);
            
            System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            
            imagen = new byte[alto][ancho][3];
            int rowSizeSinPadding = ancho * 3;
            
            // El tamaño de la fila debe ser múltiplo de 4 bytes.
            padding = (4 - (rowSizeSinPadding % 4)) % 4;
            
            // Leer y modificar los datos de los píxeles (en formato RGB, almacenados en orden BGR).
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    fis.read(pixel);
                    imagen[i][j][0] = pixel[0]; // B
                    imagen[i][j][1] = pixel[1]; // G
                    imagen[i][j][2] = pixel[2]; // R
                }
                fis.skip(padding);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para esconder un valor en una matriz imagen.
     * @param contador: contador de bytes escritos en la matriz.
     * @param valor: valor que se quiere esconder.
     * @param numbits: longitud (en bits) del valor.
     */
    private void escribirBits(int contador, int valor, int numbits) {
        int bytesPorFila = ancho * 3; // ancho de la imagen en bytes
        int mascara;
        
        for (int i = 0; i < numbits; i++) {
            int fila = (8 * contador + i) / bytesPorFila;
            int col = ((8 * contador + i) % bytesPorFila) / 3;
            int color = ((8 * contador + i) % bytesPorFila) % 3;
            
            mascara = valor >> i;
            mascara = mascara & 1;
            imagen[fila][col][color] = (byte)((imagen[fila][col][color] & 0xFE) | mascara);
        }
    }

    /**
     * Método para esconder un mensaje en una matriz imagen.
     * @param mensaje: Mensaje a esconder.
     * @param longitud: longitud del mensaje.
     */
    public void esconder(char[] mensaje, int longitud) {
        int contador = 0;
        byte elByte;
        
        // Esconder la longitud del mensaje en los primeros 16 bits.
        escribirBits(contador, longitud, 16);
        
        // El primer byte del mensaje se almacena después de la longitud (a partir del byte 17).
        contador = 2;
        
        for (int i = 0; i < longitud; i++) {
            elByte = (byte) mensaje[i];
            escribirBits(contador, elByte, 8);
            contador++;
            
            if (i % 1000 == 0) {
                System.out.println("Van " + i + " caracteres de " + longitud);
            }
        }
    }

    /**
     * Método para escribir una imagen a un archivo en formato BMP.
     * @param output: nombre del archivo donde se almacenará la imagen.
     */
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
                for (int k = 0; k < padding; k++) {
                    fos.write(pad);
                }
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para recuperar la longitud del mensaje escondido en una imagen.
     * @return La longitud del mensaje que se esconde en la imagen.
     */
    public int leerLongitud() {
        int longitud = 0;
        
        for (int i = 0; i < 16; i++) {
            int col = (i % (ancho * 3)) / 3;
            longitud = longitud | (imagen[0][col][(i % (ancho * 3)) % 3] & 1) << i;
        }
        return longitud;
    }

    /**
     * Método para recuperar un mensaje escondido en una imagen.
     * @param cadena: vector de char con espacio ya asignado.
     * @param longitud: tamaño del mensaje escondido.
     */
    public void recuperar(char[] cadena, int longitud) {
        int bytesFila = ancho * 3;
        
        for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
            cadena[posCaracter] = 0;
            
            for (int i = 0; i < 8; i++) {
                int numBytes = 16 + (posCaracter * 8) + i;
                int fila = numBytes / bytesFila;
                int col = numBytes % bytesFila / 3;
                
                cadena[posCaracter] = (char) (cadena[posCaracter] |
                        ((imagen[fila][col][numBytes % 3] & 1) << i));
            }
        }
    }
}
