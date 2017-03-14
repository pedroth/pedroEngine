package nlp.seriesSummary;


import algebra.src.DistanceMatrix;
import algebra.src.Vector;
import graph.DiffusionClustering;
import graph.Graph;
import numeric.src.Distance;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArcSummarizerDiffusion extends BaseArcSummarizer {
    private DiffusionClustering diffusionClustering;
    private double sigma;
    private double heatTime;
    private boolean isNormalized = false;

    /**
     * Instantiates a new Arc summarizer.
     *
     * @param seriesAddress     the series address
     * @param videoExtension    the video extension
     * @param heat              the heat
     * @param entropy           the entropy
     * @param knn               the knn
     * @param kcluster          the kcluster
     * @param histogramDistance the histogram distance
     */
    public ArcSummarizerDiffusion(String seriesAddress, String videoExtension, double heat, double entropy, int knn, int kcluster, Distance<Vector> histogramDistance) {
        super(seriesAddress, videoExtension, heat, entropy, knn, kcluster, histogramDistance);
    }

    @Override
    protected Map<Integer, Graph> getClusteredGraph() {
        return this.diffusionClustering.getClusteredGraph();
    }

    @Override
    public Map<Integer, List<Integer>> clusterArcs(int kcluster) {
        if (sigma == 0.0) {
            computeSigma();
        }
        if (heatTime == 0.0) {
            computeHeatTime();
        }
        this.diffusionClustering = new DiffusionClustering(knnGraph);
        this.diffusionClustering.setNormalized(isNormalized);
        return this.diffusionClustering.clusteringJama(heatTime, kcluster, (x) -> Math.exp(-(x * x) / (2 * sigma * sigma)), 1E-10, 500);
    }

    private void computeHeatTime() {

    }


    private void computeSigma() {
        DistanceMatrix distanceMatrix = knnGraph.getDistanceMatrix();
        Integer[] keyIndex = knnGraph.getKeyIndex();
        Map<Integer, Integer> inverseKeyIndex = knnGraph.getInverseKeyIndex();
        int rows = distanceMatrix.getRows();
        int samples = Integer.max(1, rows / 2);
        double acc = 0;
        for (int i = 0; i < samples; i++) {
            int randomIndex = (int) (Math.random() * rows);
            Set<Integer> adjVertex = knnGraph.getAdjVertex(keyIndex[randomIndex]);
            double acc2 = 0;
            for (Integer vertex : adjVertex) {
                acc2 += distanceMatrix.getXY(randomIndex + 1, inverseKeyIndex.get(vertex));
            }
            acc2 /= adjVertex.size();
            acc += acc2;
        }
        acc /= samples;
        sigma = acc;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public double getHeatTime() {
        return heatTime;
    }

    public void setHeatTime(double heatTime) {
        this.heatTime = heatTime;
    }

    public boolean isNormalized() {
        return isNormalized;
    }

    public void setNormalized(boolean normalized) {
        isNormalized = normalized;
    }

    @Override
    public String toString() {
        return "ArcSummarizerDiffusion{" +
                "diffusionClustering=" + diffusionClustering +
                ", sigma=" + sigma +
                ", heatTime=" + heatTime +
                ", isNormalized=" + isNormalized +
                '}';
    }
}
