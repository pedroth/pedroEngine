package nlp.seriesSummary;

import algebra.src.DistanceMatrix;
import algebra.src.Matrix;
import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import graph.Graph;
import graph.KnnGraph;
import graph.RandomWalkGraph;
import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.LowBowSegmentator;
import nlp.lowbow.eigenLowbow.LowBowSubtitles;
import nlp.lowbow.eigenLowbow.MaxDerivativeSegmentator;
import nlp.lowbow.eigenLowbow.SummaryGenLowBowManager;
import nlp.segmentedBow.sub.SegmentedBowCool;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.symbolSampler.TopKSymbol;
import nlp.symbolSampler.TopKSymbolWithProb;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.EntropyStopWordPredicate;
import nlp.utils.RemoveWordsPredicate;
import numeric.src.Distance;
import tokenizer.NumbersTokenizer;
import utils.CommandLineApi;
import utils.FFMpegVideoApi;
import utils.FilesCrawler;
import utils.StopWatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Base arc summarizer.
 */
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

    /**
     * The Log.
     */
    protected List<String> log = new ArrayList<>();
    /**
     * The Heat: percentage of eigenvalues that are preserved after heat
     */
    protected double heat;
    /**
     * The Entropy: (1 - percentage) of max entropy, cut words with high and low entropy in documents
     */
    protected double entropy;
    /**
     * The Knn: number of neighbours in knn-graph
     */
    protected int knn;
    /**
     * The Kcluster: number of cluster (number of arcs)
     */
    protected int kcluster;
    /**
     * The Histogram distance: distance of histograms
     */
    protected Distance<Vector> histogramDistance;
    /**
     * The Segmented bows.
     */
    protected List<SubSegmentedBow> segmentedBows;
    /**
     * The Graph by cluster id map.
     */
    protected Map<Integer, Graph> graphByClusterIdMap;
    /**
     * The Random walk distribution by cluster id map.
     */
    protected Map<Integer, Vector> randomWalkDistributionByClusterIdMap;
    /**
     * The Low bow manager.
     */
    protected SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager;
    /**
     * The Segment index by cluster id.
     */
    protected Map<Integer, List<Integer>> segmentIndexByClusterId;
    /**
     * The Knn graph.
     */
    protected KnnGraph knnGraph;
    /**
     * The Output address.
     */
    protected String outputAddress;
    /**
     * The Necessary word predicate.
     */
    protected RemoveWordsPredicate necessaryWordPredicate;
    /**
     * Entropy predicate
     */
    protected EntropyStopWordPredicate entropyPredicate;
    /**
     * The Cut video.
     */
    protected boolean cutVideo = true;
    /**
     * The Graph centroid by cluster id.
     */
    protected Map<Integer, Vector> graphCentroidByClusterId;
    /**
     * The Average segment length.
     */
    protected double averageSegmentLength;
    /**
     * The Standard deviation segment length.
     */
    protected double standardDeviationSegmentLength;
    /**
     * The Segment length data.
     */
    protected List<Double> segmentLengthData;

    /**
     * The Is video concat.
     */
    protected boolean isVideoConcat = false;
    /**
     * The Low bow segmentator.
     */
    protected LowBowSegmentator lowBowSegmentator = MaxDerivativeSegmentator.getInstance();
    Comparator<String> stringComparator = (o1, o2) -> {
        final String regex = "[Ss][0-9]+[Ee][0-9]+";
        String[] split1 = o1.split(regex);
        String[] split2 = o2.split(regex);
        if (split1.length <= 1 || split2.length <= 1) {
            return o1.compareTo(o2);
        }
        if (split1[0].equals(split2[0])) {
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher1 = pattern.matcher(o1);
            final Matcher matcher2 = pattern.matcher(o2);
            matcher1.find();
            matcher2.find();
            String group1 = matcher1.group(0);
            String group2 = matcher2.group(0);
            NumbersTokenizer number = new NumbersTokenizer(null);
            String[] token1 = number.tokenize(group1 + split1[split1.length - 1]);
            String[] token2 = number.tokenize(group2 + split2[split2.length - 1]);
            //warning assumes equal size on tokens variables (time is precious - lame excuse)
            for (int i = 0; i < token1.length; i++) {
                Integer integer1 = Integer.valueOf(token1[i]);
                Integer integer2 = Integer.valueOf(token2[i]);
                if (integer1 != integer2) {
                    return integer1 - integer2;
                }
            }
            return 0;
        }
        return split1[0].compareTo(split2[0]);
    };

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
        ArcSummarizerSpectral arcSummarizerSpectral =
                new ArcSummarizerSpectral("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "mp4", 0.045, 0.1, 5, 10, ArcSummarizerSpectral.simplexDist);
        arcSummarizerSpectral.buildSummary("C:/pedro/escolas/ist/Tese/Series/BreakingBad/summary", 5);
    }

    @Override
    public void buildSummary(String outputAddress, double timeLengthMinutes) {
        try {
            this.outputAddress = outputAddress;
            this.outputAddress += ('/' == this.outputAddress.charAt(outputAddress.length() - 1) ? "" : "/");
            final String infoAddress = outputAddress + "/info/";
            TextIO textIO = new TextIO();
            FilesCrawler.creatDirs(outputAddress);
            FilesCrawler.creatDirs(infoAddress);

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

            if (videos.size() == 0 || subtitles.size() == 0) {
                throw new RuntimeException("no video or subtitle files");
            }

            //construct managers
            this.lowBowManager = new SummaryGenLowBowManager<>();
            BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

            TextIO text = new TextIO();
            SubsSplitter textSplitter = new SubsSplitter();

            //build entropy predicate
            if (entropy > 0.0) {
                //bow representation
                for (String subtitle : subtitles) {
                    text.read(subtitle);
                    bowManager.add(new Bow(text.getText(), textSplitter));
                }
                bowManager.build();

                //build predicate
                EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, entropy);
                this.necessaryWordPredicate = predicate;
                this.entropyPredicate = predicate;
                log.add("EntropyStopWordPredicate built: " + stopWatch.getEleapsedTime());
                System.out.println("EntropyStopWordPredicate built: " + stopWatch.getEleapsedTime());
                stopWatch.resetTime();
            }

            textIO.write(infoAddress + "removedWords.txt", this.necessaryWordPredicate.getNotNecessaryWordString());

            //lowbow representation
            for (int i = 0; i < subtitles.size(); i++) {
                text.read(subtitles.get(i));
                textSplitter = this.necessaryWordPredicate == null ? new SubsSplitter() : new SubsSplitter(this.necessaryWordPredicate);
                LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i));
                this.lowBowManager.add(low);
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

            //segment statistics
            this.segmentLengthData = getSegmentLengthData();
            this.averageSegmentLength = computeAverageSegmentLength();
            this.standardDeviationSegmentLength = computeStandardDeviationLength();
            //rejectSegmentOutliers();

            //build knn-graph
            DistanceMatrix distanceMatrix = this.lowBowManager.getDistanceMatrixOfSegmentations(histogramDistance);
            this.knnGraph = new KnnGraph(distanceMatrix, knn < 1 ? (int) Math.log(distanceMatrix.getRows()) : knn);
            log.add("knn graph done!! : " + stopWatch.getEleapsedTime());
            System.out.println("knn graph done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            textIO.write(infoAddress + "segmentGraph.txt", knnGraph.toStringGephi());

            //clustering
            segmentIndexByClusterId = clusterArcs(kcluster);
            for (Map.Entry<Integer, List<Integer>> entry : segmentIndexByClusterId.entrySet()) {
                String clusterSizeString = "cluster " + entry.getKey() + " size:  " + entry.getValue().size();
                log.add(clusterSizeString);
                System.out.println(clusterSizeString);
            }

            printNumberOfSegmentsPerCluster(infoAddress, textIO);

            graphByClusterIdMap = getClusteredGraph();
            log.add("Clustering done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Clustering done!! : " + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            printSegmentPartition(infoAddress, textIO);

            //summarize
            randomWalkSummary(graphByClusterIdMap, timeLengthMinutes, this.outputAddress);

            topWordsPrint(20, infoAddress);

            log.add("Summary done!! : " + stopWatch.getEleapsedTime());
            System.out.println("Summary done!! : " + stopWatch.getEleapsedTime());
            log.add("FINISH");

            textIO.write(infoAddress + "Param.txt", this.toString());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            e.printStackTrace();
            log.add(sw.toString());
        }
    }

    private void printNumberOfSegmentsPerCluster(String infoAddress, TextIO textIO) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, List<Integer>> entry : segmentIndexByClusterId.entrySet()) {
            stringBuilder.append(entry.getKey() + "\t" + entry.getValue().size() + "\n");
        }
        textIO.write(infoAddress + "numberOfSegmentsPerCluster.txt", stringBuilder.toString());
    }

    private void printSegmentPartition(String infoAddress, TextIO textIO) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, Graph> integerGraphEntry : graphByClusterIdMap.entrySet()) {
            stringBuilder.append(integerGraphEntry.getValue().toStringGephi());
        }
        textIO.write(infoAddress + "segmentGraphPartition.txt", stringBuilder.toString());
    }

    public List<Double> getSegmentLengthData() {
        if (segmentLengthData != null) {
            return segmentLengthData;
        }
        List<Double> ans = new ArrayList<>(segmentedBows.size());
        for (int i = 0; i < segmentedBows.size(); i++) {
            ans.add(segmentedBows.get(i).getTimeIntervalMinutes());
        }
        return ans;
    }

    //Not useful
    private void rejectSegmentOutliers() {
        double mu = this.averageSegmentLength;
        double sigma = this.standardDeviationSegmentLength;
        for (int i = 0; i < segmentedBows.size(); i++) {
            double t = segmentedBows.get(i).getTimeIntervalMinutes();
            double z = (t - mu) / sigma;
            if (z < -1) {
                segmentedBows.remove(i);
            }
        }
    }

    private double computeStandardDeviationLength() {
        double mu = 0;
        int size = segmentedBows.size();
        for (int i = 0; i < size; i++) {
            double t = segmentedBows.get(i).getTimeIntervalMinutes();
            mu += (t - averageSegmentLength) * (t - averageSegmentLength);
        }
        return Math.sqrt(mu / size);
    }

    private double computeAverageSegmentLength() {
        double mu = 0;
        int size = segmentedBows.size();
        for (int i = 0; i < size; i++) {
            double t = segmentedBows.get(i).getTimeIntervalMinutes();
            mu += t;
        }
        return mu / size;
    }

    private void topWordsPrint(int k, String infoAddress) {
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

            if (graphCentroidByClusterId == null) {
                graphCentroidByClusterId = new HashMap<>();
            }

            graphCentroidByClusterId.put(clusterId, acc);

            Matrix row = new Matrix(acc).transpose();
            stringBuilder2.append(row).append("\n");
            TopKSymbolWithProb topKSymbolWithProb = new TopKSymbolWithProb(k);
            stringBuilder.append("cluster ").append(clusterId).append(": ").append(topKSymbolWithProb.nextSymbol(acc, lowBowManager.getSimplex())).append("\n\n");
        }
        textIO.write(infoAddress + "TopWords.txt", stringBuilder.toString());
        textIO.write(infoAddress + "GraphCentroid.txt", stringBuilder2.toString());
    }

    /**
     * Gets clustered graph.
     *
     * @return the clustered graph
     */
    protected abstract Map<Integer, Graph> getClusteredGraph();

    /**
     * Cluster arcs.
     *
     * @param kcluster the kcluster
     * @return the map
     */
    public abstract Map<Integer, List<Integer>> clusterArcs(int kcluster);

    private void randomWalkSummary(Map<Integer, Graph> map, double timeLengthMinutes, String outputAddress) {
        for (Integer key : map.keySet()) {
            StopWatch stopWatch = new StopWatch();
            Graph graphClass = map.get(key);

            //random walk
            int[] permutation = getRandomWalkRankPermutation(key, graphClass);
            if (permutation == null) {
                continue;
            }
            // select most important segments under time constraint
            cutVideoUnderConstraint(permutation, outputAddress, key, timeLengthMinutes, graphClass);
            String logString = "Video summary arc " + key + " is done " + stopWatch.getEleapsedTime();
            log.add(logString);
            System.out.println(logString);
        }
    }

    private void cutVideoUnderConstraint(int[] permutation, String outputAddress, int clusterId, double timeLengthMinutes, Graph graphClass) {
        List<String> segmentedAddresses = new ArrayList<>();

        // vertexIdByIndex values are index values in {1, ..., n}
        Integer[] vertexIdByIndex = graphClass.getKeyIndex();

        double acc = 0;
        int k = permutation.length - 1;

        String auxAddress = outputAddress;
        auxAddress += clusterId + "/";
        FilesCrawler.creatDirs(auxAddress);

        StringBuilder stringBuilder = new StringBuilder();
        TopKSymbol topKSymbol = new TopKSymbol(30);
        while (acc < timeLengthMinutes && k >= 0) {
            SubSegmentedBow segmentedBow = segmentedBows.get(vertexIdByIndex[permutation[k--]] - 1);
            double timeIntervalMinutes = segmentedBow.getTimeIntervalMinutes();
            if (acc + timeIntervalMinutes < timeLengthMinutes) {
                if (cutVideo) {
                    String videoSegmentAddress = auxAddress + parseVideoAddress(segmentedBow.getVideoAddress()) + segmentedBow.getInterval().toString().replace(" ", "") + ".mp4";
                    segmentedBow.cutSegment(videoSegmentAddress);
                    segmentedAddresses.add(videoSegmentAddress);
                }
                stringBuilder.append(topKSymbol.nextSymbol(segmentedBow.getSegmentBow(), this.lowBowManager.getSimplex())).append("\n");
                acc += timeIntervalMinutes;
            }
        }

        if (isVideoConcat && segmentedAddresses.size() > 0 && cutVideo) {
            concatVideos(segmentedAddresses, auxAddress, String.valueOf(clusterId));
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

    private void concatVideos(List<String> segmentedAddresses, String auxAddress, String clusterId) {
        Collections.sort(segmentedAddresses, stringComparator);
        String outputConcatAddress = auxAddress + "Arc" + clusterId + "Summary";
        FFMpegVideoApi.concat(segmentedAddresses, outputConcatAddress + ".mp4");
        CommandLineApi commandLineApi = new CommandLineApi();
        for (String segmentedAddress : segmentedAddresses) {
            try {
                commandLineApi.callCommand("rm " + segmentedAddress);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
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
    public List<SubSegmentedBow> getSegmentedBows() {
        return segmentedBows;
    }

    /**
     * Gets segment index by cluster id.
     *
     * @return the segment index by cluster id
     */
    public Map<Integer, List<Integer>> getSegmentIndexByClusterId() {
        return segmentIndexByClusterId;
    }

    private String parseVideoAddress(String videoAddress) {
        String[] split = videoAddress.split("\\\\");
        String[] secondSplit = split[split.length - 1].split("\\.");
        return secondSplit[0];
    }

    /**
     * Gets low bow manager.
     *
     * @return the low bow manager
     */
    public SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> getLowBowManager() {
        return lowBowManager;
    }

    /**
     * Gets knn graph.
     *
     * @return the knn graph
     */
    public KnnGraph getKnnGraph() {
        return knnGraph;
    }

    /**
     * Gets tf idf distance.
     *
     * @return the tf idf distance
     */
    public Distance<Vector> getTfIdfDistance() {
        return (x, y) -> {
            if (entropyPredicate == null) {
                return 0;
            }
            Vector wordEntropy = this.entropyPredicate.getWordProbForSimplex(lowBowManager.getSimplex());
            wordEntropy = new Vector(wordEntropy.applyFunction((z) -> -Math.log(z)));
            Vector xbow = Vector.pointMult(x, wordEntropy);
            Vector ybow = Vector.pointMult(y, wordEntropy);
            return 1 - (Vector.innerProd(xbow, ybow) / (xbow.norm() * ybow.norm()));
        };
    }

    /**
     * Gets histogram distance.
     *
     * @return the histogram distance
     */
    public Distance<Vector> getHistogramDistance() {
        return histogramDistance;
    }

    /**
     * Sets histogram distance.
     *
     * @param histogramDistance the histogram distance
     */
    public void setHistogramDistance(Distance<Vector> histogramDistance) {
        this.histogramDistance = histogramDistance;
    }

    /**
     * Is cut video.
     *
     * @return the boolean
     */
    public boolean isCutVideo() {
        return cutVideo;
    }

    /**
     * Sets cut video.
     *
     * @param cutVideo the cut video
     */
    public void setCutVideo(boolean cutVideo) {
        this.cutVideo = cutVideo;
    }

    /**
     * Gets graph centroid by cluster id.
     *
     * @return the graph centroid by cluster id
     */
    public Map<Integer, Vector> getGraphCentroidByClusterId() {
        return graphCentroidByClusterId;
    }

    /**
     * Gets standard deviation segment length.
     *
     * @return the standard deviation segment length
     */
    public double getStandardDeviationSegmentLength() {
        return standardDeviationSegmentLength;
    }

    /**
     * Gets average segmentlength.
     *
     * @return the average segmentlength
     */
    public double getAverageSegmentLength() {
        return averageSegmentLength;
    }

    /**
     * Is video concat.
     *
     * @return the boolean
     */
    public boolean isVideoConcat() {
        return isVideoConcat;
    }

    /**
     * Sets video concat.
     *
     * @param videoConcat the video concat
     */
    public void setVideoConcat(boolean videoConcat) {
        isVideoConcat = videoConcat;
    }

    /**
     * Gets low bow segmentator.
     *
     * @return the low bow segmentator
     */
    public LowBowSegmentator getLowBowSegmentator() {
        return lowBowSegmentator;
    }

    /**
     * Sets low bow segmentator.
     *
     * @param lowBowSegmentator the low bow segmentator
     */
    public void setLowBowSegmentator(LowBowSegmentator lowBowSegmentator) {
        this.lowBowSegmentator = lowBowSegmentator;
    }

    public Predicate<String> getNecessaryWordPredicate() {
        return necessaryWordPredicate;
    }

    public void setNecessaryWordPredicate(RemoveWordsPredicate necessaryWordPredicate) {
        this.necessaryWordPredicate = necessaryWordPredicate;
    }

    @Override
    public String toString() {
        return "BaseArcSummarizer{" +
                "log=" + log +
                ", heat=" + heat +
                ", entropy=" + entropy +
                ", knn=" + knn +
                ", kcluster=" + kcluster +
                ", histogramDistance=" + histogramDistance +
                ", outputAddress='" + outputAddress + '\'' +
                ", necessaryWordPredicate=" + necessaryWordPredicate +
                ", entropyPredicate=" + entropyPredicate +
                ", cutVideo=" + cutVideo +
                ", averageSegmentLength=" + averageSegmentLength +
                ", standardDeviationSegmentLength=" + standardDeviationSegmentLength +
                ", isVideoConcat=" + isVideoConcat +
                ", lowBowSegmentator=" + lowBowSegmentator +
                '}';
    }
}
