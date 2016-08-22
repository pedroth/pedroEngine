package nlp.lowbow.src;

import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import algebra.src.Vector;

/**
 * The type Lowbow subtitle eigen.
 */
public class LowbowSubtitleEigen extends LowBowSubtitles {
    private Matrix eigenLowbow;
    private int eigenBandwidth;
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
     * @param eigenBandwidth the eigen bandwidth is a valure between 1 and textLength
     */
    public void build(int eigenBandwidth) {
        super.build();
        this.eigenBandwidth = eigenBandwidth > 0 && eigenBandwidth <= textLength ? eigenBandwidth : textLength;
        LineLaplacian laplacian = new LineLaplacian(this.textLength);
        Vector[] eigenVectors = laplacian.getEigenVectors();
        Vector[] eigenBadwidth = new Vector[this.eigenBandwidth];
        for (int i = eigenBandwidth - 1; i >= this.textLength - this.eigenBandwidth; i--) {
            eigenBadwidth[i] = eigenVectors[i];
        }
        this.eigenBasis = new Matrix(eigenVectors);
        eigenBasis.transpose();
        this.eigenLowbow = Matrix.prod(eigenBasis, rawCurve);
    }
}
