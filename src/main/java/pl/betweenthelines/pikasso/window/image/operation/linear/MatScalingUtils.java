package pl.betweenthelines.pikasso.window.image.operation.linear;

import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.List;

public class MatScalingUtils {

    public static final byte DO_NOTHING = 0;
    public static final byte METHOD_1 = 1;
    public static final byte METHOD_2 = 2;
    public static final byte METHOD_3 = 3;

    private static final double MAX_LEVEL = 255;
    private static final double HALF_LEVEL = 127;
    private static final double MIN_LEVEL = 0;
    private static final List<Byte> availableMethods = Arrays.asList(DO_NOTHING, METHOD_1, METHOD_2, METHOD_3);

    public static void scale(Mat mat, byte method) {
        if (method == DO_NOTHING) {
            return;
        }
        if (!availableMethods.contains(method)) {
            throw new IllegalArgumentException();
        }

        int channels = mat.channels();
        double[] max = new double[channels];
        Arrays.fill(max, MIN_LEVEL);
        double[] min = new double[channels];
        Arrays.fill(min, MIN_LEVEL);

        if (method == METHOD_1) {
            for (int y = 0; y < mat.height(); y++) {
                for (int x = 0; x < mat.width(); x++) {
                    double[] levels = mat.get(y, x);

                    for (int ch = 0; ch < channels; ch++) {
                        double level = levels[ch];

                        if (level > max[ch]) max[ch] = level;
                        if (level < min[ch]) min[ch] = level;
                    }
                }
            }
        }

        for (int y = 0; y < mat.height(); y++) {
            for (int x = 0; x < mat.width(); x++) {
                double[] levels = mat.get(y, x);

                for (int ch = 0; ch < channels; ch++) {
                    double level = levels[ch];

                    double newLevel = calculateLevel(level, min[ch], max[ch], method);
                    levels[ch] = newLevel;
                }

                mat.put(y, x, levels);
            }
        }
    }

    private static double calculateLevel(double oldLevel, double min, double max, byte method) {
        switch (method) {
            case METHOD_1:
                return (oldLevel - min) / (max - min) * MAX_LEVEL;
            case METHOD_2:
                if (oldLevel < 0) return 0;
                if (oldLevel == 0) return HALF_LEVEL;
                return MAX_LEVEL;
            case METHOD_3:
                if (oldLevel < 0) return 0;
                if (oldLevel <= MAX_LEVEL) return oldLevel;
                return MAX_LEVEL;
        }

        throw new IllegalArgumentException();
    }
}
