package nlp.utils;


import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import nlp.textSplitter.SubsSplitter;
import utils.FFMpegVideoApi;
import utils.Interval;

public class SegmentedBow implements Comparable<SegmentedBow> {
    private Interval<Integer> interval;
    private LowBowSubtitles lowBowSubtitles;

    public SegmentedBow(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles) {
        this.interval = interval;
        this.lowBowSubtitles = lowBowSubtitles;
    }

    public Interval<Integer> getInterval() {
        return interval;
    }

    public LowBowSubtitles getLowBowSubtitles() {
        return lowBowSubtitles;
    }

    public void cutSegment(String outputAddress) {
        SubsSplitter textSplitter = lowBowSubtitles.getTextSplitter();
        SubsSplitter.Subtitle xmin = textSplitter.getSubtitleFromIndexWord(interval.getXmin());
        SubsSplitter.Subtitle xmax = textSplitter.getSubtitleFromIndexWord(interval.getXmax());
        FFMpegVideoApi.cutVideo(lowBowSubtitles.getVideoAddress(), xmin.getInterval().getXmin(), xmax.getInterval().getXmax(), outputAddress);
    }

    @Override
    public int compareTo(SegmentedBow o) {
        return lowBowSubtitles.getVideoAddress().compareTo(o.getLowBowSubtitles().getVideoAddress()) * interval.getXmin().compareTo(o.getInterval().getXmin());
    }
}
