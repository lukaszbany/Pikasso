package pl.betweenthelines.pikasso.window.image;

import lombok.Data;

/**
 * Prechowuje współrzędne zaznaczonego fragmentu obrazu.
 */
@Data
public class SelectionData {
    private double startX = 0;
    private double startY = 0;
    private double endX = 0;
    private double endY = 0;
}
