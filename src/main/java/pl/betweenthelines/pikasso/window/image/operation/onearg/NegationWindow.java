package pl.betweenthelines.pikasso.window.image.operation.onearg;

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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.image.FileData;

/**
 * Klasa reprezentująca okno negacji.
 */
public class NegationWindow {

    /**
     * Podgląd obrazu przed i po operacji.
     */
    ImageView beforeImageView;
    ImageView afterImageView;

    /**
     * Elementy okna.
     */
    Stage negationStage;
    VBox vBox;
    HBox hBox;
    Button save;

    /**
     * Konstruktor obiektu. Tworzy układ okna.
     *
     * @param openedFileData dane o otwartym pliku.
     */
    public NegationWindow(FileData openedFileData) {
        Image before = ImageUtils.toGrayscale(openedFileData.getImageView().getImage());
        createBeforeImageView(before);

        Image after = negation(before);
        createAfterImageView(after);

        HBox beforeImageViewHbox = new HBox(beforeImageView);
        beforeImageViewHbox.setAlignment(Pos.CENTER);

        HBox afterImageViewHbox = new HBox(afterImageView);
        afterImageViewHbox.setAlignment(Pos.CENTER);

        hBox = new HBox(beforeImageViewHbox, afterImageViewHbox);
        hBox.setAlignment(Pos.CENTER);

        createButtonsArea(openedFileData, after);

        Scene negationScene = createScene(beforeImageViewHbox, afterImageViewHbox);

        negationStage = new Stage();
        negationStage.initModality(Modality.APPLICATION_MODAL);

        negationStage.setScene(negationScene);
        negationStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        negationStage.setTitle("Negacja");
        save.requestFocus();
        negationStage.showAndWait();
    }

    /**
     * Oblicza wielkość okna na podstawie wielkości obrazów i tworzy układ okna.
     *
     * @param beforeImageViewHbox obszar z podglądem obrazu przed zmianami
     * @param afterImageViewHbox  obszar z podglądem obrazu po zmianach
     * @return <tt>Scene</tt> z układem okna
     */
    private Scene createScene(HBox beforeImageViewHbox, HBox afterImageViewHbox) {
        double windowWidth = afterImageView.getBoundsInLocal().getWidth() * 2;
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + 55;
        Scene negationScene = new Scene(vBox, windowWidth, windowHeight);
        negationScene.setOnKeyPressed(this::handleKeyPressed);
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);
        return negationScene;
    }

    /**
     * Obsluguje naciśnięcie klawisza ESC (zamyka okno)
     *
     * @param event zdarzenie naciśnięcia klawisza
     */
    private void handleKeyPressed(KeyEvent event) {
        if (KeyCode.ESCAPE.equals(event.getCode())) closeWindow();
    }

    /**
     * Tworzy obszar z przyciskami.
     *
     * @param openedFileData informacje o otwartym obrazie
     * @param after          obraz wynikowy
     */
    private void createButtonsArea(FileData openedFileData, Image after) {
        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> closeWindow());

        save = new Button("Zachowaj");
        save.setOnAction(event -> saveAndClose(openedFileData, after));

        HBox buttons = new HBox(cancel, save);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(55);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        vBox = new VBox(hBox, buttons);
    }

    /**
     * Zapisuje zmiany w obrazie i zamyka okno.
     *
     * @param openedFileData informacje o otwartym obrazie
     * @param after          obraz wynikowy
     */
    private void saveAndClose(FileData openedFileData, Image after) {
        openedFileData.setImage(after);
        closeWindow();
    }

    /**
     * Zamyka okno.
     */
    private void closeWindow() {
        negationStage.close();
    }

    /**
     * Tworzy podgląd obrazu po operacji.
     *
     * @param after obraz wynikowy
     */
    private void createAfterImageView(Image after) {
        afterImageView = new ImageView(after);
        afterImageView.setPreserveRatio(true);
        afterImageView.setFitWidth(400);
        afterImageView.setFitHeight(400);
    }

    /**
     * Tworzy podgląd obrazu przed operacją.
     *
     * @param before obraz wejściowy
     */
    private void createBeforeImageView(Image before) {
        beforeImageView = new ImageView((before));
        beforeImageView.setPreserveRatio(true);
        beforeImageView.setFitWidth(400);
        beforeImageView.setFitHeight(400);
    }

    /**
     * Dokonuje negacji obrazu. Obraz jest zamieniany na obiekt Mat
     * obsługiwany przez bibliotekę openCV, konwertowany na skalę szarości
     * i negowany, po czym zamieniany z powrotem na obiekt Image.
     *
     * @param before obraz przed operacją
     * @return zanegowany obraz
     */
    private Image negation(Image before) {
        Mat inImage = ImageUtils.imageToMat(before);
        Mat outImage = new Mat();
        Imgproc.cvtColor(inImage, outImage, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_not(outImage, outImage);

        return ImageUtils.mat2Image(outImage);
    }

}
