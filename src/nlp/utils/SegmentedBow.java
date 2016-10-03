package nlp.utils;


import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import nlp.textSplitter.SubsSplitter;
import utils.FFMpegVideoApi;
import utils.Interval;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class SegmentedBow implements Comparable<SegmentedBow> {
    private Interval<Integer> interval;
    private LowBowSubtitles lowBowSubtitles;
    private Vector segmentBow;
    private double timeIntervalMinutes;

    public SegmentedBow(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles) {
        this.interval = interval;
        this.lowBowSubtitles = lowBowSubtitles;
        computeTimeInterval();
        buildSegmentBow();
    }

    private void computeTimeInterval() {
        SubsSplitter textSplitter = lowBowSubtitles.getTextSplitter();
        LocalTime tmin = textSplitter.getSubtitleFromIndexWord(interval.getXmin() - 1).getInterval().getXmin();
        LocalTime tmax = textSplitter.getSubtitleFromIndexWord(interval.getXmax() - 1).getInterval().getXmax();
        this.timeIntervalMinutes = tmin.until(tmax, ChronoUnit.NANOS) / (60 * 1E9);
    }

    private void buildSegmentBow() {
        Matrix rawCurveSeg = lowBowSubtitles.getRawCurveFromHeatRepresentation(interval.getXmin(), interval.getXmax());
        Vector[] lowCurve = rawCurveSeg.getRowsVectors();
        segmentBow = new Vector(lowCurve[0].getDim());
        for (int i = 0; i < lowCurve.length; i++) {
            segmentBow = Vector.add(segmentBow, lowCurve[i]);
        }
        segmentBow = Vector.scalarProd(1.0 / lowCurve.length, segmentBow);
    }

    public Interval<Integer> getInterval() {
        return interval;
    }

    public LowBowSubtitles getLowBowSubtitles() {
        return lowBowSubtitles;
    }

    public void cutSegment(String outputAddress) {
        SubsSplitter textSplitter = lowBowSubtitles.getTextSplitter();
        SubsSplitter.Subtitle xmin = textSplitter.getSubtitleFromIndexWord(interval.getXmin() - 1);
        SubsSplitter.Subtitle xmax = textSplitter.getSubtitleFromIndexWord(interval.getXmax() - 1);
        FFMpegVideoApi.cutVideo(lowBowSubtitles.getVideoAddress(), xmin.getInterval().getXmin(), xmax.getInterval().getXmax(), outputAddress);
    }

    public String cutSegmentSubtile() {
        SubsSplitter textSplitter = lowBowSubtitles.getTextSplitter();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = interval.getXmin() - 1; i < interval.getXmax(); i++) {
            stringBuilder.append(textSplitter.getSubtitleFromIndexWord(i).getOriginalText());
        }
        return stringBuilder.toString();
    }

    @Override
    public int compareTo(SegmentedBow o) {
        int compareStr = lowBowSubtitles.getVideoAddress().compareTo(o.getLowBowSubtitles().getVideoAddress());
        if (compareStr == 0) {
            return interval.getXmin().compareTo(o.getInterval().getXmin());
        }
        return compareStr;
    }

    public Vector getSegmentBow() {
        return segmentBow;
    }

    public double getTimeIntervalMinutes() {
        return timeIntervalMinutes;
    }

    public String getVideoAddress() {
        return this.lowBowSubtitles.getVideoAddress();
    }
}
