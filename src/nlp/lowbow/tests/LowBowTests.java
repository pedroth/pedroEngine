package nlp.lowbow.tests;

import inputOutput.MyText;
import nlp.lowbow.src.*;
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
//		low.setSigma(0.08);
		low.setSigmaAuto();
		low.setSmoothingCoeff(0.003);
		low.build();
		HeatMethod heat = new MatrixHeatFlow();
		low.heatFlow(HEATTIME, heat);
		t.write("C:/Users/Pedroth/Desktop/out1.txt", low.generateText());
	}
	
	/**
	 * Sparse Method
	 */
	@Test
	public void test2() {
		MyText t = new MyText();
		t.read("src/nlp/resources/TextExample.txt");
		LowBow low = new LowBow(t.getText(), new StopWordsSplitter("src/nlp/resources/wordLists/stopWords.txt"));
		low.setSamplesPerTextLength(1.0);
//		low.setSigma(0.08);
		low.setSigmaAuto();
		low.setSmoothingCoeff(0.003);
		low.build();
		HeatMethod heat = new SparseHeatFlow();
		low.heatFlow(HEATTIME, heat);
		t.write("C:/Users/Pedroth/Desktop/out2.txt", low.generateText());
	}

	/**
	 * low bow with prepositions
	 */
	@Test
	public void test3() {
		MyText t = new MyText();
		t.read("src/nlp/resources/TextExample.txt");
		LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(t.getText());
		low.setSamplesPerTextLength(1.0);
//		low.setSigma(0.08);
		low.setSigmaAuto();
		low.setSmoothingCoeff(0.003);
		low.build();
		HeatMethod heat = new SparseHeatFlow();
		low.heatFlow(HEATTIME, heat);
		t.write("C:/Users/Pedroth/Desktop/out3.txt", low.generateText());
	}

	@Test
	public void test4() {
		MyText t = new MyText();
		t.read("src/nlp/resources/TextExample.txt");
		LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(t.getText());
		low.setSamplesPerTextLength(1.0);
//		low.setSigma(0.08);
		low.setSigmaAuto();
		low.setSmoothingCoeff(0.003);
		low.build();
		low.writeMatrixFile("C:/Users/Pedroth/Desktop/out4.txt");
	}

}
