package pl.betweenthelines.pikasso.window.image.histogram;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import lombok.Getter;
import pl.betweenthelines.pikasso.exception.ImageIsTooBigException;
import pl.betweenthelines.pikasso.exception.ImageNotLoadedYetException;

import java.io.IOException;

import static pl.betweenthelines.pikasso.window.image.histogram.ChannelProperties.Channel.*;

@Getter
public class Histogram {

    private static final int MAX_SIDE_SIZE = 40000;
    private static final int LEVELS = 256;

    private Image image;
    private int pixelsTotal;
    private int pixelsInRange;

    private int minLevel;
    private int maxLevel;

    private ChannelProperties red;
    private ChannelProperties green;
    private ChannelProperties blue;
    private ChannelProperties gray;

    private boolean isGrayscale = true;

    public Histogram(Image image, int minLevel, int maxLevel) throws ImageNotLoadedYetException, IOException, ImageIsTooBigException {
        validateImageSize(image);
        this.image = image;
        this.pixelsTotal = (int) (image.getHeight() * image.getWidth());
        this.pixelsInRange = pixelsTotal;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        buildHistogramData();
    }

    private void validateImageSize(Image image) throws ImageIsTooBigException {
        ;
        if (image.getWidth() > MAX_SIDE_SIZE || image.getHeight() > MAX_SIDE_SIZE) {
            throw new ImageIsTooBigException();
        }
    }

    private void buildHistogramData() throws ImageNotLoadedYetException {
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            throw new ImageNotLoadedYetException();
        }

        readRGBA(pixelReader);
    }

    private void readRGBA(PixelReader pixelReader) {
        long[] red = new long[LEVELS];
        long[] green = new long[LEVELS];
        long[] blue = new long[LEVELS];
        long[] gray = new long[LEVELS];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);
                int gg;
                if (r == g && r == b) {
                    gg = r;
                } else {
                    isGrayscale = false;
                    gg = (int) (0.3 * r + 0.59 * g + 0.11 * b);
                }

                red[r]++;
                green[g]++;
                blue[b]++;
                gray[gg]++;
            }
        }

        this.red = new ChannelProperties(RED, red, pixelsTotal, minLevel, maxLevel);
        this.green = new ChannelProperties(GREEN, green, pixelsTotal, minLevel, maxLevel);
        this.blue = new ChannelProperties(BLUE, blue, pixelsTotal, minLevel, maxLevel);
        this.gray = new ChannelProperties(GRAY, gray, pixelsTotal, minLevel, maxLevel);
    }

}
