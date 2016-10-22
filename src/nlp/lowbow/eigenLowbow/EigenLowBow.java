package nlp.lowbow.eigenLowbow;


import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.simpleLowBow.BaseLowBow;
import nlp.lowbow.symbolSampler.SymbolSampler;
import nlp.textSplitter.TextSplitter;
import nlp.utils.Simplex;
import numeric.src.MyMath;

/**
 * The type Eigen low bow.
 */
public class EigenLowBow extends BaseLowBow {
    private final static double epsilon = 1E-3;
    // Eigen vectors of the LineLaplacian matrix
    private Matrix eigenBasis;
    // Eigen values of the LineLaplacian matrix
    private Vector eigenValues;
    // coordinates of the Lowbow curve given by the eigenBasis.
    private Matrix eigenCoord;
    // number of coefficients that encode the low dimensional representation of lowbow
    private int numberOfLowDimCoeff;

    /**
     * Instantiates a new Eigen low bow.
     *
     * @param originalText the original text
     * @param textSplitter the text splitter
     */
    public EigenLowBow(String originalText, TextSplitter textSplitter) {
        super(originalText, textSplitter);
    }

    /**
     * Instantiates a new Eigen low bow.
     *
     * @param originalText the original text
     * @param textSplitter the text splitter
     * @param simplex the simplex
     */
    public EigenLowBow(String originalText, TextSplitter textSplitter, Simplex simplex) {
        super(originalText, textSplitter, simplex);
    }

    /**
     * Instantiates a new Eigen low bow.
     *
     * @param lowBow the low bow
     */
    public EigenLowBow(EigenLowBow lowBow) {
        super(lowBow);
        this.eigenBasis = lowBow.getEigenBasis();
        this.eigenValues = lowBow.getEigenValues();
        this.eigenCoord = lowBow.getEigenCoord();
    }

    @Override
    public String generateText(SymbolSampler symbolSampler) {
        Matrix low = getRawCurveFromHeatRepresentation();
        Vector[] lowCurve = low.getRowsVectors();
        StringBuilder acc = new StringBuilder();
        for (int i = 0; i < lowCurve.length; i++) {
            acc.append(symbolSampler.nextSymbol(lowCurve[i], simplex) + "\n");
        }
        return acc.toString();
    }

    /**
     * Gets eigen basis.
     *
     * @return the eigen basis
     */
    public Matrix getEigenBasis() {
        return eigenBasis;
    }


    /**
     * Gets eigen values.
     *
     * @return the eigen values
     */
    public Vector getEigenValues() {
        return eigenValues;
    }


    /**
     * Gets eigen coord.
     *
     * @return the eigen coord
     */
    public Matrix getEigenCoord() {
        return eigenCoord;
    }

    /**
     * Delete raw curve.
     */
    public void deleteRawCurve() {
        this.rawCurve = null;
    }

    /**
     * Delete heat representation.
     */
    public void deleteHeatRepresentation() {
        this.eigenCoord = null;
    }

    /**
     * Build heat representation.
     *
     * @param eigenBasis the eigen basis
     * @param eigenValues the eigen values
     * @param k k <= eigenBasis.getRows and is the k eigenVectors used to reduce dimensionality of the lowBow
     */
    public void buildHeatRepresentation(Matrix eigenBasis, Vector eigenValues, int k) {
        this.numberOfLowDimCoeff = k;
        this.eigenBasis = eigenBasis;
        this.eigenValues = eigenValues;
        if (textLength > 50) {
            this.eigenCoord = eigenBasis.getSubMatrix(1, textLength, 1, k).transpose().prodParallel(rawCurve);
        } else {
            this.eigenCoord = eigenBasis.getSubMatrix(1, textLength, 1, k).transpose().prod(rawCurve);
        }
    }

    /**
     * Build raw curve from heat representation.
     */
    public void buildRawCurveFromHeatRepresentation() {
        this.rawCurve = getRawCurveFromHeatRepresentation();
    }

    /**
     * Gets number of low dim coeff.
     *
     * @return the number of low dim coeff
     */
    public int getNumberOfLowDimCoeff() {
        return numberOfLowDimCoeff;
    }

    /**
     * Heat distance.
     *
     * @param lowBow the low bow
     * @return the double
     */
    public double heatDistance(EigenLowBow lowBow) {
        double heatTime = getHeatTime();
        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        Matrix a = this.eigenCoord.getSubMatrix(1, numberOfLowDimCoeff, 1, this.eigenCoord.getColumns());
        Matrix b = lowBow.getEigenCoord().getSubMatrix(1, numberOfLowDimCoeff, 1, this.eigenCoord.getColumns());
        Matrix diff = Matrix.diff(a, b);
        return diag.prod(diff).squareNorm();
    }

    /**
     * Exp st.
     *
     * @param t the t
     * @param k the k
     * @return the matrix
     */
    protected Matrix expSt(double t, int k) {
        Matrix expEigen = eigenValues.getSubMatrix(1, numberOfLowDimCoeff, 1, 1);
        expEigen.applyFunction(x -> Math.exp(-x * t));
        return Matrix.diag(expEigen);
    }

    /**
     * Gets heat time. It is the necessary time for the lowbow heat evolution to reach its truncated representation.
     * This means to solve exp(-s(numberOfLowDimCoeff * heatTime)) = epsilon, where epsilon is some positive constant
     *
     * @return the heat time
     */
    public double getHeatTime() {
        if (numberOfLowDimCoeff == eigenBasis.getRows()) {
            return 0;
        }
        if (numberOfLowDimCoeff == 1) {
            return 1;
        }
        return -Math.log(epsilon) / eigenValues.getX(numberOfLowDimCoeff);
    }

    /**
     * Gets raw curve from heat representation.
     *
     * @param xmin the xmin
     * @param xmax the xmax
     * @return the raw curve from heat representation segmented from xmin to xmax
     */
    public Matrix getRawCurveFromHeatRepresentation(int xmin, int xmax) {
        //switch variables if xmax < xmin
        if (xmax < xmin) {
            xmin = xmin + xmax;
            xmax = xmin - xmax;
            xmin = xmin - xmax;
        }
        int clamMin = (int) MyMath.clamp(xmin, 1, textLength);
        int clamMax = (int) MyMath.clamp(xmax, 1, textLength);
        double heatTime = getHeatTime();
        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        if (clamMax - clamMin < 50) {
            return eigenBasis.getSubMatrix(clamMin, clamMax, 1, numberOfLowDimCoeff).prod(diag.prod(eigenCoord));
        }
        return eigenBasis.getSubMatrix(clamMin, clamMax, 1, numberOfLowDimCoeff).prodParallel(diag.prod(eigenCoord));
    }

    /**
     * Gets raw curve from heat representation.
     *
     * @return the raw curve from heat representation
     */
    public Matrix getRawCurveFromHeatRepresentation() {
        return getRawCurveFromHeatRepresentation(1, textLength);
    }

    @Override
    public Vector[] getCurve() {
        if (rawCurve == null) {
            return getRawCurveFromHeatRepresentation().getRowsVectors();
        }
        return super.getCurve();
    }
}
