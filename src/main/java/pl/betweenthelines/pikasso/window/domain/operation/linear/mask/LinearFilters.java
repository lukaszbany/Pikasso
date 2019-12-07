package pl.betweenthelines.pikasso.window.domain.operation.linear.mask;

public class LinearFilters {

    public static final Mask3x3 SMOOTH_1 = new Mask3x3("SMOOTH_1", false, 0, 1, 0, 1, 4, 1, 0, 1, 0);
    public static final Mask3x3 SMOOTH_2 = new Mask3x3("SMOOTH_2", false, 1, 2, 1, 2, 4, 2, 1, 2, 1);

    public static final Mask3x3 SHARPEN_1 = new Mask3x3("SHARPEN_1", false, 0, -1, 0, -1, 5, -1, 0, -1, 0);
    public static final Mask3x3 SHARPEN_2 = new Mask3x3("SHARPEN_2", false, 0, -1, 0, -1, 4, -1, 0, -1, 0);
    public static final Mask3x3 SHARPEN_3 = new Mask3x3("SHARPEN_3", false, -1, -1, -1, -1, 8, -1, -1, -1, -1);
    public static final Mask3x3 SHARPEN_4 = new Mask3x3("SHARPEN_4", false, -1, -1, -1, -1, 9, -1, -1, -1, -1);

    public static final Mask3x3 EDGE_DETECTION_1 = new Mask3x3("EDGE_DETECTION_1", false, 1, -2, 1, -2, 5, -2, 1, -2, 1);
    public static final Mask3x3 EDGE_DETECTION_2 = new Mask3x3("EDGE_DETECTION_2", false, -1, -1, -1, -1, 9, -1, -1, -1, -1);
    public static final Mask3x3 EDGE_DETECTION_3 = new Mask3x3("EDGE_DETECTION_3", false, 0, -1, 0, -1, 5, -1, 0, -1, 0);

    public static final Mask3x3 PREWITT_X = new Mask3x3("PREWITT_X", false, 1, 0, -1, 1, 0, -1, 1, 0, -1);
    public static final Mask3x3 PREWITT_Y = new Mask3x3("PREWITT_Y", false, 1, 1, 1, 0, 0, 0, -1, -1, -1);

}
