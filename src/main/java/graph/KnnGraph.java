package graph;


import algebra.DistanceMatrix;
import numeric.Distance;
import numeric.MyMath;
import utils.Pair;

import java.util.List;
import java.util.PriorityQueue;

/**
 * The type Knn graph. this graph identifies vertex with numbers in {1, ..., |V|}
 *
 * @param <L> the type parameter
 */
public class KnnGraph<L> extends Graph {
    private int k;
    private DistanceMatrix distanceMatrix;

    /**
     * Instantiates a new Knn graph.
     *
     * @param distanceMatrix the distance matrix
     * @param k              the k
     */
    public KnnGraph(DistanceMatrix distanceMatrix, int k) {
        this.distanceMatrix = distanceMatrix;
        this.k = (int) MyMath.clamp(k, 0, distanceMatrix.getRows() - 1);
        buildKnn();
    }

    public KnnGraph(KnnGraph graph) {
        this(new DistanceMatrix(graph.getDistanceMatrix()), graph.getK());
    }

    /**
     * Instantiates a new Knn graph.
     *
     * @param data     the data
     * @param k        the k
     * @param distance the distance
     */
    public KnnGraph(List<L> data, int k, Distance<L> distance) {
        this.distanceMatrix = getDistanceMatrixFromData(data, distance);
        this.k = (int) MyMath.clamp(k, 0, distanceMatrix.getRows() - 1);
        buildKnn();
    }

    private DistanceMatrix getDistanceMatrixFromData(List<L> data, Distance<L> distance) {
        DistanceMatrix ans = new DistanceMatrix(data.size());
        int size = data.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                L xJ = data.get(j);
                L yI = data.get(i);
                ans.setXY(i + 1, j + 1, distance.dist(yI, xJ));
            }
        }
        return ans;
    }

    private void buildKnn() {
        PriorityQueue<IndexPair> heap;
        int n = distanceMatrix.getRows();
        for (int j = 1; j <= n; j++) {
            heap = new PriorityQueue<>();
            for (int i = 1; i <= n; i++) {
                if (i == j) {
                    continue;
                }
                IndexPair indexPair = new IndexPair(i, distanceMatrix.getXY(i, j));
                heap.add(indexPair);
            }
            for (int l = 0; l < k && l < heap.size(); l++) {
                int i = heap.poll().getI();
                addEdge(j, i);
                putEdgeProperty(new Pair<>(j, i), EDGE_WEIGHT_KEY, distanceMatrix.getXY(j, i));
                addEdge(i, j);
                putEdgeProperty(new Pair<>(i, j), EDGE_WEIGHT_KEY, distanceMatrix.getXY(i, j));
            }
        }
    }

    public int getK() {
        return k;
    }

    public DistanceMatrix getDistanceMatrix() {
        return distanceMatrix;
    }

    private class IndexPair implements Comparable<IndexPair> {
        private int i;
        private double x;

        /**
         * Instantiates a new Index pair.
         *
         * @param i the i
         * @param x the x
         */
        public IndexPair(int i, double x) {
            this.i = i;
            this.x = x;
        }

        @Override
        public int compareTo(IndexPair o) {
            return Double.compare(this.x, o.getX());
        }

        /**
         * Gets i.
         *
         * @return the i
         */
        public int getI() {
            return i;
        }

        /**
         * Gets x.
         *
         * @return the x
         */
        public double getX() {
            return x;
        }
    }
}
