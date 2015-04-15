package nlp;

import graph.Graph;
import inputOutput.MyText;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.HashMap;

import nlp.textSplitter.SpaceSplitter;
import nlp.textSplitter.StopWordsSplitter;
import nlp.textSplitter.TextSplitter;
import algebra.Vec2;

public class LowBowSummaryPrepositions extends LowBowSummary {
	String[] closeToOriginalText;
	boolean[] isPreposition;
	/**
	 * array which at position i points to k \in {0, ... , i} 0 means there is
	 * no prepositions before position i
	 */
	int[] prepositionIndex;
	HashMap<String, Boolean> prepositionMap;
	HashMap<Integer, Integer> MapMyTextToCloseToOriginalext;
	StopWordsSplitter wordsTextSplitter;
	/**
	 * function that maps elements of text to elements of closeToOriginalText,
	 * based on the alignment. The alignment produces a non injective function
	 * between close to original text and text. So there is no inverse function,
	 * but with some constraint on the domain we are able to invert it. We
	 * transform the function into a injective one by choosing the first
	 * connection(TODO: explain better this).
	 */
	HashMap<Integer, Integer> textToCOtextMap;
	/**
	 * graph that builds a relation between words from the vocabulary and their
	 * index on text. vertices from 1 to numWords correspond to words, vertices
	 * from numWords+1 to textLength correspond to index positions on text.
	 */
	Graph gWordsIndex;

	public LowBowSummaryPrepositions(String in) {
		super(in, new StopWordsSplitter("src/nlp/wordsLists/stopWordsPlusPrepositions.txt"));
		wordsTextSplitter = new StopWordsSplitter("src/nlp/wordsLists/stopWordsMinusPrepositions.txt");
		closeToOriginalText = wordsTextSplitter.split(in);
		prepositionMap = new HashMap<String, Boolean>();
		BufferedReader br = null;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader("src/nlp/wordsLists/prepositions.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				prepositionMap.put(sCurrentLine, true);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		isPreposition = new boolean[closeToOriginalText.length];
		prepositionIndex = new int[closeToOriginalText.length];

		for (int i = 0; i < closeToOriginalText.length; i++) {
			if (prepositionMap.get(closeToOriginalText[i]) != null) {
				isPreposition[i] = true;
			}
		}

		MyText t1 = new MyText();

		String acm = "";
		/**
		 * print text and close to original text
		 */
		//
		// for (int i = 0; i < textLength; i++) {
		// acm += text[i] + "\n";
		// }
		//
		// t1.write("C:/Users/pedro/Desktop/Text1.txt", acm);
		//
		// acm = "";
		//
		// for (int i = 0; i < closeToOriginalText.length; i++) {
		// acm += closeToOriginalText[i] + "\n";
		// }
		//
		// t1.write("C:/Users/pedro/Desktop/Text2.txt", acm);

		for (int i = 0; i < closeToOriginalText.length; i++) {
			if (!isPreposition[i]) {
				int index = searchPrepositionIndex(i);
				prepositionIndex[i] = index;
				/**
				 * check if there are two prepositions next to each others
				 */
				if ((index - 1) > 0 && isPreposition[index - 1]) {
					prepositionIndex[index] = index - 1;
				}
			}
		}

		acm = "";
		/**
		 * print graph prepositions on close to original text
		 */
		// for (int i = 0; i < closeToOriginalText.length; i++) {
		// acm += closeToOriginalText[i] + "\t" + (prepositionIndex[i] != 0 ?
		// closeToOriginalText[prepositionIndex[i]]:"") + "\n";
		// }
		//
		// t1.write("C:/Users/pedro/Desktop/Text2.txt", acm);
		/**
		 * Alignment
		 */
		// acm = "";
		TextAlignment textAlig = new TextAlignment();
		Vec2[] align = textAlig.align(text, closeToOriginalText);
		// for (int i = 0; i < align.length; i++) {
		// int k = (int) align[i].getX();
		// int l = (int) align[i].getY();
		// acm += text[k] + "\t" + closeToOriginalText[l] + "\n";
		// }
		// t1.write("C:/Users/pedro/Desktop/Text2.txt", acm);
		/**
		 * maps text to close to original text
		 */
		textToCOtextMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < align.length; i++) {
			int key = (int) align[i].getX();
			int value = (int) align[i].getY();
			if (!textToCOtextMap.containsKey(key)) {
				textToCOtextMap.put(key, value);
			}
		}
		/**
		 * words to index correspondence
		 */
		gWordsIndex = new Graph();
		for (int i = 1; i <= numWords; i++) {
			gWordsIndex.addVertex(i);
		}
		for (int i = 0; i < textLength; i++) {
			gWordsIndex.addEdge(wordsIndex.get(text[i]), numWords + i + 1);
		}
//		acm = "";
//		for (int i = 1; i <= numWords; i++) {
//			acm += wordsIndexInv.get(i) + "|\t";
//			for (int adj : gWordsIndex.getEdges(i)) {
//				acm += text[adj - numWords - 1] + "\t";
//			}
//			acm += "\n";
//		}
//		t1.write("C:/Users/pedro/Desktop/Text3.txt", acm);
	}

	private int searchPrepositionIndex(int i) {
		int ans = 0;
		for (int j = i - 1; j >= 0; j--) {
			if (isPreposition[j]) {
				ans = j;
				break;
			}
		}
		return ans;
	}
	
	@Override
	public String getSummary(double lambda) {
		String acm = super.getSummary(lambda);
		TextSplitter spliter = new SpaceSplitter();
		String[] split = spliter.split(acm);
		return acm;
	}

	public static void main(String[] args) {
		MyText text = new MyText();
		text.read("C:/Users/pedro/Desktop/research/Text.txt");
		LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(text.getText());
		low.setSamplesPerTextLength(1);
		low.setSmoothingCoeff(0.01);
		low.setSigma(0.009);
		low.init();
		low.getSummary(0.1);
	}
}
