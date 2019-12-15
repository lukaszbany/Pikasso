package pl.betweenthelines.pikasso.window.domain.operation.linear.mask;

import nu.pattern.OpenCV;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MaskUtilsTest {

    private static final double[] MASK_OF_ONES = {1, 1, 1, 1, 1, 1, 1, 1, 1};
    private static final double[] EXPECTED_VALUES = {
            1, 2, 3, 2, 1,
            2, 4, 6, 4, 2,
            3, 6, 9, 6, 3,
            2, 4, 6, 5, 2,
            1, 2, 3, 2, 1
    };

    @Test
    public void shouldCombineMasks() {
        OpenCV.loadLocally();
        Mask3x3 mask1 = new Mask3x3("mask", false, MASK_OF_ONES);
        Mask3x3 mask2 = new Mask3x3("mask", false, MASK_OF_ONES);

        Mask5x5 result = MaskUtils.combineMasks(mask1, mask2);

        Assert.assertEquals(81, result.getKernelSize());
        Assert.assertArrayEquals(EXPECTED_VALUES, result.getValues(), 2);
    }
}