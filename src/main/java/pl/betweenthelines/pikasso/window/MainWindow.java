package pl.betweenthelines.pikasso.window;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Light.Point;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.domain.FileData;
import pl.betweenthelines.pikasso.window.domain.operation.NegationWindow;
import pl.betweenthelines.pikasso.window.domain.operation.PosterizeWindow;
import pl.betweenthelines.pikasso.window.domain.operation.StretchToRangeWindow;
import pl.betweenthelines.pikasso.window.domain.operation.ThresholdOneArgWindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static pl.betweenthelines.pikasso.utils.ImageUtils.getImageSelection;

public class MainWindow {

    private static final double ZOOM_SPEED = 0.05;
    private Stage mainStage;
    private File lastDirectory;
    private FileData openedFileData;
    private MenuBar menuBar;
    private Pane imagePane;
    private ScrollPane scrollPane;
    private ImageView imageView;

    private List<MenuItem> enabledWhenFileOpended;

    private HBox statusBar;
    private Slider zoomSlider;
    private Label imageSize;

    private HistogramWindow histogramWindow;
    private List<Window> openedWindows;

    private Rectangle selection;
    private Point startPoint;

    public MainWindow(Stage mainStage) {
        this.mainStage = mainStage;
        enabledWhenFileOpended = new ArrayList<>();
        openedWindows = new ArrayList<>();
        buildWindow();
        refreshWindow();
    }

    private void buildWindow() {
        createMainStage();
        mainStage.setScene(createScene());

        mainStage.show();
    }

    private Scene createScene() {
        createMenu();
        createScrollPane();
        Separator separator = new Separator();
        createStatusBar();


        VBox mainVBox = new VBox(menuBar, scrollPane, separator, statusBar);
        mainVBox.getStylesheets().add("main.css");
        return new Scene(mainVBox, 1000, 600);
    }

    private void createSelection() {
        startPoint = new Point();
        selection = new Rectangle(0, 0, 0, 0);
        Paint fill = Color.rgb(69, 69, 69, 0.2);
        selection.setFill(fill);

        Paint stroke = Color.WHITE;
        selection.setStroke(stroke);
        selection.getStrokeDashArray().add(10.0);
        imageView.setOnMousePressed(event -> {
            startPoint.setX(event.getX());
            startPoint.setY(event.getY());
            selection.setX(event.getX());
            selection.setY(event.getY());
            selection.setWidth(0);
            selection.setHeight(0);
        });

        imageView.setOnMouseDragged(event -> {
            double xPoint = Math.min(event.getX(), startPoint.getX());
            double yPoint = Math.min(event.getY(), startPoint.getY());
            double height = Math.abs(event.getY() - startPoint.getY());
            double width = Math.abs(event.getX() - startPoint.getX());

            selection.setX(xPoint);
            selection.setY(yPoint);
            selection.setHeight(height);
            selection.setWidth(width);
        });

        imageView.setOnMouseReleased(event -> {
            double divider = imageView.getImage().getHeight() / imageView.getFitHeight();
            double maxX = imageView.getImage().getWidth() / divider;
            double maxY = imageView.getFitHeight();
            double offsetX = 0 - Math.min(event.getX(), startPoint.getX());
            double offsetY = 0 - Math.min(event.getY(), startPoint.getY());
            double xPoint = Math.max(0, Math.min(event.getX(), startPoint.getX()));
            double yPoint = Math.max(0, Math.min(event.getY(), startPoint.getY()));

            double height = Math.abs(event.getY() - startPoint.getY());
            if (offsetY > 0) {
                height -= offsetY;
            } else if (event.getY() > maxY) {
                double maxHeight = maxY - startPoint.getY();
                height = Math.min(height, maxHeight);
            }

            double width = Math.abs(event.getX() - startPoint.getX());
            if (offsetX > 0) {
                width -= offsetX;
            } else if (event.getX() > maxX) {
                double maxWidth = maxX - startPoint.getX();
                width = Math.min(width, maxWidth);
            }

            selection.setX(xPoint);
            selection.setY(yPoint);
            selection.setHeight(height);
            selection.setWidth(width);

            if (height > 2 && width > 2) {
                Image imageSelection = getImageSelection(imageView, selection.getX(), selection.getY(), selection.getWidth(), selection.getHeight());
                openedFileData.setImageSelection(imageSelection);
                openedFileData.setSelection(selection);
            } else {
                openedFileData.setImageSelection(null);
                openedFileData.setSelection(null);
            }
        });
    }

    private void createMenu() {
        menuBar = new MenuBar();
        imageView = new ImageView();
        menuBar.getMenus().addAll(createFileMenu(), createImageMenu(), createOperationsMenu());
    }

    private Menu createOperationsMenu() {
        Menu operationsMenu = new Menu("Operacje");
        MenuItem desaturate = createDesaturationItem();

        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        Menu oneArg = new Menu("Jednoargumentowe");
        MenuItem negation = new MenuItem("Negacja");
        negation.setOnAction(event -> {
            try {
                NegationWindow negationWindow = new NegationWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        MenuItem thresholdOneArg = new MenuItem("Progowanie");
        thresholdOneArg.setOnAction(event -> {
            try {
                ThresholdOneArgWindow thresholdOneArgWindow = new ThresholdOneArgWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        MenuItem posterize = new MenuItem("Posteryzacja");
        posterize.setOnAction(event -> {
            try {
                PosterizeWindow posterizeWindow = new PosterizeWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        MenuItem stretchToRange = new MenuItem("Rozciąganie do poziomów jasności");
        stretchToRange.setOnAction(event -> {
            try {
                StretchToRangeWindow stretchToRangeWindow = new StretchToRangeWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        oneArg.getItems().addAll(negation, thresholdOneArg, posterize, stretchToRange);

        operationsMenu.getItems().addAll(desaturate, separator1, oneArg);
        return operationsMenu;
    }

    private MenuItem createDesaturationItem() {
        MenuItem desaturate = new MenuItem("Desaturacja");
        enabledWhenFileOpended.add(desaturate);
        desaturate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ColorAdjust colorAdjust = new ColorAdjust(0, -1, 0, 0);
                imageView.setEffect(colorAdjust);
            }
        });
        return desaturate;
    }

    private void createMainStage() {
        mainStage.setTitle("PIKAsso");
        mainStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        mainStage.setOnCloseRequest(event -> Platform.exit());
    }

    private void createStatusBar() {
        Label sliderValue = createZoomSlider();
        Separator separator = new Separator(Orientation.VERTICAL);
        imageSize = new Label("");
        statusBar = new HBox(imageSize, separator, sliderValue, zoomSlider);
        statusBar.setMinHeight(25);
        statusBar.setMaxHeight(25);
        statusBar.setAlignment(Pos.CENTER_RIGHT);
    }

    private Label createZoomSlider() {
        zoomSlider = new Slider();
        zoomSlider.setMin(0.1);
        zoomSlider.setValue(1);
        zoomSlider.setMax(4);
        Label sliderValue = new Label((int) (zoomSlider.getValue() * 100) + "%");
        zoomSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderValue.setText((int) (zoomSlider.getValue() * 100) + "%");
            resetSelection();
        });
        return sliderValue;
    }

    private void resetSelection() {
        selection.setHeight(0);
        selection.setWidth(0);
    }

    private void createScrollPane() {
        createSelection();
        imagePane = new Pane(imageView, selection);
        scrollPane = new ScrollPane(imagePane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.prefWidthProperty().bind(mainStage.widthProperty());
        scrollPane.prefHeightProperty().bind(mainStage.heightProperty());
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!zoomSlider.isDisabled() && event.isControlDown()) {
                double delta = event.getDeltaY();
                event.consume();
                if (delta > 0) {
                    zoomSlider.setValue(zoomSlider.getValue() + ZOOM_SPEED);
                } else {
                    zoomSlider.setValue(zoomSlider.getValue() - ZOOM_SPEED);
                }
            }
        });
    }

    private Menu createImageMenu() {
        Menu imageMenu = new Menu("Obraz");
        imageMenu.getItems().add(createHistogramItem());
        return imageMenu;
    }

    private MenuItem createHistogramItem() {
        MenuItem histogramItem = new MenuItem("Histogram");
        enabledWhenFileOpended.add(histogramItem);
        histogramItem.setOnAction(event -> {
            if (histogramWindow != null) {
                histogramWindow.close();
                openedWindows.remove(histogramWindow);
                histogramWindow = null;
            }

            try {
                histogramWindow = new HistogramWindow(openedFileData);
                openedWindows.add(histogramWindow);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        return histogramItem;
    }

    private Menu createFileMenu() {
        Menu menu = new Menu("Plik");
        MenuItem openFile = createOpenFileItem();
        MenuItem saveFileAs = createSaveFileAs();
        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        MenuItem closeFile = createCloseFileItem();
        SeparatorMenuItem separator2 = new SeparatorMenuItem();
        MenuItem closeApp = createCloseAppItem();

        menu.getItems().addAll(openFile, saveFileAs, separator1, closeFile, separator2, closeApp);
        return menu;
    }

    private MenuItem createSaveFileAs() {
        MenuItem saveFileAs = new MenuItem("Zapisz jako");
        enabledWhenFileOpended.add(saveFileAs);

        FileChooser fileChooser = prepareSaveFileChooser();

        saveFileAs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (lastDirectory != null) {
                    fileChooser.setInitialDirectory(lastDirectory);
                }

                File file = fileChooser.showSaveDialog(mainStage);
                if (file != null) {
                    //TODO: Check if user haven't write other extension in filename!
                    String extension = FilenameUtils.getExtension(file.getName());
                    BufferedImage bufferedImage = ImageUtils.getBufferedImage(imageView);
                    if (savingAsJpgOrBmp(extension)) {
                        bufferedImage = convertToRGB(bufferedImage);
                    }

                    saveBufferedImage(file, extension, bufferedImage);
                }
            }
        });
        return saveFileAs;
    }

    private FileChooser prepareSaveFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik jako...");
        FileChooser.ExtensionFilter jpg = new FileChooser.ExtensionFilter("Obraz JPG", "*.jpg");
        FileChooser.ExtensionFilter bmp = new FileChooser.ExtensionFilter("Obraz BMP", "*.bmp");
        FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("Obraz PNG", "*.png");
        fileChooser.getExtensionFilters().addAll(jpg, bmp, png);

        return fileChooser;
    }

    private boolean savingAsJpgOrBmp(String extension) {
        return "jpg".equals(extension.toLowerCase()) || "bmp".equals(extension.toLowerCase());
    }

    private BufferedImage convertToRGB(BufferedImage bufferedImage) {
        BufferedImage rgbImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgbImage.createGraphics().drawImage(bufferedImage, 0, 0, null);

        return rgbImage;
    }

    private void saveBufferedImage(File file, String extension, BufferedImage bufferedImage) {
        try {
            boolean success = ImageIO.write(bufferedImage, extension, file);
            if (!success) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            ErrorHandler.showAlert(e, "Błąd podczas zapisu pliku.");
        }
    }

    private MenuItem createOpenFileItem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz plik");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Obrazy", "*.jpg", "*.bmp", "*.png");
        fileChooser.getExtensionFilters().add(extensionFilter);
        MenuItem openFile = new MenuItem("Otwórz");
        openFile.setOnAction(event -> {
            if (lastDirectory != null) {
                fileChooser.setInitialDirectory(lastDirectory);
            }

            File file;
            try {
                file = fileChooser.showOpenDialog(mainStage);
            } catch (Exception e) {
                ErrorHandler.showAlert(e, "Błąd podczas otwierania pliku!");
                return;
            }

            if (file != null) {
                try {
                    openImage(file);
                } catch (FileNotFoundException e) {
                    ErrorHandler.handleError(e);
                }
            }

            refreshWindow();
        });
        return openFile;
    }

    private void openImage(File file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        Image image = new Image(fileInputStream);
        imageView.setEffect(null);
        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(zoomSlider.valueProperty().multiply(image.getHeight()));
        imageView.setImage(image);
        zoomSlider.setValue(calculateZoom(image));
        openedFileData = new FileData(file, imageView, null, null);
        lastDirectory = file.getParentFile();
    }

    private double calculateZoom(Image image) {
        double heightMultiplier = 1;
        double widthMultiplier = 1;
        if (image.getHeight() > scrollPane.getHeight()) {
            heightMultiplier = image.getHeight() / scrollPane.getHeight();
        }
        if (image.getWidth() > scrollPane.getWidth()) {
            widthMultiplier = image.getWidth() / scrollPane.getWidth();
        }

        return 1 / (Math.max(heightMultiplier, widthMultiplier));
    }

    private MenuItem createCloseFileItem() {
        MenuItem closeFile = new MenuItem("Zamknij plik");
        enabledWhenFileOpended.add(closeFile);
        closeFile.setOnAction(event -> {
            openedFileData = null;
            imageView.setImage(null);
            refreshWindow();
        });
        return closeFile;
    }

    private MenuItem createCloseAppItem() {
        MenuItem closeApp = new MenuItem("Zamknij program");
        closeApp.setOnAction(event -> Platform.exit());
        return closeApp;
    }

    private void refreshWindow() {
        refreshWindowTitle();
        enabledWhenFileOpended.forEach(menuItem -> menuItem.setDisable(openedFileData == null));
        refreshStatusBar();
        resetSelection();

        closeOpenedWindows();
    }

    private void refreshStatusBar() {
        zoomSlider.setDisable(openedFileData == null);
        refreshImageSize();
    }

    private void refreshImageSize() {
        if (openedFileData != null) {
            Image openedImage = openedFileData.getImageView().getImage();
            imageSize.setText((int) openedImage.getWidth() + "x" + (int) openedImage.getHeight());
        } else {
            imageSize.setText("");
        }
    }

    private void closeOpenedWindows() {
        openedWindows.forEach(Window::close);
        openedWindows.clear();
    }

    private void refreshWindowTitle() {
        if (openedFileData != null) {
            mainStage.setTitle("PIKAsso - " + openedFileData.getFile().getName());
        } else {
            mainStage.setTitle("PIKAsso");
        }
    }
}
