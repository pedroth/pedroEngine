package nlp.segmentedBow;


import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import utils.Interval;

public class SegmentedBowHeat extends BaseSegmentedBow {

    public SegmentedBowHeat(Interval<Integer> interval, LowBowSubtitles lowBowSubtitles) {
        super(interval, lowBowSubtitles);
    }

    protected void buildSegmentBow() {
        Matrix rawCurveSeg = lowBowSubtitles.getRawCurveFromHeatRepresentation(interval.getXmin(), interval.getXmax());
        Vector[] lowCurve = rawCurveSeg.getRowsVectors();
        segmentBow = new Vector(lowCurve[0].getDim());
        for (int i = 0; i < lowCurve.length; i++) {
            segmentBow = Vector.add(segmentBow, lowCurve[i]);
        }
        segmentBow = Vector.scalarProd(1.0 / lowCurve.length, segmentBow);
    }
}
