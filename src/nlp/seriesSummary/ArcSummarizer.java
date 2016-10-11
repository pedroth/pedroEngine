package nlp.seriesSummary;

import algebra.src.DistanceMatrix;
import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import graph.Graph;
import graph.KnnGraph;
import graph.RandomWalkGraph;
import graph.SpectralClustering;
import inputOutput.TextIO;
import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import nlp.lowbow.src.eigenLowbow.SummaryGenLowBowManager;
import nlp.lowbow.src.symbolSampler.TopKSymbol;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.NecessaryWordPredicate;
import nlp.utils.SegmentedBowHeat;
import nlp.utils.Simplex;
import numeric.src.Distance;
import utils.FilesCrawler;
import utils.StopWatch;

import java.io.File;
import java.util.*;

/**
 * The type Arc summarizer.
 */
public class ArcSummarizer extends SeriesSummarization {
    /**
     * The constant distanceByNameMap.
     */
    public static Map<String, Distance<Vector>> distanceByNameMap = new HashMap<>(3);
    /**
     * The constant simplexDist.
     */
    public static Distance<Vector> simplexDist = (x, y) -> {
        double acc = 0;
        for (int i = 1; i <= x.getDim(); i++) {
            acc += Math.sqrt(x.getX(i) * y.getX(i));
        }
        return Math.acos(acc);
    };
    /**
     * The constant cosineDist.
     */
    public static Distance<Vector> cosineDist = (x, y) -> {
        return 1 - Vector.innerProd(x, y) / (x.norm() * y.norm());
    };
    /**
     * The constant euclideanDist.
     */
    public static Distance<Vector> euclideanDist = (x, y) -> {
        return Vector.diff(x, y).norm();
    };

    static {
        distanceByNameMap.put("simplex", simplexDist);
        distanceByNameMap.put("euclidean", euclideanDist);
        distanceByNameMap.put("cosine", cosineDist);
    }

    private List<String> log = new ArrayList<>();
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
    public ArcSummarizer(String seriesAddress, String videoExtension, double heat, double entropy, int knn, int kcluster, Distance<Vector> histogramDistance) {
        super(seriesAddress, videoExtension);
        this.heat = heat;
        this.entropy = entropy;
        this.knn = knn;
        this.kcluster = kcluster;
        this.histogramDistance = histogramDistance;
    }

    /**
     * Gets distance by name.
     *
     * @param name the name
     * @return the distance by name
     */
    public static Distance<Vector> getDistanceByName(String name) {
        return distanceByNameMap.get(name.toLowerCase());
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        ArcSummarizer arcSummarizer = new ArcSummarizer("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "mp4", 0.025, 0.1, 5, 20, ArcSummarizer.simplexDist);
        arcSummarizer.buildSummary("C:/pedro/escolas/ist/Tese/Series/BreakingBad/summary", 10);
    }

    @Override
    public void buildSummary(String outputAddress, double timeLengthMinutes) {
        try {
            TextIO textIO = new TextIO();
            outputAddress += ('/' == outputAddress.charAt(outputAddress.length() - 1) ? "" : "/");
            creatDirs(outputAddress);

            StopWatch stopWatch = new StopWatch();
            String seriesAddress = this.getSeriesAddress();
            List<String> subtitles = FilesCrawler.listFilesWithExtension(seriesAddress, SUBTITLE_EXTENSION);
            Collections.sort(subtitles);
            List<String> videos = FilesCrawler.listFilesWithExtension(seriesAddress, this.getVideoExtension());
            Collections.sort(videos);
            log.add("Read files : " + stopWatch.getEleapsedTime());
            System.out.println("Read files : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //construct managers
            SummaryGenLowBowManager<LowBowSubtitles> lowBowManager = new SummaryGenLowBowManager<>();
            BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

            TextIO text = new TextIO();
            SubsSplitter textSplitter = new SubsSplitter();

            //bow representation
            for (String subtitle : subtitles) {
                text.read(subtitle);
                bowManager.add(new Bow(text.getText(), textSplitter));
            }
            bowManager.build();

            //build predicate
            NecessaryWordPredicate predicate = new NecessaryWordPredicate(bowManager, entropy);
            log.add("NecessaryWordPredicate built: " + stopWatch.getEleapsedTime());
            System.out.println("NecessaryWordPredicate built: " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            textIO.write(outputAddress + "removedWords.txt", predicate.getNotNecessaryWordString());

            //lowbow representation
            for (int i = 0; i < subtitles.size(); i++) {
                text.read(subtitles.get(i));
                textSplitter = new SubsSplitter(predicate);
                lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
            }
            log.add("Lowbow for each episode: " + stopWatch.getEleapsedTime());
            System.out.println("Lowbow for each episode: " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //heat model
            lowBowManager.buildModel(heat);
            log.add("Lowbow heat flow done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Lowbow heat flow done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //build segmentation
            lowBowManager.buildSegmentations();
            List<SegmentedBowHeat> segmentedBows = lowBowManager.getSegmentedBows();
            Collections.sort(segmentedBows);
            log.add("Segmentation done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Segmentation done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //build knn-graph
            DistanceMatrix distanceMatrix = lowBowManager.getDistanceMatrixOfSegmentations(histogramDistance);
            KnnGraph graph = new KnnGraph(distanceMatrix, knn);
            log.add("knn graph done!! : " + stopWatch.getEleapsedTime());
            System.out.println("knn graph done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            textIO.write(outputAddress + "segmentGraph.txt", graph.toStringGephi());

            //clustering
            SpectralClustering spectralClustering = new SpectralClustering(graph);
            Map<Integer, List<Integer>> dataToClass = spectralClustering.clustering(kcluster);
            Map<Integer, Graph> map = spectralClustering.getclusteredGraph();
            log.add("Spectral clustering done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Spectral clustering done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //summarize
            randomWalkSummary(segmentedBows, map, timeLengthMinutes, outputAddress);
            log.add("Summary done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Summary done!! : " + stopWatch.getEleapsedTime());
            log.add("<FINISH>");
        } catch (Exception e) {
            log.add(e.getMessage());
        }
    }

    private void randomWalkSummary(List<SegmentedBowHeat> segmentedBows, Map<Integer, Graph> map, double timeLengthMinutes, String outputAddress) {
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
                SegmentedBowHeat segmentedBow = segmentedBows.get(keyIndex[permutation[k--]] - 1);
                double timeIntervalMinutes = segmentedBow.getTimeIntervalMinutes();
                if (acc + timeIntervalMinutes < timeLengthMinutes) {
                    segmentedBow.cutSegment(auxAddress + parseVideoAddress(segmentedBow.getVideoAddress()) + segmentedBow.getInterval() + ".mp4");
                    stringBuilder.append(topKSymbol.nextSymbol(segmentedBow.getSegmentBow(), simplex)).append("\n");
                    acc += timeIntervalMinutes;
                }
            }
            TextIO text = new TextIO();
            text.write(auxAddress + key + ".txt", stringBuilder.toString());
        }
    }

    /**
     * Gets log.
     *
     * @return the log
     */
    public List<String> getLog() {
        return log;
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
