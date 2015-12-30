package numeric.src;

import inputOutput.MyText;
import nlp.textSplitter.TextSplitter;

import java.util.HashMap;

public class MySet {
	HashMap<String, Boolean> set;
	public MySet(String[] input) {
		set = new HashMap<String, Boolean>();
		for(String s : input) {
			set.put(s, true);
		}
	}
	
	public static MySet union(MySet a, MySet b) {
		String[] setA = a.getSet();
		String[] setB = b.getSet();
		int n = setA.length + setB.length;
		int index = 0;
		String[] setC = new String[n];
		for (int i = 0; i < setA.length; i++) {
			setC[index++] = setA[i];
		}
		for (int i = 0; i < setB.length; i++) {
			setC[index++] = setB[i];
		}
		return new MySet(setC);
	}
	
	public static MySet diff(MySet a, MySet b) {
		MySet ans = a.copy();
		String[] setB = b.getSet();
		for (int i = 0; i < setB.length; i++) {
			Boolean aux = a.find(setB[i]);
			if(aux != null) {
				ans.remove(setB[i]);
			}
		}
		return ans;
	}
	
	public static void main(String[] args) {
		MyText t1 = new MyText();
		t1.read("C:/Users/pedro/Desktop/research/resources/stopWords.txt");
		MyText t2 = new MyText();
		t2.read("C:/Users/pedro/Desktop/research/resources/prepositions.txt");
		String s1 = t1.getText();
		String s2 = t2.getText();
		TextSplitter splitter = new TextSplitter() {

			@Override
			public String[] split(String s) {
				return s.split("\\r?\\n");
			}
		};
		MySet a = new MySet(splitter.split(s1));
		MySet b = new MySet(splitter.split(s2));
		MySet c = MySet.union(a, b);
		System.out.println("<<<Union>>>");
		System.out.println(c);
		System.out.println("<<<Diff>>>");
		c = MySet.diff(a, b);
		System.out.println(c);
	}

	public String[] getSet() {
		return set.keySet().toArray(new String[0]);
	}

	public Boolean find(String s) {
		return set.get(s);
	}

	public MySet copy() {
		String[] setA = this.getSet();
		return new MySet(setA);
	}

	public void remove(String s) {
		set.remove(s);
	}

	public void add(String s) {
		set.put(s, true);
	}

	public String toString() {
		String[] aux = this.getSet();
		String acm = "";
		for (int i = 0; i < aux.length; i++) {
			acm += aux[i] + "\n";
		}
		return acm;
	}
	
}
