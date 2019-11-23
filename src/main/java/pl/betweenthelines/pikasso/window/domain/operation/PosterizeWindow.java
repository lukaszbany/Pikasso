package pl.betweenthelines.pikasso.window.domain.operation;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pl.betweenthelines.pikasso.window.domain.FileData;

import java.awt.image.BufferedImage;

public class PosterizeWindow {

    private static final double MIN_LEVEL = 2;
    private static final double DEFAULT = 8;
    private static final double MAX_LEVEL = 16;

    ImageView beforeImageView;
    ImageView afterImageView;

    Stage negationStage;
    VBox vBox;
    HBox hBox;

    Image before;
    Image after;
    double currentLevel;

    public PosterizeWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);

        currentLevel = DEFAULT;
        after = posterize();
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);

        hBox = new HBox(beforeImageView, afterImageView);

        Label value = new Label("8");
        value.setPrefWidth(20);
        Slider slider = new Slider(MIN_LEVEL, MAX_LEVEL, DEFAULT);
        slider.setPrefWidth(100);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.intValue());
            value.setText(String.valueOf(newValue.intValue()));
        });
        slider.setOnMouseReleased(event -> {
            currentLevel = slider.getValue();
            reloadPreview();
        });

        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            negationStage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            openedFileData.getImageView().setImage(after);
            negationStage.close();
        });
        HBox buttons = new HBox(slider, value, cancel, save);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);

        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene negationScene = new Scene(vBox, windowWidth, windowHeight);

        negationStage = new Stage();
        negationStage.initModality(Modality.APPLICATION_MODAL);

        negationStage.setScene(negationScene);
        negationStage.setTitle("Posteryzacja");
        save.requestFocus();
        negationStage.showAndWait();
    }

    private void reloadPreview() {
        after = posterize();
        afterImageView.setImage(after);
    }

    private Image posterize() {
        BufferedImage resultImage = new BufferedImage((int) before.getWidth(), (int) before.getHeight(), BufferedImage.TYPE_INT_RGB);
        PixelReader pixelReader = before.getPixelReader();

        int multiplier = 255 / (int) (currentLevel - 1);
        int divider = 255 / (int) currentLevel;

        for (int y = 0; y < before.getHeight(); y++) {
            for (int x = 0; x < before.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);

                int newR = calculateLevel(r, multiplier, divider);
                int newG = calculateLevel(g, multiplier, divider);
                int newB = calculateLevel(b, multiplier, divider);

                int newArgb = (a << 24) | (newR << 16) | (newG << 8) | newB;

                resultImage.setRGB(x, y, newArgb);
            }
        }

        return SwingFXUtils.toFXImage(resultImage, null);
    }

    private int calculateLevel(int oldLevel, int multiplier, int divider) {
        int newLevel = 0;

        while (oldLevel > divider) {
            oldLevel -= multiplier;
            newLevel += multiplier;
        }

        return (int) Math.min(newLevel, 255);
    }

}
