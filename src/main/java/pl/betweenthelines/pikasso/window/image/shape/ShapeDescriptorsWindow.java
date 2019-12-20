package pl.betweenthelines.pikasso.window.image.shape;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import pl.betweenthelines.pikasso.error.ErrorHandler;
import pl.betweenthelines.pikasso.exception.ImageIsNotBinaryException;
import pl.betweenthelines.pikasso.utils.ImageUtils;
import pl.betweenthelines.pikasso.window.HistogramWindow;
import pl.betweenthelines.pikasso.window.image.FileData;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

public class ShapeDescriptorsWindow {

    /**
     * Szerokość panelu deskryptorów kształtu.
     */
    private static final int DESCRIPTORS_WIDTH = 300;

    /**
     * Minimalna wysokość okna.
     */
    private static final int MINIMAL_HEIGHT = 550;

    /**
     * Indeks obiektu na liście konturów.
     */
    public static final int OBJECT_INDEX = 1;

    /**
     * Podgląd obrazu.
     */
    private ImageView imageView;

    /**
     * Obraz, którego dotyczą obliczenia
     */
    private Image image;

    /**
     * Elementy okna.
     */
    private Stage stage;
    private HBox hBox;
    private VBox shapeFeaturesVBox;
    private VBox momentDescriptorsVBox;
    private VBox centralMomentDescriptorsVBox;
    private VBox normalizedMomentDescriptorsVBox;
    private VBox momentInvariantsDescriptorsVBox;


    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Punkt środka ciężkości obiektu.
     */
    private Point center;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Pole powierzchni obiektu.
     */
    private double s;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Obwód obiektu.
     */
    private double l;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Minimalna odległość pomiędzy konturem a punktem środka ciężkości.
     */
    private double contourToCenterDistanceMin;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Maksymalna odległość pomiędzy konturem a punktem środka ciężkości.
     */
    private double contourToCenterDistanceMax;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Suma odległości pomiędzy wszystkimi punktami konturu obiektu
     * a punktem środka ciężkości.
     */
    private double sumOfContourToCenterDistances;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Suma kwadratów odległości pomiędzy wszystkimi punktami konturu
     * obiektu a punktem środka ciężkości.
     */
    private double sumOfContourToCenterDistancePowers;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Suma kwadratów odległości pomiędzy wszystkimi punktami konturu
     * obiektu a punktem środka ciężkości.
     */
    private double sumOfPointToCenterDistances;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Suma minimalnych odległości pomiędzy każdym punktem obiektu
     * a jego konturem.
     */
    private double minimumPointToContourDistancesSum;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Maksymalny gabaryt obiektu.
     */
    private double maxDimension;

    /**
     * Wartość pomocnicza przy wyliczaniu współczynników kształtu:
     * <p>
     * Liczba punktów konturu obiektu.
     */
    private double contourPointCount;

    /**
     * Konstruktor tworzący układ okna.
     *
     * @param openedFileData dane o otwartym pliku.
     */
    public ShapeDescriptorsWindow(FileData openedFileData) throws ImageIsNotBinaryException {
        image = ImageUtils.binarize(openedFileData.getImageView().getImage());
        createImageView();
        HBox imageViewHbox = new HBox(imageView);
        imageViewHbox.setAlignment(Pos.CENTER);

        shapeFeaturesVBox = new VBox();
        TitledPane shapeFeaturesPane = new TitledPane("Współczynniki ksztaltu", shapeFeaturesVBox);

        momentDescriptorsVBox = new VBox();
        TitledPane momentDescriptorsPane = new TitledPane("Momenty zwykłe", momentDescriptorsVBox);

        centralMomentDescriptorsVBox = new VBox();
        TitledPane centralMomentDescriptorsPane = new TitledPane("Momenty centralne", centralMomentDescriptorsVBox);

        normalizedMomentDescriptorsVBox = new VBox();
        TitledPane normalizedMomentDescriptorsPane = new TitledPane("Momenty znormalizowane", normalizedMomentDescriptorsVBox);

        momentInvariantsDescriptorsVBox = new VBox();
        TitledPane momentInvariantsDescriptorsPane = new TitledPane("Niezmienniki momentowe", momentInvariantsDescriptorsVBox);

        Accordion accordion = new Accordion(
                shapeFeaturesPane,
                momentDescriptorsPane,
                centralMomentDescriptorsPane,
                normalizedMomentDescriptorsPane,
                momentInvariantsDescriptorsPane
        );
        VBox descriptorsVBox = new VBox(accordion);
        descriptorsVBox.setMinHeight(MINIMAL_HEIGHT);
        descriptorsVBox.setPrefWidth(DESCRIPTORS_WIDTH);

        hBox = new HBox(imageViewHbox, new Separator(Orientation.VERTICAL), descriptorsVBox);

        Scene scene = createScene(imageViewHbox);

        calculateShapeDescriptors();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setScene(scene);
        stage.getIcons().add(new Image("PIKAsso-icon.jpg"));
        stage.setTitle("Detekcja krawędzi");
        stage.showAndWait();
    }

    /**
     * Tworzy podgląd obrazu przed operacją.
     */
    private void createImageView() {
        imageView = new ImageView((image));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setOnMousePressed(event -> {
            try {
                new HistogramWindow(imageView);
            } catch (Exception e) {
                ErrorHandler.handleError(e);
            }
        });
    }

    /**
     * Oblicza wielkość okna na podstawie wielkości obrazów i tworzy układ okna.
     *
     * @param imageViewHbox obszar z podglądem obrazu przed zmianami
     * @return <tt>Scene</tt> z układem okna
     */
    private Scene createScene(HBox imageViewHbox) {
        double windowWidth = imageViewHbox.getBoundsInLocal().getWidth() + DESCRIPTORS_WIDTH;
        double windowHeight = Math.max(imageViewHbox.getBoundsInLocal().getHeight(), MINIMAL_HEIGHT);

        Scene scene = new Scene(hBox, windowWidth, windowHeight);
        scene.setOnKeyPressed(event -> {
            if (KeyCode.ESCAPE.equals(event.getCode())) stage.close();
        });

        imageViewHbox.setPrefWidth(windowWidth - DESCRIPTORS_WIDTH);
        return scene;
    }

    /**
     * Oblicza współczynniki kształtu dla obiektu binarnego. Jeśli obraz nie jest
     * binarny, wszystkie piksele o poziomie jasności > 0 są traktowane jako poziom
     * nie zerowy.
     */
    private void calculateShapeDescriptors() throws ImageIsNotBinaryException {
        Mat mat = ImageUtils.imageToMat(image);
        ImageUtils.binarize(mat);
        image = ImageUtils.mat2Image(mat);
        imageView.setImage(image);

        Moments moments = Imgproc.moments(mat, true);
        fillMoments(moments);
        fillCentralMoments(moments);
        fillNormalizedMoments(moments);
        fillHuMoments(moments);

        calculateContoursAndHelperValues(mat, moments);
        fillShapeFeatures();
    }

    /**
     * Wyświetla na ekranie wartości momentów zwykłych.
     *
     * @param moments wartości momentów
     */
    private void fillMoments(Moments moments) {
        WebView m = new WebView();
        m.getEngine().loadContent("m<sub>00</sub> = " + moments.m00 + "<br/>" +
                "m<sub>10</sub> = " + moments.m10 + "<br/>" +
                "m<sub>01</sub> = " + moments.m01 + "<br/>" +
                "m<sub>20</sub> = " + moments.m20 + "<br/>" +
                "m<sub>11</sub> = " + moments.m11 + "<br/>" +
                "m<sub>02</sub> = " + moments.m02 + "<br/>" +
                "m<sub>30</sub> = " + moments.m30 + "<br/>" +
                "m<sub>21</sub> = " + moments.m21 + "<br/>" +
                "m<sub>12</sub> = " + moments.m12 + "<br/>" +
                "m<sub>03</sub> = " + moments.m03
        );
        momentDescriptorsVBox.getChildren().add(m);
    }

    /**
     * Wyświetla na ekranie wartości momentów centralnych.
     *
     * @param moments wartości momentów
     */
    private void fillCentralMoments(Moments moments) {
        WebView mu = new WebView();
        mu.getEngine().loadContent("M<sub>20</sub> = " + moments.mu20 + "<br/>" +
                "M<sub>11</sub> = " + moments.mu11 + "<br/>" +
                "M<sub>02</sub> = " + moments.mu02 + "<br/>" +
                "M<sub>30</sub> = " + moments.mu30 + "<br/>" +
                "M<sub>21</sub> = " + moments.mu21 + "<br/>" +
                "M<sub>12</sub> = " + moments.mu12 + "<br/>" +
                "M<sub>03</sub> = " + moments.mu03
        );
        centralMomentDescriptorsVBox.getChildren().add(mu);
    }

    /**
     * Wyświetla na ekranie wartości momentów znormalizowanych.
     *
     * @param moments wartości momentów
     */
    private void fillNormalizedMoments(Moments moments) {
        WebView n = new WebView();
        n.getEngine().loadContent("N<sub>20</sub> = " + moments.nu20 + "<br/>" +
                "N<sub>11</sub> = " + moments.nu11 + "<br/>" +
                "N<sub>02</sub> = " + moments.nu02 + "<br/>" +
                "N<sub>30</sub> = " + moments.nu30 + "<br/>" +
                "N<sub>21</sub> = " + moments.nu21 + "<br/>" +
                "N<sub>12</sub> = " + moments.nu12 + "<br/>" +
                "N<sub>03</sub> = " + moments.nu03
        );
        normalizedMomentDescriptorsVBox.getChildren().add(n);
    }

    /**
     * Wyświetla na ekranie wartości niezmienników momentowych
     *
     * @param moments wartości momentów
     */
    private void fillHuMoments(Moments moments) {
        Mat hu = new Mat();
        Imgproc.HuMoments(moments, hu);

        WebView m = new WebView();
        m.getEngine().loadContent("M1 = " + hu.get(0, 0)[0] + "<br/>" +
                "M2 = " + hu.get(1, 0)[0] + "<br/>" +
                "M3 = " + hu.get(2, 0)[0] + "<br/>" +
                "M4 = " + hu.get(3, 0)[0] + "<br/>" +
                "M5 = " + hu.get(4, 0)[0] + "<br/>" +
                "M6 = " + hu.get(5, 0)[0] + "<br/>" +
                "M7 = " + hu.get(6, 0)[0]);

        momentInvariantsDescriptorsVBox.getChildren().add(m);
    }

    /**
     * Znajduje punkt środka ciężkości obiektu, wyznacza linię konturu,
     * na jej podstawie oblicza obwód i pole obiektu.
     * Inicjalizuje i oblicza wartości zmiennych pomocniczych potrzebnych
     * do obliczeń współczynników kształtu.
     *
     * @param mat     obraz
     * @param moments wartości momentów
     */
    private void calculateContoursAndHelperValues(Mat mat, Moments moments) throws ImageIsNotBinaryException {
        double cx = moments.m10 / moments.m00;
        double cy = moments.m01 / moments.m00;
        center = new Point(cx, cy);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        if (contours.size() < 2) {
            throw new ImageIsNotBinaryException();
        }

        MatOfPoint objectContour = contours.get(OBJECT_INDEX);
        MatOfPoint2f objectContour2f = new MatOfPoint2f(objectContour.toArray());
        s = Imgproc.contourArea(objectContour);
        l = Imgproc.arcLength(objectContour2f, true);

        initializeHelperValues(objectContour);
        calculateHelperValues(mat, objectContour, objectContour2f);
    }

    /**
     * Inicjalizuje wartości zmiennych pomocniczych potrzebnych
     * do obliczeń współczynników kształtu.
     *
     * @param objectContour kontur obiektu
     */
    private void initializeHelperValues(MatOfPoint objectContour) {
        contourToCenterDistanceMin = l;
        contourToCenterDistanceMax = 0;
        sumOfContourToCenterDistances = 0;
        sumOfContourToCenterDistancePowers = 0;
        sumOfPointToCenterDistances = 0;
        minimumPointToContourDistancesSum = 0;
        maxDimension = 0;
        contourPointCount = objectContour.toList().size();
    }

    /**
     * Oblicza wartości zmiennych pomocniczych potrzebnych do obliczeń współczynników
     * kształtu.
     * <p>
     * Przechodzi przez wszystkie punkty obrazu i jeśli znajduje się w obiekcie, oblicza
     * jego odległość od środka ciężkości obiektu oraz najmniejszą odległość od konturu.
     * <p>
     * Następnie wylicza odległości od punktu środka ciężkości obiektu dla każdego punktu
     * konturu. Wyliczone w ten sposób wartości wykorzystuje do uzupełnienia wartości
     * pomocniczych.
     *
     * @param mat             obraz
     * @param objectContour   kontur
     * @param objectContour2f kontur w formacie 2f
     */
    private void calculateHelperValues(Mat mat, MatOfPoint objectContour, MatOfPoint2f objectContour2f) {
        for (int col = 0; col < mat.cols(); col++) {
            for (int row = 0; row < mat.rows(); row++) {
                Point point = new Point(row, col);
                double result = Imgproc.pointPolygonTest(objectContour2f, point, false);
                if (result >= 0) {
                    double distance = getDistance(center, point);
                    sumOfPointToCenterDistances += distance;

                    double minimumDistanceToContour = l;
                    for (Point countourPoint : objectContour.toList()) {
                        double contourDistance = getDistance(point, countourPoint);
                        if (contourDistance < minimumDistanceToContour) {
                            minimumDistanceToContour = contourDistance;
                        }
                    }
                    minimumPointToContourDistancesSum += minimumDistanceToContour;
                }
            }
        }

        for (Point point : objectContour.toList()) {
            double distance = getDistance(center, point);

            if (distance > contourToCenterDistanceMax) {
                contourToCenterDistanceMax = distance;
            }
            if (distance < contourToCenterDistanceMin) {
                contourToCenterDistanceMax = distance;
            }

            sumOfContourToCenterDistances += distance;
            sumOfContourToCenterDistancePowers += distance * distance;

            for (Point point2 : objectContour.toList()) {
                double distanceFromPoint2 = getDistance(point, point2);

                if (distanceFromPoint2 > maxDimension) {
                    maxDimension = distanceFromPoint2;
                }
            }
        }
    }

    /**
     * Oblicza odległość między podanymi punktami.
     *
     * @param pointA punkt A
     * @param pointB punkt B
     * @return odległość między podanymi punktami
     */
    private double getDistance(Point pointA, Point pointB) {
        double xDifference = pointA.x - pointB.x;
        double yDifference = pointA.y - pointB.y;
        return sqrt(xDifference * xDifference + yDifference * yDifference);
    }

    /**
     * Wyświetla wartości współczynników kształtu na ekranie.
     */
    private void fillShapeFeatures() {
        double w1 = 2 * sqrt(s / PI);
        double w2 = l / PI;
        double w3 = (l / (2 * sqrt(PI * s))) - 1;
        double w4 = s / (sqrt(2 * PI * sumOfPointToCenterDistances));
        double w5 = (s * s * s) / (minimumPointToContourDistancesSum * minimumPointToContourDistancesSum);
        double w6 = sqrt((sumOfContourToCenterDistances * sumOfContourToCenterDistances)
                / (contourPointCount * sumOfContourToCenterDistancePowers - 1));
        double w7 = contourToCenterDistanceMin / contourToCenterDistanceMax;
        double w8 = maxDimension / l;
        double w9 = (2 * sqrt(PI * s)) / l;

        WebView w = new WebView();
        w.getEngine().loadContent("W1 = " + w1 + "<br/>" +
                "W2 = " + w2 + "<br/>" +
                "W3 = " + w3 + "<br/>" +
                "W4 = " + w4 + "<br/>" +
                "W5 = " + w5 + "<br/>" +
                "W6 = " + w6 + "<br/>" +
                "W7 = " + w7 + "<br/>" +
                "W8 = " + w8 + "<br/>" +
                "W9 = " + w9
        );
        shapeFeaturesVBox.getChildren().add(w);
    }
}
