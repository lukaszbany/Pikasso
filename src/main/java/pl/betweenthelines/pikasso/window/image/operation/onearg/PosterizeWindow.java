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
import pl.betweenthelines.pikasso.window.image.FileData;

import java.awt.image.BufferedImage;

/**
 * Klasa reprezentująca okno posteryzacji.
 */
public class PosterizeWindow {

    /**
     * Minimalna liczba poziomów szarości.
     */
    private static final double MIN_LEVEL = 2;

    /**
     * Domyślna liczba poziomów szarości.
     */
    private static final double DEFAULT = 8;

    /**
     * Maksymalna liczba poziomów szarości.
     */
    private static final double MAX_LEVEL = 16;

    /**
     * Minimalna szerokość okna.
     */
    private static final int MINIMAL_WIDTH = 650;

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
     * Obecnie wybrana przez użytkownika liczba poziomów jasności.
     */
    double currentLevel;

    /**
     * Konstruktor wczytujący obraz i tworzący układ okna.
     *
     * @param openedFileData
     */
    public PosterizeWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();
        createBeforeImageView();

        currentLevel = DEFAULT;
        after = posterize();
        createAfterImageView();

        HBox beforeImageViewHbox = new HBox(beforeImageView);
        beforeImageViewHbox.setAlignment(Pos.CENTER);
        HBox afterImageViewHbox = new HBox(afterImageView);
        afterImageViewHbox.setAlignment(Pos.CENTER);
        hBox = new HBox(beforeImageViewHbox, afterImageViewHbox);
        hBox.setAlignment(Pos.CENTER);

        Label value = new Label("8");
        value.setPrefWidth(20);
        Slider slider = createSlider(value);

        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            closeWindow();
        });
        Button save = new Button("Zachowaj");
        save.setOnAction(event -> saveAndClose(openedFileData));
        createButtonsArea(value, slider, cancel, save);

        Scene scene = createScene(beforeImageViewHbox, afterImageViewHbox);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Posteryzacja");
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
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) closeWindow();
        });
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);
        return scene;
    }

    /**
     * Tworzy obszar z przyciskami.
     *
     * @param value  <tt>Label</tt> z liczbą poziomów jasności.
     * @param slider do wybierania liczby poziomów jasności.
     * @param cancel przycisk "anuluj"
     * @param save   przycisk "zapisz"
     */
    private void createButtonsArea(Label value, Slider slider, Button cancel, Button save) {
        HBox buttons = new HBox(slider, value, cancel, save);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);
    }

    /**
     * Zapisuje obraz i zamyka okno.
     *
     * @param openedFileData informacje o otwartym obrazie
     */
    private void saveAndClose(FileData openedFileData) {
        openedFileData.setImage(after);
        closeWindow();
    }

    /**
     * Zamyka okno.
     */
    private void closeWindow() {
        stage.close();
    }

    /**
     * Tworzy <tt>Slider</tt> do wyboru liczby poziomów szarości.
     *
     * @param value <tt>Label</tt> z liczbą poziomów jasności.
     * @return <tt>Slider</tt> do wyboru liczby poziomów szarości.
     */
    private Slider createSlider(Label value) {
        Slider slider = new Slider(MIN_LEVEL, MAX_LEVEL, DEFAULT);
        slider.setPrefWidth(100);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> updateValue(value, slider, newValue));
        slider.setOnMouseReleased(event -> handleMouseReleased(slider));

        return slider;
    }

    /**
     * Obsługuje puszczenie klawisza myszki - zapisuje wybraną liczbę
     * poziomów jasności.
     *
     * @param slider do wybierania liczby poziomów jasności.
     */
    private void handleMouseReleased(Slider slider) {
        currentLevel = slider.getValue();
        reloadPreview();
    }

    /**
     * Obsługuję przesunięcie <tt>Slidera</tt> - wyświetla wybraną wartość.
     *
     * @param value    <tt>Label</tt> z liczbą poziomów jasności.
     * @param slider   do wybierania liczby poziomów jasności.
     * @param newValue wybrana przez użytkownika wartość
     */
    private void updateValue(Label value, Slider slider, Number newValue) {
        slider.setValue(newValue.intValue());
        value.setText(String.valueOf(newValue.intValue()));
    }

    /**
     * Tworzy podgląd obrazu po operacji.
     */
    private void createAfterImageView() {
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);
    }

    /**
     * Tworzy podgląd obrazu przed operacją.
     */
    private void createBeforeImageView() {
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);
    }

    /**
     * Dokonuje posteryzacji i odświeża podgląd.
     */
    private void reloadPreview() {
        after = posterize();
        afterImageView.setImage(after);
    }

    /**
     * Dokonuje posteryzacji.
     * Obraz jest zamieniany na obiekt <tt>BufferedImage</tt>, po czym obliczane są:
     * <ul>
     *     <li>multiplier - wartość pierwszego wyjściowego poziomu mniejszego od zera</li>
     *     <li>divider - wartość dzieląca piksele (mniejsza od niego wartość zostanie zmieniona na poziom mniejszy, większa - na większy</li>
     * </ul>
     * Przykładowo przy trzech poziomach jasności multiplier będzie wynosił 127, a divider 85. W konsekwencji:
     * <ul>
     *      <li>wartość mniejsza niż 85 zostanie zamieniona na 0</li>
     *      <li>wartość większa/równa niż 85 i mniejsza niż 170 (2*85) zostanie zamieniona na 127</li>
     *      <li>wartość większa/równa niż 170 zostanie zamieniona na 255</li>
     * </ul>
     * <p>
     * Zamiana jest przeprowadzana dla każdego piksela.
     *
     * @return obraz o zadanej liczbie poziomów szarości.
     */
    private Image posterize() {
        BufferedImage resultImage = new BufferedImage((int) before.getWidth(), (int) before.getHeight(), BufferedImage.TYPE_INT_RGB);
        PixelReader pixelReader = before.getPixelReader();

        int multiplier = 255 / (int) (currentLevel - 1);
        int divider = 255 / (int) currentLevel;

        for (int y = 0; y < before.getHeight(); y++) {
            for (int x = 0; x < before.getWidth(); x++) {
                processPixel(resultImage, pixelReader, multiplier, divider, y, x);
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
     * @param multiplier  mnożnik
     * @param divider     dzielnik
     * @param y           współrzędna y
     * @param x           współrzędna x
     */
    private void processPixel(BufferedImage resultImage, PixelReader pixelReader, int multiplier, int divider, int y, int x) {
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

    /**
     * Przeprowadza obliczenia dla podanego poziomu jasności.
     *
     * @param oldLevel   wejściowy poziom jasności
     * @param multiplier mnożnik
     * @param divider    dzielnik
     * @return wyjściowy poziom szarości
     */
    private int calculateLevel(int oldLevel, int multiplier, int divider) {
        int newLevel = 0;

        while (oldLevel > divider) {
            oldLevel -= multiplier;
            newLevel += multiplier;
        }

        return Math.min(newLevel, 255);
    }

}
