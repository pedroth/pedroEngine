package nlp.segmentedBow;

import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.eigenLowbow.EigenLowBow;
import utils.Interval;

public class SegmentBow<L extends EigenLowBow> extends BaseSegmentedBow<L> {

    public SegmentBow(Interval<Integer> interval, L lowBowOrig) {
        super(interval, lowBowOrig);
    }

    @Override
    protected void buildSegmentBow() {
        if (lowBowOrig.getRawCurve() == null) {
            lowBowOrig.build();
        }
        Matrix rawCurve = lowBowOrig.getRawCurve();
        Matrix rawCurveSegment = rawCurve.getSubMatrix(interval.getXmin(), interval.getXmax(), 1, lowBowOrig.getNumWords());
        Vector[] lowCurve = rawCurveSegment.getRowsVectors();
        segmentBow = new Vector(lowCurve[0].getDim());
        for (int i = 0; i < lowCurve.length; i++) {
            segmentBow = Vector.add(segmentBow, lowCurve[i]);
        }
        segmentBow = Vector.scalarProd(1.0 / lowCurve.length, segmentBow);
    }
}
