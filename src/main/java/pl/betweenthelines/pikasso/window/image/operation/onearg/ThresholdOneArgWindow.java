package pl.betweenthelines.pikasso.window.image.operation.onearg;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.image.FileData;

/**
 * Reprezentuje okno progowania.
 */
public class ThresholdOneArgWindow {

    /**
     * Minimalny poziom jasności
     */
    private static final double MIN_LEVEL = 0;

    /**
     * Maksymalny poziom jasności
     */
    private static final double MAX_LEVEL = 255;

    /**
     * Minimalna szerokość okna
     */
    private static final int MINIMAL_WIDTH = 600;

    /**
     * Podgląd obrazu przed i po operacji.
     */
    ImageView beforeImageView;
    ImageView afterImageView;

    /**
     * Elementy okna.
     */
    Stage stage;
    VBox vBox;
    HBox hBox;

    /**
     * Obrazy przed i po operacji.
     */
    Image before;
    Image after;

    /**
     * Wybrany przez użytkownika poziom progowania.
     */
    double currentLevel;

    /**
     * Zmienna przechowująca informację o tym, czy użytkownik wybrał odwrócone progowanie (<tt>true</tt> jeśli tak).
     */
    boolean inverted;

    /**
     * Zmienna przechowująca informację o tym, czy użytkownik wybrał zachowanie poziomów szarości (<tt>true</tt> jeśli tak).
     */
    boolean preserveGrayscale;

    /**
     * Konstruktor tworzący układ okna.
     *
     * @param openedFileData dane o otwartym pliku.
     */
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

        HBox beforeImageViewHbox = new HBox(beforeImageView);
        beforeImageViewHbox.setAlignment(Pos.CENTER);
        HBox afterImageViewHbox = new HBox(afterImageView);
        afterImageViewHbox.setAlignment(Pos.CENTER);
        hBox = new HBox(beforeImageViewHbox, afterImageViewHbox);
        hBox.setAlignment(Pos.CENTER);

        CheckBox preserveGray = createPreserveGrayCheckBox();
        CheckBox invert = createInvertCheckbox();
        Label value = new Label("0");
        value.setPrefWidth(20);
        Slider slider = createSlider(value);

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

        Scene scene = createScene(beforeImageViewHbox, afterImageViewHbox);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Progowanie");
        save.requestFocus();
        stage.showAndWait();
    }

    /**
     * Tworzy CheckBox do wybrania odwrotnego progowania.
     *
     * @return
     */
    private CheckBox createInvertCheckbox() {
        CheckBox invert = new CheckBox("Odwrotne");
        invert.selectedProperty().addListener((observable, oldValue, newValue) -> {
            inverted = newValue;
            reloadPreview();
        });
        return invert;
    }

    /**
     * Tworzy CheckBox do zachowania poziomów jasności.
     *
     * @return
     */
    private CheckBox createPreserveGrayCheckBox() {
        CheckBox preserveGray = new CheckBox("Zachowaj poziom szarości");
        preserveGray.selectedProperty().addListener((observable, oldValue, newValue) -> {
            preserveGrayscale = newValue;
            reloadPreview();
        });
        return preserveGray;
    }

    /**
     * Tworzy Slider do wyboru poziomu progowania.
     *
     * @param value <tt>Label</tt> z poziomem progowania
     * @return <tt>Slider</tt> do wyboru poziomu progowania.
     */
    private Slider createSlider(Label value) {
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
        return slider;
    }

    /**
     * Oblicza wielkość okna na podstawie wielkości obrazów i tworzy układ okna.
     *
     * @param beforeImageViewHbox obszar z podglądem obrazu przed zmianami
     * @param afterImageViewHbox  obszar z podglądem obrazu po zmianach
     * @return <tt>Scene</tt> z układem okna
     */
    private Scene createScene(HBox beforeImageViewHbox, HBox afterImageViewHbox) {
        double windowWidth = Math.max(MINIMAL_WIDTH, afterImageView.getBoundsInLocal().getWidth() * 2);
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) stage.close();
        });
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);
        return scene;
    }

    /**
     * Dokonuje posteryzacji i odświeża podgląd.
     */
    private void reloadPreview() {
        after = threshold();
        afterImageView.setImage(after);
    }

    /**
     * Dokonuje progowania.
     * Obraz jest zamieniany na obiekt Mat, sprowadzany do poziomów szarości
     * i wykonywana jest operacja threshold z biblioteki openCV z parametrami
     * zadanymi przez użytkownika.
     *
     * @return obraz po operacji
     */
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
