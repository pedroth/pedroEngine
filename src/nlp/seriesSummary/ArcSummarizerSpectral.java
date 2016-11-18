package nlp.seriesSummary;

import algebra.src.Vector;
import graph.Graph;
import graph.SpectralClustering;
import numeric.src.Distance;

import java.util.List;
import java.util.Map;

/**
 * The type Arc summarizer.
 */
public class ArcSummarizerSpectral extends BaseArcSummarizer {
    private double sigma = 1.0;
    private boolean isNormalized = true;
    private SpectralClustering spectralClustering;

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
    public ArcSummarizerSpectral(String seriesAddress, String videoExtension, double heat, double entropy, int knn, int kcluster, Distance<Vector> histogramDistance) {
        super(seriesAddress, videoExtension, heat, entropy, knn, kcluster, histogramDistance);
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        ArcSummarizerSpectral arcSummarizerSpectral = new ArcSummarizerSpectral("C:/pedro/escolas/ist/Tese/Series/MrRobot/", "mkv", 0.045, 0.1, 3, 6, ArcSummarizerSpectral.simplexDist);
        arcSummarizerSpectral.buildSummary("C:/pedro/escolas/ist/Tese/Series/MrRobot/summary3", 10);
    }

    @Override
    protected Map<Integer, Graph> getClusteredGraph() {
        return this.spectralClustering.getClusteredGraph();
    }

    @Override
    public Map<Integer, List<Integer>> clusterArcs(int kcluster) {
        this.spectralClustering = new SpectralClustering(knnGraph);
        this.spectralClustering.setNormalized(isNormalized);
        return this.spectralClustering.clusteringJama(kcluster, (x) -> Math.exp(-(x * x) / (2 * sigma * sigma)), 1E-10, 500);
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    public boolean isNormalized() {
        return isNormalized;
    }

    public void setNormalized(boolean normalized) {
        isNormalized = normalized;
    }
}
