package pl.betweenthelines.pikasso.window.domain.operation.linear.mask;

import lombok.Getter;
import org.opencv.core.Mat;

import static org.opencv.core.CvType.CV_32F;

@Getter
public class Mask5x5 implements Mask {

    private String name;
    private double[] values;
    private int size;
    private int kernelSize;
    private Mat mat;

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

    private int calculateKernelSize() {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return (int) sum;
    }

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
