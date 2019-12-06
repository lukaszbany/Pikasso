package pl.betweenthelines.pikasso.window.domain.histogram;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.PreviewWindow;
import pl.betweenthelines.pikasso.window.domain.FileData;

public class EqualizeHistogram {

    public static Image equalizeHistogram(FileData openedFileData) {
        Image before = openedFileData.getImageView().getImage();

        Mat inImage = ImageUtils.imageToMat(before);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(outImage, outImage);

        Image after = ImageUtils.mat2Image(outImage);
        PreviewWindow previewWindow = new PreviewWindow(before, after);
        return previewWindow.getResult();
    }
}