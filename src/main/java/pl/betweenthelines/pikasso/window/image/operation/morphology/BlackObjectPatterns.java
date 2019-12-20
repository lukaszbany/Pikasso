package pl.betweenthelines.pikasso.window.image.operation.morphology;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Wzorce czarnego obiektu na biały tle
 */
class BlackObjectPatterns {

    /**
     * Poziom jasności obiektu
     */
    private static final double OBJ = 0;

    /**
     * Umowny poziom jasności reprezentujący piksel,
     * którego nie stosujemy do wzorca.
     */
    private static final double ANY = 150;

    /**
     * Poziom jasności tła
     */
    private static final double BCK = 255;

    @Getter
    private static final List<double[]> PATTERNS = new ArrayList<>();

    /**
     * Utworzenie wzorców
     */
    static {
        PATTERNS.add(new double[]{
                BCK, BCK, BCK,
                ANY, OBJ, ANY,
                OBJ, OBJ, OBJ
        });

        PATTERNS.add(new double[]{
                OBJ, ANY, BCK,
                OBJ, OBJ, BCK,
                OBJ, ANY, BCK
        });

        PATTERNS.add(new double[]{
                OBJ, OBJ, OBJ,
                ANY, OBJ, ANY,
                BCK, BCK, BCK
        });

        PATTERNS.add(new double[]{
                BCK, ANY, OBJ,
                BCK, OBJ, OBJ,
                BCK, ANY, OBJ
        });

        PATTERNS.add(new double[]{
                ANY, BCK, BCK,
                OBJ, OBJ, BCK,
                ANY, OBJ, ANY
        });

        PATTERNS.add(new double[]{
                ANY, OBJ, ANY,
                OBJ, OBJ, BCK,
                ANY, BCK, BCK
        });

        PATTERNS.add(new double[]{
                ANY, OBJ, ANY,
                BCK, OBJ, OBJ,
                BCK, BCK, ANY
        });

        PATTERNS.add(new double[]{
                BCK, BCK, ANY,
                BCK, OBJ, OBJ,
                ANY, OBJ, ANY
        });
    }
}
