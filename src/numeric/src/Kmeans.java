package numeric.src;


import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Kmeans.
 */
public class Kmeans {
    private Vector[] data;
    // data index to classification map
    private Map<Integer, Integer> classification;
    // classification to list of index map
    private Map<Integer, List<Integer>> inverseClassification;
    private Vector[] clusters;

    /**
     * Instantiates a new Kmeans.
     *
     * @param data is a n by m matrix where each column represents a data point
     */
    public Kmeans(Matrix data) {
        this.data = data.getVectorColumns();
    }

    /**
     * Instantiates a new Kmeans.
     *
     * @param data the data
     */
    public Kmeans(List<Vector> data) {
        this.data = new Matrix(data).getVectorColumns();
    }

    /**
     * Run kmeans.
     *
     * @param k the k
     */
    public void runKmeans(int k) {
        double epsilon = 1E-3;
        double convError;
        initClusters(k);
        do {
            classifyData(k);
            Vector[] newClusters = updateClusters();
            convError = getConvergenceError(newClusters);
            clusters = newClusters;
        } while (convError > epsilon);
    }

    private double getConvergenceError(Vector[] newClusters) {
        double convergenceError = 0;
        for (int i = 0; i < newClusters.length; i++) {
            convergenceError += Vector.diff(newClusters[i], clusters[i]).norm();
        }
        return convergenceError;
    }

    private Vector[] updateClusters() {
        int dim = clusters[0].getDim();
        // init auxiliary variables
        Vector[] acc = new Vector[clusters.length];
        for (int i = 0; i < clusters.length; i++) {
            acc[i] = new Vector(dim);
        }
        //compute average of each cluster
        for (int i = 0; i < clusters.length; i++) {
            List<Integer> dataIndex = inverseClassification.get(i);
            if (dataIndex == null) {
                continue;
            }
            for (Integer index : dataIndex) {
                acc[i] = Vector.add(acc[i], data[index]);
            }
        }
        for (int i = 0; i < acc.length; i++) {
            if (inverseClassification.containsKey(i)) {
                acc[i] = Vector.scalarProd(1.0 / inverseClassification.get(i).size(), acc[i]);
            }
        }
        return acc;
    }

    private void classifyData(int k) {
        classification = new HashMap<>(data.length);
        inverseClassification = new HashMap<>(k);
        for (int i = 0; i < data.length; i++) {
            int pointClass = classifyPoint(data[i]);
            classification.put(i, pointClass);
            if (!inverseClassification.containsKey(pointClass)) {
                inverseClassification.put(pointClass, new ArrayList<>());
            }
            inverseClassification.get(pointClass).add(i);
        }
    }

    /**
     * Classify point.
     *
     * @param p the p
     * @return the int
     */
    public int classifyPoint(Vector p) {
        int minIndex = -1;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < clusters.length; i++) {
            double squareNorm = Vector.diff(p, clusters[i]).squareNorm();
            if (minDist >= squareNorm) {
                minDist = squareNorm;
                minIndex = i;
            }
        }
        return minIndex;
    }


    private void initClusters(int k) {
        // Forgy initialization https://en.wikipedia.org/wiki/K-means_clustering
        clusters = new Vector[k];
        for (int i = 0; i < k; i++) {
            Vector cluster = new Vector(data[((int) Math.floor(Math.random() * data.length))]);
            clusters[i] = cluster;
        }
    }

    /**
     * Gets classification.
     *
     * @return the classification map, where keys are data indexes (number between 0, ..., n-1) and the output is class classified by the Kmeans (number between 0, ..., k - 1)
     */
    public Map<Integer, Integer> getClassification() {
        return classification;
    }

    /**
     * Gets inverse classification map, where the key is the number that identifies a class (number between 0, ..., k-1) and the output is a list of indexes of data that are from that class
     *
     * @return the inverse classification map a class
     */
    public Map<Integer, List<Integer>> getInverseClassification() {
        return inverseClassification;
    }

    /**
     * Get clusters.
     *
     * @return the vector [ ]
     */
    public Vector[] getClusters() {
        return clusters;
    }
}
