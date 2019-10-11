package pl.betweenthelines.pikasso.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;

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
}
