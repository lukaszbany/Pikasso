package pl.betweenthelines.pikasso.window.domain.operation.linear;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class ScalingUtils {

    public static final byte DO_NOTHING = 0;
    public static final byte METHOD_1 = 1;
    public static final byte METHOD_2 = 2;
    public static final byte METHOD_3 = 3;

    private static final int MAX_LEVEL = 255;
    private static final int MIN_LEVEL = 0;
    private static final List<Byte> availableMethods = Arrays.asList(DO_NOTHING, METHOD_1, METHOD_2, METHOD_3);

    public static Image scale(Image image, byte method) {
        if (method == DO_NOTHING) {
            return image;
        }
        if (!availableMethods.contains(method)) {
            throw new IllegalArgumentException();
        }

        BufferedImage resultImage = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_RGB);
        PixelReader pixels = image.getPixelReader();

        int maxR, maxG, maxB;
        maxR = maxG = maxB = MIN_LEVEL;
        int minR, minG, minB;
        minR = minG = minB = MAX_LEVEL;

        if (method == METHOD_1) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = pixels.getArgb(x, y);
                    int r = (0xff & (argb >> 16));
                    int g = (0xff & (argb >> 8));
                    int b = (0xff & argb);

                    if (r > maxR) maxR = r;
                    if (r < minR) minR = r;
                    if (g > maxG) maxG = g;
                    if (g < minG) minG = g;
                    if (b > maxB) maxB = b;
                    if (b < minB) minB = b;
                }
            }
        }

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = pixels.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);

                int newR = calculateLevel(r, minR, maxR, method);
                int newG = calculateLevel(g, minG, maxG, method);
                int newB = calculateLevel(b, minB, maxB, method);

                int newArgb = (a << 24) | (newR << 16) | (newG << 8) | newB;
                resultImage.setRGB(x, y, newArgb);
            }
        }

        return SwingFXUtils.toFXImage(resultImage, null);
    }

    private static int calculateLevel(int oldLevel, int min, int max, byte method) {
        switch (method) {
            case METHOD_1:
                return (oldLevel - min) / (max - min) * MAX_LEVEL;
            case METHOD_2:
                if (oldLevel < 0) return 0;
                if (oldLevel == 0) return MAX_LEVEL / 2;
                return MAX_LEVEL;
            case METHOD_3:
                if (oldLevel < 0) return 0;
                if (oldLevel <= MAX_LEVEL) return oldLevel;
                return MAX_LEVEL;
        }

        throw new IllegalArgumentException();
    }
}
