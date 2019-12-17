package pl.betweenthelines.pikasso.window.image.operation.directional;

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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.HistogramWindow;
import pl.betweenthelines.pikasso.window.image.FileData;
import pl.betweenthelines.pikasso.window.image.operation.linear.MatScalingUtils;

import static javafx.geometry.Orientation.VERTICAL;
import static org.opencv.core.Core.*;
import static pl.betweenthelines.pikasso.window.image.operation.linear.MatScalingUtils.*;

public class SobelFilterWindow {

    private static final int BORDER_MINIMUM = 254;
    private static final int BORDER_MAXIMUM = 255;
    private static final int OPTIONS_HEIGHT = 160;
    private static final int MINIMAL_WIDTH = 550;

    private static final byte SOBEL_X = 1;
    private static final byte SOBEL_Y = 2;
    private static final byte SOBEL_XY = 3;

    private ImageView beforeImageView;
    private ImageView afterImageView;

    private Stage stage;
    private VBox vBox;
    private HBox hBox;

    private Image before;
    private Image after;
    private double times;

    private byte currentMask;
    private int currentBorderType;
    private Scalar border;
    private byte currentScalingMethod;
    private boolean scharrFilter;
    CheckBox scharrCheckbox;

    public SobelFilterWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();

        ToggleGroup options = new ToggleGroup();
        RadioButton mask1 = createMaskRadioButton(options, "X", SOBEL_X);
        RadioButton mask2 = createMaskRadioButton(options, "Y", SOBEL_Y);
        RadioButton mask3 = createMaskRadioButton(options, "XY", SOBEL_XY);
        mask1.setSelected(true);
        handleOptionChanges(options);
        scharrCheckbox = new CheckBox("Filtr Scharra");
        scharrCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            scharrFilter = newValue;
            reloadPreview();
        });

        currentMask = SOBEL_X;
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

        HBox masksHBox = new HBox(mask1, mask2, mask3, scharrCheckbox);
        masksHBox.setSpacing(15);
        masksHBox.setPrefHeight(60);
        masksHBox.setAlignment(Pos.CENTER);
        HBox radioHBox = new HBox(borderVBox, new Separator(VERTICAL), scalingVBox);
        radioHBox.setSpacing(15);

        HBox buttons = new HBox(new VBox(masksHBox, new Separator(), radioHBox), new Separator(VERTICAL), buttonsTimesVbox);
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
        stage.setTitle("Filtr Sobela");
        save.requestFocus();
        stage.showAndWait();
    }

    private VBox createBorderOptions() {
        ToggleGroup borderTypeGroup = new ToggleGroup();
        Label borderTypeLabel = new Label("Piksele brzegowe:");

        RadioButton replicatedBorder = new RadioButton("Bez zmian");
        replicatedBorder.setUserData(Core.BORDER_CONSTANT);
        replicatedBorder.setToggleGroup(borderTypeGroup);
        replicatedBorder.setSelected(true);

        RadioButton reflectedBorder = new RadioButton("Powielenie pikseli brzegowych");
        reflectedBorder.setUserData(Core.BORDER_REPLICATE);
        reflectedBorder.setToggleGroup(borderTypeGroup);

        RadioButton existingBorder = new RadioButton("Istniejące sąsiedztwo");
        existingBorder.setUserData(Core.BORDER_DEFAULT);
        existingBorder.setToggleGroup(borderTypeGroup);

        RadioButton minimum = new RadioButton("Wartość minimalna");
        minimum.setUserData(BORDER_MINIMUM);
        minimum.setToggleGroup(borderTypeGroup);

        RadioButton maximum = new RadioButton("Wartość maksymalna");
        maximum.setUserData(BORDER_MAXIMUM);
        maximum.setToggleGroup(borderTypeGroup);

        borderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            int selected = (int) newValue.getUserData();
            if (selected == BORDER_MINIMUM) {
                currentBorderType = Core.BORDER_CONSTANT;
                border = new Scalar(0, 0, 0, 255);
            } else if (selected == BORDER_MAXIMUM) {
                currentBorderType = Core.BORDER_CONSTANT;
                border = new Scalar(255, 255, 255, 255);
            } else {
                currentBorderType = selected;
                border = null;
            }

            reloadPreview();
        });

        return new VBox(borderTypeLabel, replicatedBorder, reflectedBorder, existingBorder, minimum, maximum);
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

    private void handleOptionChanges(ToggleGroup options) {
        options.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                changeCurrentMask(newValue);
            }
        });
    }

    private void changeCurrentMask(Toggle newValue) {
        currentMask = (byte) newValue.getUserData();
        handleScharrFilter();
        reloadPreview();
    }

    private void handleScharrFilter() {
        if (currentMask == SOBEL_XY) {
            scharrFilter = false;
            scharrCheckbox.setDisable(true);
        } else {
            scharrCheckbox.setDisable(false);
            scharrFilter = scharrCheckbox.isSelected();
        }
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

        int kernelSize = scharrFilter && currentMask != SOBEL_XY ? -1 : 3;
        int dx = 0;
        int dy = 0;
        switch (currentMask) {
            case SOBEL_X:
                dx = 1;
                break;
            case SOBEL_Y:
                dy = 1;
                break;
            case SOBEL_XY:
                dx = 1;
                dy = 1;
        }

        Imgproc.Sobel(image, image, -1, dx, dy, kernelSize, 1, 0, currentBorderType);
        if (border != null) {
            Mat submat = image.submat(1, image.height() - 1, 1, image.width() - 1);
            copyMakeBorder(submat, image, 1, 1, 1, 1, BORDER_ISOLATED, border);
        }
    }

}
