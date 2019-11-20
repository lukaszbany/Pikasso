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

public class ImageUtils {
    public static BufferedImage getBufferedImage(ImageView imageView) {
        return SwingFXUtils.fromFXImage(imageView.snapshot(null, null), null);
    }

    public static Image getFxImage(ImageView imageView) {
        BufferedImage bufferedImage = getBufferedImage(imageView);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static WritableImage getImageSelection(ImageView imageView, double x, double y, double width, double height) {
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        Rectangle2D rectangle2D = new Rectangle2D(x, y, width, height);
        snapshotParameters.setViewport(rectangle2D);
        snapshotParameters.setFill(Color.TRANSPARENT);
        WritableImage imageSelection = new WritableImage((int) width, (int) height);
        imageView.snapshot(snapshotParameters, imageSelection);

        return imageSelection;
    }

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

    public static Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);

        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static Image toGrayscale(Image image) {
        Mat inImage = ImageUtils.imageToMat(image);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);

        return ImageUtils.mat2Image(outImage);
    }
}
