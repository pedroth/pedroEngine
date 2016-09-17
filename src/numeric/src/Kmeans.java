package numeric.src;


import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Kmeans.
 */
public class Kmeans {

    private Vector[] data;
    private int[] classification;
    private Vector[] clusters;
    private Vector dataAverage;
    private Vector standardDeviation;

    /**
     * Instantiates a new Kmeans.
     *
     * @param data is a n by m matrix that corres
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
        this.data = new Matrix(data.toArray(new Vector[data.size()])).getVectorColumns();
    }

    public void runKmeans(int k) {
        double epsilon = 1E-3;
        double gradNorm;
        initClusters();
        do {
            List<List<Integer>> listOfIndex = classifyData(k);
            gradNorm = updateClusters(listOfIndex);
        } while (gradNorm > epsilon);
    }

    private double updateClusters(List<List<Integer>> listOfIndex) {
        return 0;
    }

    private List<List<Integer>> classifyData(int k) {
        List<List<Integer>> listOfIndex = new ArrayList<>(k);
        for (int i = 0; i < k; i++) {
            listOfIndex.add(new ArrayList<>());
        }

        return listOfIndex;
    }

    private void initClusters() {
        //
    }

    public int[] getClassification() {
        return classification;
    }

    public Vector[] getClusters() {
        return clusters;
    }
}
