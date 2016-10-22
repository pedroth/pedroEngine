package nlp.lowbow.simpleLowBow;

import algebra.src.Vec2;
import graph.Graph;
import inputOutput.TextIO;
import nlp.lowbow.symbolSampler.SymbolSampler;
import nlp.textSplitter.SpaceSplitter;
import nlp.textSplitter.StopWordsSplitter;
import nlp.textSplitter.TextSplitter;
import nlp.utils.TextAlignment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class LowBowSummaryPrepositions extends LowBow {
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
	 * index on text. vertices from 1 to numWords correspond to words(their
	 * index corresponds image of wordsIndex(word)), vertices from (numWords +
	 * 1) to (numWords + textLength) correspond to index positions on text.
	 */
	Graph gWordsIndex;

	public LowBowSummaryPrepositions(String in) {
		super(in, new StopWordsSplitter("src/nlp/resources/wordLists/stopWordsPlusPrepositions.txt"));
		wordsTextSplitter = new StopWordsSplitter("src/nlp/resources/wordLists/stopWordsMinusPrepositions.txt");
		closeToOriginalText = wordsTextSplitter.split(in);
		prepositionMap = new HashMap<String, Boolean>();
		BufferedReader br = null;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader("src/nlp/resources/wordLists/prepositions.txt"));
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
			if (prepositionMap.containsKey(closeToOriginalText[i])) {
				isPreposition[i] = true;
			}
		}

		TextIO t1 = new TextIO();

		String acm = "";
		/**
		 * print text and close to original text
		 */
		 for (int i = 0; i < textLength; i++) {
			 acm += text[i] + "\n";
		 }
		
//		 t1.write("C:/Users/pedro/Desktop/Text1.txt", acm);
		
		 acm = "";
		
		 for (int i = 0; i < closeToOriginalText.length; i++) {
		 acm += closeToOriginalText[i] + "\n";
		 }
		
//		 t1.write("C:/Users/pedro/Desktop/Text1.txt", acm);

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
		 for (int i = 0; i < closeToOriginalText.length; i++) {
		 acm += closeToOriginalText[i] + "\t" + (prepositionIndex[i] != 0 ?
		 closeToOriginalText[prepositionIndex[i]]:"") + "\n";
		 }
		
//		 t1.write("C:/Users/pedro/Desktop/Text2.txt", acm);
		/**
		 * Alignment
		 */
		 acm = "";
		TextAlignment textAlig = new TextAlignment();
		List<Vec2> align = textAlig.align(text, closeToOriginalText);
		 for (int i = 0; i < align.size(); i++) {
		 int k = (int) align.get(i).getX();
		 int l = (int) align.get(i).getY();
		 acm += text[k] + "\t" + closeToOriginalText[l] + "\n";
		 }
//		 t1.write("C:/Users/pedro/Desktop/Text3.txt", acm);
		/**
		 * maps text to close to original text
		 */
		textToCOtextMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < align.size(); i++) {
			int key = (int) align.get(i).getX();
			int value = (int) align.get(i).getY();
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
			gWordsIndex.addEdge(simplex.get(text[i]), numWords + i + 1);
		}
		printWordsToTextGraph();
	}

	public static void main(String[] args) {
		TextIO text = new TextIO();
		text.read("C:/Users/pedro/Desktop/research/Text.txt");
		LowBowSummaryPrepositions low = new LowBowSummaryPrepositions(text.getText());
		low.setSamplesPerTextLength(1);
		low.setSmoothingCoeff(0.01);
		low.setSigma(0.009);
		low.build();
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
	public String generateText(SymbolSampler symbolSampler) {
		String acm = super.generateText(symbolSampler);
		TextSplitter spliter = new SpaceSplitter();
		String[] split = spliter.split(acm);
		for (int i = 0; i < split.length; i++) {
			String preposition = getPreposition(split[i], i);
			acm += ((preposition != null) ? preposition : "") + " " + split[i] + " \n";
		}
		return acm;
	}

	private String getPreposition(String s, int index) {
		String ans = "";
		/**
		 * compute best index from word generated at time step *
		 */
		int bestTextIndex = -1;
		int min = Integer.MAX_VALUE;
		for (Integer adj : gWordsIndex.getAdjVertex(simplex.get(s))) {
			if (adj == null) {
				return null;
			}
			int computeIndex = adj - numWords - 1;
			int cost = Math.abs(computeIndex - index);
			if (min > cost) {
				min = cost;
				bestTextIndex = computeIndex;
			}
		}

		if (textToCOtextMap.containsKey(bestTextIndex)) {
			Integer coTextIndex = textToCOtextMap.get(bestTextIndex);
			ans += (prepositionIndex[coTextIndex] != 0) ? closeToOriginalText[prepositionIndex[coTextIndex]] : "";
		}

		if (ans.equals("")) {
			return null;
		} else {
			return ans;
		}
	}

	private void printWordsToTextGraph() {
		TextIO t1 = new TextIO();
		String acm = "";
		for (int i = 1; i <= numWords; i++) {
			acm += simplex.get(i) + "|\t";
			for (int adj : gWordsIndex.getAdjVertex(i)) {
				acm += text[adj - numWords - 1] + "\t";
			}
			acm += "\n";
		}
//		t1.write("C:/Users/pedro/Desktop/Text4.txt", acm);
	}
}
