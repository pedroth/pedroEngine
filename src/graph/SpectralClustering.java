package graph;

import Jama.EigenvalueDecomposition;
import algebra.src.Matrix;
import algebra.src.Vector;
import javafx.util.Pair;
import numeric.src.HyperEigenAlgo;
import numeric.src.Kmeans;
import numeric.src.SymmetricEigen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

/**
 * The type Spectral clustering.
 */
public class SpectralClustering {
    private final static String CLASS_VERTEX_PROPERTY = "class";
    private final KnnGraph graph;
    private Map<Integer, List<Integer>> inverseClassification;
    private Map<Integer, Integer> classification;
    private Matrix eigenCoeff;

    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public SpectralClustering(KnnGraph graph) {
        this.graph = new KnnGraph(graph);
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
        k = Integer.max(k, 1);
        //compute laplacian matrix
        Matrix W = getWeightMatrix(similarityMeasure);
        Matrix D = getDegreeMatrix(W);
        Matrix laplacianMatrix = Matrix.scalarProd(0.5, Matrix.diff(D, W));

        //compute eigenVectors
        int maxEigenValue = Integer.min(k + 1, laplacianMatrix.getRows());

        SymmetricEigen symmetricEigen = new SymmetricEigen(laplacianMatrix);
        symmetricEigen.computeEigen(epsilon, maxEigenValue, new HyperEigenAlgo());
        symmetricEigen.orderEigenValuesAndVector();

        Matrix U = new Matrix(symmetricEigen.getEigenVectors());
        this.eigenCoeff = U;
        Matrix subMatrix = U.getSubMatrix(1, U.getRows(), 2, maxEigenValue);

        //kmeans
        Kmeans kmeans = new Kmeans(subMatrix.transpose());
        kmeans.runKmeans(k, epsilon, repetitions);

        //fix index to graph index
        this.classification = fixIndexOfClassificationMap(kmeans.getClassification());
        this.inverseClassification = fixIndexOfInverseClassificationMap(kmeans.getInverseClassification());
        return inverseClassification;
    }

    public Map<Integer, List<Integer>> clusteringJama(int k, Function<Double, Double> similarityMeasure, double epsilon, int repetitions) {
        k = Integer.max(k, 1);
        //compute laplacian matrix
        Matrix W = getWeightMatrix(similarityMeasure);
        Matrix D = getDegreeMatrix(W);
        Matrix laplacianMatrix = Matrix.scalarProd(0.5, Matrix.diff(D, W));

        //compute eigenVectors
        int rows = laplacianMatrix.getRows();
        int maxEigenValue = Integer.min(k + 1, rows);
        EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(new Jama.Matrix(laplacianMatrix.getMatrix()));

        Matrix U = new Matrix(eigenvalueDecomposition.getV().getArray());
        this.eigenCoeff = U;
        Matrix subMatrix = U.getSubMatrix(1, U.getRows(), 2, maxEigenValue);

        //kmeans
        Kmeans kmeans = new Kmeans(subMatrix.transpose());
        kmeans.runKmeans(k, epsilon, repetitions);

        //fix index to graph index
        this.classification = fixIndexOfClassificationMap(kmeans.getClassification());
        this.inverseClassification = fixIndexOfInverseClassificationMap(kmeans.getInverseClassification());
        return inverseClassification;
    }

    /**
     * returns a map where key is the number of the class a the value is the segmented graph with similarity function on the  edges
     *
     * @return the graph
     */
    public Map<Integer, Graph> getclusteredGraph() {
        if (inverseClassification == null) {
            return null;
        }
        Map<Integer, Graph> map = new HashMap<>(inverseClassification.size());
        for (Map.Entry<Integer, List<Integer>> entry : inverseClassification.entrySet()) {
            Integer kclass = entry.getKey();
            Graph kgraph = segmentGraph(kclass);
            map.put(kclass, kgraph);
        }
        return map;
    }

    private Graph segmentGraph(Integer kclass) {
        String visitedPropertyName = "visited";
        Graph kgraph = new Graph();
        Stack<Integer> stack = new Stack<>();

        Integer initial = inverseClassification.get(kclass).get(0);
        stack.push(initial);
        graph.putVertexProperty(initial, visitedPropertyName, true);

        for (Integer u : graph.getVertexSet()) {
            graph.putVertexProperty(u, visitedPropertyName, false);
        }
        //breath-first visit algorithm
        while (!stack.empty()) {
            Integer u = stack.pop();
            for (Integer v : graph.getAdjVertex(u)) {
                if (graph.getVertexProperty(v, CLASS_VERTEX_PROPERTY) != kclass) {
                    continue;
                }
                if (!((boolean) graph.getVertexProperty(v, visitedPropertyName))) {
                    stack.push(v);
                    graph.putVertexProperty(u, visitedPropertyName, true);
                }
                kgraph.addEdge(u, v);
                Pair<Integer, Integer> pair = new Pair<>(u, v);
                kgraph.putEdgeProperty(pair, Graph.EDGE_WEIGHT_KEY, graph.getEdgeProperty(pair, Graph.EDGE_WEIGHT_KEY));
            }
        }
        return kgraph;
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

    /**
     * Gets graph.
     *
     * @return the graph
     */
    public KnnGraph getGraph() {
        return graph;
    }

    /**
     * Gets eigen coeff.
     *
     * @return the eigen coeff
     */
    public Matrix getEigenCoeff() {
        return eigenCoeff;
    }

    public Map<Integer, Integer> getClassification() {
        return classification;
    }


    private Map<Integer, List<Integer>> fixIndexOfInverseClassificationMap(Map<Integer, List<Integer>> map) {
        Integer[] keyIndex = this.graph.getKeyIndex();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer kclass = entry.getKey();
            for (Integer index : entry.getValue()) {
                graph.putVertexProperty(keyIndex[index], CLASS_VERTEX_PROPERTY, kclass);
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            List<Integer> value = entry.getValue();
            for (int i = 0; i < value.size(); i++) {
                value.set(i, keyIndex[value.get(i)]);
            }
        }
        return map;
    }

    private Map<Integer, Integer> fixIndexOfClassificationMap(Map<Integer, Integer> map) {
        Integer[] keyIndex = this.graph.getKeyIndex();
        Map<Integer, Integer> ansMap = new HashMap<>(map.size());
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Integer key = entry.getKey();
            ansMap.put(keyIndex[key], entry.getValue());
        }
        return ansMap;
    }
}
