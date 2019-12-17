package pl.betweenthelines.pikasso.window.image.operation.morphology;

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
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.HistogramWindow;
import pl.betweenthelines.pikasso.window.image.FileData;
import pl.betweenthelines.pikasso.window.image.operation.linear.FilteringUtils;

import static javafx.geometry.Orientation.VERTICAL;

public class MorphologyWindow {

    private static final int BORDER_MINIMUM = 254;
    private static final int BORDER_MAXIMUM = 255;
    private static final double MIN_LEVEL = 3;
    private static final double MAX_LEVEL = 7;
    private static final int OPTIONS_HEIGHT = 130;
    private static final int MINIMAL_WIDTH = 700;

    private ImageView beforeImageView;
    private ImageView afterImageView;

    private Stage stage;
    private VBox vBox;
    private HBox hBox;

    private Image before;
    private Image after;
    private Slider slider;

    private int times;
    private int currentOperation;
    private int currentShape;
    private int currentSize;
    private int currentBorderType;
    private Scalar border;

    public MorphologyWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();

        ToggleGroup operations = new ToggleGroup();
        RadioButton erode = createOperationButton(operations, Imgproc.MORPH_ERODE, "Erozja");
        RadioButton dilate = createOperationButton(operations, Imgproc.MORPH_DILATE, "Dylatacja");
        RadioButton open = createOperationButton(operations, Imgproc.MORPH_OPEN, "Otwarcie");
        RadioButton close = createOperationButton(operations, Imgproc.MORPH_CLOSE, "Zamknięcie");

        erode.setSelected(true);
        handleOperationChanges(operations);
        VBox operationVBox = new VBox(erode, dilate, open, close);

        ToggleGroup shape = new ToggleGroup();
        RadioButton rectangle = createOperationButton(shape, Imgproc.MORPH_RECT, "Kwadrat");
        RadioButton cross = createOperationButton(shape, Imgproc.MORPH_CROSS, "Romb");
        RadioButton ellipse = createOperationButton(shape, Imgproc.MORPH_ELLIPSE, "Elipsa");

        rectangle.setSelected(true);
        handleShapeChanges(shape);
        VBox shapeVBox = new VBox(rectangle, cross, ellipse);

        times = 1;
        currentOperation = Imgproc.MORPH_ERODE;
        currentShape = Imgproc.MORPH_RECT;
        currentSize = 3;
        currentBorderType = Core.BORDER_REPLICATE;
        border = new Scalar(0, 0, 0, 255);

        HBox kSliderHbox = createSliderHBox();
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

        VBox parametrizedSlider = new VBox(kSliderHbox);
        parametrizedSlider.setSpacing(5);
        parametrizedSlider.setAlignment(Pos.CENTER);

        VBox radioAndKSliderVBox = new VBox(operationVBox);
        radioAndKSliderVBox.setSpacing(15);
        radioAndKSliderVBox.setAlignment(Pos.CENTER);
        HBox buttonsHbox = new HBox(cancel, save);
        buttonsHbox.setAlignment(Pos.CENTER);
        buttonsHbox.setSpacing(15);
        HBox timesSliderHBox = new HBox(timesSlider, timesValue);
        VBox buttonsTimesVbox = new VBox(timesSliderHBox, buttonsHbox);
        buttonsTimesVbox.setAlignment(Pos.CENTER);
        buttonsTimesVbox.setSpacing(15);

        VBox borderVBox = createBorderOptions();

        HBox buttons = new HBox(operationVBox,
                new Separator(VERTICAL), shapeVBox,
                new Separator(VERTICAL), kSliderHbox,
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
        stage.setTitle("Wygładzanie");
        save.requestFocus();
        stage.showAndWait();
    }

    private void handleOperationChanges(ToggleGroup options) {
        options.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                changeCurrentOperation(newValue);
            }
        });
    }

    private void handleShapeChanges(ToggleGroup options) {
        options.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                changeCurrentShape(newValue);
            }
        });
    }

    private void changeCurrentOperation(Toggle newValue) {
        currentOperation = (int) newValue.getUserData();
        reloadPreview();
    }

    private void changeCurrentShape(Toggle newValue) {
        currentShape = (int) newValue.getUserData();
        reloadPreview();
    }

    private void createAfterImageView() {
        after = applyOperation();
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

    private HBox createSliderHBox() {
        createSlider();

        HBox sliderHbox = new HBox(slider);
        sliderHbox.setAlignment(Pos.CENTER);
        sliderHbox.setSpacing(5);
        return sliderHbox;
    }

    private RadioButton createOperationButton(ToggleGroup operations, int operation, String name) {
        RadioButton maskButton = new RadioButton(name);
        maskButton.setUserData(operation);
        maskButton.setToggleGroup(operations);
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

    private VBox createBorderOptions() {
        ToggleGroup borderTypeGroup = new ToggleGroup();
        Label borderTypeLabel = new Label("Piksele brzegowe:");

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
            }

            reloadPreview();
        });

        return new VBox(borderTypeLabel, existingBorder, minimum, maximum);
    }

    private void createSlider() {
        slider = new Slider(MIN_LEVEL, MAX_LEVEL, MIN_LEVEL);
        slider.setPrefWidth(80);
        slider.setBlockIncrement(2);
        slider.setMajorTickUnit(2);
        slider.setMinorTickCount(0);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int intValue = newValue.intValue();
            if (intValue % 2 != 0) {
                slider.setValue(intValue);
                currentSize = intValue;
                reloadPreview();
            }
        });
    }

    private void reloadPreview() {
        after = applyOperation();
        afterImageView.setImage(after);
    }

    private Image applyOperation() {
        Mat image = ImageUtils.imageToMat(before);

        Mat shape = Imgproc.getStructuringElement(currentShape, new Size(currentSize, currentSize));
        switch (currentOperation) {
            case Imgproc.MORPH_ERODE:
                Imgproc.erode(image, image, shape, new Point(-1, -1), times, currentBorderType, border);
                break;
            case Imgproc.MORPH_DILATE:
                Imgproc.dilate(image, image, shape, new Point(-1, -1), times, currentBorderType, border);
                break;
            case Imgproc.MORPH_OPEN:
            case Imgproc.MORPH_CLOSE:
                Imgproc.morphologyEx(image, image, currentOperation, shape, new Point(-1, -1), times, currentBorderType, border);
        }

        if (currentBorderType == Core.BORDER_CONSTANT) {
            FilteringUtils.handleBorder(image, border);
        }

        return ImageUtils.mat2Image(image);
    }

}
