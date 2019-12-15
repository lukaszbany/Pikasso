package pl.betweenthelines.pikasso.window.domain.operation.linear;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.window.domain.operation.linear.mask.Mask3x3;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.CV_32F;

public class FilteringUtils {

    public static void applyMaskWithBlur(Mat image, Mask3x3 mask, int borderType, Scalar border) {
        Imgproc.GaussianBlur(image, image, new Size(3, 3), 0);

        applyMask(image, mask, borderType, border);
    }

    public static void applyMask(Mat image, Mask3x3 mask, int borderType, Scalar border) {
        Imgproc.filter2D(image, image, CV_32F, mask.getMat(), new Point(-1, -1), 0, borderType);
        if (border != null) {
            Mat submat = image.submat(1, image.height() - 1, 1, image.width() - 1);
            copyMakeBorder(submat, image, 1, 1, 1, 1, BORDER_ISOLATED, border);
        }
    }

}

