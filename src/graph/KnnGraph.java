package graph;


import algebra.src.DistanceMatrix;
import numeric.src.MyMath;

import java.util.PriorityQueue;

public class KnnGraph extends Graph {
    private int k;
    private DistanceMatrix distanceMatrix;

    public KnnGraph(DistanceMatrix distanceMatrix, int k) {
        this.distanceMatrix = distanceMatrix;
        this.k = (int) MyMath.clamp(k, 0, distanceMatrix.getRows() - 1);
        buildKnn();
    }

    private void buildKnn() {
        PriorityQueue<IndexPair> heap;
        int n = distanceMatrix.getRows();
        for (int j = 1; j < n; j++) {
            heap = new PriorityQueue<>();
            for (int i = j + 1; i <= n; i++) {
                IndexPair indexPair = new IndexPair(i, distanceMatrix.getXY(i, j));
                heap.add(indexPair);
            }
            for (int l = 0; l < k && l < heap.size(); l++) {
                int i = heap.poll().getI();
                addEdge(j, i);
                addEdge(i, j);
            }
        }
    }

    private class IndexPair implements Comparable<IndexPair> {
        private int i;
        private double x;

        public IndexPair(int i, double x) {
            this.i = i;
            this.x = x;
        }

        @Override
        public int compareTo(IndexPair o) {
            return Double.compare(this.x, o.getX());
        }

        public int getI() {
            return i;
        }

        public double getX() {
            return x;
        }
    }
}
