package nlp.lowbow.eigenLowbow;


import algebra.Matrix;
import algebra.Vector;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.sub.SegmentedBowFactory;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public class ZeroSecondDerivativeSegmentator<L extends EigenLowBow, B extends BaseSegmentedBow<L>> implements LowBowSegmentator<L, B> {
    private static ZeroSecondDerivativeSegmentator instance = new ZeroSecondDerivativeSegmentator();

    private ZeroSecondDerivativeSegmentator() {
        //black constructor prevents instance creation
    }

    public static ZeroSecondDerivativeSegmentator getInstance() {
        return instance;
    }

    @Override
    public List<B> getSegmentation(SegmentedBowFactory<L, B> factory, L lowBow) {
        List<B> segmentedBows = new ArrayList<>();
        Vector segFunc = computeSegmentation(lowBow);
        Vector zerosIndicator = findZeros(segFunc);
        int minIndex = 1;
        int size = zerosIndicator.size();
        for (int i = 2; i <= size; i++) {
            if (zerosIndicator.getX(i) == 1.0 && i != minIndex) {
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

    private Vector findZeros(Vector seg) {
        int dim = seg.getDim();
        Vector ans = new Vector(dim - 1);
        for (int i = 1; i <= dim - 1; i++) {
            ans.setX(i, seg.getX(i) * seg.getX(i + 1) < 0 ? 1.0 : 0.0);
        }
        return ans;
    }


    private Vector computeSegmentation(L lowBow) {
        Matrix dGamma = lowBow.computeLowBowDerivative();
        Matrix d2Gamma = lowBow.computeLowBow2ndDerivative();
        Vector[] dGammaRows = dGamma.getRowsVectors();
        Vector[] d2GammaRows = d2Gamma.getRowsVectors();
        double[] ans = new double[d2Gamma.getRows()];
        for (int i = 0; i < dGammaRows.length; i++) {
            ans[i] = Vector.innerProd(dGammaRows[i], d2GammaRows[i]);
        }
        return new Vector(ans);
    }
}
