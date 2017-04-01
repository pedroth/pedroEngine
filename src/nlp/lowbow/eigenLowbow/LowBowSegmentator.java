package nlp.lowbow.eigenLowbow;

import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.sub.SegmentedBowFactory;

import java.util.List;

public interface LowBowSegmentator<L extends EigenLowBow, B extends BaseSegmentedBow<L>> {
    List<B> getSegmentation(SegmentedBowFactory<L, B> factory, L lowBow);
}
