package pl.betweenthelines.pikasso.window.domain;

import lombok.Data;

@Data
public class SelectionData {
    private double startX = 0;
    private double startY = 0;
    private double endX = 0;
    private double endY = 0;
}
