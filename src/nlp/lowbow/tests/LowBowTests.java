package nlp.lowbow.tests;

import algebra.src.DistanceMatrix;
import algebra.src.Matrix;
import algebra.src.Vector;
import graph.Graph;
import graph.KnnGraph;
import graph.RandomWalkGraph;
import graph.SpectralClustering;
import inputOutput.CsvReader;
import inputOutput.MyText;
import nlp.lowbow.src.eigenLowbow.LowBowSubtitles;
import nlp.lowbow.src.eigenLowbow.SummaryGenLowBowManager;
import nlp.lowbow.src.simpleLowBow.BaseLowBowManager;
import nlp.lowbow.src.simpleLowBow.LambdaTestFlow;
import nlp.lowbow.src.simpleLowBow.LowBow;
import nlp.lowbow.src.simpleLowBow.MatrixHeatFlow;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.lowbow.src.symbolSampler.SymbolAtMaxPos;
import nlp.lowbow.src.symbolSampler.SymbolSampler;
import nlp.lowbow.src.symbolSampler.TopKSymbol;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.LowBowPrinter;
import nlp.utils.NecessaryWordPredicate;
import nlp.utils.SegmentedBow;
import numeric.src.Distance;
import org.junit.Assert;
import org.junit.Test;
import utils.Csv2Matrix;
import utils.FilesCrawler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The type Low simpleDocModel tests.
 */
public class LowBowTests {
    /**
     * Test lambda sensitivity.
     */
    @Test
    public void testLambdaSensitivity() {
        int samples = 100;
        double step = 1.0 / (samples - 1);
        double lambda = 0;
        MyText t = new MyText();
        t.read("src/nlp/resources/texts/TextExample.txt");
        String text = "Hello , please input some text here. For example a c c c b b a c c";
        LowBow lowOrig = new LowBow(t.getText(), new MyTextSplitter());
        lowOrig.setSigmaAuto();
        lowOrig.setSamplesPerTextLength(3);
        lowOrig.build();
        LambdaTestFlow heatMethod = new LambdaTestFlow(0.01, lowOrig);
        double a = 0.0117, b = 0.0123;
        for (int i = 0; i < samples; i++) {
            LowBow low = new LowBow(t.getText(), new MyTextSplitter());
            low.setSigmaAuto();
            low.setSamplesPerTextLength(3);
            double sigma = 1;//(a * b + (b + 1) * lambda) / (a + lambda);
            low.heatFlow(lambda, heatMethod);
            System.out.println(lambda + "\t" + heatMethod.lambdaMeasure());
            lambda += step;
        }
    }

    /**
     * Texts under different smoothing
     */
    @Test
    public void lowbowSmoothingGeneratiodnTest() {
        MyText text = new MyText();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
        int samples = 10;
        ArrayList[] subSamples = new ArrayList[samples];
        double acc = 0;
        double mulAcc = 1;

        for (int i = 0; i < samples; i++) {
            LowBow lowBowSubtitles = new LowBow(text.getText(), new SubsSplitter());
            lowBowSubtitles.build();
            lowBowSubtitles.heatFlow(acc, new MatrixHeatFlow());
            String generateText = lowBowSubtitles.generateText(new SymbolAtMaxPos(i == 0 ? 0 : 2));
            subSamples[i] = new ArrayList<>();
            String[] split = generateText.split("\n");
            for (int j = 0; j < split.length; j++) {
                subSamples[i].add(split[j]);
            }
            System.out.println(acc);
            mulAcc *= 1.0 / 10;
            acc += 9 * mulAcc;
        }

        StringBuilder stringBuilder = new StringBuilder();
        int size = subSamples[0].size();
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < samples; i++) {
                stringBuilder.append(subSamples[i].get(j) + ((i == samples - 1) ? "" : ","));
            }
            stringBuilder.append("\n");
        }
        text.write("C:/Users/Pedroth/Desktop/subExperiments4.csv", stringBuilder.toString());
    }


    /**
     * Test Subtitle LowBow Cut
     */
    @Test
    public void TestSubtitles() {
        String principalAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall";
        String endFile = ".srt";
        int numOfEpisodes = 10;
        String[] address = new String[numOfEpisodes];
        BaseLowBowManager<LowBowSubtitles> manager = new BaseLowBowManager<>();
        MyText text = new MyText();
        for (int i = 0; i < numOfEpisodes; i++) {
            address[i] = principalAddress + (i + 1) + endFile;
            text.read(address[i]);
            manager.add(new LowBowSubtitles(text.getText(), new SubsSplitter()));
        }

        for (LowBowSubtitles lowBowSubtitles : manager.getDocModels()) {
            System.out.println(lowBowSubtitles);
        }
        manager.getSimplex().getKeySet().forEach(System.out::println);
    }

    @Test
    public void printLowBow() {
        MyText text = new MyText();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
        LowBow lowBowSubtitles = new LowBow(text.getText(), new SubsSplitter());
        lowBowSubtitles.build();
//        lowBowSubtitles.heatFlow(250, new HeatFlow());
        text.write("C:/Users/Pedroth/Desktop/subExperiments.csv", lowBowSubtitles.toString(new LowBowPrinter()));
        System.out.println(lowBowSubtitles.getSimplex().toString());
    }


    @Test
    public void readLowBow() {
        CsvReader csvReader = new CsvReader();
        csvReader.read("C:/Users/Pedroth/Desktop/lowbow.csv");
        Matrix lowBow = csvReader.map(Csv2Matrix.getInstance());
        MyText text = new MyText();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
        LowBow lowBowSubtitles = new LowBow(text.getText(), new SubsSplitter());
        lowBowSubtitles.build();
        SymbolAtMax symbolSampler = new SymbolAtMax();
        text.write("C:/Users/Pedroth/Desktop/normalText.txt", lowBowSubtitles.generateText(symbolSampler));
        lowBowSubtitles.setCurve(Matrix.transpose(lowBow).getVectorColumns());
        text.write("C:/Users/Pedroth/Desktop/FreqText.txt", lowBowSubtitles.generateText(symbolSampler));
    }

    @Test
    public void HeatRepresentationTest() {
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
        Collections.sort(videos);

        SummaryGenLowBowManager<LowBowSubtitles> lowBowManager = new SummaryGenLowBowManager<>();
        SummaryGenLowBowManager<LowBowSubtitles> lowBowManagerRaw = new SummaryGenLowBowManager<>();

        MyText text = new MyText();
        SubsSplitter textSplitter = new SubsSplitter();

        textSplitter = new SubsSplitter(x -> true);
        for (int i = 0; i < 1; i++) {
            text.read(subtitles.get(i));
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
            lowBowManagerRaw.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        lowBowManager.buildModel(1.0);
        lowBowManagerRaw.build();

        LowBowSubtitles lowBowSubtitlesHeat = lowBowManager.getDocModels().get(0);
        LowBowSubtitles lowBowSubtitles = lowBowManagerRaw.getDocModels().get(0);

        lowBowSubtitlesHeat.buildRawCurveFromHeatRepresentation();

        double norm = lowBowSubtitlesHeat.getRawCurve().forbeniusDistSquare(lowBowSubtitles.getRawCurve());
        System.out.println(norm);
        Assert.assertTrue(norm < 5);
    }

    @Test
    public void spectralClusterTest() {
        //get all files
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
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
        NecessaryWordPredicate predicate = new NecessaryWordPredicate(bowManager, 0.25);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        //heat model
        lowBowManager.buildModel(0.04);
        //build segmentation
        lowBowManager.buildSegmentations();
        List<SegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Collections.sort(segmentedBows);

        //distances
        Distance<Vector> cosineSim = (x, y) -> {
            return 1 - Vector.innerProd(x, y) / (x.norm() * y.norm());
        };
        Distance<Vector> euclideanDist = (x, y) -> {
            return Vector.diff(x, y).norm();
        };
        Distance<Vector> simplexDist = (x, y) -> {
            double acc = 0;
            for (int i = 1; i <= x.getDim(); i++) {
                acc += Math.sqrt(x.getX(i) * y.getX(i));
            }
            return Math.acos(acc);
        };

        Distance<Vector> tfIdf = (x, y) -> {
            Vector wordEntropy = predicate.getWordProbForSimplex(lowBowManager.getSimplex());
//            int n = wordEntropy.getDim();
            wordEntropy = new Vector(wordEntropy.applyFunction((z) -> -Math.log(z)));
            Vector xbow = Vector.pointMult(x, wordEntropy);
            Vector ybow = Vector.pointMult(y, wordEntropy);
            return 1 - Vector.innerProd(xbow, ybow) / (xbow.norm() * ybow.norm());
        };

        //build knn-graph
        DistanceMatrix distanceMatrix = lowBowManager.getDistanceMatrixOfSegmentations(simplexDist);
        KnnGraph graph = new KnnGraph(distanceMatrix, 4);

        //clustering
        SpectralClustering spectralClustering = new SpectralClustering(graph);
        Map<Integer, List<Integer>> dataToClass = spectralClustering.clustering(5);

        //segments top k symbols
        SymbolSampler symbolSampler = new TopKSymbol(50);
        for (int i = 0; i < segmentedBows.size(); i++) {
            LowBowSubtitles lowBowSubtitles = segmentedBows.get(i).getLowBowSubtitles();
            String videoAddress = lowBowSubtitles.getVideoAddress();
            videoAddress = videoAddress.substring(videoAddress.length() - 10, videoAddress.length() - 3);
            String baseAdd = "C:/Users/Pedroth/Desktop/Experiments/";
            creatDirs(baseAdd);
            String address = baseAdd + (i + 1) + "" + videoAddress;
            String text1 = symbolSampler.nextSymbol(segmentedBows.get(i).getSegmentBow(), lowBowSubtitles.getSimplex());
            text.write(address + ".txt", text1);
        }

        //cut segments
        for (Map.Entry<Integer, List<Integer>> entry : dataToClass.entrySet()) {
            Integer key = entry.getKey();
            for (Integer index : entry.getValue()) {
                SegmentedBow segmentedBow = segmentedBows.get(index - 1);
                LowBowSubtitles lowBowSubtitles = segmentedBow.getLowBowSubtitles();
                String videoAddress = lowBowSubtitles.getVideoAddress();
                videoAddress = videoAddress.substring(videoAddress.length() - 10, videoAddress.length() - 3);
                String str = "C:/Users/Pedroth/Desktop/cut/" + key + "/";
                creatDirs(str);
                String address = str + videoAddress + "" + (index);
                segmentedBow.cutSegment(address + ".mp4");
            }
        }

        text.write("C:/Users/Pedroth/Desktop/OverTheGardenWallGraph.txt", graph.toStringGephi());
    }


    private void cutSegmentsGraph(List<SegmentedBow> segmentedBows, Graph graph) {
        String str = "C:/Users/Pedroth/Desktop/cutGraph/";
        for (Integer u : graph.getVertexSet()) {
            String address = str + u + "/";
            creatDirs(address);
            segmentedBows.get(u - 1).cutSegment(address + u + ".mp4");
            for (Integer v : graph.getAdjVertex(u)) {
                segmentedBows.get(v - 1).cutSegment(address + v + ".mp4");
            }
        }
    }

    private void creatDirs(String address) {
        File dirs = new File(address);
        dirs.mkdirs();
    }

    @Test
    public void testSegmentation() {
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
        Collections.sort(videos);

        SubsSplitter textSplitter = new SubsSplitter();
        MyText text = new MyText();
        SummaryGenLowBowManager<LowBowSubtitles> lowBowManager = new SummaryGenLowBowManager<>();
        BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

        //bow representation
        for (String subtitle : subtitles) {
            text.read(subtitle);
            bowManager.add(new Bow(text.getText(), textSplitter));
        }
        bowManager.build();

        NecessaryWordPredicate predicate = new NecessaryWordPredicate(bowManager, 0.1);

        //lowbow representation
        for (int i = 0; i < 1; i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        lowBowManager.buildModel(0.025);
        text.write("C:/Users/Pedroth/Desktop/epi1.txt", lowBowManager.getDocModels().get(0).generateText(new TopKSymbol(10)));
        lowBowManager.buildSegmentations();
        List<SegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Collections.sort(segmentedBows);
        for (int i = 0; i < segmentedBows.size(); i++) {
            String outAddress = "C:/Users/Pedroth/Desktop/" + "cut" + i + ".mp4";
            segmentedBows.get(i).cutSegment(outAddress);
        }
    }


    @Test
    public void testSummarization() {
        //get all files
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
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
        NecessaryWordPredicate predicate = new NecessaryWordPredicate(bowManager, 0.25);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        //heat model
        lowBowManager.buildModel(0.04);

        //build segmentation
        lowBowManager.buildSegmentations();
        List<SegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Collections.sort(segmentedBows);

        Distance<Vector> simplexDist = (x, y) -> {
            double acc = 0;
            for (int i = 1; i <= x.getDim(); i++) {
                acc += Math.sqrt(x.getX(i) * y.getX(i));
            }
            return Math.acos(acc);
        };

        //build knn-graph
        DistanceMatrix distanceMatrix = lowBowManager.getDistanceMatrixOfSegmentations(simplexDist);
        KnnGraph graph = new KnnGraph(distanceMatrix, 4);

        //clustering
        SpectralClustering spectralClustering = new SpectralClustering(graph);
        Map<Integer, List<Integer>> dataToClass = spectralClustering.clustering(5);
        Map<Integer, Graph> map = spectralClustering.getclusteredGraph();
        for (Integer key : map.keySet()) {
            Graph graphClass = map.get(key);
            RandomWalkGraph randomWalkGraph = new RandomWalkGraph(graphClass);
            int numVertex = graphClass.getNumVertex();
            Vector v = new Vector(numVertex);
            v.fill(1.0 / numVertex);
            System.out.println(randomWalkGraph.getStationaryDistribution(v, 1E-3));
        }

        text.write("C:/Users/Pedroth/Desktop/OverTheGardenWallGraph.txt", graph.toStringGephi());
    }

}
