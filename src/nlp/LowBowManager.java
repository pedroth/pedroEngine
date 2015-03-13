package nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import numeric.Pca;
import algebra.Vector;
/**
 * 
 * @author pedro
 *
 */
public class LowBowManager {
	private ArrayList<LowBow> lowbows;
	private HashMap<String, Integer> wordIndex;
	private HashMap<Integer, String> wordIndexInv;
	
	public LowBowManager() {
		lowbows = new ArrayList<LowBow>();
		wordIndex = new HashMap<String, Integer>();
		wordIndexInv = new HashMap<Integer, String>();
	}

	public void add(LowBow l) {
		lowbows.add(l);
		HashMap<String, Integer> lVocabulary = l.getWordsIndex();
		Set<String> keys = lVocabulary.keySet();
		int acmIndex = wordIndex.size();
		for (String s : keys) {
			Integer aux = wordIndex.get(s);
			if (aux == null) {
				acmIndex++;
				wordIndex.put(s, acmIndex);
				wordIndexInv.put(acmIndex, s);
			}
		}
	}

	public void init() {
		int n = lowbows.size();
		for (int i = 0; i < n; i++) {
			LowBow temp = lowbows.get(i);
			LowBow lowbow = new LowBow(temp.getOriginalText(), wordIndex, wordIndexInv);
			lowbow.setSmoothingCoeff(temp.getSmoothingCoeff());
			lowbow.init(temp.getSamplesPerTextLength(), temp.getSigma());
			lowbows.set(i, lowbow);
		}
	}
	
	public void buildPca() {
		int size = 0;
		for (LowBow lowbow : lowbows) {
			size+=lowbow.getSamples();
		}
		Vector[] data = new Vector[size];
		
		int n = lowbows.size();
		
		int index = 0;
		for(int i = 0; i < n; i++) {
			Vector[] curve = lowbows.get(i).getCurve();
			for(int j = 0; j < curve.length; j++) {
				data[index] = curve[j];
				index++;
			}
		} 
		Pca pca = new Pca();
		Vector[] pc = pca.getNPca(data, 3);
		Vector myu = pca.getAverage();
		for (LowBow lowbow : lowbows) {
			lowbow.buildPca(pc, myu);
		}
	}

	public ArrayList<LowBow> getLowbows() {
		return lowbows;
	}
	
	public void removeAll() {
		lowbows.removeAll(lowbows);
		wordIndex = new HashMap<String, Integer>();
	}
}
