package nlp.lowbow.eigenLowbow;

import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.sub.SegmentedBowFactory;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public final class MaxDerivativeSegmentator<L extends EigenLowBow, B extends BaseSegmentedBow<L>> implements LowBowSegmentator<L, B> {
    private static MaxDerivativeSegmentator instance = new MaxDerivativeSegmentator();

    private MaxDerivativeSegmentator() {
        //black constructor prevents instance creation
    }

    public static MaxDerivativeSegmentator getInstance() {
        return instance;
    }

    @Override
    public List<B> getSegmentation(SegmentedBowFactory<L, B> factory, L lowBow) {
        List<B> segmentedBows = new ArrayList<>();
        Vector segFunc = computeSegmentation(lowBow);
        Vector maxIndicator = findLocalMax(segFunc);
        int minIndex = 1;
        int size = maxIndicator.size();

        for (int i = 2; i <= size; i++) {
            if (maxIndicator.getX(i) == 1.0 && i != minIndex) {
                segmentedBows.add((B) factory.getInstance(new Interval(minIndex, i), lowBow));
                minIndex = i + 1;
            }
        }
        int textLength = lowBow.getTextLength();
        if (minIndex != textLength) {
            segmentedBows.add((B) factory.getInstance(new Interval(minIndex, textLength), lowBow));
        }

        if (lowBow.getRawCurve() != null) {
            lowBow.deleteRawCurve();
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

    private Vector computeSegmentation(L lowBow) {
        Matrix dGamma = lowBow.computeLowBowDerivative();
        Vector[] dGammaRows = dGamma.getRowsVectors();
        double[] ans = new double[dGamma.getRows()];
        for (int i = 0; i < dGammaRows.length; i++) {
            ans[i] = Vector.innerProd(dGammaRows[i], dGammaRows[i]);
        }
        return new Vector(ans);
    }
}
