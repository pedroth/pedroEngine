package nlp.lowbow.eigenLowbow;

import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;

import java.util.List;

public interface LowBowSegmentator {
    List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory, LowBowSubtitles lowBowSubtitles);
}
