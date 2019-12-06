package pl.betweenthelines.pikasso.window.domain.operation.linear;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.window.domain.operation.linear.mask.Mask3x3;

import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.CvType.CV_32F;

public class FilteringUtils {

    public static void applyMaskWithBlur(Mat image, Mask3x3 mask, int borderType) {
        Mat destination = new Mat(image.rows(), image.cols(), image.type());

        Imgproc.GaussianBlur(image, destination, new Size(3, 3), 0);
        Imgproc.filter2D(destination, destination, CV_32F, mask.getMat(), new Point(-1, -1), 0, borderType);

        if (borderType == BORDER_CONSTANT) {
            Mat cropped = destination.submat(1, destination.height() - 1, 1, destination.width() - 1);
            cropped.convertTo(cropped, image.type());
            cropped.copyTo(image.submat(1, image.height() - 1, 1, image.width() - 1));
        } else {
            destination.convertTo(destination, image.type());
            destination.copyTo(image);
        }

    }

}

