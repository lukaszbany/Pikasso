package pl.betweenthelines.pikasso.window.image.operation.linear;

import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.List;

/**
 * Klasa służąca do skalowania obrazu wyjściowego po operacji.
 */
public class MatScalingUtils {

    /**
     * Metody skalowania.
     */
    public static final byte DO_NOTHING = 0;
    public static final byte METHOD_1 = 1;
    public static final byte METHOD_2 = 2;
    public static final byte METHOD_3 = 3;

    /**
     * Maksymalny poziom jasności.
     */
    private static final double MAX_LEVEL = 255;

    /**
     * Srodkowy poziom jasności
     */
    private static final double HALF_LEVEL = 127;

    /**
     * Minimalny poziom jasnosci
     */
    private static final double MIN_LEVEL = 0;

    /**
     * Lista dostępny metod skalowania.
     */
    private static final List<Byte> availableMethods = Arrays.asList(DO_NOTHING, METHOD_1, METHOD_2, METHOD_3);

    /**
     * Skaluje podany obraz na jeden ze sposobów:
     * <ul>
     *     <li>DO_NOTHING - nie robi nic z obrazem</li>
     *     <li>METHOD_1 - metoda równomierna</li>
     *     <li>METHOD_2 - metoda trójwartościowa</li>
     *     <li>METHOD_3 - metoda obcinająca</li>
     * </ul>
     *
     * @param mat    obraz wejściowy
     * @param method metoda skalowania
     */
    public static void scale(Mat mat, byte method) {
        if (method == DO_NOTHING) {
            return;
        }
        if (!availableMethods.contains(method)) {
            throw new IllegalArgumentException();
        }

        int channels = mat.channels();
        double[] max = createAndFillArray(channels, MIN_LEVEL);
        double[] min = createAndFillArray(channels, MAX_LEVEL);

        if (method == METHOD_1) {
            findMaxAndMinForEveryChannel(mat, max, min);
        }

        calculateNewLevelsForImage(mat, method, max, min);
    }

    /**
     * Tworzy tablicę wielkości liczby kanałów i wypełnia ją podaną wartością.
     *
     * @param channels         liczba kanałów (rozmiar tablicy)
     * @param valueToFillArray wartość do wypełnienia tablicy
     * @return wypełnioną tablicę
     */
    private static double[] createAndFillArray(int channels, double valueToFillArray) {
        double[] max = new double[channels];
        Arrays.fill(max, valueToFillArray);

        return max;
    }

    /**
     * Znajduje wartości minimalne i maksymalne dla każdego kanału i zapisuje
     * w podanych tablicach.
     *
     * @param mat obraz wejściowy
     * @param max tablica wartości maksymalnych
     * @param min tablica wartości minimalnych
     */
    private static void findMaxAndMinForEveryChannel(Mat mat, double[] max, double[] min) {
        for (int y = 0; y < mat.height(); y++) {
            for (int x = 0; x < mat.width(); x++) {
                checkAndSaveIfMaxOrMinForEveryChannel(mat, max, min, y, x);
            }
        }
    }

    /**
     * Sprawdza wartości poziomów jasności piksela o podanych współrzędnych i zapisuje
     * jako element maksymalny, jeśli jest większy od obecnego maksimum lub element
     * minimalny, jeśli mniejszy od obecnego minimum.
     *
     * @param mat obraz wejściowy
     * @param max tablica elementów maksymalnych dla każdego kanału
     * @param min tablica elementów minimalnych dla każdego kanału
     * @param y   współrzędna y
     * @param x   współrzędna x
     */
    private static void checkAndSaveIfMaxOrMinForEveryChannel(Mat mat, double[] max, double[] min, int y, int x) {
        double[] levels = mat.get(y, x);

        for (int ch = 0; ch < mat.channels(); ch++) {
            checkAndSaveForOneChannel(max, min, levels[ch], ch);
        }
    }

    /**
     * Sprawdza czy podana wartość jest minimalną lub maksymalną dla podanego kanału.
     *
     * @param max          tablica elementów maksymalnych dla każdego kanału
     * @param min          tablica elementów minimalnych dla każdego kanału
     * @param currentLevel poziom jasności obecnego piksela
     * @param ch           indeks kanału
     */
    private static void checkAndSaveForOneChannel(double[] max, double[] min, double currentLevel, int ch) {
        double level = currentLevel;

        if (level > max[ch]) max[ch] = level;
        if (level < min[ch]) min[ch] = level;
    }

    /**
     * Oblicza nowy poziom dla każdego piksela obrazu.
     *
     * @param mat    obraz wejściowy
     * @param method metoda skalowania
     * @param max    tablica elementów maksymalnych dla każdego kanału
     * @param min    tablica elementów minimalnych dla każdego kanału
     */
    private static void calculateNewLevelsForImage(Mat mat, byte method, double[] max, double[] min) {
        for (int y = 0; y < mat.height(); y++) {
            for (int x = 0; x < mat.width(); x++) {
                calculateNewLevelForEveryChannel(mat, method, max, min, y, x);
            }
        }
    }

    /**
     * Oblicza nowy poziom dla piksela o podanych współrzędnych (każdego kanału).
     *
     * @param mat    obraz wejściowy
     * @param method metoda skalowania
     * @param max    tablica elementów maksymalnych dla każdego kanału
     * @param min    tablica elementów minimalnych dla każdego kanału
     * @param y      współrzędna y
     * @param x      współrzędna x
     */
    private static void calculateNewLevelForEveryChannel(Mat mat, byte method, double[] max, double[] min, int y, int x) {
        double[] levels = mat.get(y, x);

        for (int ch = 0; ch < mat.channels(); ch++) {
            double level = levels[ch];

            double newLevel = calculateLevel(level, min[ch], max[ch], method);
            levels[ch] = newLevel;
        }

        mat.put(y, x, levels);
    }

    /**
     * Oblicza nową wartość na podstawie podanego poziomu jasności przy pomocy
     * podanej metody skalowania.
     *
     * @param oldLevel obecny poziom jasności
     * @param min      minimalny poziom jasności dla tego kanału
     * @param max      maksymalny poziom jasności dla tego kanału
     * @param method   metoda skalowania
     * @return obliczony nowy poziom jasności
     */
    private static double calculateLevel(double oldLevel, double min, double max, byte method) {
        switch (method) {
            case METHOD_1:
                return calculateByMethod1(oldLevel, min, max);
            case METHOD_2:
                return calculateByMethod2(oldLevel);
            case METHOD_3:
                return calculateByMethod3(oldLevel);
        }

        throw new IllegalArgumentException();
    }

    /**
     * Skaluje podany piksel metodą równomierną.
     *
     * @param oldLevel obecny poziom jasności
     * @param min      minimalny poziom jasności dla tego kanału
     * @param max      maksymalny poziom jasności dla tego kanału
     * @return wartość przeskalowana metodą równomierną
     */
    private static double calculateByMethod1(double oldLevel, double min, double max) {
        return (oldLevel - min) / (max - min) * MAX_LEVEL;
    }

    /**
     * Skaluje podany piksel metodą trójwartościowa.
     *
     * @param oldLevel obecny poziom jasności
     * @return wartość przeskalowana metodą trójwartościowa
     */
    private static double calculateByMethod2(double oldLevel) {
        if (oldLevel < 0) return 0;
        if (oldLevel == 0) return HALF_LEVEL;
        return MAX_LEVEL;
    }

    /**
     * Skaluje podany piksel metodą obcinająca.
     *
     * @param oldLevel obecny poziom jasności
     * @return wartość przeskalowana metodą obcinająca
     */
    private static double calculateByMethod3(double oldLevel) {
        if (oldLevel < 0) return 0;
        if (oldLevel <= MAX_LEVEL) return oldLevel;
        return MAX_LEVEL;
    }
}
