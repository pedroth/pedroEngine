package nlp.seriesSummary;

import algebra.src.Vec2;
import algebra.src.Vector;
import graph.Graph;
import inputOutput.TextIO;
import javafx.util.Pair;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.utils.LdaWrapper;
import numeric.src.Distance;

import java.util.*;

/**
 * Almost a copy of arcSummarizer, need to abstract summarization method
 */
public class ArcSummarizerLda extends BaseArcSummarizer {
    private double alpha = 0.1;
    private double beta = 0.01;
    private int numberIterations = 10000;

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
    public ArcSummarizerLda(String seriesAddress, String videoExtension, double heat, double entropy, int knn, int kcluster, Distance<Vector> histogramDistance) {
        super(seriesAddress, videoExtension, heat, entropy, knn, kcluster, histogramDistance);
    }

    @Override
    protected Map<Integer, Graph> getClusteredGraph() {
        if (this.getSegmentIndexByClusterId() == null) {
            return null;
        }
        Map<Integer, Graph> map = new HashMap<>(this.getSegmentIndexByClusterId().size());
        for (Map.Entry<Integer, List<Integer>> entry : this.getSegmentIndexByClusterId().entrySet()) {
            Integer kclass = entry.getKey();
            Graph kgraph = segmentGraph(kclass);
            map.put(kclass, kgraph);
        }
        return map;
    }

    private Graph segmentGraph(Integer kclass) {
        Graph kgraph = new Graph();
        Stack<Integer> stack = new Stack<>();

        for (Integer index : this.segmentIndexByClusterId.get(kclass)) {
            stack.push(index);
        }

        while (!stack.empty()) {
            Integer u = stack.pop();
            kgraph.addVertex(u);
            for (Integer v : knnGraph.getAdjVertex(u)) {
                if (!this.segmentIndexByClusterId.get(kclass).contains(v)) {
                    continue;
                }
                kgraph.addEdge(u, v);
                Pair<Integer, Integer> pair = new Pair<>(u, v);
                kgraph.putEdgeProperty(pair, Graph.EDGE_WEIGHT_KEY, knnGraph.getEdgeProperty(pair, Graph.EDGE_WEIGHT_KEY));
            }
        }
        return kgraph;
    }

    private List<Vector> readMatrix(String address) {
        List<Vector> ans = new ArrayList<>();
        TextIO textIO = new TextIO();
        textIO.read(address);
        String[] split = textIO.getText().split("\n");
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split(" ");
            double[] splitdouble = new double[split1.length];
            for (int j = 0; j < split1.length; j++) {
                splitdouble[j] = Double.valueOf(split1[j]);
            }
            ans.add(new Vector(splitdouble));
        }
        return ans;
    }

    @Override
    public Map<Integer, List<Integer>> clusterArcs(int kcluster) {
        //build corpus
        StringBuilder stringBuilder = new StringBuilder();
        List<SubSegmentedBow> segmentedBows = this.getSegmentedBows();
        for (SubSegmentedBow baseSegmentedBow : segmentedBows) {
            stringBuilder.append(baseSegmentedBow.cutSegmentSubtitleWords()).append("\n");
        }
        //lda
        TextIO textIO = new TextIO();
        String address = this.outputAddress + "segmentsCorpus.txt";
        textIO.write(address, stringBuilder.toString());
        LdaWrapper.computeLda(address, kcluster, alpha, beta, numberIterations, "ldaModel");
        List<Vector> thetas = readMatrix(this.outputAddress + "ldaModel.theta");
        //cluster
        Map<Integer, List<Integer>> segmentIndexByClassificationLda = new HashMap<>();
        for (int i = 0; i < kcluster; i++) {
            segmentIndexByClassificationLda.put(i, new ArrayList<>());
        }

        // fix index
        Integer[] keyIndex = knnGraph.getKeyIndex();
        for (int i = 0; i < thetas.size(); i++) {
            Vec2 max = thetas.get(i).getMax();
            segmentIndexByClassificationLda.get((int) max.getY() - 1).add(keyIndex[i]);
        }
        return segmentIndexByClassificationLda;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public int getNumberIterations() {
        return numberIterations;
    }

    public void setNumberIterations(int numberIterations) {
        this.numberIterations = numberIterations;
    }

    @Override
    public String toString() {
        return "ArcSummarizerLda{" +
                "alpha=" + alpha +
                ", beta=" + beta +
                ", numberIterations=" + numberIterations +
                '}' + "\n" + super.toString();
    }
}
