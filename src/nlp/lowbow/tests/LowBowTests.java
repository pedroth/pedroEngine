package nlp.lowbow.tests;

import algebra.src.DistanceMatrix;
import algebra.src.Matrix;
import graph.KnnGraph;
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
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.LowBowPrinter;
import nlp.utils.NecessaryWordPredicate;
import nlp.utils.SegmentedBow;
import org.junit.Assert;
import org.junit.Test;
import utils.Csv2Matrix;
import utils.FilesCrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
        Collections.sort(videos);

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

        NecessaryWordPredicate predicate = new NecessaryWordPredicate(bowManager, 0.05);
        textSplitter = new SubsSplitter(predicate);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        lowBowManager.buildModel(1.0);

        DistanceMatrix distanceMatrix = lowBowManager.getDistanceMatrix();
        KnnGraph graph = new KnnGraph(distanceMatrix, 1);
        text.write("C:/Users/Pedroth/Desktop/OverTheGardenWallGraph.txt", graph.toStringGephi());
    }

    @Test
    public void testSegmentation() {
        List<String> subtitles = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv");
        Collections.sort(videos);

        SummaryGenLowBowManager<LowBowSubtitles> lowBowManager = new SummaryGenLowBowManager<>();

        MyText text = new MyText();
        SubsSplitter textSplitter = new SubsSplitter(x -> true);

        //lowbow representation
        for (int i = 0; i < subtitles.size(); i++) {
            text.read(subtitles.get(i));
            lowBowManager.add(new LowBowSubtitles<>(text.getText(), textSplitter, videos.get(i)));
        }
        lowBowManager.buildModel(0.1);
        lowBowManager.buildSegmentations();
        List<SegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Collections.sort(segmentedBows);
        System.out.println(segmentedBows.size());
    }

}
