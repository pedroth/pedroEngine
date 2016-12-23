package numeric.src;

import inputOutput.TextIO;
import nlp.textSplitter.TextSplitter;

import java.util.HashMap;
import java.util.HashSet;

/**
 * The type My set.
 */
public class MySet {
    /**
     * The Set.
     */
    HashMap<String, Boolean> set;

    /**
     * Instantiates a new My set.
     *
     * @param input the input
     */
    public MySet(String[] input) {
        set = new HashMap<>();
        for (String s : input) {
            set.put(s, true);
        }
    }


    /**
     * Instantiates a new My set.
     */
    public MySet() {
        set = new HashMap<>();
    }

    public MySet(HashSet<String> input) {
        set = new HashMap<>();
        for (String s : input) {
            set.put(s, true);
        }
    }

    /**
     * Union my set.
     *
     * @param a the a
     * @param b the b
     * @return the my set
     */
    public static MySet union(MySet a, MySet b) {
        String[] setA = a.getSet();
        String[] setB = b.getSet();
        MySet set = new MySet();
        for (int i = 0; i < setA.length; i++) {
            set.add(setA[i]);
        }
        for (int i = 0; i < setB.length; i++) {
            set.add(setB[i]);
        }
        return set;
    }

    /**
     * Diff my set.
     *
     * @param a the a
     * @param b the b
     * @return the my set
     */
    public static MySet diff(MySet a, MySet b) {
        MySet ans = a.copy();
        String[] setB = b.getSet();
        for (int i = 0; i < setB.length; i++) {
            Boolean aux = a.find(setB[i]);
            if (aux != null) {
                ans.remove(setB[i]);
            }
        }
        return ans;
    }

    /**
     * Intersection my set.
     *
     * @param a the a
     * @param b the b
     * @return the my set
     */
    public static MySet intersection(MySet a, MySet b) {
        MySet ans = a.copy();
        String[] set = a.getSet();
        for (int i = 0; i < set.length; i++) {
            Boolean aBoolean = b.find(set[i]);
            if (aBoolean != null && aBoolean) {
                ans.add(set[i]);
            }
        }
        return ans;
    }

    /**
     * Jaccard index.
     *
     * @param a the a
     * @param b the b
     * @return the double
     */
    public static double jaccardIndex(MySet a, MySet b) {
        return 1.0 * MySet.intersection(a, b).size() / MySet.union(a, b).size();
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        TextIO t1 = new TextIO();
        t1.read("src/nlp/resources/wordLists/stopWords.txt");
        TextIO t2 = new TextIO();
        t2.read("src/nlp/resources/wordLists/prepositions.txt");
        String s1 = t1.getText();
        String s2 = t2.getText();
        TextSplitter splitter = in -> in.split("\\r?\\n");
        MySet a = new MySet(splitter.split(s1));
        MySet b = new MySet(splitter.split(s2));
        System.out.println("<<<Union>>>");
        System.out.println(MySet.union(a, b));
        System.out.println("<<<Diff>>>");
        System.out.println(MySet.diff(a, b));
        System.out.println("<<<Intersection>>>");
        System.out.println(MySet.intersection(a, b));
        System.out.println("<<<Jaccard index>>>");
        System.out.println(MySet.jaccardIndex(a, b));
    }

    /**
     * Get set.
     *
     * @return the string [ ]
     */
    public String[] getSet() {
        return set.keySet().toArray(new String[0]);
    }

    /**
     * Find boolean.
     *
     * @param s the s
     * @return the boolean
     */
    public Boolean find(String s) {
        return set.get(s);
    }

    /**
     * Copy my set.
     *
     * @return the my set
     */
    public MySet copy() {
        String[] setA = this.getSet();
        return new MySet(setA);
    }

    /**
     * Remove void.
     *
     * @param s the s
     */
    public void remove(String s) {
        set.remove(s);
    }

    /**
     * Add void.
     *
     * @param s the s
     */
    public void add(String s) {
        if (!set.containsKey(s)) {
            set.put(s, true);
        }
    }

    public String toString() {
        String[] aux = this.getSet();
        String acm = "";
        for (int i = 0; i < aux.length; i++) {
            acm += aux[i] + "\n";
        }
        return acm;
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return set.size();
    }

}
