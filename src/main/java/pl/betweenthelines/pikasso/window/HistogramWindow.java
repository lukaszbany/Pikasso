package pl.betweenthelines.pikasso.window;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.exception.ImageIsTooBigException;
import pl.betweenthelines.pikasso.exception.ImageNotLoadedYetException;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.image.FileData;
import pl.betweenthelines.pikasso.window.image.histogram.ChannelProperties;
import pl.betweenthelines.pikasso.window.image.histogram.EqualizeHistogram;
import pl.betweenthelines.pikasso.window.image.histogram.Histogram;
import pl.betweenthelines.pikasso.window.image.histogram.StretchHistogram;

import java.io.IOException;
import java.text.DecimalFormat;

import static pl.betweenthelines.pikasso.window.image.histogram.ChannelProperties.Channel.*;

/**
 * Klasa reprezentująca okno z histogramem.
 */
public class HistogramWindow implements Window {

    /**
     * Maksymalny poziom jasności.
     */
    public static final int MAX_LEVEL = 255;

    /**
     * Minimalny poziom jasności.
     */
    public static final int MIN_LEVEL = 0;

    /**
     * Format wyświetlanych liczb.
     */
    private static DecimalFormat FORMATTER = new DecimalFormat("0.00");

    /**
     * Okno histogramu i jego elementy.
     */
    private Stage histogramStage;
    private HBox histogramHBox;
    private VBox optionsWrapper;

    /**
     * Wykres histogramu.
     */
    private BarChart<String, Number> chart;

    /**
     * Dane histogramu.
     */
    private Histogram histogram;

    /**
     * Podgląd obrazu, którego dotyczy histogram.
     */
    private ImageView imagePreview;

    /**
     * Dane o otwartym pliku.
     */
    private FileData openedFileData;

    /**
     * CheckBoxy odpowiadające za wyświetlanie na wykresie poszczególnych kanałów.
     */
    private CheckBox redCheckbox;
    private CheckBox greenCheckbox;
    private CheckBox blueCheckbox;
    private CheckBox grayCheckbox;

    /**
     * Konstruktor budujący okno z histogramem i tworzący histogram.
     *
     * @param openedFileData dane o otwartym pliku (i zaznaczonym fragmencie)
     * @throws ImageNotLoadedYetException jeśli na obrazie zostanie wykonana operacji,
     *                                    zanim zostanie załadowany w całości.
     * @throws IOException                w razie błędu odczytu pliku
     * @throws ImageIsTooBigException     jeśli otwarty obraz ma zbyt duży rozmiar
     */
    public HistogramWindow(FileData openedFileData) throws ImageNotLoadedYetException, IOException, ImageIsTooBigException {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAnimated(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        chart = new BarChart<>(xAxis, yAxis);
        chart.setBarGap(0);
        this.openedFileData = openedFileData;

        histogramStage = new Stage();
        histogramHBox = new HBox();

        reloadHistogram(MIN_LEVEL, MAX_LEVEL);

        redCheckbox = createChannelVisibilityCheckbox(RED);
        greenCheckbox = createChannelVisibilityCheckbox(GREEN);
        blueCheckbox = createChannelVisibilityCheckbox(BLUE);
        grayCheckbox = createChannelVisibilityCheckbox(GRAY);

        createLeftOptions();
        VBox histogramVBox = createChart();
        HBox bottomOptionsHBox = createBottomOptions();

        histogramVBox.getChildren().addAll(chart, bottomOptionsHBox);
        histogramHBox.getChildren().addAll(optionsWrapper, histogramVBox);

        Scene histogramScene = new Scene(histogramHBox, 800, 640);
        histogramScene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) histogramStage.close();
        });
        histogramScene.getStylesheets().add("histogram.css");
        histogramStage.setScene(histogramScene);
        histogramStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        histogramStage.setTitle("Histogram obrazu");
        histogramStage.show();
    }

    /**
     * Tworzy menu z opcjami dotyczącymi poszczególnych kanałów (statytyki,
     * sterowanie widocznością) po lewej stronie okna.
     */
    private void createLeftOptions() {
        optionsWrapper = new VBox(createOptions());
        optionsWrapper.setMinWidth(175);
        optionsWrapper.getStyleClass().add("frame");
    }

    /**
     * Ustawia opcje wykresu histogramu.
     *
     * @return <tt>VBox</tt> z wykresem.
     */
    private VBox createChart() {
        VBox histogramVBox = new VBox();
        histogramVBox.getStyleClass().add("frame");

        chart.setCategoryGap(0);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.prefWidthProperty().bind(histogramHBox.widthProperty());
        chart.prefHeightProperty().bind(histogramHBox.heightProperty());
        chart.setAnimated(false);
        return histogramVBox;
    }

    /**
     * Tworzy opcje na dole okna dotyczące (zawężanie zakresu histogramu, odświeżenie)
     *
     * @return <tt>HBox</tt> zawierający opcje.
     */
    private HBox createBottomOptions() {
        TextField min = createTextField(String.valueOf(MIN_LEVEL));
        TextField max = createTextField(String.valueOf(MAX_LEVEL));
        RangeSlider range = createRangeSlider(min, max);
        Button refresh = createRefreshButton(range);

        HBox bottomOptionsHBox = new HBox(min, range, max, refresh);
        bottomOptionsHBox.setMaxHeight(100);
        bottomOptionsHBox.setSpacing(5);
        bottomOptionsHBox.setAlignment(Pos.TOP_RIGHT);
        return bottomOptionsHBox;
    }

    /**
     * Tworzy <tt>TextField</tt> z wartością slidera.
     *
     * @param text treść
     * @return <tt>TextField</tt> z wartością slidera.
     */
    private TextField createTextField(String text) {
        TextField textField = new TextField(text);
        textField.setMaxWidth(40);
        textField.setDisable(true);
        return textField;
    }

    /**
     * Tworzy przycisk do odświeżania histogramu.
     *
     * @param range slider z wartościami zakresu
     * @return <tt>Button</tt> do odświeżania histogramu.
     */
    private Button createRefreshButton(RangeSlider range) {
        Button refresh = new Button("Odśwież");
        refresh.setOnAction(event -> handleRefreshAction(range));
        return refresh;
    }

    /**
     * Obsługuje akcję odświeżenia histogramu.
     *
     * @param range slider z wartościami zakresu
     */
    private void handleRefreshAction(RangeSlider range) {
        try {
            reloadHistogram((int) range.getLowValue(), (int) range.getHighValue());
            optionsWrapper.getChildren().clear();
            optionsWrapper.getChildren().add(createOptions());
        } catch (Exception e) {
            ErrorHandler.handleError(e);
        }
    }

    /**
     * Tworzy <tt>RangeSlider</tt> sterujący zakresem poziomów jasności na histogramie.
     *
     * @param min pole tekstowe z wartością dolną zakresu
     * @param max pole tekstowe z wartością górną zakresu
     * @return <tt>RangeSlider</tt> z zakresem histogramu
     */
    private RangeSlider createRangeSlider(TextField min, TextField max) {
        RangeSlider range = new RangeSlider();
        range.setShowTickMarks(true);
        range.setShowTickLabels(true);
        range.setMin(MIN_LEVEL);
        range.setMax(MAX_LEVEL);
        range.setHighValue(MAX_LEVEL);
        range.setPrefHeight(200);
        range.setPrefWidth(400);
        range.highValueProperty().addListener((observable, oldValue, newValue) -> max.setText(String.valueOf(newValue.intValue())));
        range.lowValueProperty().addListener((observable, oldValue, newValue) -> min.setText(String.valueOf(newValue.intValue())));
        return range;
    }

    /**
     * Tworzy <tt>VBox</tt> z opcjami sterującymi widocznością kanałów
     * i statystykami ich dotyczącymi znajdujący się po lewej stronie
     * okna.
     *
     * @return <tt>VBox</tt> z opcjami
     */
    private VBox createOptions() {
        VBox optionsVBox = new VBox();
        optionsVBox.setMinWidth(140);

        imagePreview = new ImageView(getImage());
        imagePreview.setPreserveRatio(true);
        imagePreview.setFitHeight(80);
        imagePreview.setFitWidth(142);
        Separator separator0 = new Separator();

        Label pixelSum = new Label("Razem pikseli:\n" + histogram.getPixelsTotal());
        Separator separator1 = new Separator();

        Label redCount = new Label("Pikseli: " + histogram.getRed().getPixelCount());
        Label redMedian = new Label("Mediana: " + histogram.getRed().getMedian());
        Label redMean = new Label("Średnia: " + FORMATTER.format(histogram.getRed().getMean()));
        Label redSD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getRed().getStandardDeviation()));
        Separator separator2 = new Separator();

        Label greenCount = new Label("Pikseli: " + histogram.getGreen().getPixelCount());
        Label greenMedian = new Label("Mediana: " + histogram.getGreen().getMedian());
        Label greenMean = new Label("Średnia: " + FORMATTER.format(histogram.getGreen().getMean()));
        Label greenSD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getGreen().getStandardDeviation()));
        Separator separator3 = new Separator();

        Label blueCount = new Label("Pikseli: " + histogram.getBlue().getPixelCount());
        Label blueMedian = new Label("Mediana: " + histogram.getBlue().getMedian());
        Label blueMean = new Label("Średnia: " + FORMATTER.format(histogram.getBlue().getMean()));
        Label blueSD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getBlue().getStandardDeviation()));
        Separator separator4 = new Separator();

        Label grayCount = new Label("Pikseli: " + histogram.getGray().getPixelCount());
        Label grayMedian = new Label("Mediana: " + histogram.getGray().getMedian());
        Label grayMean = new Label("Średnia: " + FORMATTER.format(histogram.getGray().getMean()));
        Label graySD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getGray().getStandardDeviation()));

        Separator separator5 = new Separator();

        Label meanColorLabel = new Label("Średni kolor: ");

        VBox meanColorVBox = createMediumColorVBox();
        meanColorVBox.setAlignment(Pos.CENTER);

        Separator separator6 = new Separator();

        Button stretchHistogram = createStretchHistogramButton();
        Button equalizeHistogram = createEqualizeHistogramButton();

        optionsVBox.getChildren().addAll(imagePreview, separator0,
                pixelSum, separator1,
                redCheckbox, redCount, redMedian, redMean, redSD, separator2,
                greenCheckbox, greenCount, greenMedian, greenMean, greenSD, separator3,
                blueCheckbox, blueCount, blueMedian, blueMean, blueSD, separator4,
                grayCheckbox, grayCount, grayMedian, grayMean, graySD, separator5,
                meanColorLabel, meanColorVBox, separator6,
                stretchHistogram, equalizeHistogram);

        return optionsVBox;
    }

    /**
     * Tworzy podgląd średniego koloru w obrazie (lub średniego poziomu jasności)
     * wraz z jego wartością w formacie heksadecymalnym (#RRGGBB).
     *
     * @return <tt>VBox</tt> ze średnim kolorem.
     */
    private VBox createMediumColorVBox() {
        Color color = Color.rgb(
                (int) histogram.getRed().getMean(),
                (int) histogram.getGreen().getMean(),
                (int) histogram.getBlue().getMean()
        );
        Rectangle meanColor = new Rectangle(35, 35, color);
        Label hexColorLabel = new Label(getHexColor(
                histogram.getRed().getMean(),
                histogram.getGreen().getMean(),
                histogram.getBlue().getMean()));

        return new VBox(meanColor, hexColorLabel);
    }

    /**
     * Tworzy przycisk do rozciągnięcie histogramu.
     *
     * @return <tt>Button</tt> do rozciągnięcia histogramu.
     */
    private Button createStretchHistogramButton() {
        Button stretchHistogram = new Button("Rozciągnij histogram");
        stretchHistogram.setOnAction(this::handleStretchHistogramAction);

        return stretchHistogram;
    }

    /**
     * Obsługuje kliknięcie przycisku "Rozciągnij histogram" za pomocą klasy
     * <tt>StretchHistogram</tt>, po czym ustawia nowy obraz w oknie głównym
     * i odświeża histogram.
     *
     * @param event zdarzenie kliknięcia przycisku
     */
    private void handleStretchHistogramAction(Event event) {
        try {
            Image newImage = StretchHistogram.stretchHistogram(openedFileData, histogram);
            openedFileData.setImage(newImage);
            reloadHistogram(MIN_LEVEL, MAX_LEVEL);
        } catch (Exception e) {
            ErrorHandler.handleError(e);
        }
    }

    /**
     * Tworzy przycisk do wyrównania histogramu.
     *
     * @return <tt>Button</tt> do /**
     * * Tworzy przycisk do wyrównania histogramu.
     */
    private Button createEqualizeHistogramButton() {
        Button equalizeHistogram = new Button("Wyrównaj histogram");
        equalizeHistogram.setOnAction(this::handleEqualizeHistogramAction);

        return equalizeHistogram;
    }

    /**
     * Obsługuje kliknięcie przycisku "Wyrównaj histogram" za pomocą klasy
     * <tt>EqualizeHistogram</tt>, po czym ustawia nowy obraz w oknie głównym
     * i odświeża histogram.
     * <p>
     * Informacja o zaznaczeniu jest ignorowana - operacja jest przeprowadzana
     * na całym obrazie.
     *
     * @param event
     */
    private void handleEqualizeHistogramAction(Event event) {
        try {
            openedFileData.setSelection(null);
            openedFileData.setImageSelection(null);
            reloadHistogram(MIN_LEVEL, MAX_LEVEL);

            Image newImage = EqualizeHistogram.equalizeHistogram(openedFileData);
            openedFileData.setImage(newImage);
            reloadHistogram(MIN_LEVEL, MAX_LEVEL);
        } catch (Exception e) {
            ErrorHandler.handleError(e);
        }
    }

    /**
     * Odświeża histogram dla podanego zakresu poziomów jasności.
     *
     * @param minLevel dolny poziom jasności
     * @param maxLevel górny poziom jasności
     * @throws ImageIsTooBigException     błąd związany ze zbyt dużym obrazem.
     * @throws IOException                błąd wczytywania obrazu
     * @throws ImageNotLoadedYetException błąd związany wykonaniem operacji na obrazie,
     *                                    który nie zdążył się załadować.
     */
    private void reloadHistogram(int minLevel, int maxLevel) throws ImageIsTooBigException, IOException, ImageNotLoadedYetException {
        histogram = new Histogram(getImage(), minLevel, maxLevel);
        chart.getData().clear();
        chart.getData().addAll(
                histogram.getRed().getSeries(),
                histogram.getGreen().getSeries(),
                histogram.getBlue().getSeries(),
                histogram.getGray().getSeries()
        );
    }

    /**
     * Pobiera obraz lub jego zaznaczony fragment.
     *
     * @return obraz lub jego zaznaczony fragment
     */
    private Image getImage() {
        if (openedFileData.getImageSelection() != null) {
            return openedFileData.getImageSelection();
        }

        return ImageUtils.getFxImage(openedFileData.getImageView());
    }

    /**
     * Z podanych poziomów jasności generuje wartość heksadecymalną
     * odpowiadającą temu kolorowi.
     *
     * @param red   poziom kanału czerwonego
     * @param green poziom kanału zielonego
     * @param blue  poziom kanału niebieskiego
     * @return <tt>String</tt> z wartością heksadecymalną koloru.
     */
    private String getHexColor(double red, double green, double blue) {
        return "#" + Integer.toHexString((int) red) +
                Integer.toHexString((int) green) +
                Integer.toHexString((int) blue);
    }

    /**
     * Tworzy <tt>CheckBox</tt> z sterujący widocznością podanego kanału na histogramie.
     * To, czy kanał jest domyślnie widoczny, zależy od tego, czy obraz jest kolorowy,
     * czy w odcieniach szarości:
     * <or>
     * <li>Obraz kolorowy - domyślnie widoczne kanały RGB</li>
     * <li>Obraz szaroodcieniowy - domyślnie widoczny poziom szarości</li>
     * </or>
     *
     * @param channel kanał, dla ktorego tworzony jest <tt>CheckBox</tt>
     * @return <tt>CheckBox</tt> z podanym kanałem
     */
    private CheckBox createChannelVisibilityCheckbox(ChannelProperties.Channel channel) {
        boolean isVisible = GRAY.equals(channel) == histogram.isGrayscale();

        CheckBox checkBox = new CheckBox(channel.getName());
        checkBox.setSelected(isVisible);
        if (!isVisible) {
            chart.getStyleClass().add(channel.getUncheckedClass());
        }

        checkBox.selectedProperty().addListener((observable, oldValue, newValue)
                -> handleSelectedChange(channel, newValue));

        return checkBox;
    }

    /**
     * Obsługuje zaznaczenie lub odznaczenie <tt>CheckBoxa</tt>. Nadaje lub usuwa
     * klasę CSS dla odpowiedniego kanału na wykresie.
     *
     * @param channel  kanał, którego dotyczy akcja
     * @param newValue nowa wartość zaznaczenia
     */
    private void handleSelectedChange(ChannelProperties.Channel channel, Boolean newValue) {
        if (newValue) {
            chart.getStyleClass().remove(channel.getUncheckedClass());
        } else {
            chart.getStyleClass().add(channel.getUncheckedClass());
        }
    }

    /**
     * Zamyka okno histogramu.
     */
    public void close() {
        histogramStage.close();
    }

    /**
     * Konstruktor okna histogramu bez opcji - tylko wykres.
     *
     * @param imageView z obrazem, dla którego utworzony będzie histogram.
     * @throws ImageNotLoadedYetException jeśli na obrazie zostanie wykonana operacji,
     *                                    zanim zostanie załadowany w całości.
     * @throws IOException                w razie błędu odczytu pliku
     * @throws ImageIsTooBigException     jeśli otwarty obraz ma zbyt duży rozmiar
     */
    public HistogramWindow(ImageView imageView) throws ImageNotLoadedYetException, IOException, ImageIsTooBigException {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAnimated(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);

        histogramStage = new Stage();
        histogramHBox = new HBox();

        chart = new BarChart<>(xAxis, yAxis);
        chart.setBarGap(0);
        chart.setCategoryGap(0);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.prefWidthProperty().bind(histogramHBox.widthProperty());
        chart.prefHeightProperty().bind(histogramHBox.heightProperty());
        chart.setAnimated(false);

        this.openedFileData = new FileData(imageView);

        reloadHistogram(MIN_LEVEL, MAX_LEVEL);

        redCheckbox = createChannelVisibilityCheckbox(RED);
        greenCheckbox = createChannelVisibilityCheckbox(GREEN);
        blueCheckbox = createChannelVisibilityCheckbox(BLUE);
        grayCheckbox = createChannelVisibilityCheckbox(GRAY);

        histogramHBox.getChildren().addAll(chart);
        histogramHBox.setAlignment(Pos.CENTER);

        Scene histogramScene = new Scene(histogramHBox, 560, 480);
        histogramScene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) histogramStage.close();
        });
        histogramScene.getStylesheets().add("histogram.css");
        histogramStage.setScene(histogramScene);
        histogramStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        histogramStage.setTitle("Histogram obrazu");
        histogramStage.initModality(Modality.WINDOW_MODAL);
        histogramStage.show();
    }
}
