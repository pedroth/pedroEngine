package nlp.seriesSummary;

import algebra.src.DistanceMatrix;
import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import graph.Graph;
import graph.KnnGraph;
import graph.RandomWalkGraph;
import graph.SpectralClustering;
import inputOutput.MyText;
import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import nlp.lowbow.src.eigenLowbow.SummaryGenLowBowManager;
import nlp.lowbow.src.symbolSampler.TopKSymbol;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.NecessaryWordPredicate;
import nlp.utils.SegmentedBow;
import nlp.utils.Simplex;
import numeric.src.Distance;
import utils.FilesCrawler;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArcSummarizer extends SeriesSummarization {
    public static Distance<Vector> simplexDist = (x, y) -> {
        double acc = 0;
        for (int i = 1; i <= x.getDim(); i++) {
            acc += Math.sqrt(x.getX(i) * y.getX(i));
        }
        return Math.acos(acc);
    };
    public static Distance<Vector> cosineDist = (x, y) -> {
        return 1 - Vector.innerProd(x, y) / (x.norm() * y.norm());
    };
    public static Distance<Vector> euclideanDist = (x, y) -> {
        return Vector.diff(x, y).norm();
    };
    // percentage of eigenvalues that are preserved after heat;
    private double heat;
    // (1 - percentage) of max entropy, cut words with high and low entropy in documents
    private double entropy;
    // number of neighbours in knn-graph
    private int knn;
    // number of cluster (number of arcs)
    private int kcluster;
    // distance of histograms
    private Distance<Vector> histogramDistance;

    public ArcSummarizer(String seriesAddress, String videoExtension, double heat, double entropy, int knn, int kcluster, Distance<Vector> histogramDistance) {
        super(seriesAddress, videoExtension);
        this.heat = heat;
        this.entropy = entropy;
        this.knn = knn;
        this.kcluster = kcluster;
        this.histogramDistance = histogramDistance;
    }

    public static void main(String[] args) {
        ArcSummarizer arcSummarizer = new ArcSummarizer("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv", 0.04, 0.25, 4, 5, ArcSummarizer.simplexDist);
        arcSummarizer.buildSummary("C:/Users/Pedroth/Desktop/cut/", 10);
    }

    @Override
    public void buildSummary(String outputAddress, double timeLengthMinutes) {
        String seriesAddress = this.getSeriesAddress();
        List<String> subtitles = FilesCrawler.listFilesWithExtension(seriesAddress, SUBTITLE_EXTENSION);
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension(seriesAddress, this.getVideoExtension());
        Collections.sort(videos);

        //construct managers
        SummaryGenLowBowManager<LowBowSubtitles> lowBowManager = new SummaryGenLowBowManager<>();
        BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

        MyText text = new MyText();
        SubsSplitter textSplitter = new SubsSplitter();

        //bow representation
        for (String subtitle : subtitles) {
            text.read(subtitle);
            bowManager.add(new Bow(text.getText(), textSplitter));
        }
        bowManager.build();

        //build predicate
        NecessaryWordPredicate predicate = new NecessaryWordPredicate(bowManager, entropy);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        //heat model
        lowBowManager.buildModel(heat);

        //build segmentation
        lowBowManager.buildSegmentations();
        List<SegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Collections.sort(segmentedBows);

        //build knn-graph
        DistanceMatrix distanceMatrix = lowBowManager.getDistanceMatrixOfSegmentations(histogramDistance);
        KnnGraph graph = new KnnGraph(distanceMatrix, knn);

        //clustering
        SpectralClustering spectralClustering = new SpectralClustering(graph);
        Map<Integer, List<Integer>> dataToClass = spectralClustering.clustering(kcluster);
        Map<Integer, Graph> map = spectralClustering.getclusteredGraph();

        //summarize
        randomWalkSummary(segmentedBows, map, timeLengthMinutes, outputAddress);

    }

    private void randomWalkSummary(List<SegmentedBow> segmentedBows, Map<Integer, Graph> map, double timeLengthMinutes, String outputAddress) {
        outputAddress += ('/' == outputAddress.charAt(outputAddress.length() - 1) ? "" : "/");
        Simplex simplex = segmentedBows.get(0).getLowBowSubtitles().getSimplex();
        for (Integer key : map.keySet()) {
            Graph graphClass = map.get(key);

            //random walk
            RandomWalkGraph randomWalkGraph = new RandomWalkGraph(graphClass);
            int numVertex = graphClass.getNumVertex();
            Vector v = new Vector(numVertex);
            v.fill(1.0 / numVertex);
            Vector stationaryDistribution = randomWalkGraph.getStationaryDistribution(v, 1E-3);

            // copy vector to Double[]
            int dim = stationaryDistribution.getDim();
            Double[] stationaryDistArray = new Double[dim];
            for (int i = 0; i < dim; i++) {
                stationaryDistArray[i] = stationaryDistribution.getX(i + 1);
            }
            //sort array
            QuickSortWithPermutation sort = new QuickSortWithPermutation();
            sort.sort(stationaryDistArray);
            int[] permutation = sort.getPermutation();

            //segments key
            Integer[] keyIndex = graphClass.getKeyIndex();

            // select most important segments under time constraint
            double acc = 0;
            int k = permutation.length - 1;
            String auxAddress = outputAddress;
            auxAddress += key + "/";
            creatDirs(auxAddress);
            StringBuilder stringBuilder = new StringBuilder();
            TopKSymbol topKSymbol = new TopKSymbol(30);
            while (acc < timeLengthMinutes && k > 0) {
                // keyIndex values are index values in {1, ..., n}
                SegmentedBow segmentedBow = segmentedBows.get(keyIndex[permutation[k--]] - 1);
                double timeIntervalMinutes = segmentedBow.getTimeIntervalMinutes();
                if (acc + timeIntervalMinutes < timeLengthMinutes) {
                    segmentedBow.cutSegment(auxAddress + parseVideoAddress(segmentedBow.getVideoAddress()) + segmentedBow.getInterval() + ".mp4");
                    stringBuilder.append(topKSymbol.nextSymbol(segmentedBow.getSegmentBow(), simplex)).append("\n");
                    acc += timeIntervalMinutes;
                }
            }
            MyText text = new MyText();
            text.write(auxAddress + key + ".txt", stringBuilder.toString());
        }
    }

    private void creatDirs(String address) {
        File dirs = new File(address);
        dirs.mkdirs();
    }

    private String parseVideoAddress(String videoAddress) {
        String[] split = videoAddress.split("\\\\");
        String[] secondSplit = split[split.length - 1].split("\\.");
        return secondSplit[0];
    }
}
