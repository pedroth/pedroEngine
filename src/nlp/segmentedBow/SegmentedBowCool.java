package nlp.segmentedBow;


import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import utils.Interval;

public class SegmentedBowCool extends BaseSegmentedBow {
    public SegmentedBowCool(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles) {
        super(interval, lowBowSubtitles);
    }

    @Override
    protected void buildSegmentBow() {
        if (lowBowSubtitles.getRawCurve() == null) {
            lowBowSubtitles.build();
        }
        Matrix rawCurve = lowBowSubtitles.getRawCurve();
        Matrix rawCurveSegment = rawCurve.getSubMatrix(interval.getXmin(), interval.getXmax(), 1, lowBowSubtitles.getNumWords());
        Vector[] lowCurve = rawCurveSegment.getRowsVectors();
        segmentBow = new Vector(lowCurve[0].getDim());
        for (int i = 0; i < lowCurve.length; i++) {
            segmentBow = Vector.add(segmentBow, lowCurve[i]);
        }
        segmentBow = Vector.scalarProd(1.0 / lowCurve.length, segmentBow);

    }
}
