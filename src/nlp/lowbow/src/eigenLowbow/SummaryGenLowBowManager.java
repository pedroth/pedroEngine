package nlp.lowbow.src.eigenLowbow;

import algebra.src.DistanceMatrix;
import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.src.simpleLowBow.BaseLowBowManager;
import nlp.utils.SegmentedBow;
import numeric.src.Distance;
import numeric.src.MyMath;

import java.util.List;

/**
 * The type Summary gen low bow manager.
 *
 * @param <L> the type parameter
 */
public class SummaryGenLowBowManager<L extends LowBowSubtitles> extends BaseLowBowManager<L> {
    // Eigen vectors of the LineLaplacian matrix
    private Matrix eigenBasisGlobal;
    // Eigen values of the LineLaplacian matrix
    private Vector eigenValuesGlobal;
    private List<SegmentedBow> segmentedBows;

    /**
     * Instantiates a new Summary gen low bow manager.
     */
    public SummaryGenLowBowManager() {
        // blank on purpose
    }

    /**
     * Build model.
     *
     * @param p the p percentage of the maxTextLength
     */
    public void buildModel(double p) {
        super.build();
        // it can be showed that maxTextLength is always a integer, it is a double for convenience
        int maxTextLength = (int) getMaxTextLength();
        LineLaplacian L = new LineLaplacian(maxTextLength);
        this.eigenBasisGlobal = new Matrix(L.getEigenVectors());
        this.eigenValuesGlobal = new Vector(L.getEigenValues());
        int k = (int) MyMath.clamp(p * maxTextLength + 1, 1, maxTextLength);
        for (L lowbow : this.getDocModels()) {
            lowbow.buildHeatRepresentation(eigenBasisGlobal, eigenValuesGlobal, k);
            lowbow.deleteRawCurve();
        }

    }

    /**
     * Gets eigen basis global.
     *
     * @return the eigen basis global
     */
    public Matrix getEigenBasisGlobal() {
        return eigenBasisGlobal;
    }

    /**
     * Gets eigen values global.
     *
     * @return the eigen values global
     */
    public Vector getEigenValuesGlobal() {
        return eigenValuesGlobal;
    }

    /**
     * Gets distance matrix. Uses the Forbenius norm to compute this distance matrix
     *
     * @return the distance matrix
     */
    public DistanceMatrix getDistanceMatrix() {
        Distance<L> distance = EigenLowBow::heatDistance;
        return getDistanceMatrix(distance);
    }

    public DistanceMatrix getDistanceMatrixOfSegmentations(Distance<SegmentedBow> distance) {
        int size = segmentedBows.size();
        DistanceMatrix distanceMatrix = new DistanceMatrix(size);
        for (int i = 2; i <= size; i++) {
            for (int j = 1; j < i; j++) {
                SegmentedBow lowbowJ = segmentedBows.get(j - 1);
                SegmentedBow lowbowI = segmentedBows.get(i - 1);
                distanceMatrix.setXY(i, j, distance.dist(lowbowI, lowbowJ));
            }
        }
        return distanceMatrix;
    }

    public void buildSegmentations() {
        for (L docModel : docModels) {
            List segmentation = docModel.getSegmentation();
            segmentedBows.addAll(segmentation);
        }
    }

    public List<SegmentedBow> getSegmentedBows() {
        return segmentedBows;
    }
}
