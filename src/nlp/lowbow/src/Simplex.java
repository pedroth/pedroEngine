package nlp.lowbow.src;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
/**
 * 
 * @author Pedroth
 *
 */
public class Simplex {
	/**
	 * maps words to the corresponding coordinate. coordinates index starts at
	 * 1.
	 */
	protected Map<String, Integer> wordsIndex;
	/**
	 * inverse map of wordsIndex
	 */
	protected Map<Integer, String> wordsIndexInv;

	public Simplex() {
		super();
		wordsIndex = new TreeMap<>();
		wordsIndexInv = new TreeMap<>();
	}

	public Integer get(String key) {
		return wordsIndex.get(key);
	}

	public String get(Integer key) {
		return wordsIndexInv.get(key);
	}

	public void put(String x, Integer y) {
		wordsIndex.put(x, y);
		wordsIndexInv.put(y, x);
	}
	
	public int size() {
		return wordsIndex.size();
	}
	
	public Set<String> getKeySet() {
		return wordsIndex.keySet();
	}

	@Override
	public String toString() {
		return "AbstractSimplex{" +
				"wordsIndex=" + wordsIndex +
				", wordsIndexInv=" + wordsIndexInv +
				'}';
	}
}
