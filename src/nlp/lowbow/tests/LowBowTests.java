package nlp.lowbow.tests;

import inputOutput.MyText;
import nlp.lowbow.src.*;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.StopWordsSplitter;
import org.junit.Test;

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

}
