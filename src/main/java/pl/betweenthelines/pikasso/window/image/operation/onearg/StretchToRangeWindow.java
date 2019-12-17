package pl.betweenthelines.pikasso.window.image.operation.onearg;

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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.image.FileData;

import java.awt.image.BufferedImage;

/**
 * Klasa reprezentująca okno rozciągania poziomów jasności
 * do zadanych poziomów.
 */
public class StretchToRangeWindow {

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
    private static final int MINIMAL_WIDTH = 750;

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
     * Slidery do wyboru wartości poziomów jasności.
     */
    RangeSlider rangeP;
    RangeSlider rangeQ;
    Slider backgroundSlider;

    /**
     * Konstruktor tworzący układ okna.
     *
     * @param openedFileData dane o otwartym pliku.
     */
    public StretchToRangeWindow(FileData openedFileData) {
        before = ImageUtils.toGrayscale(openedFileData.getImageView().getImage());
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);

        VBox sliders = createOptionSliders();

        Label backgroundLabel = new Label("Poziom tła:");
        backgroundSlider = new Slider(MIN_LEVEL, MAX_LEVEL, MIN_LEVEL);
        Label backgroundValue = new Label("0");
        backgroundValue.setPrefWidth(20);
        backgroundSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            backgroundValue.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
        HBox backgroundHBox = new HBox(backgroundLabel, backgroundSlider, backgroundValue);

        after = stretch();
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

        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            stage.close();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> {
            openedFileData.setImage(after);
            stage.close();
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

        Scene scene = createScene(beforeImageViewHbox, afterImageViewHbox);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Rozciąganie do zadanych poziomów");
        save.requestFocus();
        stage.showAndWait();
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
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 110;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) stage.close();
        });
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);
        return scene;
    }

    /**
     * Tworzy Slidery do wyboru poziomów jasności.
     *
     * @return obszar ze sliderami
     */
    private VBox createOptionSliders() {
        Label p = new Label("Wartość p: ");
        Label p1 = new Label("0");
        p1.setPrefWidth(20);
        Label p2 = new Label("255");
        p2.setPrefWidth(20);
        createRangePSlider(p1, p2);

        Label q = new Label("Wartość q: ");
        Label q3 = new Label("0");
        q3.setPrefWidth(20);
        Label q4 = new Label("255");
        q4.setPrefWidth(20);
        createRangeQSlider(q3, q4);

        return new VBox(new HBox(p, p1, rangeP, p2), new HBox(q, q3, rangeQ, q4));
    }

    /**
     * Tworzy slider do wyboru poziomów dolnego i górnego wartości Q.
     *
     * @param q3 Label do wyświetlenia wartości dolnej
     * @param q4 Label do wyświetlenia wartości górnej
     */
    private void createRangeQSlider(Label q3, Label q4) {
        rangeQ = new RangeSlider();
        rangeQ.setShowTickMarks(true);
        rangeQ.setShowTickLabels(true);
        rangeQ.setMin(MIN_LEVEL);
        rangeQ.setMax(MAX_LEVEL);
        rangeQ.setHighValue(MAX_LEVEL);
        rangeQ.setPrefHeight(200);
        rangeQ.setPrefWidth(400);

        rangeQ.highValueProperty().addListener((observable, oldValue, newValue) -> {
            q4.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
        rangeQ.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            q3.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
    }

    /**
     * Tworzy slider do wyboru poziomów dolnego i górnego wartości P.
     *
     * @param p1 Label do wyświetlenia wartości dolnej
     * @param p2 Label do wyświetlenia wartości górnej
     */
    private void createRangePSlider(Label p1, Label p2) {
        rangeP = new RangeSlider();
        rangeP.setShowTickMarks(true);
        rangeP.setShowTickLabels(true);
        rangeP.setMin(MIN_LEVEL);
        rangeP.setMax(MAX_LEVEL);
        rangeP.setHighValue(MAX_LEVEL);
        rangeP.setPrefHeight(200);
        rangeP.setPrefWidth(400);

        rangeP.highValueProperty().addListener((observable, oldValue, newValue) -> {
            p2.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
        rangeP.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            p1.setText(String.valueOf(newValue.intValue()));
            reloadPreview();
        });
    }

    /**
     * Wykonuje operację i odświeża podgląd
     */
    private void reloadPreview() {
        after = stretch();
        afterImageView.setImage(after);
    }

    /**
     * Dokonuje rozciągnięcia poziomów szarości do zadanych przez użytkownika.
     * Obraz jest zamieniany na obiekt <tt>BufferedImage</tt>, po czym  dla każdego
     * piksela obliczany jest nowy poziom szarości.
     *
     * @return obraz o zadanych poziomach szarości
     */
    private Image stretch() {
        BufferedImage resultImage = new BufferedImage((int) before.getWidth(), (int) before.getHeight(), BufferedImage.TYPE_INT_RGB);
        PixelReader pixelReader = before.getPixelReader();

        for (int y = 0; y < before.getHeight(); y++) {
            for (int x = 0; x < before.getWidth(); x++) {
                processOnePixel(resultImage, pixelReader, y, x);
            }
        }

        return SwingFXUtils.toFXImage(resultImage, null);
    }

    /**
     * Przeprowadza obliczenia na jednym pikselu. Pobiera wartość piksela, odczytuje
     * z niej poziomy jasności poszczególnych kanałów, dokonuje obliczeń dla każdego
     * kanału osobno, po czym zapisuje wartość piksela w obrazie wynikowym.
     *
     * @param resultImage obraz wynikowy
     * @param pixelReader do odczytu piksela
     * @param y           współrzędna y
     * @param x           współrzędna x
     */
    private void processOnePixel(BufferedImage resultImage, PixelReader pixelReader, int y, int x) {
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

    /**
     * Pobiera wybrane przez użytkownika wartości i oblicza nowy poziom
     * szarości dla piksela o podenym poziomie p.
     *
     * @param p wejściowy poziom szarości
     * @return wyjściowy poziom szarości
     */
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
