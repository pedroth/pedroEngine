package nlp.segmentedBow;


import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import utils.Interval;

public interface SegmentedBowFactory<B extends BaseSegmentedBow> {
    B getInstance(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles);
}
