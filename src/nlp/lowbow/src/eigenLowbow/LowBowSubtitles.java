package nlp.lowbow.src.eigenLowbow;


import algebra.src.LineGradient;
import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import nlp.textSplitter.SubsSplitter;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public class LowBowSubtitles<T extends SubsSplitter> extends EigenLowBow {
    private String videoAddress;

    public LowBowSubtitles(String in, T subtitleSplitter, String videoAddress) {
        super(in, subtitleSplitter);
        this.videoAddress = videoAddress;
    }

    public LowBowSubtitles(String in, T subtitleSplitter) {
        super(in, subtitleSplitter);
    }


    public String getVideoAddress() {
        return videoAddress;
    }

    @Override
    public T getTextSplitter() {
        return (T) super.getTextSplitter();
    }

    public List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory) {
        List<BaseSegmentedBow> segmentedBows = new ArrayList<>();
        Vector segFunc = computeSegmentation();
        Vector zerosIndicator = findZeros(segFunc);
        int minIndex = 1;
        int size = zerosIndicator.size();
        for (int i = 2; i <= size; i++) {
            if (zerosIndicator.getX(i) == 1.0 && i != minIndex) {
                segmentedBows.add(factory.getInstance(new Interval(minIndex, i), this));
                minIndex = i + 1;
            }
        }
        if (minIndex != textLength) {
            segmentedBows.add(factory.getInstance(new Interval(minIndex, textLength), this));
        }

        if (this.getRawCurve() != null) {
            this.deleteRawCurve();
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

    private Vector computeSegmentation() {
        Matrix dGamma = computeLowBowDerivative();
        Matrix d2Gamma = computeLowBow2ndDerivative();
        Vector[] dGammaRows = dGamma.getRowsVectors();
        Vector[] d2GammaRows = d2Gamma.getRowsVectors();
        double[] ans = new double[d2Gamma.getRows()];
        for (int i = 0; i < dGammaRows.length; i++) {
            ans[i] = Vector.innerProd(dGammaRows[i], d2GammaRows[i]);
        }
        return new Vector(ans);
    }

    private Matrix computeLowBow2ndDerivative() {
        double heatTime = getHeatTime();
        int numberOfLowDimCoeff = getNumberOfLowDimCoeff();
        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        diag = Matrix.diag(getEigenValues().getSubVector(1, numberOfLowDimCoeff)).prod(diag);
        if (textLength > 50) {
            return getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prodParallel(diag.prod(getEigenCoord()));
        }
        return getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prod(diag.prod(getEigenCoord()));
    }

    private Matrix computeLowBowDerivative() {
        double heatTime = getHeatTime();
        LineGradient opD = new LineGradient(textLength);
        int numberOfLowDimCoeff = getNumberOfLowDimCoeff();
        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        Matrix yt;
        if (textLength > 50) {
            yt = getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prodParallel(diag.prod(getEigenCoord()));
        } else {
            yt = getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prod(diag.prod(getEigenCoord()));
        }
        return opD.prod(yt);
    }


}
