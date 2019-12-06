package pl.betweenthelines.pikasso.window.domain.operation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.domain.FileData;
import pl.betweenthelines.pikasso.window.domain.operation.mask.Mask3x3;

import java.util.ArrayList;
import java.util.List;

import static javafx.geometry.Orientation.VERTICAL;
import static pl.betweenthelines.pikasso.window.domain.operation.mask.LinearFilters.SHARPEN_1;

public class CreateMaskWindow {

    private static final int OPTIONS_HEIGHT = 90;
    private static final String X = "X";
    private static final String DEAFAULT_MASK_LABEL = String.format("%4s%4s%4s\n%4s%4s%4s\n%4s%4s%4s", X, X, X, X, X, X, X, X, X);

    private ImageView beforeImageView;
    private ImageView afterImageView;

    private Stage stage;
    private VBox vBox;
    private HBox hBox;

    private Image before;
    private Image after;
    private double times;

    private Mask3x3 currentMask;
    private int currentBorderType;
    private ObservableList<Integer> availableValues;

    public CreateMaskWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();
        availableValues = FXCollections.observableArrayList();
        for (int j = -20; j <= 20; j++) availableValues.add(j);

        List<Spinner<Integer>> spinners = createSpinners();
        HBox spinner1Hbox = new HBox(spinners.get(0), spinners.get(1), spinners.get(2));
        HBox spinner2Hbox = new HBox(spinners.get(3), spinners.get(4), spinners.get(5));
        HBox spinner3Hbox = new HBox(spinners.get(6), spinners.get(7), spinners.get(8));
        VBox vBox = new VBox(spinner1Hbox, spinner2Hbox, spinner3Hbox);
        vBox.setPrefWidth(180);
        HBox createMaskHBox = new HBox(vBox);
        createMaskHBox.setAlignment(Pos.CENTER);
        createMaskHBox.setPrefWidth(200);

        currentMask = new Mask3x3("DEFAULT", false, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        currentBorderType = Core.BORDER_CONSTANT;
        times = 1;

        createBeforeImageView();
        createAfterImageView();

        hBox = new HBox(beforeImageView, afterImageView);

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

        HBox buttons = new HBox(createMaskHBox, new Separator(VERTICAL), borderVBox, new Separator(VERTICAL), buttonsTimesVbox);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(OPTIONS_HEIGHT);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);

        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + OPTIONS_HEIGHT;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.setTitle("Własna maska");
        save.requestFocus();
        stage.showAndWait();
    }

    private List<Spinner<Integer>> createSpinners() {
        List<Spinner<Integer>> spinners = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            SpinnerValueFactory<Integer> values = new SpinnerValueFactory.ListSpinnerValueFactory<>(availableValues);
            values.setValue(1);
            Spinner<Integer> spinner = new Spinner<>(values);
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                handleValueChange(spinners);
            });
            spinners.add(spinner);
        }
        return spinners;
    }

    private void handleValueChange(List<Spinner<Integer>> spinners) {
        double[] spinnerValues = new double[9];
        for (int j = 0; j < 9; j++) {
            spinnerValues[j] = spinners.get(j).getValue();
        }

        changeCurrentMask(spinnerValues);
    }

    private VBox createBorderOptions() {
        ToggleGroup borderTypeGroup = new ToggleGroup();
        Label borderTypeLabel = new Label("Piksele brzegowe:");

        RadioButton replicatedBorder = new RadioButton("Bez zmian");
        replicatedBorder.setUserData(Core.BORDER_CONSTANT);
        replicatedBorder.setToggleGroup(borderTypeGroup);
        replicatedBorder.setSelected(true);

        RadioButton reflectedBorder = new RadioButton("Powielenie pikseli brzegowych");
        reflectedBorder.setUserData(Core.BORDER_REFLECT);
        reflectedBorder.setToggleGroup(borderTypeGroup);

        RadioButton existingBorder = new RadioButton("Istniejące sąsiedztwo");
        existingBorder.setUserData(Core.BORDER_DEFAULT);
        existingBorder.setToggleGroup(borderTypeGroup);

        borderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            currentBorderType = (int) newValue.getUserData();
            reloadPreview();
        });

        return new VBox(borderTypeLabel, replicatedBorder, reflectedBorder, existingBorder);
    }

    private void changeCurrentMask(double[] values) {
        currentMask = new Mask3x3("CUSTOM", false, values);
        reloadPreview();
    }

    private void createAfterImageView() {
        after = applyMask(SHARPEN_1);
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

    private void reloadPreview() {
        after = applyMask(currentMask);
        afterImageView.setImage(after);
    }

    private Image applyMask(Mask3x3 mask) {
        if (mask == null) {
            return before;
        }
        Mat image = ImageUtils.imageToMat(before);

        if (mask.getKernelSize() == 1) {
            applyMask(mask, image);
        } else {
            applyMaskWithColorConversion(mask, image);
        }

        return ImageUtils.mat2Image(image);
    }

    private void applyMask(Mask3x3 mask, Mat image) {
        for (int i = 0; i < times; i++) {
            FilteringUtils.applyMaskWithBlur(image, mask, currentBorderType);
        }
    }

    private void applyMaskWithColorConversion(Mask3x3 mask, Mat image) {
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);

        for (int i = 0; i < times; i++) {
            FilteringUtils.applyMaskWithBlur(image, mask, currentBorderType);
        }

        Core.convertScaleAbs(image, image);
    }

}
