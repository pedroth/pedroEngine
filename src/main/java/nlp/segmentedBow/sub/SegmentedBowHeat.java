package nlp.segmentedBow.sub;


import algebra.Matrix;
import algebra.Vector;
import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import utils.Interval;

public class SegmentedBowHeat extends SubSegmentedBow {

    public SegmentedBowHeat(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles) {
        super(interval, lowBowSubtitles);
    }

    protected void buildSegmentBow() {
        Matrix rawCurveSeg = lowBowOrig.getRawCurveFromHeatRepresentation(interval.getXmin(), interval.getXmax());
        Vector[] lowCurve = rawCurveSeg.getRowsVectors();
        segmentBow = new Vector(lowCurve[0].getDim());
        for (int i = 0; i < lowCurve.length; i++) {
            segmentBow = Vector.add(segmentBow, lowCurve[i]);
        }
        segmentBow = Vector.scalarProd(1.0 / lowCurve.length, segmentBow);
    }
}
