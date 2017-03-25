package nlp.lowbow.eigenLowbow;


import algebra.src.LineGradient;
import algebra.src.Matrix;
import nlp.textSplitter.TextSplitter;
import nlp.utils.Simplex;

public class EigenLowBowSeg extends EigenLowBow {

    public EigenLowBowSeg(String originalText, TextSplitter textSplitter) {
        super(originalText, textSplitter);
    }

    public EigenLowBowSeg(String originalText, TextSplitter textSplitter, Simplex simplex) {
        super(originalText, textSplitter, simplex);
    }

    public EigenLowBowSeg(EigenLowBow lowBow) {
        super(lowBow);
    }

    /**
     * Compute low bow derivative.
     *
     * @return the matrix
     */
    public Matrix computeLowBowDerivative() {
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
