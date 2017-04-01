package nlp.segmentedBow;


import algebra.src.Vector;
import nlp.lowbow.eigenLowbow.EigenLowBow;
import utils.Interval;

import java.util.function.Function;

public abstract class BaseSegmentedBow<L extends EigenLowBow> {
    /**
     * intervals \in \mathbb{N}_{+}^{2}.
     */
    protected Interval<Integer> interval;
    protected L lowBowOrig;
    protected Vector segmentBow;

    public BaseSegmentedBow(Interval<Integer> interval, L lowBowOrig) {
        this.interval = interval;
        this.lowBowOrig = lowBowOrig;
        buildSegmentBow();
    }

    protected abstract void buildSegmentBow();

    public Interval<Integer> getInterval() {
        return interval;
    }

    public L getLowBowOrig() {
        return lowBowOrig;
    }


    public String toString(Function<BaseSegmentedBow, String> function) {
        return function.apply(this);
    }

    public Vector getSegmentBow() {
        return segmentBow;
    }

}
