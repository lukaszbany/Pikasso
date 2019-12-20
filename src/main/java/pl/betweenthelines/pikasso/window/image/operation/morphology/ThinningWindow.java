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
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.exception.ImageIsNotBinaryException;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.HistogramWindow;
import pl.betweenthelines.pikasso.window.image.FileData;
import pl.betweenthelines.pikasso.window.image.operation.linear.FilteringUtils;

import java.util.ArrayList;
import java.util.List;

import static javafx.geometry.Orientation.VERTICAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 * Klasa reprezentująca okno wykrywania krawędzi w obrazie.
 */
public class ThinningWindow {

    private static final double WHITE = 255;
    private static final double BLACK = 0;
    private static final double ANY = 150;

    /**
     * Wartości wskazujące rodzaj opracji na pikselach brzegowych.
     */
    private static final int BORDER_MINIMUM = 254;
    private static final int BORDER_MAXIMUM = 255;

    /**
     * Wysokość panelu opcji.
     */
    private static final int OPTIONS_HEIGHT = 140;

    /**
     * Minimalna szerokość okna.
     */
    private static final int MINIMAL_WIDTH = 550;

    /**
     * Podgląd obrazu przed i po operacji.
     */
    private ImageView beforeImageView;
    private ImageView afterImageView;

    /**
     * Elementy okna.
     */
    private Stage stage;
    private VBox vBox;
    private HBox hBox;
    private Slider stepSlider;
    private Label stepValue;

    /**
     * Obrazy przed i po operacji.
     */
    private Image before;
    private Image after;

    /**
     * Wskazuje wybrany przez użytkownika krok ścieniania.
     */
    private int step;

    /**
     * Aktualnie wybrana przez użytkownika operacja na pikselach brzegowych.
     */
    private int currentBorderType;

    /**
     * Wartość pikseli brzegowych (jeśli wybrana stała wartość)
     */
    private Scalar border;

    /**
     * Początkowe wartości poziomu jasności dla obiektu i tła.
     */
    private double object = BLACK;
    private double background = WHITE;

    /**
     * Lista wzorców do porównania.
     */
    private List<double[]> patterns;

    /**
     * Lista obrazów w kolejnych krokach skieletyzacji.
     */
    List<Image> stepImages;

    /**
     * Konstruktor tworzący układ okna.
     *
     * @param openedFileData dane o otwartym pliku.
     */
    public ThinningWindow(FileData openedFileData) throws ImageIsNotBinaryException {
        before = ImageUtils.binarize(openedFileData.getImageView().getImage());
        border = new Scalar(255, 255, 255, 255);
        patterns = BlackObjectPatterns.getPATTERNS();

        currentBorderType = Core.BORDER_CONSTANT;

        createStepSlider();
        createBeforeImageView();
        createAfterImageView();

        HBox beforeImageViewHbox = new HBox(beforeImageView);
        beforeImageViewHbox.setAlignment(Pos.CENTER);
        HBox afterImageViewHbox = new HBox(afterImageView);
        afterImageViewHbox.setAlignment(Pos.CENTER);
        hBox = new HBox(beforeImageViewHbox, afterImageViewHbox);
        hBox.setAlignment(Pos.CENTER);

        Button apply = new Button("Zastosuj");
        apply.setOnAction(event -> {
            try {
                reloadPreview();
            } catch (ImageIsNotBinaryException e) {
                ErrorHandler.handleError(e);
                stage.close();
            }
        });
        Button cancel = new Button("Odrzuć");
        cancel.setOnAction(event -> {
            stage.close();
        });
        Button save = new Button("Zachowaj obecny krok");
        save.setOnAction(event -> saveAndClose(openedFileData));
        HBox buttonsHbox = new HBox(cancel, save);
        buttonsHbox.setSpacing(15);
        buttonsHbox.setAlignment(Pos.CENTER);
        HBox stepSliderHBox = new HBox(stepSlider, stepValue);
        stepSliderHBox.setAlignment(Pos.CENTER);
        VBox buttonsStepVbox = new VBox(stepSliderHBox, apply, buttonsHbox);
        buttonsStepVbox.setAlignment(Pos.CENTER);
        buttonsStepVbox.setSpacing(15);

        VBox objectBackgroundVBox = createObjectBackgroundOptions();
        VBox borderVBox = createBorderOptions();

        HBox buttons = new HBox(
                objectBackgroundVBox, new Separator(VERTICAL),
                borderVBox, new Separator(VERTICAL),
                buttonsStepVbox
        );

        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(OPTIONS_HEIGHT);
        buttons.setAlignment(Pos.CENTER);
        vBox = new VBox(hBox, buttons);

        Scene scene = createScene(beforeImageViewHbox, afterImageViewHbox);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Ścienianie");
        save.requestFocus();
        stage.showAndWait();
    }

    /**
     * Zapisuje obecnie wybrany krok i zamyka okno.
     *
     * @param openedFileData dane o otwartym pliku.
     */
    private void saveAndClose(FileData openedFileData) {
        if (stepImages != null && stepImages.size() > step) {
            openedFileData.setImage(stepImages.get(step));
        } else {
            openedFileData.setImage(after);
        }

        stage.close();
    }

    /**
     * Tworzy slider do poruszania się pomiędzy krokami skieletyzacji.
     */
    private void createStepSlider() {
        stepSlider = new Slider(1, 10, 1);
        stepSlider.setPrefWidth(180);
        stepSlider.setDisable(true);
        stepValue = new Label("1");
        stepValue.setPrefWidth(30);
        stepSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            stepValue.setText(String.valueOf(newValue.intValue()));
            step = newValue.intValue() - 1;

            if (afterImageView != null && stepImages.size() > step) {
                afterImageView.setImage(stepImages.get(step));
            }
        });
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
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + OPTIONS_HEIGHT;
        Scene scene = new Scene(vBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) stage.close();
        });
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);
        return scene;
    }

    /**
     * Tworzy obszar z opcjami dotyczącymi pikseli brzegowych.
     *
     * @return obszar z opcjami dotyczącymi pikseli brzegowych.
     */
    private VBox createBorderOptions() {
        ToggleGroup borderTypeGroup = new ToggleGroup();
        Label borderTypeLabel = new Label("Piksele brzegowe:");

        RadioButton maximum = new RadioButton("Wartość maksymalna");
        maximum.setUserData(BORDER_MAXIMUM);
        maximum.setToggleGroup(borderTypeGroup);
        maximum.setSelected(true);

        RadioButton minimum = new RadioButton("Wartość minimalna");
        minimum.setUserData(BORDER_MINIMUM);
        minimum.setToggleGroup(borderTypeGroup);

        borderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue)
                -> handleBorderOptionChange(newValue));

        return new VBox(borderTypeLabel, maximum, minimum);
    }

    /**
     * Obsługuję zmianę opcji dotyczących pikseli brzegowych.
     *
     * @param newValue wybrana opcja
     */
    private void handleBorderOptionChange(Toggle newValue) {
        int selected = (int) newValue.getUserData();
        if (selected == BORDER_MINIMUM) {
            currentBorderType = Core.BORDER_CONSTANT;
            border = new Scalar(0, 0, 0, 255);
        } else {
            currentBorderType = Core.BORDER_CONSTANT;
            border = new Scalar(255, 255, 255, 255);
        }
    }

    /**
     * Tworzy opcje wyboru typu obiektu (czarno obiekt na białym tle
     * lub biały na czarnym)
     *
     * @return obszar z opcjami wyboru
     */
    private VBox createObjectBackgroundOptions() {
        ToggleGroup group = new ToggleGroup();
        Label borderTypeLabel = new Label("Typ obiektu:");

        RadioButton black = new RadioButton("Czarny obiekt na białym tle");
        black.setUserData(BLACK);
        black.setToggleGroup(group);
        black.setSelected(true);

        RadioButton white = new RadioButton("Biały obiekt na czarnym tle");
        white.setUserData(WHITE);
        white.setToggleGroup(group);

        group.selectedToggleProperty().addListener((observable, oldValue, newValue)
                -> handleObjectBackgroundOptionChange(newValue));

        return new VBox(borderTypeLabel, black, white);
    }

    /**
     * Aktualizuje wybrany typ obiektu.
     *
     * @param newValue
     */
    private void handleObjectBackgroundOptionChange(Toggle newValue) {
        double selected = (double) newValue.getUserData();
        if (selected == BLACK) {
            object = BLACK;
            background = WHITE;
            patterns = BlackObjectPatterns.getPATTERNS();
        } else {
            object = WHITE;
            background = BLACK;
            patterns = WhiteObjectPatterns.getPATTERNS();
        }
    }

    /**
     * Tworzy podgląd obrazu po operacji.
     */
    private void createAfterImageView() throws ImageIsNotBinaryException {
        after = before;
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

    /**
     * Tworzy podgląd obrazu przed operacją.
     */
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

    /**
     * Przeprowadza ścienianie i odświeża podgląd.
     */
    private void reloadPreview() throws ImageIsNotBinaryException {
        after = applyThinning();
        afterImageView.setImage(after);
    }

    /**
     * Czyści istniejącą listę obrazów w kolejnych krokach, binaryzuje obraz,
     * przeprowadza operację ścieniania i odświeża podgląd.
     *
     * @return ścieniony obraz
     * @throws ImageIsNotBinaryException jeśli obrazu nie udało się zbinaryzować
     */
    private Image applyThinning() throws ImageIsNotBinaryException {
        stepImages = new ArrayList<>();
        Mat image = ImageUtils.imageToMat(before);
        ImageUtils.binarize(image);

        applyThinning(image);
        refreshSlider();

        return ImageUtils.mat2Image(image);
    }

    /**
     * Przeprowadza operację ścieniania klasycznym algorytmem szkieletyzacji.
     *
     * @param image obraz do ścieniania
     * @throws ImageIsNotBinaryException jeśli obrazu nie udało się zbinaryzować
     */
    private void applyThinning(Mat image) throws ImageIsNotBinaryException {
        Mat copy = new Mat();
        image.copyTo(copy);

        boolean remain = true;
        while (remain) {
            remain = false;
            for (int j = 1; j < 8; j += 2) {
                for (int col = 1; col < image.cols() - 1; col++) {
                    for (int row = 1; row < image.rows() - 1; row++) {
                        double p = image.get(row, col)[0];
                        if (p != BLACK && p != WHITE) {
                            throw new ImageIsNotBinaryException();
                        }

                        double jNeighbour = getNeighbour(image, j, col, row);
                        if (p == object && jNeighbour == background) {
                            if (!anyPatternMatches(image, col, row)) {
                                copy.put(row, col, object);
                            } else {
                                copy.put(row, col, background);
                                remain = true;
                            }
                        }
                    }
                }
            }

            FilteringUtils.handleBorder(copy, border);
            copy.copyTo(image);
            stepImages.add(ImageUtils.mat2Image(image));
        }
    }

    /**
     * Odświeża slider do przesuwania kroków szkieletyzacji.
     */
    private void refreshSlider() {
        stepSlider.setMax(stepImages.size());
        stepSlider.setValue(stepImages.size());
        stepSlider.setDisable(false);
    }

    /**
     * Sprawdza, czy któryś z wzorców pasuje do sąsiedztwa obecnego piksela.
     *
     * @param image obraz
     * @param col   kolumna piksela
     * @param row   wiersz piksela
     * @return <tt>true</tt> jeśli co najmniej jeden wzorzec pasuje.
     */
    private boolean anyPatternMatches(Mat image, int col, int row) {
        for (double[] pattern : patterns) {
            if (patternMatches(pattern, image, col, row)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sprawdza, czy wzorzec pasuje do sąsiedztwa piksela
     *
     * @param pattern wzorzec do sprawdzenia
     * @param image   obraz
     * @param col     kolumna piksela
     * @param row     wiersz piksela
     * @return <tt>true</tt> jeśli wzorzec pasuje.
     */
    private boolean patternMatches(double[] pattern, Mat image, int col, int row) {
        for (int i = 0; i < 9; i++) {
            if (pattern[i] == ANY) continue;
            double value = getNeighbour(image, i, col, row);

            if (pattern[i] != value) {
                return false;
            }
        }

        return true;
    }

    /**
     * Pobiera wartość sąsiedniego piksela o podanym indeksie.
     *
     * @param image          obraz
     * @param neighbourIndex indeks sąsiada
     * @param col            kolumna piksela
     * @param row            wiersz piksela
     * @return wartość sąsiedniego piksela
     */
    private double getNeighbour(Mat image, int neighbourIndex, int col, int row) {
        switch (neighbourIndex) {
            case 0:
                return image.get(row - 1, col - 1)[0];
            case 1:
                return image.get(row - 1, col)[0];
            case 2:
                return image.get(row - 1, col + 1)[0];
            case 3:
                return image.get(row, col - 1)[0];
            case 4:
                return image.get(row, col)[0];
            case 5:
                return image.get(row, col + 1)[0];
            case 6:
                return image.get(row + 1, col - 1)[0];
            case 7:
                return image.get(row + 1, col)[0];
            case 8:
                return image.get(row + 1, col + 1)[0];
        }

        throw new IllegalArgumentException();
    }

}
