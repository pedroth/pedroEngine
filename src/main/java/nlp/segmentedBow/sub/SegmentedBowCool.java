package nlp.segmentedBow.sub;


import algebra.Matrix;
import algebra.Vector;
import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import utils.Interval;

public class SegmentedBowCool extends SubSegmentedBow {

    public SegmentedBowCool(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles) {
        super(interval, lowBowSubtitles);
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
