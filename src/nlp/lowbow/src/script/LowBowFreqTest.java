package nlp.lowbow.src.script;

import algebra.src.Vector;
import apps.src.LowBowVisualizer;
import inputOutput.MyText;
import nlp.lowbow.src.GraphLaplacianFlow;
import nlp.lowbow.src.LowBow;
import nlp.lowbow.src.LowBowManager;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.textSplitter.MyTextSplitter;
import nlp.utils.LowBowPrinter;

import java.util.List;

public class LowBowFreqTest {
    public static void main(String[] args) {
        LowBowManager lowBowManager = new LowBowManager();
        MyText text = new MyText("src/nlp/resources/texts/TextExample.txt");

        LowBow lowBow = new LowBow(text.getText(), new MyTextSplitter());
        lowBow.setSigma(0.00001);
        lowBow.setSmoothingCoeff(0.01);
        lowBow.setSamplesPerTextLength(2.0);

        lowBowManager.add(lowBow);

        lowBow = new LowBow(text.getText(), new MyTextSplitter());
        lowBow.setSigma(0.00001);
        lowBow.setSmoothingCoeff(0.01);
        lowBow.setSamplesPerTextLength(2.0);

        lowBowManager.add(lowBow);

        lowBowManager.build();

        List<LowBow> lowBowList = lowBowManager.getLowbows();
        lowBowList.get(1).heatFlow(0.999, new GraphLaplacianFlow());
        Vector[] curve = lowBowList.get(0).getCurve();
        Vector[] smoothedCurve = lowBowList.get(1).getCurve();

        text.write("C:/Users/Pedroth/Desktop/Original.txt", lowBowList.get(0).generateText(new SymbolAtMax()));

        text.write("C:/Users/Pedroth/Desktop/OriginalMat.txt", LowBowPrinter.printMatlabMatrix(lowBowList.get(0).getCurve()));

        text.write("C:/Users/Pedroth/Desktop/SmoothedMat.txt", LowBowPrinter.printMatlabMatrix(lowBowList.get(1).getCurve()));

        text.write("C:/Users/Pedroth/Desktop/Smoothed.txt", lowBowList.get(1).generateText(new SymbolAtMax()));

        for (int i = 0; i < curve.length; i++) {
            curve[i] = dualDistribution(smoothedCurve[i]);
        }


        text.write("C:/Users/Pedroth/Desktop/highFreq.txt", lowBowList.get(0).generateText(new SymbolAtMax()));

        text.write("C:/Users/Pedroth/Desktop/highFreqMat.txt", LowBowPrinter.printMatlabMatrix(curve));

        new LowBowVisualizer("high frequency Test", 500, 500, lowBowManager);
    }

    public static Vector dualDistribution(Vector p) {
        int dim = p.getDim();
        Vector ans = new Vector(dim);

        double acc = 0;
        for (int i = 1; i <= dim; i++) {
            acc += p.getX(i);
            ans.setX(i, 1 - acc);
        }
        return ans;
    }
}
