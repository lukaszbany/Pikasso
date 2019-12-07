package pl.betweenthelines.pikasso.window.domain.operation.median;

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
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.domain.FileData;

import static javafx.geometry.Orientation.VERTICAL;
import static org.opencv.core.Core.BORDER_CONSTANT;

public class MedianFilterWindow {

    private static final int OPTIONS_HEIGHT = 80;
    private static final int MINIMAL_WIDTH = 550;

    private static final int KERNEL_3x3 = 3;
    private static final int KERNEL_3x5 = 35;
    private static final int KERNEL_5x3 = 53;
    private static final int KERNEL_5x5 = 5;
    private static final int KERNEL_7x7 = 7;

    private ImageView beforeImageView;
    private ImageView afterImageView;

    private Stage stage;
    private VBox vBox;
    private HBox hBox;

    private Image before;
    private Image after;
    private Slider slider;
    private double times;
    private int currentKernelSize;

    private int currentBorderType;

    public MedianFilterWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();

        ToggleGroup options = new ToggleGroup();
        RadioButton mask1 = createMaskRadioButton(options, "3x3", KERNEL_3x3);
//        RadioButton mask2 = createMaskRadioButton(options, "3x5", KERNEL_3x5);
//        RadioButton mask3 = createMaskRadioButton(options, "5x3", KERNEL_5x3);
        RadioButton mask4 = createMaskRadioButton(options, "5x5", KERNEL_5x5);
        RadioButton mask5 = createMaskRadioButton(options, "7x7", KERNEL_7x7);
        mask1.setSelected(true);
        handleOptionChanges(options);
        HBox radioHBox = new HBox(mask1, mask4, mask5);
        radioHBox.setSpacing(15);

        currentKernelSize = KERNEL_3x3;
        currentBorderType = Core.BORDER_CONSTANT;
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

        Slider timesSlider = new Slider(1, 32, 1);
        timesSlider.setPrefWidth(100);
        Label timesValue = new Label("1x");
        timesValue.setPrefWidth(30);
        timesSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            timesValue.setText(newValue.intValue() + "x");
            times = newValue.intValue();
            reloadPreview();
        });

        HBox buttonsHbox = new HBox(cancel, save);
        HBox timesSliderHBox = new HBox(timesSlider, timesValue);
        VBox buttonsTimesVbox = new VBox(timesSliderHBox, buttonsHbox);

        VBox borderVBox = createBorderOptions();

        HBox buttons = new HBox(radioHBox,
                new Separator(VERTICAL), borderVBox,
                new Separator(VERTICAL), buttonsTimesVbox);
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
        stage.setTitle("Filtracja medianowa");
        save.requestFocus();
        stage.showAndWait();
    }

    private RadioButton createMaskRadioButton(ToggleGroup options, String text, int kernelSize) {
        RadioButton maskButton = new RadioButton(text);
        maskButton.setUserData(kernelSize);
        maskButton.setToggleGroup(options);
        maskButton.setPrefHeight(25);
        return maskButton;
    }

    private void handleOptionChanges(ToggleGroup options) {
        options.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                changeCurrentMask(newValue);
            }
        });
    }

    private void changeCurrentMask(Toggle newValue) {
        currentKernelSize = (int) newValue.getUserData();
        reloadPreview();
    }

    private void createAfterImageView() {
        after = applyMask();
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);
    }

    private void createBeforeImageView() {
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);
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

        borderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            currentBorderType = (int) newValue.getUserData();
            reloadPreview();
        });

        return new VBox(borderTypeLabel, replicatedBorder, reflectedBorder);
    }

    private void reloadPreview() {
        after = applyMask();
        afterImageView.setImage(after);
    }

    private Image applyMask() {
        Mat image = ImageUtils.imageToMat(before);
        Mat destination = new Mat(image.rows(), image.cols(), image.type());
        image.copyTo(destination);

        if (currentBorderType == BORDER_CONSTANT) {
            applyMaskWithConstantBorder(image, destination);
        } else {
            applyMask(image, destination);
        }

        return ImageUtils.mat2Image(image);
    }

    private void applyMask(Mat image, Mat destination) {
        for (int i = 0; i < times; i++) {
            Imgproc.medianBlur(destination, destination, currentKernelSize);
        }
        destination.copyTo(image);
    }

    private void applyMaskWithConstantBorder(Mat image, Mat destination) {
        for (int i = 0; i < times; i++) {
            Imgproc.medianBlur(destination, destination, currentKernelSize);
        }

        restoreBorder(image, destination);
    }

    private void restoreBorder(Mat image, Mat destination) {
        Mat cropped = destination.submat(1, destination.height() - 1, 1, destination.width() - 1);
        cropped.convertTo(cropped, image.type());
        cropped.copyTo(image.submat(1, image.height() - 1, 1, image.width() - 1));
    }

}
