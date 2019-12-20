package pl.betweenthelines.pikasso.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 * Klasa pomocnicza do wykonywania operacji na obrazie, takich jak
 * konwersja pomiędzy obiektami obsługiwanymi przez JavaFX lub obiektami
 * biblioteki OpenCV.
 */
public class ImageUtils {

    /**
     * Pobiera aktualny obraz z ImageView (np. poddany modyfikacjom)
     * i konwertuje do obiektu <tt>BufferedImage</tt>.
     *
     * @param imageView zawierające Image.
     * @return wynikowy <tt>BufferedImage</tt>
     */
    public static BufferedImage getBufferedImage(ImageView imageView) {
        return SwingFXUtils.fromFXImage(imageView.snapshot(null, null), null);
    }

    /**
     * Pobiera aktualny obraz z ImageView (np. poddany modyfikacjom)
     * i konwertuje do obiektu <tt>Image</tt>.
     *
     * @param imageView zawierające Image.
     * @return wynikowy <tt>Image</tt>
     */
    public static Image getFxImage(ImageView imageView) {
        BufferedImage bufferedImage = getBufferedImage(imageView);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Pobiera fragment obrazu na podstawie podanych parametrów
     * i zwraca jako obiekt <tt>WritableImage</tt>.
     *
     * @param imageView z obrazem
     * @param x         współrzędna x
     * @param y         współrzędna y
     * @param width     szerokość
     * @param height    wysokość
     * @return obiekt <tt>WritableImage</tt> z fragmentem obrazu.
     */
    public static WritableImage getImageSelection(ImageView imageView, double x, double y, double width, double height) {
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        Rectangle2D rectangle2D = new Rectangle2D(x, y, width, height);
        snapshotParameters.setViewport(rectangle2D);
        snapshotParameters.setFill(Color.TRANSPARENT);
        WritableImage imageSelection = new WritableImage((int) width, (int) height);
        imageView.snapshot(snapshotParameters, imageSelection);

        return imageSelection;
    }

    /**
     * Konwertuje obiekt Image do obiektu Mat obsługiwanego przez
     * bibliotekę OpenCV.
     *
     * @param image obraz do konwersji
     * @return obiekt <tt>Mat</tt> z obrazem
     */
    public static Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);

        return mat;
    }

    /**
     * Konwertuje obiekt Mat do obietku Image obsługiwanego przez Javę.
     *
     * @param mat obraz do konwersji
     * @return obiekt <tt>Image</tt> z obrazem.
     */
    public static Image mat2Image(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);

        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    /**
     * Sprowadza podany obraz do skali szarości.
     *
     * @param image wejściowy obraz
     * @return obiekt <tt>Image</tt> w skali szarości
     */
    public static Image toGrayscale(Image image) {
        Mat inImage = ImageUtils.imageToMat(image);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);

        return ImageUtils.mat2Image(outImage);
    }

    /**
     * Zamienia obraz na szaro-odcieniowy i binaryzuje go.
     *
     * @param image obraz do zbinaryzowania
     */
    public static void binarize(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(image, image, 0, 255, THRESH_BINARY);
    }

    public static Image binarize(Image image) {
        Mat mat = imageToMat(image);
        binarize(mat);

        return mat2Image(mat);
    }
}
