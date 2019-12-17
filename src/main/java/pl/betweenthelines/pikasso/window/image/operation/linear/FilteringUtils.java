package pl.betweenthelines.pikasso.window.image.operation.linear;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.window.image.operation.linear.mask.Mask;
import pl.betweenthelines.pikasso.window.image.operation.linear.mask.Mask3x3;

import static org.opencv.core.Core.BORDER_ISOLATED;
import static org.opencv.core.Core.copyMakeBorder;
import static org.opencv.core.CvType.CV_32F;

/**
 * Klasa pomocnicza zawierająca metody do przeprowadzania filtrowania 2D.
 */
public class FilteringUtils {

    /**
     * Przeprowadza filtrację podaną maską ze wstępny rozmyciem gaussowskim.
     *
     * @param image      obraz wejściowy
     * @param mask       maska filtrowania
     * @param borderType metoda operacji na pikselach brzegowych
     * @param border     wartość pikseli brzegowych (jeżeli stała)
     */
    public static void applyMaskWithBlur(Mat image, Mask3x3 mask, int borderType, Scalar border) {
        Imgproc.GaussianBlur(image, image, new Size(3, 3), 0);

        applyMask(image, mask, borderType, border);
    }

    /**
     * Przeprowadza filtrację podaną maską.
     *
     * @param image      obraz wejściowy
     * @param mask       maska filtrowania (3x3 lub 5x5)
     * @param borderType metoda operacji na pikselach brzegowych
     * @param border     wartość pikseli brzegowych (jeżeli stała)
     */
    public static void applyMask(Mat image, Mask mask, int borderType, Scalar border) {
        Imgproc.filter2D(image, image, CV_32F, mask.getMat(), new Point(-1, -1), 0, borderType);
        handleBorder(image, border);
    }

    /**
     * Przeprowadza dwie filtrację - najpierw maską 1, później maską 2.
     *
     * @param image      obraz wejściowy
     * @param mask1      maska filtrowania 1
     * @param mask2      maska filtrowania 2
     * @param borderType metoda operacji na pikselach brzegowych
     * @param border     wartość pikseli brzegowych (jeżeli stała)
     */
    public static void applyMasks(Mat image, Mask3x3 mask1, Mask3x3 mask2, int borderType, Scalar border) {
        Imgproc.filter2D(image, image, CV_32F, mask1.getMat(), new Point(-1, -1), 0, borderType);
        Imgproc.filter2D(image, image, CV_32F, mask2.getMat(), new Point(-1, -1), 0, borderType);
        handleBorder(image, border);
    }

    /**
     * Przeprowadza operację na pikselach brzegowych obrazu - jeśli mają mieć stałą wartość.
     *
     * @param image  obraz
     * @param border wartość pikseli brzegowych
     */
    public static void handleBorder(Mat image, Scalar border) {
        if (border != null) {
            Mat submat = image.submat(1, image.height() - 1, 1, image.width() - 1);
            copyMakeBorder(submat, image, 1, 1, 1, 1, BORDER_ISOLATED, border);
        }
    }

}

