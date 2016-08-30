package nlp.lowbow.src;

import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import algebra.src.Vector;

/**
 * The type Lowbow subtitle eigen.
 */
public class LowbowSubtitleEigen extends LowBowSubtitles {
    private Matrix eigenLowbow;
    private Matrix eigenBasis;
    private Vector eigenValues;

    /**
     * Instantiates a new Lowbow subtitle eigen.
     *
     * @param in the in
     */
    public LowbowSubtitleEigen(String in) {
        super(in);
    }


    /**
     * Build void.
     *
     * @param minEigen the min eigen
     * @param maxEigen the max eigen
     */
    public void build(int minEigen, int maxEigen) {
        super.build();
        // exchange minEigen by maxEigen if the condition below is true
        if (minEigen > maxEigen) {
            minEigen = maxEigen + minEigen;
            maxEigen = minEigen - maxEigen;
            minEigen = minEigen - maxEigen;
        }
        // bound minEigen and maxEigen to acceptable limits
        minEigen = Math.max(0, minEigen);
        maxEigen = Math.min(maxEigen, textLength - 1);

        LineLaplacian laplacian = new LineLaplacian(this.textLength);
        Vector[] eigenVectors = laplacian.getEigenVectors();
        Vector eigenValues = new Vector(laplacian.getEigenValues());
        this.eigenBasis = new Matrix(eigenVectors);
        this.eigenLowbow = Matrix.prod(Matrix.transpose(eigenBasis), rawCurve);
        Matrix prod = eigenBasis.getSubMatrix(1, this.textLength, minEigen, maxEigen).prod(Matrix.diag(eigenValues.getSubVector(minEigen, maxEigen)).prod(eigenLowbow.getSubMatrix(minEigen, maxEigen, 1, this.numWords)));
        prod.applyFunction(x -> Math.abs(x));
        Vector sum = new Vector(prod.getColumns());
        sum.fill(1);
        sum = prod.prodVector(sum);
        sum.applyFunction(x -> 1 / x);
        prod = Matrix.diag(sum).prod(prod);
        this.curve = prod.getRowsVectors();
    }
}
