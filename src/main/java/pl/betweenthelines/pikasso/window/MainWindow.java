package pl.betweenthelines.pikasso.window;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Light.Point;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
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
import pl.betweenthelines.pikasso.exception.FileOpenException;
import pl.betweenthelines.pikasso.exception.FileTypeNotSupported;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.image.FileData;
import pl.betweenthelines.pikasso.window.image.operation.directional.PrewittFilterWindow;
import pl.betweenthelines.pikasso.window.image.operation.directional.RobertsFilterWindow;
import pl.betweenthelines.pikasso.window.image.operation.directional.SobelFilterWindow;
import pl.betweenthelines.pikasso.window.image.operation.linear.*;
import pl.betweenthelines.pikasso.window.image.operation.median.MedianFilterWindow;
import pl.betweenthelines.pikasso.window.image.operation.morphology.MorphologyWindow;
import pl.betweenthelines.pikasso.window.image.operation.onearg.NegationWindow;
import pl.betweenthelines.pikasso.window.image.operation.onearg.PosterizeWindow;
import pl.betweenthelines.pikasso.window.image.operation.onearg.StretchToRangeWindow;
import pl.betweenthelines.pikasso.window.image.operation.onearg.ThresholdOneArgWindow;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pl.betweenthelines.pikasso.utils.ImageUtils.getImageSelection;

/**
 * Klasa reprezentuje główne okno programu.
 */
public class MainWindow {

    /**
     * Definiuje szybkość powiększania i pomniejszania obrazu za pomocą scrolla.
     */
    private static final double ZOOM_SPEED = 0.05;

    /**
     * Lista akceptowanych rozszerzeń.
     */
    private static final List<String> ACCEPTED_EXTENSIONS = Arrays.asList("*.jpg", "*.jpeg", "*.bmp", "*.png", "*.tif");

    /**
     * Główne okno programu.
     */
    private Stage mainStage;

    /**
     * Przechowuje ścieżkę do katalogu, z którego pochodzi ostatnio otwarty obraz.
     */
    private File lastDirectory;

    /**
     * Obiekt zawierający informacje o otwartym pliku.
     */
    private FileData openedFileData;

    /**
     * Pasek menu.
     */
    private MenuBar menuBar;

    /**
     * Obiekty odpowiadające za wyświetlanie obrazu.
     */
    private Pane imagePane;
    private ScrollPane scrollPane;
    private ImageView imageView;

    /**
     * Pozycja menu - cofnięcie ostatniej zmiany.
     */
    private MenuItem undoItem;

    /**
     * Lista pozycji menu, które są zablokowane, jeśli żaden obraz nie jest otwarty.
     */
    private List<MenuItem> enabledWhenFileOpended;

    /**
     * Pasek statusu wyświetlany na dole okna.
     */
    private HBox statusBar;

    /**
     * Slider do powiększania i pomnięjszania obrazu.
     */
    private Slider zoomSlider;

    /**
     * Wartość przybliżenia.
     */
    private Label imageSize;

    /**
     * Okno histogramu.
     */
    private HistogramWindow histogramWindow;

    /**
     * Lista otwartych okien.
     */
    private List<Window> openedWindows;

    /**
     * Prostokąt reprezentujący zaznaczony obszar.
     */
    private Rectangle selection;

    /**
     * Punkt początkowy zaznaczonego obszaru.
     */
    private Point startPoint;

    /**
     * Konstruktor głównego okna programu.
     *
     * @param mainStage
     */
    public MainWindow(Stage mainStage) {
        this.mainStage = mainStage;
        enabledWhenFileOpended = new ArrayList<>();
        openedWindows = new ArrayList<>();
        buildWindow();
        refreshWindow();
    }

    /**
     * Buduje główne okno programu.
     */
    private void buildWindow() {
        createMainStage();
        mainStage.setScene(createScene());

        mainStage.show();
    }

    /**
     * Buduje układ głównego okna programu.
     *
     * @return <tt>Scene</tt> - obiekt z układem głównego okna programu.
     */
    private Scene createScene() {
        createMenu();
        createScrollPane();
        Separator separator = new Separator();
        createStatusBar();


        VBox mainVBox = new VBox(menuBar, scrollPane, separator, statusBar);
        mainVBox.getStylesheets().add("main.css");
        return new Scene(mainVBox, 1000, 600);
    }

    /**
     * Tworzy menu główne programu.
     */
    private void createMenu() {
        menuBar = new MenuBar();
        imageView = new ImageView();
        menuBar.getMenus().addAll(createFileMenu(), createEditMenu(), createImageMenu(), createOperationsMenu());
    }

    /**
     * Tworzy menu z operacjami na obrazie.
     *
     * @return menu operacji
     */
    private Menu createOperationsMenu() {
        Menu operationsMenu = new Menu("Operacje");
        MenuItem desaturate = createDesaturationItem();

        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        Menu oneArg = createOneArgMenu();
        Menu linear = createLinear();
        Menu median = createMedian();
        Menu directional = createDirectional();
        MenuItem morphologicalOperations = createMorphologicalOperationsMenu();

        operationsMenu.getItems().addAll(desaturate, separator1, oneArg, linear, median, directional, morphologicalOperations);
        return operationsMenu;
    }

    /**
     * Tworzy menu z operacjami morfologicznymi.
     *
     * @return menu z operacjami morfologicznymi
     */
    private MenuItem createMorphologicalOperationsMenu() {
        MenuItem morphologicalOperations = new MenuItem("Operacje morfologiczne");
        enabledWhenFileOpended.add(morphologicalOperations);
        morphologicalOperations.setOnAction(event -> {
            try {
                MorphologyWindow morphologyWindow = new MorphologyWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        return morphologicalOperations;
    }

    /**
     * Tworzy menu z operacjami jednoargumentowymi.
     *
     * @return menu z operacjami jednoargumentowymi.
     */
    private Menu createOneArgMenu() {
        Menu oneArg = new Menu("Jednoargumentowe");
        MenuItem negation = new MenuItem("Negacja");
        enabledWhenFileOpended.add(negation);
        negation.setOnAction(event -> {
            try {
                NegationWindow negationWindow = new NegationWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem thresholdOneArg = new MenuItem("Progowanie");
        enabledWhenFileOpended.add(thresholdOneArg);
        thresholdOneArg.setOnAction(event -> {
            try {
                ThresholdOneArgWindow thresholdOneArgWindow = new ThresholdOneArgWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem posterize = new MenuItem("Posteryzacja");
        enabledWhenFileOpended.add(posterize);
        posterize.setOnAction(event -> {
            try {
                PosterizeWindow posterizeWindow = new PosterizeWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem stretchToRange = new MenuItem("Rozciąganie do poziomów jasności");
        enabledWhenFileOpended.add(stretchToRange);
        stretchToRange.setOnAction(event -> {
            try {
                StretchToRangeWindow stretchToRangeWindow = new StretchToRangeWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
        oneArg.getItems().addAll(negation, thresholdOneArg, posterize, stretchToRange);
        return oneArg;
    }

    /**
     * Tworzy menu z operacjami liniowymi.
     *
     * @return menu z operacjami liniowymi.
     */
    private Menu createLinear() {
        Menu linear = new Menu("Liniowe");
        MenuItem smoothing = new MenuItem("Wygładzanie");
        enabledWhenFileOpended.add(smoothing);
        smoothing.setOnAction(event -> {
            try {
                SmoothLinearWindow smoothLinearWindow = new SmoothLinearWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem sharpen = new MenuItem("Wyostrzanie");
        enabledWhenFileOpended.add(sharpen);
        sharpen.setOnAction(event -> {
            try {
                SharpenWindow sharpenWindow = new SharpenWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem edgeDetection = new MenuItem("Detekcja krawędzi");
        enabledWhenFileOpended.add(edgeDetection);
        edgeDetection.setOnAction(event -> {
            try {
                EdgeDetectionWindow edgeDetectionWindow = new EdgeDetectionWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem createMask = new MenuItem("Własna maska");
        enabledWhenFileOpended.add(createMask);
        createMask.setOnAction(event -> {
            try {
                CreateMaskWindow createMaskWindow = new CreateMaskWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem combineMasks = new MenuItem("Łączenie masek");
        enabledWhenFileOpended.add(combineMasks);
        combineMasks.setOnAction(event -> {
            try {
                CombineMasksWindow createMaskWindow = new CombineMasksWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        linear.getItems().addAll(smoothing, sharpen, edgeDetection, new SeparatorMenuItem(), createMask, combineMasks);
        return linear;
    }

    /**
     * Tworzy menu z operacjami medianowymi.
     *
     * @return menu z operacjami medianowymi.
     */
    private Menu createMedian() {
        Menu linear = new Menu("Medianowe");
        MenuItem medianFiltering = new MenuItem("Filtracja medianowa");
        enabledWhenFileOpended.add(medianFiltering);
        medianFiltering.setOnAction(event -> {
            try {
                MedianFilterWindow medianFilterWindow = new MedianFilterWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });


        linear.getItems().addAll(medianFiltering);
        return linear;
    }

    /**
     * Tworzy menu z operacjami kierunkowymi.
     *
     * @return menu z operacjami kierunkowymi.
     */
    private Menu createDirectional() {
        Menu linear = new Menu("Kierunkowe");
        MenuItem sobelFilter = new MenuItem("Filtr Sobela");
        enabledWhenFileOpended.add(sobelFilter);
        sobelFilter.setOnAction(event -> {
            try {
                SobelFilterWindow sobelFilterWindow = new SobelFilterWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem robertsFilter = new MenuItem("Filtr Robertsa");
        enabledWhenFileOpended.add(robertsFilter);
        robertsFilter.setOnAction(event -> {
            try {
                RobertsFilterWindow robertsFilterWindow = new RobertsFilterWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        MenuItem prewittFilter = new MenuItem("Filtr Prewitta");
        enabledWhenFileOpended.add(prewittFilter);
        prewittFilter.setOnAction(event -> {
            try {
                PrewittFilterWindow prewittFilterWindow = new PrewittFilterWindow(openedFileData);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });

        linear.getItems().addAll(sobelFilter, robertsFilter, prewittFilter);
        return linear;
    }

    /**
     * Tworzy opcję menu - desaturacja
     *
     * @return <tt>MenuItem</tt> desaturacja
     */
    private MenuItem createDesaturationItem() {
        MenuItem desaturate = new MenuItem("Desaturacja");
        enabledWhenFileOpended.add(desaturate);
        desaturate.setOnAction(event -> {
            Image image = ImageUtils.toGrayscale(imageView.getImage());
            openedFileData.setImage(image);
        });
        return desaturate;
    }

    /**
     * Ustawia tytuł, ikonę i zachowanie w momencie zamknięcia głównego okna.
     */
    private void createMainStage() {
        mainStage.setTitle("PIKAsso");
        mainStage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        mainStage.setOnCloseRequest(event -> Platform.exit());
    }

    /**
     * Tworzy pasek statusu na dole ekranu.
     */
    private void createStatusBar() {
        Label sliderValue = createZoomSlider();
        Separator separator = new Separator(Orientation.VERTICAL);
        imageSize = new Label("");
        statusBar = new HBox(imageSize, separator, sliderValue, zoomSlider);
        statusBar.setMinHeight(25);
        statusBar.setMaxHeight(25);
        statusBar.setAlignment(Pos.CENTER_RIGHT);
    }

    /**
     * Tworzy slider obsługujący powiększanie i pomniejszanie obrazu.
     *
     * @return <tt>Label</tt> z wartością zoomu.
     */
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

    /**
     * Tworzy zawartość okna głównego z otwartym obrazem.
     */
    private void createScrollPane() {
        createSelection();
        imagePane = new Pane(imageView, selection);
        scrollPane = new ScrollPane(imagePane);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.prefWidthProperty().bind(mainStage.widthProperty());
        scrollPane.prefHeightProperty().bind(mainStage.heightProperty());
        scrollPane.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
        scrollPane.setOnDragOver(this::handleFileDrag);
        scrollPane.setOnDragDropped(this::handleFileDropped);
    }

    /**
     * Obsługuje przeniesienie pliku do okna programu.
     *
     * @param event zdarzenie przeniesienia pliku.
     */
    private void handleFileDrag(DragEvent event) {
        if (event.getGestureSource() != scrollPane &&
                event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }

        event.consume();
    }

    /**
     * Otwiera plik przeniesiony i upuszczony w oknie programu.
     *
     * @param event zdarzenie upuszczenia pliku.
     */
    private void handleFileDropped(DragEvent event) {
        boolean success = false;
        try {
            success = tryToOpenFile(event);
        } catch (Exception e) {
            ErrorHandler.handleError(e);
        }

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Obsługuje powiększanie i pomniejszanie obrazu za pomocą scrolla
     * myszki (przy wciśniętym klawiszu CTRL).
     *
     * @param event zdarzenie scrolla myszki.
     */
    private void handleZoom(ScrollEvent event) {
        if (!zoomSlider.isDisabled() && event.isControlDown()) {
            double delta = event.getDeltaY();
            event.consume();
            if (delta > 0) {
                zoomSlider.setValue(zoomSlider.getValue() + ZOOM_SPEED);
            } else {
                zoomSlider.setValue(zoomSlider.getValue() - ZOOM_SPEED);
            }
        }
    }

    /**
     * Metoda próbuje otworzyć plik przeniesiony myszką do okna programu.
     *
     * @param event zdarzenie upuszczenia pliku
     * @return <tt>true</tt> w razie sukcesu
     * @throws FileTypeNotSupported jeśli plik ma nieobsługiwany typ
     * @throws IOException          jeśli nastąpi błąd wczytywania pliku
     * @throws FileOpenException    jeśli nastąpi błąd otwierania pliku
     */
    private boolean tryToOpenFile(DragEvent event) throws FileTypeNotSupported, IOException, FileOpenException {
        Dragboard dragboard = event.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            File file = dragboard.getFiles()
                    .stream()
                    .filter(MainWindow.this::hasAcceptedExtension)
                    .findFirst()
                    .orElseThrow(FileTypeNotSupported::new);

            openImage(file);
            success = true;
            refreshWindow();
        }
        return success;
    }

    /**
     * Metoda sprawdzająca czy dany plik ma rozszerzenie akceptowanego typu.
     *
     * @param file plik do sprawdzenia
     * @return <tt>true</tt> jeśli plik jest akceptowany
     */
    private boolean hasAcceptedExtension(File file) {
        String extension = "*." + FilenameUtils.getExtension(file.getName());
        return ACCEPTED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Tworzy menu edycji.
     *
     * @return menu edycji
     */
    private Menu createEditMenu() {
        Menu imageMenu = new Menu("Edycja");
        imageMenu.getItems().add(createUndoItem());
        return imageMenu;
    }

    /**
     * Tworzy opcję cofnięcia zmiany.
     *
     * @return <tt>MenuItem</tt> Cofnij
     */
    private MenuItem createUndoItem() {
        undoItem = new MenuItem("Cofnij");
        undoItem.setDisable(true);
        undoItem.setOnAction(event -> openedFileData.undo());

        KeyCombination undoCombination = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        undoItem.setAccelerator(undoCombination);

        return undoItem;
    }

    /**
     * Tworzy menu obrazu.
     *
     * @return menu obrazu.
     */
    private Menu createImageMenu() {
        Menu imageMenu = new Menu("Obraz");
        imageMenu.getItems().add(createHistogramItem());
        return imageMenu;
    }

    /**
     * Tworzy opcję histogram w menu.
     *
     * @return <tt>MenuItem</tt> histogram.
     */
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

    /**
     * Tworzy menu plik.
     *
     * @return menu plik.
     */
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

    /**
     * Tworzy opcję "zapisz jako"
     *
     * @return <tt>MenuItem</tt> zapisz jako
     */
    private MenuItem createSaveFileAs() {
        MenuItem saveFileAs = new MenuItem("Zapisz jako");
        enabledWhenFileOpended.add(saveFileAs);

        FileChooser fileChooser = prepareSaveFileChooser();

        KeyCombination keyCodeCombination = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        saveFileAs.setAccelerator(keyCodeCombination);

        saveFileAs.setOnAction(event -> {
            handleSaveAsAction(fileChooser);
        });
        return saveFileAs;
    }

    /**
     * Sprawdza typ pliku wejściowego i jeśli to potrzebne dokonuje
     * konwersji obrazu, po czym zapisuje go ze wskazanym przez
     * użytkownika typem.
     *
     * @param fileChooser do wyboru nazwy i rozszerzenia pliku wyjściowego
     */
    private void handleSaveAsAction(FileChooser fileChooser) {
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

    /**
     * Tworzy okno zapisu pliku.
     *
     * @return okno zapisu pliku.
     */
    private FileChooser prepareSaveFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik jako...");
        FileChooser.ExtensionFilter jpg = new FileChooser.ExtensionFilter("Obraz JPG", "*.jpg");
        FileChooser.ExtensionFilter bmp = new FileChooser.ExtensionFilter("Obraz BMP", "*.bmp");
        FileChooser.ExtensionFilter png = new FileChooser.ExtensionFilter("Obraz PNG", "*.png");
        fileChooser.getExtensionFilters().addAll(jpg, bmp, png);

        return fileChooser;
    }

    /**
     * Sprawdza, czy plik wyjściowy jest zapisywany jako JPG lub BMP.
     *
     * @param extension rozszerzenie pliku
     * @return <tt>true</tt> jeśli plik jest zapisywany jako JPG lub BMP.
     */
    private boolean savingAsJpgOrBmp(String extension) {
        return "jpg".equals(extension.toLowerCase()) || "bmp".equals(extension.toLowerCase());
    }

    /**
     * Konwertuje obraz do RGB.
     *
     * @param bufferedImage obraz do konwersji.
     * @return obraz w formacie RGB.
     */
    private BufferedImage convertToRGB(BufferedImage bufferedImage) {
        BufferedImage rgbImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        rgbImage.createGraphics().drawImage(bufferedImage, 0, 0, null);

        return rgbImage;
    }

    /**
     * Zapisuje obraz do pliku.
     *
     * @param file          plik do zapisu
     * @param extension     rozszerzenie
     * @param bufferedImage obraz do zapisu
     */
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

    /**
     * Tworzy menu otwarcia pliku.
     *
     * @return menu otwarcia pliku.
     */
    private MenuItem createOpenFileItem() {
        FileChooser fileChooser = createOpenFileChooser();
        MenuItem openFile = new MenuItem("Otwórz");

        KeyCombination keyCodeCombination = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        openFile.setAccelerator(keyCodeCombination);

        openFile.setOnAction(event -> {
            handleFileOpenAction(fileChooser);
        });
        return openFile;
    }

    /**
     * Tworzy okno otwarcia pliku.
     *
     * @return okno otwarcia pliku.
     */
    private FileChooser createOpenFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz plik");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Obrazy", ACCEPTED_EXTENSIONS);
        fileChooser.getExtensionFilters().add(extensionFilter);
        return fileChooser;
    }

    /**
     * Obsługuje akcję otwarcia pliku.
     *
     * @param fileChooser okno wyboru pliku
     */
    private void handleFileOpenAction(FileChooser fileChooser) {
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
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        }

        refreshWindow();
    }

    /**
     * Otwiera wskazany plik, wyświetla go w głównym oknie programu
     * i zapamiętuje katalog.
     *
     * @param file plik do otwarcia.
     * @throws IOException       jeśli wystąpi problem z wczytaniem pliku
     * @throws FileOpenException jeśli wystąpi problem z otwarciem pliku
     */
    private void openImage(File file) throws IOException, FileOpenException {
        Image image;
        String extension = FilenameUtils.getExtension(file.getName());
        if ("tif".equals(extension.toLowerCase())) {
            image = openTif(file);
        } else {
            FileInputStream fileInputStream = new FileInputStream(file);
            image = new Image(fileInputStream);
        }

        imageView.setEffect(null);
        imageView.setPreserveRatio(true);
        imageView.fitHeightProperty().bind(zoomSlider.valueProperty().multiply(image.getHeight()));
        imageView.setImage(image);
        zoomSlider.setValue(calculateZoom(image));
        openedFileData = new FileData(file, imageView, undoItem);
        lastDirectory = file.getParentFile();
    }

    /**
     * Otwiera plik z rozszerzeniem tif.
     *
     * @param file plik do otwarcia
     * @return zwraca obiekt reprezentujący obraz.
     * @throws IOException       jeśli wystąpi problem z wczytaniem pliku
     * @throws FileOpenException jeśli wystąpi problem z otwarciem pliku
     */
    private Image openTif(File file) throws IOException, FileOpenException {
        Image image;
        SeekableStream stream = new FileSeekableStream(file);
        String[] names = ImageCodec.getDecoderNames(stream);
        ImageDecoder dec = ImageCodec.createImageDecoder(names[0], stream, null);
        if (dec == null) {
            throw new FileOpenException();
        }

        RenderedImage im = dec.decodeAsRenderedImage();
        if (im == null) {
            throw new FileOpenException();
        }

        BufferedImage bufferedImage = PlanarImage.wrapRenderedImage(im).getAsBufferedImage();
        if (bufferedImage == null) {
            throw new FileOpenException();
        }

        image = SwingFXUtils.toFXImage(bufferedImage, null);
        return image;
    }

    /**
     * Dokonuje obliczeń związanych z początkowym zoomem otwartego
     * obrazu - tak aby w całości mieścił się w oknie.
     *
     * @param image otwarty obraz
     * @return wartość powiększenia
     */
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

    /**
     * Tworzy opcję menu zamknięcia obrazu.
     *
     * @return <tt>MenuItem</tt> zamknięcia obrazu.
     */
    private MenuItem createCloseFileItem() {
        MenuItem closeFile = new MenuItem("Zamknij plik");
        enabledWhenFileOpended.add(closeFile);
        closeFile.setOnAction(event -> {
            openedFileData = null;
            imageView.setImage(null);
            undoItem.setDisable(true);
            refreshWindow();
        });
        return closeFile;
    }

    /**
     * Tworzy opcję menu zamknięcia aplikacji.
     *
     * @return <tt>MenuItem</tt> zamknięcia aplikacji.
     */
    private MenuItem createCloseAppItem() {
        MenuItem closeApp = new MenuItem("Zamknij program");
        closeApp.setOnAction(event -> Platform.exit());
        return closeApp;
    }

    /**
     * Odświeża zmiany i wczytuje ponownie, które wymagają ponownego
     * załadowania po otwarciu lub zamknięciu pliku.
     */
    private void refreshWindow() {
        refreshWindowTitle();
        enabledWhenFileOpended.forEach(menuItem -> menuItem.setDisable(openedFileData == null));
        refreshStatusBar();
        resetSelection();

        closeOpenedWindows();
    }

    /**
     * Odświeża pasek statusu.
     */
    private void refreshStatusBar() {
        zoomSlider.setDisable(openedFileData == null);
        refreshImageSize();
    }

    /**
     * Odświeża wartość zoomu na pasku statusu.
     */
    private void refreshImageSize() {
        if (openedFileData != null) {
            Image openedImage = openedFileData.getImageView().getImage();
            imageSize.setText((int) openedImage.getWidth() + "x" + (int) openedImage.getHeight());
        } else {
            imageSize.setText("");
        }
    }

    /**
     * Zamyka otwarte okna programu (oprocz głównego okna).
     */
    private void closeOpenedWindows() {
        openedWindows.forEach(Window::close);
        openedWindows.clear();
    }

    /**
     * Odświeża pasek tytułowy programu (jeśli otwarto plik, ładuje nazwę pliku).
     */
    private void refreshWindowTitle() {
        if (openedFileData != null) {
            mainStage.setTitle("PIKAsso - " + openedFileData.getFile().getName());
        } else {
            mainStage.setTitle("PIKAsso");
        }
    }

    /**
     * Buduje zaznaczenie fragmentu obrazu w odpowiedzi na akcję myszką.
     * W przypadku, gdy zaznaczenie wychodzi poza obręb obrazu,
     * dokonuje obliczeń ograniczających zaznaczenie.
     */
    private void createSelection() {
        startPoint = new Point();
        selection = new Rectangle(0, 0, 0, 0);
        Paint fill = Color.rgb(69, 69, 69, 0.2);
        selection.setFill(fill);

        Paint stroke = Color.WHITE;
        selection.setStroke(stroke);
        selection.getStrokeDashArray().add(10.0);
        imageView.setOnMousePressed(this::handleMousePressed);
        imageView.setOnMouseDragged(this::handleMouseDragged);
        imageView.setOnMouseReleased(this::handleMouseReleased);
    }

    /**
     * Obsługuje kliknięcie myszką na obraz (zapamiętuje współrzędne).
     *
     * @param event zdarzenie kliknięcia myszką.
     */
    private void handleMousePressed(MouseEvent event) {
        startPoint.setX(event.getX());
        startPoint.setY(event.getY());
        selection.setX(event.getX());
        selection.setY(event.getY());
        selection.setWidth(0);
        selection.setHeight(0);
    }

    /**
     * Obsługuje przeciągnięcie myszką nad obrazem (z wciśniętym przyciskiem).
     *
     * @param event zdarzenie przeciągnięcia myszką.
     */
    private void handleMouseDragged(MouseEvent event) {
        double xPoint = Math.min(event.getX(), startPoint.getX());
        double yPoint = Math.min(event.getY(), startPoint.getY());
        double height = Math.abs(event.getY() - startPoint.getY());
        double width = Math.abs(event.getX() - startPoint.getX());

        selection.setX(xPoint);
        selection.setY(yPoint);
        selection.setHeight(height);
        selection.setWidth(width);
    }

    /**
     * Obsługuje puszczenie klawisza myszki - zapisuje zaznaczony obszar
     * i jeśli to potrzebne, dokonuje obliczeń ograniczających ten obszar.
     *
     * @param event zdarzenie kliknięcia myszką.
     */
    private void handleMouseReleased(MouseEvent event) {
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
    }

    /**
     * Resetuje zaznaczenie.
     */
    private void resetSelection() {
        selection.setHeight(0);
        selection.setWidth(0);
    }
}
