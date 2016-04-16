package nlp.lowbow.tests;

import inputOutput.MyText;
import nlp.lowbow.src.*;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.lowbow.src.symbolSampler.SymbolAtMaxPos;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.StopWordsSplitter;
import org.junit.Test;

import java.util.ArrayList;

public class LowBowTests {
    public static final double HEATTIME = 0.01;

    /**
     * Matrix Method
     */
    @Test
    public void test1() {
        MyText t = new MyText();
        LowBow low = new LowBow(t.getText(), new StopWordsSplitter("src/nlp/resources/wordLists/stopWords.txt"));
        low.setSamplesPerTextLength(1.0);
        // low.setSigma(0.08);
        low.setSigmaAuto();
        low.setSmoothingCoeff(0.003);
        low.build();
        HeatMethod heat = new MatrixHeatFlow();
        low.heatFlow(HEATTIME, heat);
        t.write("C:/Users/Pedroth/Desktop/out1.txt", low.generateText(new SymbolAtMax()));
    }

    /**
     * Sparse Method
     */
    @Test
    public void test2() {
        MyText t = new MyText();
        t.read("src/nlp/resources/texts/TextExample.txt");
        LowBow low = new LowBow(t.getText(), new StopWordsSplitter("src/nlp/resources/wordLists/stopWords.txt"));
        low.setSamplesPerTextLength(1.0);
        // low.setSigma(0.08);
        low.setSigmaAuto();
        low.setSmoothingCoeff(0.003);
        low.build();
        HeatMethod heat = new SparseHeatFlow();
        low.heatFlow(HEATTIME, heat);
        t.write("C:/Users/Pedroth/Desktop/out2.txt", low.generateText(new SymbolAtMax()));
    }

    /**
     * low bow with prepositions
     */
    @Test
    public void test3() {
        MyText t = new MyText();
        t.read("src/nlp/resources/texts/TextExample.txt");
        LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(t.getText());
        low.setSamplesPerTextLength(1.0);
        // low.setSigma(0.08);
        low.setSigmaAuto();
        low.setSmoothingCoeff(0.003);
        low.build();
        HeatMethod heat = new SparseHeatFlow();
        low.heatFlow(HEATTIME, heat);
        t.write("C:/Users/Pedroth/Desktop/out3.txt", low.generateText(new SymbolAtMax()));
    }

    @Test
    public void test4() {
        MyText t = new MyText();
        t.read("src/nlp/resources/texts/TextExample.txt");
        LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(t.getText());
        low.setSamplesPerTextLength(1.0);
        // low.setSigma(0.08);
        low.setSigmaAuto();
        low.setSmoothingCoeff(0.003);
        low.build();
        low.writeMatrixFile("C:/Users/Pedroth/Desktop/out4.txt");
    }

    @Test
    public void example1() {
        MyText t = new MyText();
        t.read("C:/Users/pedro/Desktop/research/Text.txt");
        LowBow low = new LowBow(t.getText(), new StopWordsSplitter("wordsLists/stopWords.txt"));
        low.setSamplesPerTextLength(1.0);
        low.setSigma(0.08);
        low.setSmoothingCoeff(0.003);
        low.build();
        HeatMethod heat = new MatrixHeatFlow();
        low.heatFlow(0.01, heat);
//		low.writeMatrixFile();
        System.out.println(low);
    }

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

    @Test
    public void timeTest() {
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
            String generateText = lowBowSubtitles.generateText(new SymbolAtMaxPos(1));
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
        text.write("C:/Users/Pedroth/Desktop/subExperiments.csv", stringBuilder.toString());
    }

    @Test
    /**
     * Test Subtitle LowBow Cut
     */
    public void TestSubtitles() {

    }

}
