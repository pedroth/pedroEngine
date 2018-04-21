package nlp.segmentedBow.sub;


import nlp.lowbow.eigenLowbow.EigenLowBow;
import nlp.segmentedBow.BaseSegmentedBow;
import utils.Interval;

public interface SegmentedBowFactory<L extends EigenLowBow, B extends BaseSegmentedBow<L>> {
    B getInstance(Interval<Integer> interval, L lowBow);
}
