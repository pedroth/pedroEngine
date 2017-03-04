package graph;


import Jama.EigenvalueDecomposition;
import algebra.src.Diagonal;
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
 * The type Diffusion clustering.
 */
public class DiffusionClustering {

    private final static String CLASS_VERTEX_PROPERTY = "class";
    private final KnnGraph graph;

    /**
     * The Heat time.
     */
    double heatTime;
    private Map<Integer, List<Integer>> inverseClassification;
    private Map<Integer, Integer> classification;
    private Matrix eigenVectors;
    private Vector eigenValues;
    private Matrix eigenEmbedding;
    private double reduceDimensionThreshold = 0.1;

    private boolean isNormalized = false;

    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public DiffusionClustering(KnnGraph graph) {
        this.graph = new KnnGraph(graph);
    }

    /**
     * Spectral clustering.
     *
     * @param heatTime the heat time, if heatTime < 0 then  heatTime = -log(epsilon)/ lambda(k+1) else heatTime = heatTime
     * @param k the k number of clusters
     * @param similarityMeasure the similarity measure
     * @param epsilon the epsilon
     * @param repetitions the repetitions
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
        eigenValueCopy.applyFunction(x -> Math.exp(-x * heatTime));

        int compressDim = eigenValues.size();

        for (int i = 1; i <= eigenValues.size(); i++) {
            if (eigenValues.getX(i) < reduceDimensionThreshold) {
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
        Kmeans kmeans = new Kmeans(prod);
        kmeans.runKmeans(k, epsilon, repetitions);

        //fix index to graph index
        this.classification = fixIndexOfClassificationMap(kmeans.getClassification());
        this.inverseClassification = fixIndexOfInverseClassificationMap(kmeans.getInverseClassification());
        return inverseClassification;
    }

    /**
     * Clustering jama.
     *
     * @param heatTime the heat time, if heatTime < 0 then  heatTime = -log(epsilon)/ lambda(k+1) else heatTime = heatTime
     * @param k the k
     * @param similarityMeasure the similarity measure
     * @param epsilon the epsilon
     * @param repetitions the repetitions
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

    /**
     * returns a map where key is the number of the class a the value is the segmented graph with similarity function on the  edges
     *
     * @return the graph
     */
    public Map<Integer, Graph> getClusteredGraph() {
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
        Graph kgraph = new Graph();
        Stack<Integer> stack = new Stack<>();

        for (Integer index : inverseClassification.get(kclass)) {
            stack.push(index);
        }

        while (!stack.empty()) {
            Integer u = stack.pop();
            kgraph.addVertex(u);
            kgraph.putVertexProperty(u, CLASS_VERTEX_PROPERTY, kclass);
            for (Integer v : graph.getAdjVertex(u)) {
                if (graph.getVertexProperty(v, CLASS_VERTEX_PROPERTY) != kclass) {
                    continue;
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
     * Gets classification.
     *
     * @return the classification
     */
    public Map<Integer, Integer> getClassification() {
        return classification;
    }


    private Map<Integer, List<Integer>> fixIndexOfInverseClassificationMap(Map<Integer, List<Integer>> map) {
        Integer[] keyIndex = this.graph.getKeyIndex();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            List<Integer> value = entry.getValue();
            for (int i = 0; i < value.size(); i++) {
                value.set(i, keyIndex[value.get(i)]);
            }
        }
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer kclass = entry.getKey();
            for (Integer index : entry.getValue()) {
                graph.putVertexProperty(index, CLASS_VERTEX_PROPERTY, kclass);
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
     * Gets reduce dimension threshold.
     *
     * @return the reduce dimension threshold
     */
    public double getReduceDimensionThreshold() {
        return reduceDimensionThreshold;
    }

    /**
     * Sets reduce dimension threshold.
     *
     * @param reduceDimensionThreshold the reduce dimension threshold
     */
    public void setReduceDimensionThreshold(double reduceDimensionThreshold) {
        this.reduceDimensionThreshold = reduceDimensionThreshold;
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
