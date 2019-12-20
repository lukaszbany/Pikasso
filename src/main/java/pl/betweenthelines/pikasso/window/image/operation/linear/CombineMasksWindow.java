package pl.betweenthelines.pikasso.window.image.operation.linear;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.HistogramWindow;
import pl.betweenthelines.pikasso.window.image.FileData;
import pl.betweenthelines.pikasso.window.image.operation.linear.mask.Mask3x3;
import pl.betweenthelines.pikasso.window.image.operation.linear.mask.Mask5x5;
import pl.betweenthelines.pikasso.window.image.operation.linear.mask.MaskUtils;

import java.util.ArrayList;
import java.util.List;

import static javafx.geometry.Orientation.VERTICAL;
import static pl.betweenthelines.pikasso.window.image.operation.linear.MatScalingUtils.*;

/**
 * Klasa reprezentująca okno łączenie dwóch masek w jedną.
 */
public class CombineMasksWindow {

    /**
     * Wartości wskazujące rodzaj opracji na pikselach brzegowych.
     */
    private static final int BORDER_MINIMUM = 254;
    private static final int BORDER_MAXIMUM = 255;

    /**
     * Wysokość panelu opcji.
     */
    private static final int OPTIONS_HEIGHT = 225;

    /**
     * Minimalna szerokość okna.
     */
    private static final int MINIMAL_WIDTH = 820;

    /**
     * Podgląd obrazu przed i po operacji.
     */
    private ImageView beforeImageView;
    private ImageView afterImageView;

    /**
     * Elementy okna.
     */
    private Stage stage;
    private HBox hBox;

    /**
     * Obrazy przed i po operacji.
     */
    private Image before;
    private Image after;

    /**
     * Wartość zwielokrotnienia operacji.
     */
    private double times;

    /**
     * Pola do wpisania wartości w maskach.
     */
    private List<Spinner<Integer>> spinnersA;
    private List<Spinner<Integer>> spinnersB;

    /**
     * Aktualnie wybrane przez użytkownika maski.
     */
    private Mask3x3 currentMaskA;
    private Mask3x3 currentMaskB;

    /**
     * Label z wartościami utworzonej maski
     */
    private Label combinedMaskLabel;

    /**
     * Wynik połączenia wybranych masek.
     */
    private Mask5x5 combinedMask;

    /**
     * Flaga, która wskazuje, czy wyświetlać podgląd operacji na dwóch maskach 3x3
     * (jeśli true), czy na masce wynikowej 5x5 (false).
     */
    private boolean twoMasksPreview;

    /**
     * Aktualnie wybrany przez użytkownika sposób skalowania.
     */
    private byte currentScalingMethod;

    /**
     * Aktualnie wybrana przez użytkownika operacja na pikselach brzegowych.
     */
    private int currentBorderType;

    /**
     * Wartość pikseli brzegowych (jeśli wybrana stała wartość)
     */
    private Scalar border;

    /**
     * Wartości dostępne w masce.
     */
    private ObservableList<Integer> availableValues;

    /**
     * Konstruktor tworzący układ okna. Maska wynikowa 5x5 z połączenia dwóch masek 3x3
     * tworzona jest automatycznie, po każdej zmianie wartości jednej z masek 3x3.
     *
     * @param openedFileData dane o otwartym pliku.
     */
    public CombineMasksWindow(FileData openedFileData) {
        before = openedFileData.getImageView().getImage();
        availableValues = FXCollections.observableArrayList();
        for (int j = -20; j <= 20; j++) availableValues.add(j);

        VBox masksVBox = createMasksVBoxBox();

        currentMaskA = new Mask3x3("DEFAULT", false, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        currentMaskB = new Mask3x3("DEFAULT", false, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        combinedMask = MaskUtils.combineMasks(currentMaskA, currentMaskB);
        currentBorderType = Core.BORDER_CONSTANT;
        currentScalingMethod = METHOD_3;
        twoMasksPreview = false;
        times = 1;

        HBox createMaskHBox = new HBox(masksVBox);
        createMaskHBox.setAlignment(Pos.CENTER);
        createMaskHBox.setPrefWidth(200);

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

        combinedMaskLabel = new Label("Wynik:\n" + combinedMask.toString());
        VBox resultVBox = new VBox(combinedMaskLabel);
        resultVBox.setAlignment(Pos.CENTER);
        resultVBox.setSpacing(15);

        ToggleButton previewToggleButton = new ToggleButton("Podgląd maski wynikowej");
        previewToggleButton.setPrefWidth(170);
        previewToggleButton.selectedProperty().addListener((observable, oldValue, newValue)
                -> handlePreviewToggle(previewToggleButton, newValue));

        VBox borderVBox = createBorderOptions();
        VBox previewAndBorderVBox = new VBox(previewToggleButton, borderVBox);
        previewAndBorderVBox.setAlignment(Pos.CENTER);
        previewAndBorderVBox.setSpacing(15);

        VBox scalingVBox = createScalingOptions();
        VBox combinedMaskAndScalingVBox = new VBox(resultVBox, new Separator(), scalingVBox);
        combinedMaskAndScalingVBox.setPrefWidth(120);

        HBox buttons = new HBox(createMaskHBox, new Separator(VERTICAL),
                previewAndBorderVBox, new Separator(VERTICAL),
                combinedMaskAndScalingVBox, new Separator(VERTICAL),
                buttonsTimesVbox);
        buttons.setPadding(new Insets(13, 10, 10, 0));
        buttons.setSpacing(15);
        buttons.setMaxHeight(OPTIONS_HEIGHT);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        masksVBox = new VBox(hBox, buttons);

        Scene scene = createScene(masksVBox, beforeImageViewHbox, afterImageViewHbox);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Własna maska");
        save.requestFocus();
        stage.showAndWait();
    }

    /**
     * Oblicza wielkość okna na podstawie wielkości obrazów i tworzy układ okna.
     *
     * @param masksVBox           obszar z wartościami masek
     * @param beforeImageViewHbox obszar z podglądem obrazu przed zmianami
     * @param afterImageViewHbox  obszar z podglądem obrazu po zmianach
     * @return <tt>Scene</tt> z układem okna
     */
    private Scene createScene(VBox masksVBox, HBox beforeImageViewHbox, HBox afterImageViewHbox) {
        double windowWidth = Math.max(MINIMAL_WIDTH, afterImageView.getBoundsInLocal().getWidth() * 2);
        double windowHeight = afterImageView.getBoundsInLocal().getHeight() + OPTIONS_HEIGHT;
        Scene scene = new Scene(masksVBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) stage.close();
        });
        beforeImageViewHbox.setPrefWidth(windowWidth / 2);
        afterImageViewHbox.setPrefWidth(windowWidth / 2);
        return scene;
    }

    /**
     * Obsługuje zmianę podglądu maska wynikowa / operacja na dwóch maskach po kolei.
     *
     * @param previewToggleButton przycisk do zmiany podglądu
     * @param newValue            nowa wartość przycisku
     */
    private void handlePreviewToggle(ToggleButton previewToggleButton, Boolean newValue) {
        twoMasksPreview = newValue;
        if (newValue) {
            previewToggleButton.setText("Podgląd masek składowych");
        } else {
            previewToggleButton.setText("Podgląd maski wynikowej");
        }

        reloadPreview();
    }

    /**
     * Tworzy obszar do sterowania wartościami masek składowych.
     *
     * @return obszar do sterowania wartościami masek
     */
    private VBox createMasksVBoxBox() {
        Label maskALabel = new Label("Maska A:");
        spinnersA = createSpinners();
        HBox spinnerA1Hbox = new HBox(spinnersA.get(0), spinnersA.get(1), spinnersA.get(2));
        HBox spinnerA2Hbox = new HBox(spinnersA.get(3), spinnersA.get(4), spinnersA.get(5));
        HBox spinnerA3Hbox = new HBox(spinnersA.get(6), spinnersA.get(7), spinnersA.get(8));

        Label maskBLabel = new Label("Maska B:");
        spinnersB = createSpinners();
        HBox spinnerB1Hbox = new HBox(spinnersB.get(0), spinnersB.get(1), spinnersB.get(2));
        HBox spinnerB2Hbox = new HBox(spinnersB.get(3), spinnersB.get(4), spinnersB.get(5));
        HBox spinnerB3Hbox = new HBox(spinnersB.get(6), spinnersB.get(7), spinnersB.get(8));

        VBox masksVBox = new VBox(maskALabel, spinnerA1Hbox, spinnerA2Hbox, spinnerA3Hbox, new Separator(),
                maskBLabel, spinnerB1Hbox, spinnerB2Hbox, spinnerB3Hbox);
        masksVBox.setPrefWidth(180);

        return masksVBox;
    }

    /**
     * Tworzy 9 pól do sterowania wartościami maski
     *
     * @return
     */
    private List<Spinner<Integer>> createSpinners() {
        List<Spinner<Integer>> spinners = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            addSpinner(spinners);
        }

        return spinners;
    }

    /**
     * Tworzy jedno pole do sterowania jedną wartością maski.
     *
     * @param spinners
     */
    private void addSpinner(List<Spinner<Integer>> spinners) {
        SpinnerValueFactory<Integer> values = new SpinnerValueFactory.ListSpinnerValueFactory<>(availableValues);
        values.setValue(1);

        Spinner<Integer> spinner = new Spinner<>(values);

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleValueChange();
        });

        spinners.add(spinner);
    }

    /**
     * Obsługuje zmianę wartości na którymkolwiek z pól do sterowania wartościami maski.
     * W momencie zmiany wartości w masce, tworzona jest nowa maska i przeprowadzana
     * jest operacja, po czym podgląd jest odświeżany.
     */
    private void handleValueChange() {
        double[] spinnerAValues = new double[9];
        for (int j = 0; j < 9; j++) {
            spinnerAValues[j] = spinnersA.get(j).getValue();
        }

        double[] spinnerBValues = new double[9];
        for (int j = 0; j < 9; j++) {
            spinnerBValues[j] = spinnersB.get(j).getValue();
        }

        changeCurrentMask(spinnerAValues, spinnerBValues);
    }

    /**
     * Tworzy obszar z opcjami dotyczącymi pikseli brzegowych.
     *
     * @return obszar z opcjami dotyczącymi pikseli brzegowych.
     */
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

        borderTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue)
                -> handleBorderOptionChange(newValue));

        VBox borderVbox = new VBox(borderTypeLabel, replicatedBorder, reflectedBorder, existingBorder, minimum, maximum);
        borderVbox.setAlignment(Pos.CENTER_LEFT);
        return borderVbox;
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
        } else if (selected == BORDER_MAXIMUM) {
            currentBorderType = Core.BORDER_CONSTANT;
            border = new Scalar(255, 255, 255, 255);
        } else {
            currentBorderType = selected;
            border = null;
        }

        reloadPreview();
    }

    /**
     * Tworzy obszar z opcjami dotyczącymi skalowania obrazu wynikowego.
     *
     * @return obszar z opcjami dotyczącymi skalowania obrazu wynikowego.
     */
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

    /**
     * Tworzy nowe maski z podanych wartości, przeprowadza operację i odświeża podgląd.
     *
     * @param valuesA wartości maski A
     * @param valuesB wartości maski B
     */
    private void changeCurrentMask(double[] valuesA, double[] valuesB) {
        currentMaskA = new Mask3x3("CUSTOM_A", false, valuesA);
        currentMaskB = new Mask3x3("CUSTOM_B", false, valuesB);
        combinedMask = MaskUtils.combineMasks(currentMaskA, currentMaskB);
        combinedMaskLabel.setText("Wynik:\n" + combinedMask.toString());

        reloadPreview();
    }

    /**
     * Tworzy podgląd obrazu po operacji.
     */
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

    /**
     * Tworzy podgląd obrazu po operacji.
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
     * Przeprowadza operację i odświeża podgląd.
     */
    private void reloadPreview() {
        after = applyMask();
        afterImageView.setImage(after);
    }

    /**
     * Zamienia obraz na obiekt Mat i przeprowadza wybraną przez użytkownika operację
     * (filtrację maską wynikową 5x5 lub dwiema maskami 3x3 po kolei), po czym zwraca
     * obiekt Image z wynikowym obrazem.
     *
     * @return wynikowy obraz
     */
    private Image applyMask() {
        if (currentMaskA == null || currentMaskB == null) {
            return before;
        }
        Mat image = ImageUtils.imageToMat(before);

        if (twoMasksPreview) {
            applyMasks(image);
        } else {
            applyCombinedMask(image);
        }


        return ImageUtils.mat2Image(image);
    }

    /**
     * Przeprowadza dwie fitracje: najpierw maską A, następnie maską B.
     * Jeśli suma wartości którejś z masek wynosi 0, obraz przed operacjami
     * jest zamieniany na szaroodcieniowy.
     * Na koniec przeprowadzane jest skalowanie wybraną przez użytkownika metodą.
     *
     * @param image obiekt Mat z obrazem
     */
    private void applyMasks(Mat image) {
        if (currentMaskA.getKernelSize() == 0 ||
                currentMaskB.getKernelSize() == 0 ||
                currentScalingMethod != METHOD_3) {
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        }

        for (int i = 0; i < times; i++) {
            FilteringUtils.applyMasks(image, currentMaskA, currentMaskB, currentBorderType, border);
        }

        MatScalingUtils.scale(image, currentScalingMethod);
    }


    /**
     * Przeprowadza fitrację maską wynikową powstałą z połączenia dwóch masek.
     * Jeśli suma wartości którejś maski wynikowej wynosi 0, obraz przed operacjami
     * jest zamieniany na szaroodcieniowy.
     * Na koniec przeprowadzane jest skalowanie wybraną przez użytkownika metodą.
     *
     * @param image obiekt Mat z obrazem
     */
    private void applyCombinedMask(Mat image) {
        if (combinedMask.getKernelSize() == 0 || currentScalingMethod != METHOD_3) {
            Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        }

        for (int i = 0; i < times; i++) {
            FilteringUtils.applyMask(image, combinedMask, currentBorderType, border);
        }

        MatScalingUtils.scale(image, currentScalingMethod);
    }

}
