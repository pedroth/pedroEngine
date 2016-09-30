package graph;

import algebra.src.Matrix;
import algebra.src.Vector;
import numeric.src.HyperEigenAlgo;
import numeric.src.Kmeans;
import numeric.src.SymmetricEigen;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The type Spectral clustering.
 */
public class SpectralClustering {
    private Graph graph;
    private Map<Integer, List<Integer>> inverseClassification;

    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public SpectralClustering(Graph graph) {
        this.graph = graph;
    }


    /**
     * Clustering map.
     *
     * @param k the k number of clusters
     * @return the map of classByDataIndex and alters graph to have a classification on each vertex.
     */
    public Map<Integer, List<Integer>> clustering(int k) {
        return clustering(k, (x) -> Math.exp(-x * x));
    }


    /**
     * Spectral clustering.
     *
     * @param k                 the k number of clusters
     * @param similarityMeasure the similarity measure
     * @return the map of classByDataIndex and alters graph to have a classification on each vertex.
     */
    public Map<Integer, List<Integer>> clustering(int k, Function<Double, Double> similarityMeasure) {
        Matrix W = getWeightMatrix(similarityMeasure);
        Matrix D = getDegreeMatrix(W);
        Matrix laplacianMatrix = Matrix.scalarProd(0.5, Matrix.diff(D, W));
        SymmetricEigen symmetricEigen = new SymmetricEigen(laplacianMatrix);
        symmetricEigen.computeEigen(1E-5, Integer.min(k + 1, laplacianMatrix.getRows()), new HyperEigenAlgo());
        Matrix U = new Matrix(symmetricEigen.getEigenVectors());
        Kmeans kmeans = new Kmeans(U.transpose());
        kmeans.runKmeans(k);
        Map<Integer, List<Integer>> inverseClassification = kmeans.getInverseClassification();
        Integer[] keyIndex = this.graph.getKeyIndex();
        for (Map.Entry<Integer, List<Integer>> entry : inverseClassification.entrySet()) {
            Integer kclass = entry.getKey();
            for (Integer index : entry.getValue()) {
                graph.putVertexProperty(keyIndex[index], "class", kclass);
            }
        }
        this.inverseClassification = inverseClassification;
        return inverseClassification;
    }


    private Matrix getWeightMatrix(Function<Double, Double> similarityMeasure) {
        Matrix weightMatrixExp = new Matrix(this.graph.getAdjacencyMatrix());
        Matrix weightMatrix = this.graph.getWeightMatrix(0);
        //since it is a symmetric graph we just need to do half of the graph, here I didn't care.
        for (int i = 1; i <= weightMatrixExp.getRows(); i++) {
            for (int j = 1; j <= weightMatrixExp.getColumns(); j++) {
                weightMatrixExp.setXY(i, j, weightMatrixExp.getXY(i, j) == 0.0 ? 0.0 : similarityMeasure.apply(weightMatrix.getXY(i, j)));
            }
        }
        return weightMatrixExp;
    }

    private Matrix getDegreeMatrix(Matrix w) {
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
        return Matrix.diag(degrees);
    }

    /**
     * Gets inverse classification.
     *
     * @return the inverse classification
     */
    public Map<Integer, List<Integer>> getInverseClassification() {
        return inverseClassification;
    }
}
