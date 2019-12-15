package pl.betweenthelines.pikasso.window.domain;

import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import lombok.Data;

import java.io.File;

@Data
public class FileData {

    private File file;
    private ImageView imageView;
    private Image imageSelection;
    private Rectangle selection;
    private Image previousImage;
    private MenuItem undoItem;

    public FileData(File file, ImageView imageView, MenuItem undoItem) {
        this.file = file;
        this.imageView = imageView;
        this.undoItem = undoItem;
        undoItem.setDisable(true);
    }

    public FileData(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setImage(Image image) {
        previousImage = imageView.getImage();
        imageView.setImage(image);
        undoItem.setDisable(false);
    }

    public void undo() {
        imageView.setImage(previousImage);
        previousImage = null;
        undoItem.setDisable(true);
    }


}
