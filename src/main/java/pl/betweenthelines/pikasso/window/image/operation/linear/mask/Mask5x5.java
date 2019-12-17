package pl.betweenthelines.pikasso.window.image.operation.linear.mask;

import lombok.Getter;
import org.opencv.core.Mat;

import static org.opencv.core.CvType.CV_32F;

/**
 * Klasa reprezentuje maskę 3x3.
 */
@Getter
public class Mask5x5 implements Mask {

    /**
     * Nazwa maski.
     */
    private String name;

    /**
     * Tablica wartości w masce.
     */
    private double[] values;

    /**
     * Wielkość maski.
     */
    private int size;

    /**
     * Dzielnik wartości maski potrzebny przy tworzeniu obiektu Mat. Jest to
     * suma wszystkich wartości w masce. Jeśli suma wynosi zero, wartości w masce
     * nie są dzielone.
     */
    private int kernelSize;

    /**
     * Obiekt Mat z wartościami z maski.
     */
    private Mat mat;

    /**
     * Konstruktor tworzący maskę.
     *
     * @param name   nazwa maski
     * @param values tablica wartości w masce (musi być ich 25)
     */
    public Mask5x5(String name, double... values) {
        if (values.length != 25) {
            throw new IllegalArgumentException("Incorrect values number!");
        }

        this.name = name;
        this.values = values;
        this.size = 5;
        this.kernelSize = calculateKernelSize();
        createMat();
    }

    /**
     * Tworzy obiekt Mat z wartości maski.
     */
    private void createMat() {
        mat = new Mat(size, size, CV_32F) {
            {
                int divider = kernelSize != 0 ? kernelSize : 1;

                for (int row = 0; row < size; row++) {
                    for (int col = 0; col < size; col++) {
                        double value = values[row * size + col];
                        put(row, col, value / divider);
                    }
                }
            }
        };
    }

    /**
     * Sumuje wartości w masce i wynik zapisuje jako kernelSize.
     *
     * @return sumę wartości w masce
     */
    private int calculateKernelSize() {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return (int) sum;
    }

    /**
     * Zamienia maskę na tabelkę z jej wartościami.
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%4d%4d%4d%4d%4d\n%4d%4s%4d%4d%4d\n%4d%4d%4d%4d%4d\n%4d%4s%4d%4d%4d\n%4d%4d%4d%4d%4d",
                (int) values[0], (int) values[1], (int) values[2], (int) values[3], (int) values[4],
                (int) values[5], (int) values[6], (int) values[7], (int) values[8], (int) values[9],
                (int) values[10], (int) values[11], (int) values[12], (int) values[13], (int) values[14],
                (int) values[15], (int) values[16], (int) values[17], (int) values[18], (int) values[19],
                (int) values[20], (int) values[21], (int) values[22], (int) values[23], (int) values[24]);
    }
}
