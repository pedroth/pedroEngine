package nlp.lowbow.eigenLowbow;

import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;

import java.util.List;

public interface LowBowSubSegmentator {
    List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory, LowBowSubtitles lowBowSubtitles);
}
