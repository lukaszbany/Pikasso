package pl.betweenthelines.pikasso.window.image.operation.linear.mask;

import lombok.Getter;
import org.opencv.core.Mat;

import static org.opencv.core.CvType.CV_32F;

/**
 * Klasa reprezentuje maskę 3x3.
 */
@Getter
public class Mask3x3 implements Mask {

    /**
     * Nazwa maski.
     */
    private String name;

    /**
     * Flaga oznaczająca, że maska jest parametryzowana.
     */
    private boolean isParametrized;

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
     * @param name           nazwa maski
     * @param isParametrized czy jest parametryzowana
     * @param values         tablica wartości w masce (musi być ich 9)
     */
    public Mask3x3(String name, boolean isParametrized, double... values) {
        if (values.length != 9) {
            throw new IllegalArgumentException("Incorrect values number!");
        }

        this.name = name;
        this.isParametrized = isParametrized;
        this.values = values;
        this.size = 3;
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
                put(0, 0, (values[0] / divider));
                put(0, 1, (values[1] / divider));
                put(0, 2, (values[2] / divider));

                put(1, 0, (values[3] / divider));
                put(1, 1, (values[4] / divider));
                put(1, 2, (values[5] / divider));

                put(2, 0, (values[6] / divider));
                put(2, 1, (values[7] / divider));
                put(2, 2, (values[8] / divider));
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
     * Zmienia wartość środkową maski parametryzowanej i przelicza maskę.
     *
     * @param newValue nowa wartość środkowa
     */
    public void updateMiddleElement(double newValue) {
        values[4] = newValue;
        this.kernelSize = calculateKernelSize();
        createMat();
    }

    /**
     * Zamienia maskę na tabelkę z jej wartościami.
     *
     * @return
     */
    @Override
    public String toString() {
        return String.format("%4d%4d%4d\n%4d%4s%4d\n%4d%4d%4d",
                (int) values[0], (int) values[1], (int) values[2],
                (int) values[3], (isParametrized ? "k" : (int) values[4]), (int) values[5],
                (int) values[6], (int) values[7], (int) values[8]);
    }
}
