package pl.betweenthelines.pikasso.window.domain.operation.directional;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.HistogramWindow;
import pl.betweenthelines.pikasso.window.domain.FileData;
import pl.betweenthelines.pikasso.window.domain.operation.linear.MatScalingUtils;
import pl.betweenthelines.pikasso.window.domain.operation.linear.ScalingUtils;

import static javafx.geometry.Orientation.VERTICAL;
import static org.opencv.core.Core.BORDER_CONSTANT;
import static pl.betweenthelines.pikasso.window.domain.operation.linear.ScalingUtils.*;

public class RobertsFilterWindow {

    private static final int OPTIONS_HEIGHT = 160;
    private static final int MINIMAL_WIDTH = 550;

    private static final int MIN_LEVEL = 0;
    private static final int MAX_LEVEL = 255;

    private ImageView beforeImageView;
    private ImageView afterImageView;

    private Stage stage;
    private VBox vBox;
    private HBox hBox;

    private Image before;
    private Image after;
    private double times;
    RangeSlider rangeSlider;

    private int currentBorderType;
    private byte currentScalingMethod;
    private boolean l2Gradient;

    public RobertsFilterWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();

        TextField min = new TextField(String.valueOf(MIN_LEVEL));
        min.setDisable(true);
        min.setMaxWidth(40);
        TextField max = new TextField(String.valueOf(MAX_LEVEL));
        max.setDisable(true);
        max.setMaxWidth(40);
        rangeSlider = createRangeSlider(min, max);
        CheckBox l2Checkbox = new CheckBox("L2 norm");
        l2Checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            l2Gradient = newValue;
            reloadPreview();
        });

        currentBorderType = Core.BORDER_CONSTANT;
        currentScalingMethod = METHOD_3;
        times = 1;

        createBeforeImageView();
        createAfterImageView();

        HBox beforeImageViewHbox = new HBox(beforeImageView);
        beforeImageViewHbox.setAlignment(Pos.CENTER);
        HBox afterImageViewHbox = new HBox(afterImageView);
        afterImageViewHbox.setAlignment(Pos.CENTER);
        hBox = new HBox(beforeImageViewHbox, afterImageViewHbox);
        hBox.setAlignment(Pos.CENTER);

        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            stage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            openedFileData.setImage(after);
            stage.close();
        });

        Slider timesSlider = new Slider(1, 20, 1);
        timesSlider.setPrefWidth(120);
        Label timesValue = new Label("1x");
        timesValue.setPrefWidth(30);
        timesSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            timesValue.setText(newValue.intValue() + "x");
            times = newValue.intValue();
            reloadPreview();
        });
        HBox buttonsHbox = new HBox(cancel, save);
        buttonsHbox.setSpacing(15);
        buttonsHbox.setAlignment(Pos.CENTER);
        HBox timesSliderHBox = new HBox(timesSlider, timesValue);
        timesSliderHBox.setAlignment(Pos.CENTER_RIGHT);
        VBox buttonsTimesVbox = new VBox(timesSliderHBox, buttonsHbox);
        buttonsTimesVbox.setAlignment(Pos.CENTER_RIGHT);
        buttonsTimesVbox.setSpacing(15);

        VBox borderVBox = createBorderOptions();
        VBox scalingVBox = createScalingOptions();

        HBox sliderHBox = new HBox(min, rangeSlider, max, l2Checkbox);
        sliderHBox.setSpacing(15);
        sliderHBox.setPrefHeight(60);
        sliderHBox.setAlignment(Pos.CENTER);
        HBox radioHBox = new HBox(borderVBox, new Separator(VERTICAL), scalingVBox);
        radioHBox.setSpacing(15);
        radioHBox.setAlignment(Pos.CENTER);

        HBox buttons = new HBox(new VBox(sliderHBox, new Separator(), radioHBox), new Separator(VERTICAL), buttonsTimesVbox);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(OPTIONS_HEIGHT);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);

        double windowWidth = Math.max(MINIMAL_WIDTH, afterImageView.getBoundsInLocal().getWidth() * 2);
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + OPTIONS_HEIGHT;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) stage.close();
        });
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Filtr Robertsa");
        save.requestFocus();
        stage.showAndWait();
    }

    private RangeSlider createRangeSlider(TextField min, TextField max) {
        RangeSlider range = new RangeSlider();
        range.setShowTickMarks(true);
        range.setShowTickLabels(true);
        range.setMin(MIN_LEVEL);
        range.setMax(MAX_LEVEL);
        range.setHighValue(MAX_LEVEL);
        range.setPrefWidth(200);
        range.highValueProperty().addListener(handleChanges(max));
        range.lowValueProperty().addListener(handleChanges(min));
        return range;
    }

    private ChangeListener<Number> handleChanges(TextField max) {
        return (observable, oldValue, newValue) -> {
            max.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        };
    }

    private VBox createBorderOptions() {
        ToggleGroup borderTypeGroup = new ToggleGroup();
        Label borderTypeLabel = new Label("Piksele brzegowe:");

        RadioButton replicatedBorder = new RadioButton("Bez zmian");
        replicatedBorder.setUserData(Core.BORDER_CONSTANT);
        replicatedBorder.setToggleGroup(borderTypeGroup);
        replicatedBorder.setSelected(true);

        RadioButton existingBorder = new RadioButton("Istniejące sąsiedztwo");
        existingBorder.setUserData(Core.BORDER_DEFAULT);
        existingBorder.setToggleGroup(borderTypeGroup);

        borderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            currentBorderType = (int) newValue.getUserData();
            reloadPreview();
        });

        return new VBox(borderTypeLabel, replicatedBorder, existingBorder);
    }

    private VBox createScalingOptions() {
        ToggleGroup scalingTypeGroup = new ToggleGroup();
        Label borderTypeLabel = new Label("Metoda skalowania:");

        RadioButton method1 = new RadioButton("Równomierna");
        method1.setUserData(METHOD_1);
        method1.setToggleGroup(scalingTypeGroup);

        RadioButton method2 = new RadioButton("Trójwartościowa");
        method2.setUserData(METHOD_2);
        method2.setToggleGroup(scalingTypeGroup);

        RadioButton method3 = new RadioButton("Odcinająca");
        method3.setUserData(METHOD_3);
        method3.setToggleGroup(scalingTypeGroup);
        method3.setSelected(true);

        scalingTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            currentScalingMethod = (byte) newValue.getUserData();
            reloadPreview();
        });

        return new VBox(borderTypeLabel, method3, method1, method2);
    }

    private void createAfterImageView() {
        after = applyMask();
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);
        afterImageView.setOnMousePressed(event -> {
            try {
                new HistogramWindow(afterImageView);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
    }

    private RadioButton createMaskRadioButton(ToggleGroup options, String text, byte mask) {
        RadioButton maskButton = new RadioButton(text);
        maskButton.setUserData(mask);
        maskButton.setToggleGroup(options);
        maskButton.setPrefHeight(20);
        return maskButton;
    }

    private void createBeforeImageView() {
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);
        beforeImageView.setOnMousePressed(event -> {
            try {
                new HistogramWindow(beforeImageView);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
    }

    private void reloadPreview() {
        after = applyMask();
        afterImageView.setImage(after);
    }

    private Image applyMask() {
        Mat image = ImageUtils.imageToMat(before);
        applyMask(image);

        return ImageUtils.mat2Image(image);
    }

    private void applyMask(Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);

        for (int i = 0; i < times; i++) {
            apply(image);
        }

        MatScalingUtils.scale(image, currentScalingMethod);
    }

    public void apply(Mat image) {
        Imgproc.GaussianBlur(image, image, new Size(3, 3), 0);

        int low = (int) rangeSlider.getLowValue();
        int high = (int) rangeSlider.getHighValue();

        Imgproc.Canny(image, image, low, high, 3, l2Gradient);
    }

}
