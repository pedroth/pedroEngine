package graph;


import algebra.src.Diagonal;
import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.function.Function;

/**
 * The type Graph laplacian.
 */
public class GraphLaplacian {
    private Graph graph;
    private Matrix W;
    private Diagonal D;
    private Matrix L;
    private Function<Double, Double> similarityMeasure = (x) -> Math.exp(-x * x);

    /**
     * Instantiates a new Graph laplacian.
     *
     * @param graph             the graph
     * @param similarityMeasure the similarity measure
     */
    public GraphLaplacian(Graph graph, Function<Double, Double> similarityMeasure) {
        this.graph = graph;
        this.similarityMeasure = similarityMeasure;
        this.W = getWeightMatrix();
        this.D = getDegreeMatrix(this.W);
    }

    /**
     * Gets graph.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Gets W. Matrix of similarities between vertices
     *
     * @return the w
     */
    public Matrix getW() {
        return W;
    }

    /**
     * Gets D matrix, which is a diagonal matrix where each D_{ii} = sum_j(W_{ij}) and D{ij} = 0 for i != j
     *
     * @return the d
     */
    public Diagonal getD() {
        return D;
    }

    /**
     * Gets similarity measure, which is a function that gets an distance and output a similarity measure
     *
     * @return the similarity measure
     */
    public Function<Double, Double> getSimilarityMeasure() {
        return similarityMeasure;
    }


    /**
     * Gets l. The Graph Laplacian Matrix
     *
     * @return the l
     */
    public Matrix getL() {
        if (L == null) {
            return Matrix.diff(D, W);
        }
        return L;
    }

    private Matrix getWeightMatrix() {
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
}
