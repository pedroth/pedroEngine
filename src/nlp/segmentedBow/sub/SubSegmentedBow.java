package nlp.segmentedBow.sub;

import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.textSplitter.SubsSplitter;
import utils.FFMpegVideoApi;
import utils.Interval;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public abstract class SubSegmentedBow extends BaseSegmentedBow<LowBowSubtitles> implements Comparable<SubSegmentedBow> {
    protected double timeIntervalMinutes;

    public SubSegmentedBow(Interval<Integer> interval, LowBowSubtitles lowBowOrig) {
        super(interval, lowBowOrig);
        computeTimeInterval();
    }


    private void computeTimeInterval() {
        SubsSplitter textSplitter = lowBowOrig.getTextSplitter();
        LocalTime tmin = textSplitter.getSubtitleFromIndexWord(interval.getXmin() - 1).getInterval().getXmin();
        LocalTime tmax = textSplitter.getSubtitleFromIndexWord(interval.getXmax() - 1).getInterval().getXmax();
        this.timeIntervalMinutes = tmin.until(tmax, ChronoUnit.NANOS) / (60 * 1E9);
    }

    public void cutSegment(String outputAddress) {
        SubsSplitter textSplitter = lowBowOrig.getTextSplitter();
        SubsSplitter.Subtitle xmin = textSplitter.getSubtitleFromIndexWord(interval.getXmin() - 1);
        SubsSplitter.Subtitle xmax = textSplitter.getSubtitleFromIndexWord(interval.getXmax() - 1);
        FFMpegVideoApi.cutVideo(lowBowOrig.getVideoAddress(), xmin.getInterval().getXmin(), xmax.getInterval().getXmax(), outputAddress);
    }

    public String cutSegmentSubtitle() {
        SubsSplitter textSplitter = lowBowOrig.getTextSplitter();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = interval.getXmin() - 1; i < interval.getXmax(); i++) {
            stringBuilder.append(textSplitter.getSubtitleFromIndexWord(i).getOriginalText());
        }
        return stringBuilder.toString();
    }

    public String cutSegmentSubtitleWords() {
        String[] text = lowBowOrig.getText();
        StringBuilder stringBuilder = new StringBuilder(interval.getXmax() - (interval.getXmin() - 1));
        for (int i = interval.getXmin() - 1; i < interval.getXmax(); i++) {
            stringBuilder.append(text[i]).append((i == interval.getXmax() - 1) ? "" : " ");
        }
        return stringBuilder.toString();
    }

    public double getTimeIntervalMinutes() {
        return timeIntervalMinutes;
    }

    public String getVideoAddress() {
        return this.lowBowOrig.getVideoAddress();
    }

    @Override
    public int compareTo(SubSegmentedBow o) {
        int compareStr = lowBowOrig.getVideoAddress().compareTo(o.getLowBowOrig().getVideoAddress());
        if (compareStr == 0) {
            return interval.getXmin().compareTo(o.getInterval().getXmin());
        }
        return compareStr;
    }
}
