package pl.betweenthelines.pikasso.window.domain.operation;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.domain.FileData;

public class ThresholdOneArgWindow {

    private static final double MIN_LEVEL = 0;
    private static final double MAX_LEVEL = 255;

    ImageView beforeImageView;
    ImageView afterImageView;

    Stage stage;
    VBox vBox;
    HBox hBox;

    Image before;
    Image after;
    double currentLevel;
    boolean inverted;
    boolean preserveGrayscale;

    public ThresholdOneArgWindow(FileData openedFileData) {
        before = ImageUtils.toGrayscale(openedFileData.getImageView().getImage());
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);

        currentLevel = 0;
        after = threshold();
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);

        hBox = new HBox(beforeImageView, afterImageView);

        CheckBox preserveGray = new CheckBox("Zachowaj poziom szarości");
        preserveGray.selectedProperty().addListener((observable, oldValue, newValue) -> {
            preserveGrayscale = newValue;
            reloadPreview();
        });
        CheckBox invert = new CheckBox("Odwrotne");
        invert.selectedProperty().addListener((observable, oldValue, newValue) -> {
            inverted = newValue;
            reloadPreview();
        });
        Label value = new Label("0");
        value.setPrefWidth(20);
        Slider slider = new Slider(MIN_LEVEL, MAX_LEVEL, MIN_LEVEL);
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
            stage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            openedFileData.setImage(after);
            stage.close();
        });
        HBox buttons = new HBox(preserveGray, invert, slider, value, cancel, save);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);

        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.setTitle("Progowanie");
        save.requestFocus();
        stage.showAndWait();
    }

    private void reloadPreview() {
        after = threshold();
        afterImageView.setImage(after);
    }

    private Image threshold() {
        Mat inImage = ImageUtils.imageToMat(before);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);
        if (preserveGrayscale) {
            Imgproc.threshold(outImage, outImage, currentLevel, MAX_LEVEL, inverted ? Imgproc.THRESH_TOZERO_INV : Imgproc.THRESH_TOZERO);
        } else {
            Imgproc.threshold(outImage, outImage, currentLevel, MAX_LEVEL, inverted ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY);
        }

        return ImageUtils.mat2Image(outImage);
    }

}
