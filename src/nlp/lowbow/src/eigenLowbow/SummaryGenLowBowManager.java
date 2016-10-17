package nlp.lowbow.src.eigenLowbow;

import algebra.src.DistanceMatrix;
import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.lowbow.src.simpleLowBow.BaseLowBowManager;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import nlp.segmentedBow.SegmentedBowHeat;
import numeric.src.Distance;
import numeric.src.MyMath;
import utils.StopWatch;

import java.util.ArrayList;
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
    private List<BaseSegmentedBow> segmentedBows;

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
        // it can be showed that maxTextLength is always a integer, it is a double for convenience
        int maxTextLength = (int) getMaxTextLength();
        LineLaplacian L = new LineLaplacian(maxTextLength);
        this.eigenBasisGlobal = new Matrix(L.getEigenVectors());
        this.eigenValuesGlobal = new Vector(L.getEigenValues());
        int k = (int) MyMath.clamp(p * maxTextLength + 1, 1, maxTextLength);
        for (L lowbow : this.getDocModels()) {
            lowbow.setSimplex(simplex);
            lowbow.build();
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

    public DistanceMatrix getDistanceMatrixOfSegmentations(Distance<Vector> distance) {
        int size = segmentedBows.size();
        DistanceMatrix distanceMatrix = new DistanceMatrix(size);
        for (int i = 2; i <= size; i++) {
            for (int j = 1; j < i; j++) {
                BaseSegmentedBow lowbowJ = segmentedBows.get(j - 1);
                BaseSegmentedBow lowbowI = segmentedBows.get(i - 1);
                distanceMatrix.setXY(i, j, distance.dist(lowbowI.getSegmentBow(), lowbowJ.getSegmentBow()));
            }
        }
        return distanceMatrix;
    }

    public void buildSegmentations(SegmentedBowFactory<BaseSegmentedBow> factory) {
        segmentedBows = new ArrayList<>();
        for (L docModel : docModels) {
            StopWatch stopWatch = new StopWatch();
            List<SegmentedBowHeat> segmentation = docModel.getSegmentation(factory);
            System.out.println("segmentation computation time : " + stopWatch.getEleapsedTime());
            segmentedBows.addAll(segmentation);
        }
    }

    public List<BaseSegmentedBow> getSegmentedBows() {
        return segmentedBows;
    }
}
