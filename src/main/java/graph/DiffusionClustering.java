package graph;


import Jama.EigenvalueDecomposition;
import algebra.Diagonal;
import algebra.Matrix;
import algebra.Vector;
import numeric.HyperEigenAlgo;
import numeric.Kmeans;
import numeric.SymmetricEigen;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The type Diffusion clustering.
 */
public class DiffusionClustering extends AbstractGraphClustering {
    private final static double REDUCE_DIMENSION_THRESHOLD = 0.1;
    private final static double DEFAULT_PERCENT_OF_EIGEN_VALUES = 0.04;
    /**
     * The Heat time.
     */
    double heatTime;
    private Matrix eigenVectors;
    private Vector eigenValues;
    private Matrix eigenEmbedding;
    private boolean isNormalized = false;
    private Kmeans kmeans;

    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public DiffusionClustering(KnnGraph graph) {
        super(graph);
    }

    /**
     * Spectral clustering.
     *
     * @param heatTime          the heat time, if heatTime < 0 then  heatTime = -log(epsilon)/ lambda(gamma * #EigenValues + 1) else heatTime = heatTime
     * @param k                 the k number of clusters
     * @param similarityMeasure the similarity measure
     * @param epsilon           the epsilon
     * @param repetitions       the repetitions
     * @return the map of classByDataIndex and alters graph to have a classification on each vertex.
     */
    public Map<Integer, List<Integer>> clustering(double heatTime, int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions) {
        return clustering(heatTime, k, similarityMeasure, epsilon, repetitions, new SpectralMethod() {
            private SymmetricEigen symmetricEigen;

            @Override
            public Matrix getV(Matrix laplacian) {
                symmetricEigen = new SymmetricEigen(laplacian);
                symmetricEigen.computeEigen(epsilon, laplacian.getRows(), new HyperEigenAlgo());
                return new Matrix(symmetricEigen.getEigenVectors());
            }

            @Override
            public double[] getEigenValues() {
                Double[] eigenValues = symmetricEigen.getEigenValues();
                double[] ans = new double[eigenValues.length];
                for (int i = 0; i < ans.length; i++) {
                    ans[i] = eigenValues[i];
                }
                return ans;
            }
        });
    }

    /**
     * Clustering jama.
     *
     * @param heatTime          the heat time, if heatTime < 0 then  heatTime = -log(epsilon)/ lambda(gamma * #EigenValues + 1) else heatTime = heatTime
     * @param k                 the k
     * @param similarityMeasure the similarity measure
     * @param epsilon           the epsilon
     * @param repetitions       the repetitions
     * @return the map
     */
    public Map<Integer, List<Integer>> clusteringJama(double heatTime, int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions) {
        return clustering(heatTime, k, similarityMeasure, epsilon, repetitions, new SpectralMethod() {
            private EigenvalueDecomposition symmetricEigen;

            @Override
            public Matrix getV(Matrix laplacian) {
                symmetricEigen = new EigenvalueDecomposition(new Jama.Matrix(laplacian.getMatrix()));
                return new Matrix(symmetricEigen.getV().getArray());
            }

            @Override
            public double[] getEigenValues() {
                return symmetricEigen.getRealEigenvalues();
            }
        });
    }


    private Map<Integer, List<Integer>> clustering(double heatTime, int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions, SpectralMethod spectralMethod) {
        k = Integer.max(k, 1);
        //compute laplacian matrix
        Matrix W = getWeightMatrix(similarityMeasure);
        Diagonal D = getDegreeMatrix(W);
        //danger
        Diagonal sqrt = D.sqrt().inverse();
        Matrix laplacianMatrix = isNormalized ? Matrix.scalarProd(0.5, sqrt.prod(Matrix.diff(D, W).prod(sqrt))) : Matrix.scalarProd(0.5, Matrix.diff(D, W));

        //compute eigenVectors
        Matrix U = spectralMethod.getV(laplacianMatrix);
        this.eigenVectors = U;

        this.eigenValues = new Vector(spectralMethod.getEigenValues());
        Vector eigenValueCopy = new Vector(this.eigenValues);

        final double finalHeatTime = heatTime < 0.0 ? -Math.log(REDUCE_DIMENSION_THRESHOLD) / eigenValues.getX((int) (Math.floor(DEFAULT_PERCENT_OF_EIGEN_VALUES * eigenValueCopy.size()) + 1)) : heatTime;
        eigenValueCopy.applyFunction(x -> Math.exp(-x * finalHeatTime));

        int compressDim = eigenValues.size();

        for (int i = 1; i <= eigenValueCopy.size(); i++) {
            if (eigenValueCopy.getX(i) < REDUCE_DIMENSION_THRESHOLD) {
                compressDim = i;
                break;
            }
        }

        Vector compressEigen = new Vector(compressDim);
        for (int i = 1; i <= compressDim; i++) {
            compressEigen.setX(i, eigenValueCopy.getX(i));
        }

        // exp matrix
        Diagonal expT = new Diagonal(compressEigen);
        Matrix prod = expT.prod(Matrix.transpose(U).getSubMatrix(1, compressDim, 1, U.getRows()));

        this.eigenEmbedding = prod;

        //kmeans
        kmeans = new Kmeans(prod);
        kmeans.runKmeans(k, epsilon, repetitions);

        //fix index to graph index
        this.classification = super.fixIndexOfClassificationMap(kmeans.getClassification());
        this.inverseClassification = super.fixIndexOfInverseClassificationMap(kmeans.getInverseClassification());
        return inverseClassification;
    }

    private Matrix getWeightMatrix(Function<Double, Double> similarityMeasure) {
        Matrix adjacencyMatrix = this.graph.getAdjacencyMatrix();
        Matrix weightMatrix = this.graph.getWeightMatrix(0);
        //since it is a symmetric graph we just need to do half of the graph, here I didn't care.
        for (int i = 1; i <= adjacencyMatrix.getRows(); i++) {
            for (int j = 1; j <= adjacencyMatrix.getColumns(); j++) {
                weightMatrix.setXY(i, j, adjacencyMatrix.getXY(i, j) == 0.0 ? 0.0 : similarityMeasure.apply(weightMatrix.getXY(i, j)));
            }
        }
        return weightMatrix;
    }

    private Diagonal getDegreeMatrix(Matrix w) {
        double acc;
        int columns = w.getColumns();
        int rows = w.getRows();
        Vector degrees = new Vector(rows);
        for (int i = 1; i <= rows; i++) {
            acc = 0;
            for (int j = 1; j <= columns; j++) {
                acc += w.getXY(i, j);
            }
            degrees.setX(i, acc);
        }
        return (Diagonal) Matrix.diag(degrees);
    }

    /**
     * Gets eigen coeff.
     *
     * @return the eigen coeff
     */
    public Matrix getEigenVectors() {
        return eigenVectors;
    }

    /**
     * Gets eigen values.
     *
     * @return the eigen values
     */
    public Vector getEigenValues() {
        return this.eigenValues;
    }

    /**
     * Gets eigen embedding.
     *
     * @return the eigen embedding
     */
    public Matrix getEigenEmbedding() {
        return this.eigenEmbedding;
    }

    /**
     * Gets heat time.
     *
     * @return the heat time
     */
    public double getHeatTime() {
        return heatTime;
    }

    /**
     * Sets heat time.
     *
     * @param heatTime the heat time
     */
    public void setHeatTime(double heatTime) {
        this.heatTime = heatTime;
    }


    /**
     * Is normalized.
     *
     * @return the boolean
     */
    public boolean isNormalized() {
        return isNormalized;
    }

    /**
     * Sets normalized.
     *
     * @param normalized the normalized
     */
    public void setNormalized(boolean normalized) {
        isNormalized = normalized;
    }

    private interface SpectralMethod {
        /**
         * Gets v.
         *
         * @param laplacian the laplacian
         * @return the v
         */
        Matrix getV(Matrix laplacian);

        /**
         * Get eigen values.
         *
         * @return the double [ ]
         */
        double[] getEigenValues();
    }
}
