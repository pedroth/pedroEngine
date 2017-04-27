package nlp.tests;

import algebra.src.DistanceMatrix;
import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import algebra.src.Vector;
import graph.*;
import inputOutput.CsvReader;
import inputOutput.TextIO;
import javafx.util.Pair;
import nlp.lowbow.eigenLowbow.*;
import nlp.lowbow.simpleLowBow.BaseLowBowManager;
import nlp.lowbow.simpleLowBow.LambdaTestFlow;
import nlp.lowbow.simpleLowBow.LowBow;
import nlp.lowbow.simpleLowBow.MatrixHeatFlow;
import nlp.segmentedBow.sub.SegmentedBowCool;
import nlp.segmentedBow.sub.SegmentedBowHeat;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.seriesSummary.BaseArcSummarizer;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.symbolSampler.SymbolAtMax;
import nlp.symbolSampler.SymbolAtMaxPos;
import nlp.symbolSampler.SymbolSampler;
import nlp.symbolSampler.TopKSymbol;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.EntropyStopWordPredicate;
import nlp.utils.LowBowPrinter;
import numeric.src.Distance;
import numeric.src.MySet;
import org.junit.Assert;
import org.junit.Test;
import utils.Csv2Matrix;
import utils.FilesCrawler;
import utils.StopWatch;

import java.io.File;
import java.util.*;

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
        TextIO t = new TextIO();
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
    public void lowbowSmoothingGenerationTest() {
        TextIO text = new TextIO();
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
        TextIO text = new TextIO();
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
        TextIO text = new TextIO();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
        LowBow lowBowSubtitles = new LowBow(text.getText(), new SubsSplitter());
        lowBowSubtitles.build();
//        lowBowOrig.heatFlow(250, new HeatFlow());
        text.write("C:/Users/Pedroth/Desktop/subExperiments.csv", lowBowSubtitles.toString(new LowBowPrinter()));
        System.out.println(lowBowSubtitles.getSimplex().toString());
    }


    @Test
    public void readLowBow() {
        CsvReader csvReader = new CsvReader();
        csvReader.read("C:/Users/Pedroth/Desktop/lowbow.csv");
        Matrix lowBow = csvReader.map(Csv2Matrix.getInstance());
        TextIO text = new TextIO();
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

        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();
        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManagerRaw = new SummaryGenLowBowManager<>();

        TextIO text = new TextIO();
        SubsSplitter textSplitter;

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
        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();
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
        EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, 0.25);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        //heat model
        lowBowManager.buildModel(0.04);
        //build segmentation
        lowBowManager.buildSegmentations(SegmentedBowHeat::new);
        List<SubSegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
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
            return 1 - (Vector.innerProd(xbow, ybow) / (xbow.norm() * ybow.norm()));
        };

        //build knn-graph
        DistanceMatrix distanceMatrix = lowBowManager.getDistanceMatrixOfSegmentations(simplexDist);
        KnnGraph graph = new KnnGraph(distanceMatrix, 4);

        //clustering
        SpectralClustering spectralClustering = new AndrewEtAlSpectralClustering(graph);
        Map<Integer, List<Integer>> dataToClass = spectralClustering.clustering(5);

        //segments top k symbols
        SymbolSampler symbolSampler = new TopKSymbol(50);
        for (int i = 0; i < segmentedBows.size(); i++) {
            LowBowSubtitles lowBowSubtitles = segmentedBows.get(i).getLowBowOrig();
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
                SubSegmentedBow segmentedBow = segmentedBows.get(index - 1);
                LowBowSubtitles lowBowSubtitles = segmentedBow.getLowBowOrig();
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


    private void cutSegmentsGraph(List<SegmentedBowHeat> segmentedBows, Graph graph) {
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
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "mp4");
        Collections.sort(videos);

        SubsSplitter textSplitter = new SubsSplitter();
        TextIO text = new TextIO();
        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();
        BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

        //bow representation
        for (String subtitle : subtitles) {
            text.read(subtitle);
            bowManager.add(new Bow(text.getText(), textSplitter));
        }
        bowManager.build();

        EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, 0.08);

        //lowbow representation
        for (int i = 0; i < 1; i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i));
            LowBowSegmentator lowBowSegmentator = new EqualSpaceSegmentator();
            ((EqualSpaceSegmentator) lowBowSegmentator).setSpacePercent(0.90);
            low.setLowBowSegmentator(lowBowSegmentator);
            lowBowManager.add(low);
        }
        lowBowManager.buildModel(0.04);
        text.write("C:/Users/Pedroth/Desktop/epi1.txt", lowBowManager.getDocModels().get(0).generateText(new TopKSymbol(10)));
        lowBowManager.buildSegmentations(SegmentedBowHeat::new);
        List<SubSegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Collections.sort(segmentedBows);
        for (int i = 0; i < segmentedBows.size(); i++) {
            String outAddress = "C:/Users/Pedroth/Desktop/" + "cut" + i + ".mp4";
            segmentedBows.get(i).cutSegment(outAddress);
        }
    }

    @Test
    public void testSegmentationCount() {
        double entropy = 0.1;
        String src = "C:/pedro/escolas/ist/Tese/Series/";
        List<String> address = new ArrayList<>();
        List<String> extension = new ArrayList<>();
        address.add("OverTheGardenWall");
        extension.add("mkv");
        address.add("MrRobot");
        extension.add("mkv");
        address.add("BreakingBad");
        extension.add("mp4");
        address.add("BattleStarGalactica");
        extension.add("avi");
        int samplesEpisodes = 8;
        int samplesK = 50;
        int n = address.size();
        Matrix kData = new Matrix();
        Matrix countDataPerc = new Matrix();
        Matrix countData = new Matrix();
        Matrix timePerSegmentData = new Matrix();
        int index = 0;

        for (int i = 0; i < n; i++) {
            List<String> subtitles = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", "srt");
            Collections.sort(subtitles);
            List<String> videos = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", extension.get(i));
            Collections.sort(videos);

            StopWatch stopWatch = new StopWatch();

            TextIO textIO = new TextIO();
            //bow representation
            BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();
            SubsSplitter textSplitter = new SubsSplitter();
            List<Integer> episodeIndex = new ArrayList<>();
            for (int j = 0; j < samplesEpisodes; j++) {
                int randomIndex = (int) (Math.random() * subtitles.size());
                episodeIndex.add(randomIndex);
                textIO.read(subtitles.get(randomIndex));
                bowManager.add(new Bow(textIO.getText(), textSplitter));
            }
            bowManager.build();

            //build predicate
            EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, entropy);

            //lowbow representation
            SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();
            kData = kData.concat(new Matrix(samplesK, samplesEpisodes));
            countDataPerc = countDataPerc.concat(new Matrix(samplesK, samplesEpisodes));
            countData = countData.concat(new Matrix(samplesK, samplesEpisodes));
            timePerSegmentData = timePerSegmentData.concat(new Matrix(samplesK, samplesEpisodes));

            for (int j = 0; j < samplesEpisodes; j++) {
                textIO.read(subtitles.get(episodeIndex.get(j)));
                SubsSplitter textSplitterLowBow = new SubsSplitter(predicate);
                LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(textIO.getText(), textSplitterLowBow, videos.get(j));
                lowBowManager.add(low);
            }
            System.out.println("counting...\t" + stopWatch.getEleapsedTime());
            stopWatch.resetTime();

            int maxTextLength = (int) lowBowManager.getMaxTextLength();
            LineLaplacian laplacian = new LineLaplacian(maxTextLength);
            Matrix eigenBasis = new Matrix(laplacian.getEigenVectors());
            Vector eigenValues = new Vector(laplacian.getEigenValues());

            for (int j = 0; j < samplesEpisodes; j++) {
                LowBowSubtitles<SubsSplitter> low = lowBowManager.getDocModels().get(j);
                int textLength = low.getTextLength();
                int h = (textLength - 1) / (samplesK - 1);
                int k = 1;
                for (int s = 0; s < samplesK; s++) {
                    StopWatch stopWatch2 = new StopWatch();
//                    low.buildHeatRepresentation(eigenBasis, eigenValues, k);

                    EqualSpaceSegmentator lowbowSeg = new EqualSpaceSegmentator();
                    lowbowSeg.setSpacePercent(1.0 * k / textLength);
                    low.setLowBowSegmentator(lowbowSeg);

                    kData.setXY(s + 1, index + j + 1, 1.0 * k / textLength);
                    countDataPerc.setXY(s + 1, index + j + 1, 1.0 * low.getSegmentation(SegmentedBowCool::new).size() / textLength);
                    countData.setXY(s + 1, index + j + 1, low.getSegmentation(SegmentedBowCool::new).size());
                    timePerSegmentData.setXY(s + 1, index + j + 1, 1.0 * textLength / low.getSegmentation(SegmentedBowCool::new).size());
                    System.out.println(stopWatch2.getEleapsedTime());
                    stopWatch2.resetTime();
                    k += h;
                }
            }
            index += samplesEpisodes;
        }
        TextIO textIO = new TextIO();
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        textIO.write(desktop.getAbsolutePath() + "/kE.txt", kData.toString());
        textIO.write(desktop.getAbsolutePath() + "/countE.txt", countDataPerc.toString());
        textIO.write(desktop.getAbsolutePath() + "/segmentsE.txt", countData.toString());
        textIO.write(desktop.getAbsolutePath() + "/timeE.txt", timePerSegmentData.toString());
    }


    @Test
    public void testSegmentationCountSingle() {
        int samples = 100;
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
        Collections.sort(videos);

        SubsSplitter textSplitter = new SubsSplitter();
        TextIO text = new TextIO();
        BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

        //bow representation
        for (String subtitle : subtitles) {
            text.read(subtitle);
            bowManager.add(new Bow(text.getText(), textSplitter));
        }
        bowManager.build();

        EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, 0.1);

        //lowbow representation
        int i = 0;
        text.read(subtitles.get(i));
        textSplitter = new SubsSplitter(predicate);
        LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i));
        int n = low.getTextLength();
        LineLaplacian laplacian = new LineLaplacian(n);
        Matrix eigenBasis = new Matrix(laplacian.getEigenVectors());
        Vector eigenValues = new Vector(laplacian.getEigenValues());
        int textLength = low.getTextLength();
        int h = (textLength - 1) / (samples - 1);
        int k = 1;
        for (int s = 0; s < samples; s++) {
            low.buildHeatRepresentation(eigenBasis, eigenValues, k);
            int size = low.getSegmentation(SegmentedBowCool::new).size();
            System.out.println((1.0 * k / textLength) + "\t" + size + "\t" + (1.0 * textLength / size));
            k += h;
        }
    }


    @Test
    public void heatCostK() {
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/BreakingBad/", "mp4");
        Collections.sort(videos);

        SubsSplitter textSplitter = new SubsSplitter();
        TextIO text = new TextIO();
        BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

        //bow representation
        for (String subtitle : subtitles) {
            text.read(subtitle);
            bowManager.add(new Bow(text.getText(), textSplitter));
        }
        bowManager.build();

        EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, 0.1);

        //lowbow representation
        int i = 0;
        text.read(subtitles.get(i));
        textSplitter = new SubsSplitter(predicate);
        LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i));
        int n = low.getTextLength();
        LineLaplacian laplacian = new LineLaplacian(n);
        Matrix eigenBasis = new Matrix(laplacian.getEigenVectors());
        Vector eigenValues = new Vector(laplacian.getEigenValues());
        for (int k = 1; k <= n / 2; k++) {
            low.buildHeatRepresentation(eigenBasis, eigenValues, k);
            System.out.println(low.getHeatEnergy());
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
        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();
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
        EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, 0.25);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            textSplitter = new SubsSplitter(predicate);
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        //heat model
        lowBowManager.buildModel(0.04);

        //build segmentation
        lowBowManager.buildSegmentations(SegmentedBowHeat::new);
        List<SubSegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
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
        SpectralClustering spectralClustering = new AndrewEtAlSpectralClustering(graph);
        Map<Integer, List<Integer>> dataToClass = spectralClustering.clustering(5);
        Map<Integer, Graph> map = spectralClustering.getClusteredGraph();
        for (Integer key : map.keySet()) {
            Graph graphClass = map.get(key);
            RandomWalkGraph randomWalkGraph = new RandomWalkGraph(graphClass);
            int numVertex = graphClass.getNumVertex();
            Vector v = new Vector(numVertex);
            v.fill(1.0 / numVertex);
            System.out.println(randomWalkGraph.getStationaryDistribution(v, 0.8, 1E-3));
        }

        text.write("C:/Users/Pedroth/Desktop/OverTheGardenWallGraph.txt", graph.toStringGephi());
    }

    @Test
    public void testEntropy() {
        String src = "C:/pedro/escolas/ist/Tese/Series/";
        List<String> address = new ArrayList<>();
        List<String> extension = new ArrayList<>();
        address.add("OverTheGardenWall");
        extension.add("mkv");
        address.add("MrRobot");
        extension.add("mkv");
        address.add("BreakingBad");
        extension.add("mp4");
        address.add("BattleStarGalactica");
        extension.add("amv");
        List<List<MySet>> stopwords = new ArrayList<>();

        double beginEntropy = 0.01;
        double stepEntropy = 0.01;

        int n = address.size();
        int nn = n * n;
        for (int i = 0; i < n; i++) {
            stopwords.add(new ArrayList<>());
            List<String> subtitles = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", "srt");
            Collections.sort(subtitles);
            List<String> videos = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", extension.get(i));
            Collections.sort(videos);

            SubsSplitter textSplitter = new SubsSplitter();
            TextIO text = new TextIO();
            BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();

            //bow representation
            for (String subtitle : subtitles) {
                text.read(subtitle);
                bowManager.add(new Bow(text.getText(), textSplitter));
            }
            bowManager.build();
            int simplexSize = bowManager.getSimplex().size();
            for (double entropy = beginEntropy; entropy <= 1.0; entropy += stepEntropy) {
                EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, entropy);
                int size = predicate.getNotNecessaryWords().size();
                double ratio = 1.0 * size / simplexSize;
                System.out.println(entropy + "\t" + ratio);
                stopwords.get(i).add(new MySet(predicate.getNotNecessaryWords()));
            }
            System.out.println();
        }
        System.out.println("jaccard index");
        //bad choice of indexation
        for (int i = 0; i < stopwords.get(0).size(); i++) {
            double acc = 0;
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    acc += MySet.jaccardIndex(stopwords.get(j).get(i), stopwords.get(k).get(i));
                }
            }
            double x = beginEntropy + i * stepEntropy;
            System.out.println(x + "\t" + (acc / nn));
        }
    }


    @Test
    public void testSegmentationHist() {
        double entropy = 0.1;
        int samplesEpisodes = 8;
        int samplesK = 50;
        int samples = 100;
        boolean isNeigh = true;
        LowBowSegmentator<LowBowSubtitles, SubSegmentedBow> lowBowSegmentator = MaxDerivativeSegmentator.getInstance();
        String src = "C:/pedro/escolas/ist/Tese/Series/";
        List<String> address = new ArrayList<>();
        List<String> extension = new ArrayList<>();
        address.add("OverTheGardenWall");
        extension.add("mkv");
        address.add("MrRobot");
        extension.add("mkv");
        address.add("BreakingBad");
        extension.add("mp4");
        address.add("BattleStarGalactica");
        extension.add("avi");
        int n = address.size();
        List<javafx.util.Pair> similarityData = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            List<String> subtitles = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", "srt");
            Collections.sort(subtitles);
            List<String> videos = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", extension.get(i));
            Collections.sort(videos);

            TextIO textIO = new TextIO();
            //bow representation
            BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();
            SubsSplitter textSplitter = new SubsSplitter();
            List<Integer> episodeIndex = new ArrayList<>();
            for (int j = 0; j < samplesEpisodes; j++) {
                int randomIndex = (int) (Math.random() * subtitles.size());
                episodeIndex.add(randomIndex);
                textIO.read(subtitles.get(randomIndex));
                bowManager.add(new Bow(textIO.getText(), textSplitter));
            }
            bowManager.build();

            //build predicate
            EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, entropy);

            //lowbow representation
            SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();

            for (int j = 0; j < samplesEpisodes; j++) {
                textIO.read(subtitles.get(episodeIndex.get(j)));
                SubsSplitter textSplitterLowBow = new SubsSplitter(predicate);
                LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(textIO.getText(), textSplitterLowBow, videos.get(j));
                low.setLowBowSegmentator(lowBowSegmentator);
                lowBowManager.add(low);
            }

            int maxTextLength = (int) lowBowManager.getMaxTextLength();
            LineLaplacian laplacian = new LineLaplacian(maxTextLength);
            Matrix eigenBasis = new Matrix(laplacian.getEigenVectors());
            Vector eigenValues = new Vector(laplacian.getEigenValues());

            for (int j = 0; j < samplesEpisodes; j++) {
                LowBowSubtitles<SubsSplitter> low = lowBowManager.getDocModels().get(j);
                int textLength = low.getTextLength();
                int h = (textLength - 1) / (samplesK - 1);
                int k = 1;
                for (int s = 0; s < samplesK; s++) {
//                    low.buildHeatRepresentation(eigenBasis, eigenValues, k);

                    EqualSpaceSegmentator lowBowSegmentatorEqual = new EqualSpaceSegmentator();
                    lowBowSegmentatorEqual.setSpacePercent(1.0 * k / textLength);
                    low.setLowBowSegmentator(lowBowSegmentatorEqual);

                    List<SubSegmentedBow> segmentation = low.getSegmentation(SegmentedBowCool::new);
                    if (isNeigh) {
                        compareNeighborSegments(segmentation, similarityData, BaseArcSummarizer.cosineDist, 1.0 * k / textLength, samples);
                    } else {
                        compareSegments(segmentation, similarityData, BaseArcSummarizer.cosineDist, 1.0 * k / textLength, samples);
                    }
                    k += h;
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder(similarityData.size());
        for (Pair pair : similarityData) {
            stringBuilder.append(pair.getKey() + "\t" + pair.getValue() + "\n");
        }

        TextIO textIO = new TextIO();
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        textIO.write(desktop.getAbsolutePath() + "/similarityNeighEqualData.txt", stringBuilder.toString());
    }

    @Test
    public void testSegmentationHistSub() {
        double entropy = 0.1;
        int samplesEpisodes = 8;
        int samples = 100;
        boolean isNeigh = true;
        LowBowSegmentator lowBowSegmentator = SubtitleSegmentator.getInstance();
        String src = "C:/pedro/escolas/ist/Tese/Series/";
        List<String> address = new ArrayList<>();
        List<String> extension = new ArrayList<>();
        address.add("OverTheGardenWall");
        extension.add("mkv");
        address.add("MrRobot");
        extension.add("mkv");
        address.add("BreakingBad");
        extension.add("mp4");
        address.add("BattleStarGalactica");
        extension.add("avi");
        int n = address.size();
        List<javafx.util.Pair> similarityData = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            List<String> subtitles = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", "srt");
            Collections.sort(subtitles);
            List<String> videos = FilesCrawler.listFilesWithExtension(src + address.get(i) + "/", extension.get(i));
            Collections.sort(videos);

            TextIO textIO = new TextIO();
            //bow representation
            BaseDocModelManager<Bow> bowManager = new BaseDocModelManager<>();
            SubsSplitter textSplitter = new SubsSplitter();
            List<Integer> episodeIndex = new ArrayList<>();
            for (int j = 0; j < samplesEpisodes; j++) {
                int randomIndex = (int) (Math.random() * subtitles.size());
                episodeIndex.add(randomIndex);
                textIO.read(subtitles.get(randomIndex));
                bowManager.add(new Bow(textIO.getText(), textSplitter));
            }
            bowManager.build();

            //build predicate
            EntropyStopWordPredicate predicate = new EntropyStopWordPredicate(bowManager, entropy);

            //lowbow representation
            SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();

            for (int j = 0; j < samplesEpisodes; j++) {
                textIO.read(subtitles.get(episodeIndex.get(j)));
                SubsSplitter textSplitterLowBow = new SubsSplitter(predicate);
                LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(textIO.getText(), textSplitterLowBow, videos.get(j));
                low.setLowBowSegmentator(lowBowSegmentator);
                lowBowManager.add(low);
            }

            for (int j = 0; j < samplesEpisodes; j++) {
                LowBowSubtitles<SubsSplitter> low = lowBowManager.getDocModels().get(j);
                low.build();
                List<SubSegmentedBow> segmentation = low.getSegmentation(SegmentedBowCool::new);
                if (isNeigh) {
                    compareNeighborSegments(segmentation, similarityData, BaseArcSummarizer.cosineDist, 0.0, samples);
                } else {
                    compareSegments(segmentation, similarityData, BaseArcSummarizer.cosineDist, 0.0, samples);
                }
                low.deleteRawCurve();
            }
        }

        StringBuilder stringBuilder = new StringBuilder(similarityData.size());
        for (Pair pair : similarityData) {
            stringBuilder.append(pair.getKey() + "\t" + pair.getValue() + "\n");
        }

        TextIO textIO = new TextIO();
        File desktop = new File(System.getProperty("user.home"), "Desktop");
        textIO.write(desktop.getAbsolutePath() + "/similarityDataSubNeigh.txt", stringBuilder.toString());
    }

    private void compareSegments(List<SubSegmentedBow> segmentation, List<Pair> similarityData, Distance<Vector> distance, double k, int samples) {
        int n = segmentation.size();
        samples = Integer.min(samples, (n * (n - 1)) / 2);
        Random random = new Random();
        if (n <= 1) {
            return;
        }
        for (int i = 0; i < samples; i++) {
            int l = random.nextInt(n);
            int m = (l + random.nextInt(n - 1) + 1) % n;
            Vector segmentBow1 = segmentation.get(l).getSegmentBow();
            Vector segmentBow2 = segmentation.get(m).getSegmentBow();
            similarityData.add(new Pair(k, distance.dist(segmentBow1, segmentBow2)));
        }
    }

    private void compareNeighborSegments(List<SubSegmentedBow> segmentation, List<Pair> similarityData, Distance<Vector> distance, double k, int samples) {
        int n = segmentation.size();
        Random random = new Random();
        if (n <= 1) {
            return;
        }
        for (int i = 0; i < samples; i++) {
            int l = random.nextInt(n);
            if (l == 0) {
                Vector segmentBow1 = segmentation.get(l).getSegmentBow();
                Vector segmentBow2 = segmentation.get(l + 1).getSegmentBow();
                similarityData.add(new Pair(k, distance.dist(segmentBow1, segmentBow2)));
            } else if (l == n - 1) {
                Vector segmentBow1 = segmentation.get(l).getSegmentBow();
                Vector segmentBow3 = segmentation.get(l - 1).getSegmentBow();
                similarityData.add(new Pair(k, distance.dist(segmentBow1, segmentBow3)));
            } else {
                Vector segmentBow1 = segmentation.get(l).getSegmentBow();
                Vector segmentBow2 = segmentation.get(l + 1).getSegmentBow();
                Vector segmentBow3 = segmentation.get(l - 1).getSegmentBow();
                similarityData.add(new Pair(k, distance.dist(segmentBow1, segmentBow2)));
                similarityData.add(new Pair(k, distance.dist(segmentBow1, segmentBow3)));
            }
        }
    }
}
