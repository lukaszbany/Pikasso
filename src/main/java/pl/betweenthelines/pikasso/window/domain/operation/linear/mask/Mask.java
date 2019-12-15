package pl.betweenthelines.pikasso.window.domain.operation.linear.mask;

import org.opencv.core.Mat;

public interface Mask {

    int getKernelSize();
    Mat getMat();

}
