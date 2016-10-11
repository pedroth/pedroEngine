package graph;

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
        return clustering(k, (x) -> Math.exp(-x * x), 1E-7, 1000);
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
        Matrix W = getWeightMatrix(similarityMeasure);
        Matrix D = getDegreeMatrix(W);
        Matrix laplacianMatrix = Matrix.scalarProd(0.5, Matrix.diff(D, W));
        SymmetricEigen symmetricEigen = new SymmetricEigen(laplacianMatrix);
        symmetricEigen.computeEigen(epsilon, Integer.min(k + 1, laplacianMatrix.getRows()), new HyperEigenAlgo());
        Matrix U = new Matrix(symmetricEigen.getEigenVectors());
        this.eigenCoeff = U;
        Kmeans kmeans = new Kmeans(U.getSubMatrix(1, U.getRows(), 2, U.getColumns()).transpose());
        kmeans.runKmeans(k, epsilon, repetitions);
        Map<Integer, List<Integer>> inverseClassification = kmeans.getInverseClassification();
        Integer[] keyIndex = this.graph.getKeyIndex();
        for (Map.Entry<Integer, List<Integer>> entry : inverseClassification.entrySet()) {
            Integer kclass = entry.getKey();
            for (Integer index : entry.getValue()) {
                graph.putVertexProperty(keyIndex[index], CLASS_VERTEX_PROPERTY, kclass);
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : inverseClassification.entrySet()) {
            List<Integer> value = entry.getValue();
            for (int i = 0; i < value.size(); i++) {
                value.set(i, value.get(i) + 1);
            }
        }
        this.inverseClassification = inverseClassification;
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
}
