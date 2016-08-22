package nlp.lowbow.tests;

import algebra.src.Matrix;
import inputOutput.CsvReader;
import inputOutput.MyText;
import nlp.lowbow.src.*;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.lowbow.src.symbolSampler.SymbolAtMaxPos;
import nlp.textSplitter.MyTextSplitter;
import nlp.utils.LowBowPrinter;
import org.junit.Test;
import utils.Csv2Matrix;

import java.util.ArrayList;

/**
 * The type Low bow tests.
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
            LowBowSubtitles lowBowSubtitles = new LowBowSubtitles(text.getText());
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
        LowBowManager<LowBowSubtitles> manager = new LowBowManager<>();
        MyText text = new MyText();
        for (int i = 0; i < numOfEpisodes; i++) {
            address[i] = principalAddress + (i + 1) + endFile;
            text.read(address[i]);
            manager.add(new LowBowSubtitles(text.getText()));
        }

        for (LowBowSubtitles lowBowSubtitles : manager.getLowbows()) {
            System.out.println(lowBowSubtitles);
        }
        manager.getSimplex().getKeySet().forEach(System.out::println);
    }

    @Test
    public void printLowBow() {
        MyText text = new MyText();
        text.read("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/OverTheGardenWall1.srt");
        LowBowSubtitles lowBowSubtitles = new LowBowSubtitles(text.getText());
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
        LowBowSubtitles lowBowSubtitles = new LowBowSubtitles(text.getText());
        lowBowSubtitles.build();
        SymbolAtMax symbolSampler = new SymbolAtMax();
        text.write("C:/Users/Pedroth/Desktop/normalText.txt", lowBowSubtitles.generateText(symbolSampler));
        lowBowSubtitles.setCurve(Matrix.transpose(lowBow).getVectorColumns());
        text.write("C:/Users/Pedroth/Desktop/FreqText.txt", lowBowSubtitles.generateText(symbolSampler));
    }

}
