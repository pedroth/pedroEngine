package nlp.seriesSummary;


import algebra.src.Vector;
import graph.DiffusionClustering;
import graph.Graph;
import numeric.src.Distance;

import java.util.List;
import java.util.Map;

public class ArcSummarizerDiffusion extends BaseArcSummarizer {
    private DiffusionClustering diffusionClustering;
    private double sigma = 1.0;
    private double heatTime = 1.0;

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
        this.diffusionClustering = new DiffusionClustering(knnGraph);
        return this.diffusionClustering.clusteringJama(heatTime, kcluster, (x) -> Math.exp(-(x * x) / (2 * sigma * sigma)), 1E-10, 500);
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
}
