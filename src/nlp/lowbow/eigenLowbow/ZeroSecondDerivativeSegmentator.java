package nlp.lowbow.eigenLowbow;


import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public class ZeroSecondDerivativeSegmentator implements LowBowSegmentator {
    @Override
    public List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory, LowBowSubtitles lowBowSubtitles) {
        List<BaseSegmentedBow> segmentedBows = new ArrayList<>();
        Vector segFunc = computeSegmentation(lowBowSubtitles);
        Vector zerosIndicator = findZeros(segFunc);
        int minIndex = 1;
        int size = zerosIndicator.size();
        for (int i = 2; i <= size; i++) {
            if (zerosIndicator.getX(i) == 1.0 && i != minIndex) {
                segmentedBows.add(factory.getInstance(new Interval(minIndex, i), lowBowSubtitles));
                minIndex = i + 1;
            }
        }
        int textLength = lowBowSubtitles.getTextLength();
        if (minIndex != textLength) {
            segmentedBows.add(factory.getInstance(new Interval(minIndex, textLength), lowBowSubtitles));
        }

        if (lowBowSubtitles.getRawCurve() != null) {
            lowBowSubtitles.deleteRawCurve();
        }
        return segmentedBows;
    }

    private Vector findZeros(Vector seg) {
        int dim = seg.getDim();
        Vector ans = new Vector(dim - 1);
        for (int i = 1; i <= dim - 1; i++) {
            ans.setX(i, seg.getX(i) * seg.getX(i + 1) < 0 ? 1.0 : 0.0);
        }
        return ans;
    }


    private Vector computeSegmentation(LowBowSubtitles lowBowSubtitles) {
        Matrix dGamma = lowBowSubtitles.computeLowBowDerivative();
        Matrix d2Gamma = lowBowSubtitles.computeLowBow2ndDerivative();
        Vector[] dGammaRows = dGamma.getRowsVectors();
        Vector[] d2GammaRows = d2Gamma.getRowsVectors();
        double[] ans = new double[d2Gamma.getRows()];
        for (int i = 0; i < dGammaRows.length; i++) {
            ans[i] = Vector.innerProd(dGammaRows[i], d2GammaRows[i]);
        }
        return new Vector(ans);
    }
}
