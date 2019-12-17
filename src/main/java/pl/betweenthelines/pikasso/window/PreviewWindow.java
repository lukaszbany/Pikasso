package pl.betweenthelines.pikasso.window;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

/**
 * Klasa reprezentująca okno podglądu - wyświetla obraz przed i po operacji.
 */
public class PreviewWindow {

    /**
     * Elementy okna.
     */
    Stage previewStage;
    VBox vBox;
    HBox hBox;

    /**
     * Obraz wynikowy. Jeśli użytkownik zatwierdzi nowy obraz, będzie tu obraz
     * po wykonaniu operacji. Jeśli anuluje zmiany, będzie tu obraz przed operacją.
     */
    @Getter
    Image result;

    /**
     * Konstruktor okna podglądu.
     *
     * @param before obraz przed operacją.
     * @param after  obraz po operacji.
     */
    public PreviewWindow(Image before, Image after) {
        ImageView beforeImageView = createImageView(before);
        ImageView afterImageView = createImageView(after);
        hBox = new HBox(beforeImageView, afterImageView);

        Button cancel = createButton(before, "Odrzuć");
        Button save = createButton(after, "Zachowaj");
        HBox buttons = createButtonsHBox(cancel, save);
        vBox = new VBox(hBox, buttons);

        createPreviewScene(before, afterImageView);
        save.requestFocus();
        previewStage.showAndWait();
    }

    /**
     * Tworzy przycisk z podanym tesktem, który ustawia podany obraz jako wynikowy.
     *
     * @param resultImage obraz wynikowy
     * @param text        tekst przycisku
     * @return <tt>Button</tt> o podanych parametrach
     */
    private Button createButton(Image resultImage, String text) {
        Button button = new Button(text);
        button.setOnAction(event -> setResultImage(resultImage));

        return button;
    }

    /**
     * Tworzy układ okna podglądu - oblicza wielkość okna na podstawie
     * obrazu, dla którego wyświetlany jest podgląd.
     *
     * @param before         obraz przed operacją
     * @param afterImageView <tt>ImageView</tt> zawierający obraz do podglądu
     */
    private void createPreviewScene(Image before, ImageView afterImageView) {
        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;

        Scene previewScene = new Scene(vBox, windowWidth, windowHeight);
        previewScene.setOnKeyPressed(event -> handleKeyPressed(before, event));

        previewStage = new Stage();
        previewStage.initModality(Modality.APPLICATION_MODAL);
        previewStage.setOnCloseRequest(event -> result = before);

        previewStage.setScene(previewScene);
        previewStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        previewStage.setTitle("Podgląd");
    }

    /**
     * Obsługuje naciśnięcie przycisku ESC (zamyka okno bez zapisywania zmian)
     *
     * @param before obraz przed operacją
     * @param event  akcja naciśnięcia przycisku
     */
    private void handleKeyPressed(Image before, KeyEvent event) {
        if (KeyCode.ESCAPE.equals(event.getCode())) {
            setResultImage(before);
        }
    }

    /**
     * Tworzy obszar z przyciskami.
     *
     * @param cancel przycisk do anulowania
     * @param save   przycisk do zapisania zmian
     * @return obszar z przyciskami
     */
    private HBox createButtonsHBox(Button cancel, Button save) {
        HBox buttons = new HBox(cancel, save);
        buttons.setPadding(new Insets(13, 0, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER);
        return buttons;
    }

    /**
     * Ustawia podany obraz jako wynikowy
     *
     * @param image obraz do ustawienia jako wynikowy
     */
    private void setResultImage(Image image) {
        result = image;
        previewStage.close();
    }

    /**
     * Tworzy podgląd dla podanego obrazu.
     *
     * @param image obraz do podglądu
     * @return <tt>ImageView</tt> z obrazem.
     */
    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        return imageView;
    }
}
