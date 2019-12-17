package pl.betweenthelines.pikasso.window.image.histogram;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.PreviewWindow;
import pl.betweenthelines.pikasso.window.image.FileData;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.normalize;

public class StretchHistogram {

    private static final int MIN_LEVEL = 0;
    private static final int MAX_LEVEL = 255;

    public static Image stretchHistogram(FileData openedFileData, Histogram histogram) {
        Image before = openedFileData.getImageView().getImage();

        Mat inImage = ImageUtils.imageToMat(before);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);
        normalize(outImage, outImage, MIN_LEVEL, MAX_LEVEL, NORM_MINMAX);

        Image after = ImageUtils.mat2Image(outImage);
        PreviewWindow previewWindow = new PreviewWindow(before, after);
        return previewWindow.getResult();
    }


}
