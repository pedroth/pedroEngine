package nlp.lowbow.src.script;

import algebra.src.Vector;
import apps.src.LowBowVisualizer;
import inputOutput.MyText;
import nlp.lowbow.src.LowBow;
import nlp.lowbow.src.LowBowManager;
import nlp.lowbow.src.MatrixHeatFlow;
import nlp.textSplitter.MyTextSplitter;

import java.util.List;

/**
 * Created by Pedroth on 12/28/2015.
 */
public class LowBowFreqTest {
    public static void main(String[] args) {
        LowBowManager lowBowManager = new LowBowManager();
        MyText text = new MyText("src/nlp/resources/TextExample.txt");

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
        lowBowList.get(1).heatFlow(0.999, new MatrixHeatFlow());
        Vector[] curve = lowBowList.get(0).getCurve();
        Vector[] smoothedCurve = lowBowList.get(1).getCurve();

        text.write("C:/Users/Pedroth/Desktop/teste2.txt", lowBowList.get(0).generateText());

//        for (int i = 0; i < curve.length; i++) {
//            curve[i] = Vector.diff(curve[i],smoothedCurve[i]);
//        }

        text.write("C:/Users/Pedroth/Desktop/teste.txt",lowBowList.get(0).generateText());

        new LowBowVisualizer("high frequency Test", 500,500, lowBowManager);
    }
}
