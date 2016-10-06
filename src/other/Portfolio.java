package other;

import inputOutput.TextIO;
import nlp.textSplitter.CharacterSplitter;
import nlp.utils.TextAlignment;

import java.util.*;

public class Portfolio {


	public static double dist(String x, String y) {
//		String sBig = x.length() > y.length() ? x:y;
//		String sSmall = x.length() <= y.length() ? x:y;
//		double acc = 0;
//		double cost = 1;
//		for (int i = 0; i < sBig.length(); i++) {
//			acc += (Math.abs(sBig.charAt(i) - (i < sSmall.length() ? sSmall.charAt(i):0)) != 0) ? cost : 0;
//		}
//		return acc;

		CharacterSplitter splitter = new CharacterSplitter();
		String[] s1 = splitter.split(x);
		String[] s2 = splitter.split(y);
		TextAlignment align = new TextAlignment();
		align.align(s1, s2);
		return align.getMaxNormSim();

	}

	public static void main(String[] args) {
		/*
		 *  Reading
		 */
		TextIO t1 = new TextIO();
		t1.read("C:/Users/Pedroth/Desktop/Tags.txt");
		String s = t1.getText();
		String[] vec = s.toLowerCase().split("\n");
		/*
		 * Map creation
		 */
		Map<String, Integer> mapTagsToCount = new TreeMap<String, Integer>();
		for (int i = 0; i < vec.length; i++) {
//			vec[i] = vec[i].replaceAll("(\t)+", "");
			Integer aux = mapTagsToCount.get(vec[i]);
			if (aux == null) {
				mapTagsToCount.put(vec[i], 1);
			}else {
				aux++;
				mapTagsToCount.put(vec[i], aux);
			}
		}
		/*
		 * Printing
		 */
		StringBuilder stringBuilder = new StringBuilder();
		Set<String> keys = mapTagsToCount.keySet();
		for (String k : keys) {
			stringBuilder.append(k + "\t" + mapTagsToCount.get(k) + "\n");
		}
		t1.write("C:/Users/Pedroth/Desktop/TagsHist.txt", stringBuilder.toString());
		/*
		 * distance matrix
		 */
		String[] keysArray = keys.toArray(new String[0]);
		double[][] dist = new double[keysArray.length][keysArray.length];
		for (int i = 0; i < keysArray.length; i++) {
			for (int j = 0; j < keysArray.length; j++) {
				dist[i][j] = Portfolio.dist(keysArray[i],keysArray[j]);
			}
		}
		/*
		 * Print distance matrix 
		 */
		stringBuilder = new StringBuilder(dist.length * dist.length).append("\t");
		for (int i = 0; i < dist.length; i++) {
			stringBuilder.append(keysArray[i] + ",");
		}
		stringBuilder.append("\n");
		for (int i = 0; i < dist.length; i++) {
			stringBuilder.append(keysArray[i] + ",");
			for (int j = 0; j < dist.length; j++) {
				stringBuilder.append(dist[i][j] + ",");
			}
			stringBuilder.append("\n");
		}
		t1.write("C:/Users/Pedroth/Desktop/TagsDist.txt", stringBuilder.toString());
		
		/*
		 * Clustering
		 */
		Map<String, Integer> mapCluster = new TreeMap<String, Integer>();
		Map<String, List<String>> clusters = new TreeMap<String, List<String>>();
		boolean[] alreadyCluster = new boolean[dist.length];
		double epsilon = 0.6;
		for (int i = 0; i < dist.length; i++) {
			if(!alreadyCluster[i]) {
				mapCluster.put(keysArray[i], mapTagsToCount.get(keysArray[i]));
				alreadyCluster[i] = true;
				clusters.put(keysArray[i], new ArrayList<String>());
			} else {
				continue;
			}
			for (int j = 0; j < dist.length; j++) {
				if(dist[i][j] >= epsilon && !alreadyCluster[j]) {
					int countMain = mapCluster.get(keysArray[i]);
					int countSub = mapTagsToCount.get(keysArray[j]);
					mapCluster.put(keysArray[i], countMain + countSub);
					alreadyCluster[j] = true;
					List<String> aux = clusters.get(keysArray[i]);
					aux.add(keysArray[j]);
					clusters.put(keysArray[i], aux);
				}
			}
		}
		
		/*
		 * Printing
		 */
		stringBuilder = new StringBuilder(mapCluster.keySet().size());
		keys = mapCluster.keySet();
		for (String k : keys) {
			stringBuilder.append(k + "\t" + mapCluster.get(k) + "\n");
			System.out.println(k + "\t" + clusters.get(k));
		}
		t1.write("C:/Users/Pedroth/Desktop/TagsClusterHist.txt", stringBuilder.toString());
	}
}
