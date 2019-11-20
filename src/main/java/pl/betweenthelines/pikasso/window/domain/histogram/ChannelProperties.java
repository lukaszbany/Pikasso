package pl.betweenthelines.pikasso.window.domain.histogram;

import javafx.scene.chart.XYChart;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

@Getter
public class ChannelProperties {

    private Channel channel;
    private long[] pixels;
    private int pixelsTotal;
    private int minLevel;
    private int maxLevel;

    private double median;
    private double mean;
    private double standardDeviation;
    private XYChart.Series series;

    public ChannelProperties(Channel channel, long[] pixels, int pixelsTotal, int minLevel, int maxLevel) {
        this.channel = channel;
        this.pixelsTotal = pixelsTotal;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;

        setData(pixels);
    }

    public long getPixelCountOnLevel(int brightnessLevel) {
        return pixels[brightnessLevel];
    }

    private void setData(long pixels[]) {
        this.pixels = pixels;
        saveSeries();
        calculateParameters();
    }

    private void saveSeries() {
        series = new XYChart.Series();
        series.setName(channel.getName());

        for (int i = minLevel; i <= maxLevel; i++) {
            series.getData().add(new XYChart.Data(String.valueOf(i), pixels[i]));
        }
    }

    public void calculateParameters() {
        int pixelsInRange = 0;
        for (int i = minLevel; i <= maxLevel; i++) {
            pixelsInRange += pixels[i];
        }

        double[] dpixels = new double[pixelsInRange];
        int count = 0;
        for (int i = minLevel; i <= maxLevel; i++) {
            for (int j = 0; j < pixels[i]; j++) {
                dpixels[count] = i;
                count++;
            }
        }

        int middle = pixelsInRange / 2;
        if (pixelsInRange < 2) {
            this.median = 0;
            this.mean = 0;
            this.standardDeviation = 0;
            return;
        } else if (pixelsInRange % 2 == 0) {
            this.median = (dpixels[middle] + dpixels[middle + 1]) / 2;
        } else {
            this.median = dpixels[middle];
        }

        Mean mean = new Mean();
        this.mean = mean.evaluate(dpixels);

        StandardDeviation standardDeviation = new StandardDeviation();
        this.standardDeviation = standardDeviation.evaluate(dpixels);
    }

    @Getter
    @AllArgsConstructor
    public enum Channel {
        RED("kanał czerwony", "red-unselected"),
        GREEN("kanał zielony", "green-unselected"),
        BLUE("kanał niebieski", "blue-unselected"),
        GRAY("poziom szarości", "gray-unselected");

        private String name;
        private String uncheckedClass;
    }
}
