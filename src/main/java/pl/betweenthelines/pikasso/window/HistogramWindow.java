package pl.betweenthelines.pikasso.window;

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
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.exception.ImageIsTooBigException;
import pl.betweenthelines.pikasso.exception.ImageNotLoadedYetException;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.domain.FileData;
import pl.betweenthelines.pikasso.window.domain.histogram.ChannelProperties;
import pl.betweenthelines.pikasso.window.domain.histogram.EqualizeHistogram;
import pl.betweenthelines.pikasso.window.domain.histogram.Histogram;
import pl.betweenthelines.pikasso.window.domain.histogram.StretchHistogram;

import java.io.IOException;
import java.text.DecimalFormat;

import static pl.betweenthelines.pikasso.window.domain.histogram.ChannelProperties.Channel.*;

public class HistogramWindow implements Window {

    public static final int MAX_LEVEL = 255;
    public static final int MIN_LEVEL = 0;
    private static DecimalFormat FORMATTER = new DecimalFormat("0.00");

    private Stage histogramStage;
    private HBox histogramHBox;
    private VBox optionsWrapper;
    private BarChart<String, Number> chart;
    private Histogram histogram;
    private ImageView imagePreview;
    private FileData openedFileData;

    CheckBox redCheckbox;
    CheckBox greenCheckbox;
    CheckBox blueCheckbox;
    CheckBox grayCheckbox;

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

        Scene histogramScene = new Scene(histogramHBox, 800, 600);
        histogramScene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) histogramStage.close();
        });
        histogramScene.getStylesheets().add("histogram.css");
        histogramStage.setScene(histogramScene);
        histogramStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        histogramStage.setTitle("Histogram obrazu");
        histogramStage.show();
    }

    private void createLeftOptions() {
        optionsWrapper = new VBox(createOptions());
        optionsWrapper.setMinWidth(175);
        optionsWrapper.getStyleClass().add("frame");
    }

    private VBox createChart() {
        VBox histogramVBox = new VBox();
        histogramVBox.getStyleClass().add("frame");

//        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.prefWidthProperty().bind(histogramHBox.widthProperty());
        chart.prefHeightProperty().bind(histogramHBox.heightProperty());
        chart.setAnimated(false);
        return histogramVBox;
    }

    private HBox createBottomOptions() {
        TextField min = new TextField(String.valueOf(MIN_LEVEL));
        min.setMaxWidth(40);
        min.setDisable(true);
        TextField max = new TextField(String.valueOf(MAX_LEVEL));
        max.setDisable(true);
        max.setMaxWidth(40);
        RangeSlider range = createRangeSlider(min, max);
        Button refresh = createRefreshButton(range);
        HBox bottomOptionsHBox = new HBox(min, range, max, refresh);
        bottomOptionsHBox.setMaxHeight(100);
        bottomOptionsHBox.setSpacing(5);
        bottomOptionsHBox.setAlignment(Pos.TOP_RIGHT);
        return bottomOptionsHBox;
    }

    private Button createRefreshButton(RangeSlider range) {
        Button refresh = new Button("Odśwież");
        refresh.setOnAction(event -> {
            try {
                reloadHistogram((int) range.getLowValue(), (int) range.getHighValue());
                optionsWrapper.getChildren().clear();
                optionsWrapper.getChildren().add(createOptions());
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        return refresh;
    }

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

        Label redMedian = new Label("Mediana: " + histogram.getRed().getMedian());
        Label redMean = new Label("Średnia: " + FORMATTER.format(histogram.getRed().getMean()));
        Label redSD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getRed().getStandardDeviation()));
        Separator separator2 = new Separator();

        Label greenMedian = new Label("Mediana: " + histogram.getGreen().getMedian());
        Label greenMean = new Label("Średnia: " + FORMATTER.format(histogram.getGreen().getMean()));
        Label greenSD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getGreen().getStandardDeviation()));
        Separator separator3 = new Separator();

        Label blueMedian = new Label("Mediana: " + histogram.getBlue().getMedian());
        Label blueMean = new Label("Średnia: " + FORMATTER.format(histogram.getBlue().getMean()));
        Label blueSD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getBlue().getStandardDeviation()));
        Separator separator4 = new Separator();

        Label grayMedian = new Label("Mediana: " + histogram.getGray().getMedian());
        Label grayMean = new Label("Średnia: " + FORMATTER.format(histogram.getGray().getMean()));
        Label graySD = new Label("Odch. stand.: " + FORMATTER.format(histogram.getGray().getStandardDeviation()));

        Separator separator5 = new Separator();

        Label meanColorLabel = new Label("Średni kolor: ");

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

        VBox meanColorVBox = new VBox(meanColor, hexColorLabel);
        meanColorVBox.setAlignment(Pos.CENTER);

        Separator separator6 = new Separator();

        Button stretchHistogram = createStretchHistogramButton();
        Button equalizeHistogram = createEqualizeHistogramButton();

        optionsVBox.getChildren().addAll(imagePreview, separator0,
                pixelSum, separator1,
                redCheckbox, redMedian, redMean, redSD, separator2,
                greenCheckbox, greenMedian, greenMean, greenSD, separator3,
                blueCheckbox, blueMedian, blueMean, blueSD, separator4,
                grayCheckbox, grayMedian, grayMean, graySD, separator5,
                meanColorLabel, meanColorVBox, separator6,
                stretchHistogram, equalizeHistogram);

        return optionsVBox;
    }

    private Button createStretchHistogramButton() {
        Button stretchHistogram = new Button("Rozciągnij histogram");
        stretchHistogram.setOnAction(event -> {
            try {
                Image newImage = StretchHistogram.stretchHistogram(openedFileData, histogram);
                openedFileData.setImage(newImage);
                reloadHistogram(MIN_LEVEL, MAX_LEVEL);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        return stretchHistogram;
    }

    private Button createEqualizeHistogramButton() {
        Button equalizeHistogram = new Button("Wyrównaj histogram");
        equalizeHistogram.setOnAction(event -> {
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
        });

        return equalizeHistogram;
    }

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

    private Image getImage() {
        if (openedFileData.getImageSelection() != null) {
            return openedFileData.getImageSelection();
        }

        return ImageUtils.getFxImage(openedFileData.getImageView());
    }

    private String getHexColor(double red, double green, double blue) {
        return "#" + Integer.toHexString((int) red) +
                Integer.toHexString((int) green) +
                Integer.toHexString((int) blue);
    }

    private CheckBox createChannelVisibilityCheckbox(ChannelProperties.Channel channel) {
        CheckBox checkBox = new CheckBox(channel.getName());
        boolean isVisible = GRAY.equals(channel) == histogram.isGrayscale();
        checkBox.setSelected(isVisible);
        if (!isVisible) {
            chart.getStyleClass().add(channel.getUncheckedClass());
        }

        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chart.getStyleClass().remove(channel.getUncheckedClass());
            } else {
                chart.getStyleClass().add(channel.getUncheckedClass());
            }
        });
        return checkBox;
    }


    public void close() {
        histogramStage.close();
    }
}
