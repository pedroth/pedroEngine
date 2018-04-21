package numeric;


import algebra.Diagonal;
import algebra.Matrix;
import algebra.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GaussianMixtureClustering {
    private int dataDim;
    private Vector[] data;
    // data index to classification map
    private Map<Integer, Integer> classification;
    // classification to list of index map
    private Map<Integer, List<Integer>> inverseClassification;
    private Vector[] clusters;
    private Diagonal[] covariances;
    // probability data be on cluster i;
    private Vector phi;

    /**
     * Instantiates a new Kmeans.
     *
     * @param data is a n by m matrix where each column represents a data point
     */
    public GaussianMixtureClustering(Matrix data) {
        this.data = data.getVectorColumns();
        this.dataDim = this.data[0].getDim();
    }

    /**
     * Instantiates a new Kmeans.
     *
     * @param data the data
     */
    public GaussianMixtureClustering(List<Vector> data) {
        this(new Matrix(data));
    }

    /**
     * Run kmeans.
     *
     * @param k the k
     */
    public void runClustering(int k, double epsilon) {
        double convError;
        initParameters(k);
        do {
            classifyData(k);
            Vector[] newClusters = updateParameters();
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

    private Vector[] updateParameters() {

        // init auxiliary variables
        Vector[] acc = new Vector[clusters.length];
        for (int i = 0; i < clusters.length; i++) {
            acc[i] = new Vector(dataDim);
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
        // finalize average computation and compute phi
        for (int i = 0; i < acc.length; i++) {
            if (inverseClassification.containsKey(i)) {
                double p = 1.0 / inverseClassification.get(i).size();
                acc[i] = Vector.scalarProd(p, acc[i]);
                phi.setX(i + 1, p);
            }
        }

        // init auxiliary variables
        Vector[] acc2 = new Vector[clusters.length];
        for (int i = 0; i < clusters.length; i++) {
            acc2[i] = new Vector(dataDim);
        }

        //compute variances
        for (int i = 0; i < covariances.length; i++) {
            List<Integer> dataIndex = inverseClassification.get(i);
            if (dataIndex == null) {
                continue;
            }
            for (Integer index : dataIndex) {
                Vector diff = Vector.diff(data[index], clusters[i]);
                diff.applyFunction((x) -> x * x);
                acc2[i] = Vector.add(acc2[i], diff);
            }
        }

        // finalize covariance computation
        for (int i = 0; i < acc2.length; i++) {
            if (inverseClassification.containsKey(i)) {
                double p = 1.0 / inverseClassification.get(i).size();
                acc2[i] = Vector.scalarProd(p, acc2[i]);
                covariances[i] = (Diagonal) Matrix.diag(acc2[i]);
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
        int maxIndex = -1;
        double maxProb = Double.MIN_VALUE;
        for (int i = 0; i < clusters.length; i++) {
            double prob = getProbOfClass(p, i);
            if (maxProb <= prob) {
                maxProb = prob;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private double getProbOfClass(Vector p, int k) {
        return phi.getX(k + 1) * MyMath.gaussian(p, clusters[k], covariances[k]);
    }


    private void initParameters(int k) {
        // Forgy initialization https://en.wikipedia.org/wiki/K-means_clustering
        clusters = new Vector[k];
        for (int i = 0; i < k; i++) {
            Vector cluster = new Vector(data[((int) Math.floor(Math.random() * data.length))]);
            clusters[i] = cluster;
        }
        covariances = new Diagonal[k];
        for (int i = 0; i < k; i++) {
            covariances[i] = (Diagonal) Matrix.getIdentity(this.dataDim);
        }

        phi = new Vector(k);
        phi.fill(1.0 / k);
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
