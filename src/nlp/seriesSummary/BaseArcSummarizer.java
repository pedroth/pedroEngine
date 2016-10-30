package nlp.seriesSummary;


import algebra.src.DistanceMatrix;
import algebra.src.Matrix;
import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import graph.Graph;
import graph.KnnGraph;
import graph.RandomWalkGraph;
import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import nlp.lowbow.eigenLowbow.SummaryGenLowBowManager;
import nlp.lowbow.symbolSampler.TopKSymbol;
import nlp.lowbow.symbolSampler.TopKSymbolWithProb;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowCool;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.NecessaryWordPredicate;
import numeric.src.Distance;
import utils.FilesCrawler;
import utils.StopWatch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public abstract class BaseArcSummarizer extends SeriesSummarization {
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

    protected List<String> log = new ArrayList<>();
    // percentage of eigenvalues that are preserved after heat;
    protected double heat;
    // (1 - percentage) of max entropy, cut words with high and low entropy in documents
    protected double entropy;
    // number of neighbours in knn-graph
    protected int knn;
    // number of cluster (number of arcs)
    protected int kcluster;
    // distance of histograms
    protected Distance<Vector> histogramDistance;
    protected List<BaseSegmentedBow> segmentedBows;
    protected Map<Integer, Graph> graphByClusterIdMap;
    protected Map<Integer, Vector> randomWalkDistributionByClusterIdMap;
    protected SummaryGenLowBowManager<LowBowSubtitles> lowBowManager;
    protected Map<Integer, List<Integer>> segmentIndexByClusterId;
    protected KnnGraph knnGraph;
    protected String outputAddress;
    protected NecessaryWordPredicate necessaryWordPredicate;
    protected boolean cutVideo = true;

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
    public BaseArcSummarizer(String seriesAddress, String videoExtension, double heat, double entropy, int knn, int kcluster, Distance<Vector> histogramDistance) {
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
        ArcSummarizer arcSummarizer = new ArcSummarizer("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "mp4", 0.045, 0.1, 5, 10, ArcSummarizer.simplexDist);
        arcSummarizer.buildSummary("C:/pedro/escolas/ist/Tese/Series/BreakingBad/summary", 5);
    }

    @Override
    public void buildSummary(String outputAddress, double timeLengthMinutes) {
        try {
            this.outputAddress = outputAddress;
            TextIO textIO = new TextIO();
            this.outputAddress += ('/' == this.outputAddress.charAt(outputAddress.length() - 1) ? "" : "/");
            FilesCrawler.creatDirs(outputAddress);

            //read files
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
            this.lowBowManager = new SummaryGenLowBowManager<>();
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
            this.necessaryWordPredicate = new NecessaryWordPredicate(bowManager, entropy);
            log.add("NecessaryWordPredicate built: " + stopWatch.getEleapsedTime());
            System.out.println("NecessaryWordPredicate built: " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            textIO.write(this.outputAddress + "removedWords.txt", this.necessaryWordPredicate.getNotNecessaryWordString());

            if (videos.size() == 0 || subtitles.size() == 0) throw new RuntimeException("no video or subtitle files");

            //lowbow representation
            for (int i = 0; i < subtitles.size(); i++) {
                text.read(subtitles.get(i));
                textSplitter = new SubsSplitter(this.necessaryWordPredicate);
                this.lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
            }
            log.add("Lowbow for each episode: " + stopWatch.getEleapsedTime());
            System.out.println("Lowbow for each episode: " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //heat model
            this.lowBowManager.buildModel(heat);
            log.add("Lowbow heat flow done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Lowbow heat flow done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //build segmentation
            this.lowBowManager.buildSegmentations(SegmentedBowCool::new);
            this.segmentedBows = this.lowBowManager.getSegmentedBows();
            Collections.sort(segmentedBows);
            log.add("Segmentation done!! : " + stopWatch.getEleapsedTime() + ",  number of segments : " + segmentedBows.size());
            System.out.println("Segmentation done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //build knn-graph
            DistanceMatrix distanceMatrix = this.lowBowManager.getDistanceMatrixOfSegmentations(histogramDistance);
            this.knnGraph = new KnnGraph(distanceMatrix, knn);
            log.add("knn graph done!! : " + stopWatch.getEleapsedTime());
            System.out.println("knn graph done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            textIO.write(this.outputAddress + "segmentGraph.txt", knnGraph.toStringGephi());

            //clustering
            segmentIndexByClusterId = clusterArcs(kcluster);
            for (Map.Entry<Integer, List<Integer>> entry : segmentIndexByClusterId.entrySet()) {
                String clusterSizeString = "cluster " + entry.getKey() + " size:  " + entry.getValue().size();
                log.add(clusterSizeString);
                System.out.println(clusterSizeString);
            }

            graphByClusterIdMap = getClusteredGraph();
            log.add("Clustering done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Clustering done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            //summarize
            randomWalkSummary(segmentedBows, graphByClusterIdMap, timeLengthMinutes, this.outputAddress);

            topWordsPrint(20);

            log.add("Summary done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Summary done!! : " + stopWatch.getEleapsedTime());
            log.add("FINISH");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            e.printStackTrace();
            log.add(sw.toString());
        }
    }

    private void topWordsPrint(int k) {
        TextIO textIO = new TextIO();
        int dim = segmentedBows.get(0).getSegmentBow().getDim();

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        for (Map.Entry<Integer, List<Integer>> integerListEntry : segmentIndexByClusterId.entrySet()) {
            Integer clusterId = integerListEntry.getKey();
            Vector acc = new Vector(dim);

//            Vector pdf = randomWalkDistributionByClusterIdMap.get(clusterId);
//            Graph graph = graphByClusterIdMap.get(clusterId);
//            Map<Integer, Integer> inverseKeyIndex = graph.getInverseKeyIndex();

            List<Integer> segmentIndex = integerListEntry.getValue();
            for (Integer i : segmentIndex) {
                acc = Vector.add(segmentedBows.get(i - 1).getSegmentBow(), acc);
            }
            acc = Vector.scalarProd(1.0 / segmentIndex.size(), acc);

            Matrix row = new Matrix(acc).transpose();
            stringBuilder2.append(row).append("\n");
            TopKSymbolWithProb topKSymbolWithProb = new TopKSymbolWithProb(k);
            stringBuilder.append("cluster ").append(clusterId).append(": ").append(topKSymbolWithProb.nextSymbol(acc, lowBowManager.getSimplex())).append("\n\n");
        }
        textIO.write(outputAddress + "TopWords.txt", stringBuilder.toString());
        textIO.write(outputAddress + "GraphCentroid.txt", stringBuilder2.toString());
    }

    protected abstract Map<Integer, Graph> getClusteredGraph();

    public abstract Map<Integer, List<Integer>> clusterArcs(int kcluster);

    private void randomWalkSummary(List<BaseSegmentedBow> segmentedBows, Map<Integer, Graph> map, double timeLengthMinutes, String outputAddress) {
        for (Integer key : map.keySet()) {
            Graph graphClass = map.get(key);

            //random walk
            int[] permutation = getRandomWalkRankPermutation(key, graphClass);
            if (permutation == null) {
                continue;
            }
            // select most important segments under time constraint
            cutVideoUnderConstraint(permutation, outputAddress, key, timeLengthMinutes, graphClass);
        }
    }

    private void cutVideoUnderConstraint(int[] permutation, String outputAddress, int clusterId, double timeLengthMinutes, Graph graphClass) {
        // vertexIdByIndex values are index values in {1, ..., n}
        Integer[] vertexIdByIndex = graphClass.getKeyIndex();

        double acc = 0;
        int k = permutation.length - 1;

        String auxAddress = outputAddress;
        auxAddress += clusterId + "/";
        FilesCrawler.creatDirs(auxAddress);

        StringBuilder stringBuilder = new StringBuilder();
        TopKSymbol topKSymbol = new TopKSymbol(30);
        while (acc < timeLengthMinutes && k > 0) {
            BaseSegmentedBow segmentedBow = segmentedBows.get(vertexIdByIndex[permutation[k--]] - 1);
            double timeIntervalMinutes = segmentedBow.getTimeIntervalMinutes();
            if (acc + timeIntervalMinutes < timeLengthMinutes) {
                if (cutVideo) {
                    segmentedBow.cutSegment(auxAddress + parseVideoAddress(segmentedBow.getVideoAddress()) + segmentedBow.getInterval().toString().replace(" ", "") + ".mp4");
                }
                stringBuilder.append(topKSymbol.nextSymbol(segmentedBow.getSegmentBow(), this.lowBowManager.getSimplex())).append("\n");
                acc += timeIntervalMinutes;
            }
        }
        //Write top k symbols from the selected segments
        TextIO text = new TextIO();
        text.write(auxAddress + clusterId + ".txt", stringBuilder.toString());

        //Write words from each segment in the cluster
        stringBuilder = new StringBuilder();
        for (Integer index : vertexIdByIndex) {
            stringBuilder.append(segmentedBows.get(index - 1).cutSegmentSubtitleWords()).append("\n");
        }
        text.write(auxAddress + "segmentsCorpus.txt", stringBuilder.toString());
    }

    private int[] getRandomWalkRankPermutation(Integer clusterId, Graph graphClass) {
        if (randomWalkDistributionByClusterIdMap == null) {
            randomWalkDistributionByClusterIdMap = new HashMap<>();
        }

        RandomWalkGraph randomWalkGraph = new RandomWalkGraph(graphClass);
        int numVertex = graphClass.getNumVertex();
        if (numVertex == 0) {
            return null;
        }
        Vector v = new Vector(numVertex);
        v.fill(1.0 / numVertex);
        Vector stationaryDistribution = randomWalkGraph.getStationaryDistribution(v, 0.8, 1E-5);

        randomWalkDistributionByClusterIdMap.put(clusterId, stationaryDistribution);

        // copy vector to Double[]
        int dim = stationaryDistribution.getDim();
        Double[] stationaryDistArray = new Double[dim];
        for (int i = 0; i < dim; i++) {
            stationaryDistArray[i] = stationaryDistribution.getX(i + 1);
        }
        //sort array
        QuickSortWithPermutation sort = new QuickSortWithPermutation();
        sort.sort(stationaryDistArray);
        return sort.getPermutation();
    }

    /**
     * Gets log.
     *
     * @return the log
     */
    public List<String> getLog() {
        return log;
    }

    /**
     * Gets graph by cluster id map.
     *
     * @return the graph by cluster id map
     */
    public Map<Integer, Graph> getGraphByClusterIdMap() {
        return graphByClusterIdMap;
    }

    /**
     * Gets segmented bows.
     *
     * @return the segmented bows
     */
    public List<BaseSegmentedBow> getSegmentedBows() {
        return segmentedBows;
    }

    public Map<Integer, List<Integer>> getSegmentIndexByClusterId() {
        return segmentIndexByClusterId;
    }

    private String parseVideoAddress(String videoAddress) {
        String[] split = videoAddress.split("\\\\");
        String[] secondSplit = split[split.length - 1].split("\\.");
        return secondSplit[0];
    }

    public SummaryGenLowBowManager<LowBowSubtitles> getLowBowManager() {
        return lowBowManager;
    }

    public KnnGraph getKnnGraph() {
        return knnGraph;
    }

    public Distance<Vector> getTfIdfDistance() {
        return (x, y) -> {
            Vector wordEntropy = this.necessaryWordPredicate.getWordProbForSimplex(lowBowManager.getSimplex());
            wordEntropy = new Vector(wordEntropy.applyFunction((z) -> -Math.log(z)));
            Vector xbow = Vector.pointMult(x, wordEntropy);
            Vector ybow = Vector.pointMult(y, wordEntropy);
            return 1 - (Vector.innerProd(xbow, ybow) / (xbow.norm() * ybow.norm()));
        };
    }

    public Distance<Vector> getHistogramDistance() {
        return histogramDistance;
    }

    public void setHistogramDistance(Distance<Vector> histogramDistance) {
        this.histogramDistance = histogramDistance;
    }

    public boolean isCutVideo() {
        return cutVideo;
    }

    public void setCutVideo(boolean cutVideo) {
        this.cutVideo = cutVideo;
    }
}
