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
 * The type Spectral clustering.
 */
public abstract class SpectralClustering extends AbstractGraphClustering {
    private Matrix eigenCoeff;
    private int maxEigenValue;
    private SymmetricEigen symmetricEigen;
    private EigenvalueDecomposition eigenvalueDecomposition;

    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public SpectralClustering(KnnGraph graph) {
        super(graph);
    }


    /**
     * Clustering map.
     *
     * @param k the k number of clusters
     * @return the map of classByDataIndex and alters graph to have a classification on each vertex.
     */
    public Map<Integer, List<Integer>> clustering(int k) {
        return clustering(k, (x) -> Math.exp(-x * x), 1E-10, 500);
    }


    /**
     * Spectral clustering.
     *
     * @param k                 the k number of clusters
     * @param similarityMeasure the similarity measure
     * @param epsilon           the epsilon
     * @param repetitions       the repetitions
     * @return the map of classByDataIndex and alters graph to have a classification on each vertex.
     */
    public Map<Integer, List<Integer>> clustering(int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions) {
        return clustering(k, similarityMeasure, epsilon, repetitions, new SpectralMethod() {
            @Override
            public Matrix getV(Matrix laplacian) {
                symmetricEigen = new SymmetricEigen(laplacian);
                symmetricEigen.computeEigen(epsilon, maxEigenValue, new HyperEigenAlgo());
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

    public Map<Integer, List<Integer>> clusteringJama(int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions) {
        return clustering(k, similarityMeasure, epsilon, repetitions, new SpectralMethod() {
            @Override
            public Matrix getV(Matrix laplacian) {
                eigenvalueDecomposition = new EigenvalueDecomposition(new Jama.Matrix(laplacian.getMatrix()));
                return new Matrix(eigenvalueDecomposition.getV().getArray());
            }

            @Override
            public double[] getEigenValues() {
                return eigenvalueDecomposition.getRealEigenvalues();
            }
        });
    }

    private Map<Integer, List<Integer>> clustering(int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions, SpectralMethod spectralMethod) {
        k = Integer.max(k, 1);
        //compute laplacian matrix
        Matrix W = getWeightMatrix(similarityMeasure);
        Diagonal D = getDegreeMatrix(W);
        Diagonal sqrt = D.inverse().sqrt();
        Matrix laplacianMatrix = getLaplacian(D, W);

        //compute eigenVectors
        maxEigenValue = Integer.min(k + 1, laplacianMatrix.getRows());

        Matrix U = spectralMethod.getV(laplacianMatrix);
        this.eigenCoeff = U;
        Matrix subMatrix = U.getSubMatrix(1, U.getRows(), 2, maxEigenValue);

        Matrix transformMatrix = transformMatrix(subMatrix, D, W);

        //kmeans
        Kmeans kmeans = new Kmeans(transformMatrix);
        kmeans.runKmeans(k, epsilon, repetitions);

        //fix index to graph index
        this.classification = super.fixIndexOfClassificationMap(kmeans.getClassification());
        this.inverseClassification = super.fixIndexOfInverseClassificationMap(kmeans.getInverseClassification());
        return inverseClassification;
    }

    protected abstract Matrix transformMatrix(Matrix u, Diagonal d, Matrix w);

    protected abstract Matrix getLaplacian(Diagonal d, Matrix w);

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
    public Matrix getEigenCoeff() {
        return eigenCoeff;
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
