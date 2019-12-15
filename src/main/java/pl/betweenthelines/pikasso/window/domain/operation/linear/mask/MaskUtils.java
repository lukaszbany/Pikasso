package pl.betweenthelines.pikasso.window.domain.operation.linear.mask;

public class MaskUtils {

    private static final int HELPER_MASK_SIZE = 7;
    private static final int CENTER = (HELPER_MASK_SIZE - 1) / 2;

    public static Mask5x5 combineMasks(Mask3x3 mask1, Mask3x3 mask2) {
        double[] values1 = mask1.getValues();
        double[] values2 = mask2.getValues();
        double[] result5x5 = convertTo7x7AndCombineValues(values1, values2);

        return new Mask5x5("COMBINED_MASK", result5x5);
    }

    private static double[] convertTo7x7AndCombineValues(double[] values1, double[] values2) {
        double[] values7x7 = fill7x7ArrayWithValues2(values2);

        return combineValues(values1, values7x7);
    }

    private static double[] fill7x7ArrayWithValues2(double[] values2) {
        double[] values7x7 = new double[HELPER_MASK_SIZE * HELPER_MASK_SIZE];
        int offset = CENTER - 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int posIn7x7 = (row + offset) * HELPER_MASK_SIZE + col + offset;
                int posIn3x3 = row * 3 + col;
                values7x7[posIn7x7] = values2[posIn3x3];
            }
        }
        return values7x7;
    }

    private static double[] combineValues(double[] values1, double[] values7x7) {
        double[] result5x5 = new double[25];
        int posIn5x5 = 0;
        for (int row = 1; row < HELPER_MASK_SIZE - 1; row++) {
            for (int col = 1; col < HELPER_MASK_SIZE - 1; col++, posIn5x5++) {
                int pos0 = (row - 1) * HELPER_MASK_SIZE + col - 1;
                int pos1 = (row - 1) * HELPER_MASK_SIZE + col;
                int pos2 = (row - 1) * HELPER_MASK_SIZE + col + 1;
                int pos3 = row * HELPER_MASK_SIZE + col - 1;
                int pos4 = row * HELPER_MASK_SIZE + col;
                int pos5 = row * HELPER_MASK_SIZE + col + 1;
                int pos6 = (row + 1) * HELPER_MASK_SIZE + col - 1;
                int pos7 = (row + 1) * HELPER_MASK_SIZE + col;
                int pos8 = (row + 1) * HELPER_MASK_SIZE + col + 1;
                double newValue =
                        values1[0] * values7x7[pos0] + values1[1] * values7x7[pos1] + values1[2] * values7x7[pos2] +
                                values1[3] * values7x7[pos3] + values1[4] * values7x7[pos4] + values1[5] * values7x7[pos5] +
                                values1[6] * values7x7[pos6] + values1[7] * values7x7[pos7] + values1[8] * values7x7[pos8];

                result5x5[posIn5x5] = newValue;
            }
        }

        return result5x5;
    }
}
