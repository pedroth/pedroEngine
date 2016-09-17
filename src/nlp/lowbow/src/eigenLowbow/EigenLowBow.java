package nlp.lowbow.src.eigenLowbow;


import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.src.simpleLowBow.BaseLowBow;
import nlp.lowbow.src.simpleLowBow.Simplex;
import nlp.textSplitter.TextSplitter;

public class EigenLowBow extends BaseLowBow {
    private Matrix eigenBasis;
    private Vector eigenValues;
    private Matrix eigenCoord;

    public EigenLowBow(String originalText, TextSplitter textSplitter) {
        super(originalText, textSplitter);
    }

    public EigenLowBow(String originalText, TextSplitter textSplitter, Simplex simplex) {
        super(originalText, textSplitter, simplex);
    }

    public EigenLowBow(EigenLowBow lowBow) {
        super(lowBow);
        this.eigenBasis = lowBow.getEigenBasis();
        this.eigenValues = lowBow.getEigenValues();
        this.eigenCoord = lowBow.getEigenCoord();
    }

    public Matrix getEigenBasis() {
        return eigenBasis;
    }

    public void setEigenBasis(Matrix eigenBasis) {
        this.eigenBasis = eigenBasis;
    }

    public Vector getEigenValues() {
        return eigenValues;
    }

    public void setEigenValues(Vector eigenValues) {
        this.eigenValues = eigenValues;
    }

    public Matrix getEigenCoord() {
        return eigenCoord;
    }
}
