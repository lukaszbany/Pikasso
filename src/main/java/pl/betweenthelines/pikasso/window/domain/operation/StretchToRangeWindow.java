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
import org.controlsfx.control.RangeSlider;
import pl.betweenthelines.pikasso.window.domain.FileData;

import java.awt.image.BufferedImage;

public class StretchToRangeWindow {

    private static final double MIN_LEVEL = 0;
    private static final double MAX_LEVEL = 255;

    ImageView beforeImageView;
    ImageView afterImageView;

    Stage negationStage;
    VBox vBox;
    HBox hBox;

    Image before;
    Image after;
    RangeSlider rangeP;
    RangeSlider rangeQ;
    Slider backgroundSlider;

    public StretchToRangeWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);

        Label p = new Label("Wartość p: ");
        Label p1 = new Label("0");
        p1.setPrefWidth(20);
        Label p2 = new Label("255");
        p2.setPrefWidth(20);
        rangeP = new RangeSlider();
        rangeP.setShowTickMarks(true);
        rangeP.setShowTickLabels(true);
        rangeP.setMin(MIN_LEVEL);
        rangeP.setMax(MAX_LEVEL);
        rangeP.setHighValue(MAX_LEVEL);
        rangeP.setPrefHeight(200);
        rangeP.setPrefWidth(400);
        rangeP.highValueProperty().addListener((observable, oldValue, newValue) -> {
            p1.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
        rangeP.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            p2.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });

        Label q = new Label("Wartość q: ");
        Label q3 = new Label("0");
        q3.setPrefWidth(20);
        Label q4 = new Label("255");
        q4.setPrefWidth(20);
        rangeQ = new RangeSlider();
        rangeQ.setShowTickMarks(true);
        rangeQ.setShowTickLabels(true);
        rangeQ.setMin(MIN_LEVEL);
        rangeQ.setMax(MAX_LEVEL);
        rangeQ.setHighValue(MAX_LEVEL);
        rangeQ.setPrefHeight(200);
        rangeQ.setPrefWidth(400);
        rangeQ.highValueProperty().addListener((observable, oldValue, newValue) -> {
            q3.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
        rangeQ.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            q4.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });

        VBox sliders = new VBox(new HBox(p, p1, rangeP, p2), new HBox(q, q3, rangeQ, q4));

        Label backgroundLabel = new Label("Poziom tła:");
        backgroundSlider = new Slider(MIN_LEVEL, MAX_LEVEL, MIN_LEVEL);
        Label backgroundValue = new Label("0");
        backgroundSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            backgroundValue.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
        HBox backgroundHBox = new HBox(backgroundLabel, backgroundSlider);

        after = stretch();
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);
        hBox = new HBox(beforeImageView, afterImageView);

        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            negationStage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            openedFileData.getImageView().setImage(after);
            negationStage.close();
        });
        HBox buttons = new HBox(cancel, save);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        HBox options = new HBox(sliders, new VBox(backgroundHBox, buttons));
        options.setPadding(new Insets(13, 10, 10, 0));
        options.setSpacing(15);
        options.setMaxHeight(110);
        options.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, options);

        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 110;
        Scene negationScene = new Scene(vBox, windowWidth, windowHeight);

        negationStage = new Stage();
        negationStage.initModality(Modality.APPLICATION_MODAL);

        negationStage.setScene(negationScene);
        negationStage.setTitle("Posteryzacja");
        save.requestFocus();
        negationStage.showAndWait();
    }

    private void reloadPreview() {
        after = stretch();
        afterImageView.setImage(after);
    }

    private Image stretch() {
        BufferedImage resultImage = new BufferedImage((int) before.getWidth(), (int) before.getHeight(), BufferedImage.TYPE_INT_RGB);
        PixelReader pixelReader = before.getPixelReader();

        for (int y = 0; y < before.getHeight(); y++) {
            for (int x = 0; x < before.getWidth(); x++) {
                int argb = pixelReader.getArgb(x, y);
                int a = (0xff & (argb >> 24));
                int r = (0xff & (argb >> 16));
                int g = (0xff & (argb >> 8));
                int b = (0xff & argb);

                int newR = calculateLevel(r);
                int newG = calculateLevel(g);
                int newB = calculateLevel(b);

                int newArgb = (a << 24) | (newR << 16) | (newG << 8) | newB;

                resultImage.setRGB(x, y, newArgb);
            }
        }

        return SwingFXUtils.toFXImage(resultImage, null);
    }

    private int calculateLevel(int p) {
        double p1 = rangeP.getLowValue();
        double p2 = rangeP.getHighValue();
        double q3 = rangeQ.getLowValue();
        double q4 = rangeQ.getHighValue();
        double q5 = backgroundSlider.getValue();

        if (p < p1 || p > p2) {
            return (int) q5;
        }

        return (int) ((p - p1) * ((q4 - q3) / (p2 - p1)) + q3);
    }

}
