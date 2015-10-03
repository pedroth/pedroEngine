package nlp.lowbow.tests;

import inputOutput.MyText;
import nlp.lowbow.HeatMethod;
import nlp.lowbow.LowBow;
import nlp.lowbow.LowBowSummaryPrepositions;
import nlp.lowbow.MatrixHeatFlow;
import nlp.lowbow.SparseHeatFlow;
import nlp.textSplitter.StopWordsSplitter;

public class LowBowTests {
	public static final double HEATTIME = 0.01;
	/**
	 * Matrix Method
	 */
	public static void test1() {
		MyText t = new MyText();
		t.read("C:/Users/Pedroth/Desktop/research/Text.txt");
		LowBow low = new LowBow(t.getText(), new StopWordsSplitter("wordsLists/stopWords.txt"));
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
	public static void test2() {
		MyText t = new MyText();
		t.read("C:/Users/Pedroth/Desktop/research/Text.txt");
		LowBow low = new LowBow(t.getText(), new StopWordsSplitter("wordsLists/stopWords.txt"));
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
	public static void test3() {
		MyText t = new MyText();
		t.read("C:/Users/Pedroth/Desktop/research/Text.txt");
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
	
	public static void test4() {
		MyText t = new MyText();
		t.read("C:/Users/Pedroth/Desktop/research/Text.txt");
		LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(t.getText());
		low.setSamplesPerTextLength(1.0);
//		low.setSigma(0.08);
		low.setSigmaAuto();
		low.setSmoothingCoeff(0.003);
		low.build();
		low.writeMatrixFile("C:/Users/Pedroth/Desktop/out4.txt");
	}

	public static void main(String[] args) {
//		test1();
//		test2();
//		test3();
		test4();
		
//		MyText t = new MyText();
//		t.read("C:/Users/Pedroth/Desktop/research/Text.txt");
//		TextSplitter txtSplit = new StopWordsSplitter("wordsLists/stopWords.txt");
//		String[] txt = txtSplit.split(t.getText());
//		String acc = "";
//		for (int i = 0; i < txt.length; i++) {
//			acc += txt[i] + ((i < (txt.length - 1)) ? "\n":"");
//		}
//		t.write("C:/Users/Pedroth/Desktop/out4.txt", acc);
	}

}
