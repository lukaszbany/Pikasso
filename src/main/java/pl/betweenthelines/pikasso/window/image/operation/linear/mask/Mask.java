package pl.betweenthelines.pikasso.window.image.operation.linear.mask;

import org.opencv.core.Mat;

public interface Mask {

    int getKernelSize();
    Mat getMat();

}
