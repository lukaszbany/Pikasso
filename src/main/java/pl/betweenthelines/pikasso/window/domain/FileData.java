package pl.betweenthelines.pikasso.window.domain;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.File;

@Data
@AllArgsConstructor
public class FileData {

    private File file;
    private ImageView imageView;
    private Image imageSelection;

}
