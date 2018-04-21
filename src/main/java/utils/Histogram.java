package utils;

import numeric.MyMath;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Histogram.
 */
public class Histogram {
    private List<Integer> histogram;
    private List<Interval<Double>> histogramIntervals;
    private int bins;
    private double xmin, xmax;

    /**
     * Instantiates a new Histogram.
     *
     * @param data the data
     * @param bins the bins
     */
    public Histogram(List<Double> data, int bins) {
        this.bins = bins;
        computeMinMax(data);
        computeHist(data);
    }

    /**
     * Instantiates a new Histogram.
     *
     * @param data the data
     * @param bins the bins
     * @param xmin the xmin
     * @param xmax the xmax
     */
    public Histogram(List<Double> data, int bins, double xmin, double xmax) {
        this.xmax = xmax;
        this.xmin = xmin;
        this.bins = bins;
        computeHist(data);
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        int samples = 1000000;
        List<Double> data1 = new ArrayList<>();
        for (int i = 0; i < samples; i++) {
            data1.add(Math.random());
        }
        Histogram histogram = new Histogram(data1, 15);
        List<Interval<Double>> intervals = histogram.getHistogramIntervals();
        List<Integer> hist = histogram.getHistogram();
        for (int i = 0; i < hist.size(); i++) {
            System.out.println(intervals.get(i) + " : " + (1.0 * hist.get(i) / samples));
        }
        System.out.println();
        Histogram histogram1 = new Histogram(data1, 15, 0, 1);
        List<Integer> hist2 = histogram1.getHistogram();
        List<Interval<Double>> histogramIntervals = histogram1.getHistogramIntervals();
        for (int i = 0; i < hist2.size(); i++) {
            System.out.println(histogramIntervals.get(i) + " : " + (1.0 * hist2.get(i) / samples));
        }
    }

    private void computeMinMax(List<Double> data) {
        for (Double x : data) {
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
        }
    }

    private void computeHist(List<Double> data) {
        histogram = new ArrayList<>();
        histogramIntervals = new ArrayList<>();
        double h = (xmax - xmin) / bins;
        double k = xmin;
        for (int i = 0; i < bins; i++) {
            histogram.add(0);
            histogramIntervals.add(new Interval<>(k, k + h));
            k += h;
        }

        for (Double x : data) {
            double z = (x - xmin) / h;
            //because float point errors
            z = MyMath.clamp(z, 0, bins);
            int index = -1;
            for (int i = 0; i < bins; i++) {
                if (i == bins - 1 ? (i <= z && z <= i + 1) : i <= z && z < i + 1) {
                    index = i;
                    break;
                }
            }
            histogram.set(index, histogram.get(index) + 1);
        }
    }

    /**
     * Gets bins.
     *
     * @return the bins
     */
    public int getBins() {
        return bins;
    }

    /**
     * Gets xmin.
     *
     * @return the xmin
     */
    public double getXmin() {
        return xmin;
    }

    /**
     * Gets xmax.
     *
     * @return the xmax
     */
    public double getXmax() {
        return xmax;
    }

    /**
     * Gets histogram.
     *
     * @return the histogram
     */
    public List<Integer> getHistogram() {
        return histogram;
    }

    /**
     * Gets histogram intervals. These intervals are open on the right, except for the last interval which is closed.
     *
     * @return the histogram intervals
     */
    public List<Interval<Double>> getHistogramIntervals() {
        return histogramIntervals;
    }
}
