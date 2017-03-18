package nlp.lowbow.eigenLowbow;

import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public final class MaxDerivativeSegmentator implements LowBowSegmentator {
    private static MaxDerivativeSegmentator instance = new MaxDerivativeSegmentator();

    private MaxDerivativeSegmentator() {
        //black constructor prevents instance creation
    }

    public static MaxDerivativeSegmentator getInstance() {
        return instance;
    }

    @Override
    public List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory, LowBowSubtitles lowBowSubtitles) {
        List<BaseSegmentedBow> segmentedBows = new ArrayList<>();
        Vector segFunc = computeSegmentation(lowBowSubtitles);
        Vector maxIndicator = findLocalMax(segFunc);
        int minIndex = 1;
        int size = maxIndicator.size();

        for (int i = 2; i <= size; i++) {
            if (maxIndicator.getX(i) == 1.0 && i != minIndex) {
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

    private Vector findLocalMax(Vector seg) {
        int dim = seg.getDim();
        Vector ans = new Vector(dim - 2);
        for (int i = 2; i <= dim - 1; i++) {
            double dxf = seg.getX(i) - seg.getX(i + 1);
            double dxb = seg.getX(i) - seg.getX(i - 1);
            ans.setX(i - 1, dxf > 0 && dxb > 0 ? 1.0 : 0.0);
        }
        return ans;
    }

    private Vector computeSegmentation(LowBowSubtitles lowBowSubtitles) {
        Matrix dGamma = lowBowSubtitles.computeLowBowDerivative();
        Vector[] dGammaRows = dGamma.getRowsVectors();
        double[] ans = new double[dGamma.getRows()];
        for (int i = 0; i < dGammaRows.length; i++) {
            ans[i] = Vector.innerProd(dGammaRows[i], dGammaRows[i]);
        }
        return new Vector(ans);
    }
}
