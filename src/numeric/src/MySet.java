package numeric.src;

import inputOutput.TextIO;
import nlp.textSplitter.TextSplitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * The type My set.
 */
public class MySet<T> implements Iterable {
    /**
     * The Set.
     */
    private HashMap<T, Boolean> set;

    /**
     * Instantiates a new My set.
     *
     * @param input the input
     */
    public MySet(T[] input) {
        set = new HashMap<>();
        for (T s : input) {
            set.put(s, true);
        }
    }


    /**
     * Instantiates a new My set.
     */
    public MySet() {
        set = new HashMap<>();
    }

    public MySet(HashSet<T> input) {
        set = new HashMap<>();
        for (T s : input) {
            set.put(s, true);
        }
    }

    public MySet(Iterable<T> input) {
        this.set = new HashMap<>();
        for (T s : input) {
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
        MySet set = new MySet();
        for (Object s : a) {
            set.add(s);
        }
        for (Object s : b) {
            set.add(s);
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
        for (Object s : b) {
            Boolean aux = a.find(s);
            if (aux != null) {
                ans.remove(s);
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
        for (Object s : a) {
            Boolean aBoolean = b.find(s);
            if (aBoolean != null && aBoolean) {
                ans.add(a);
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
    public static void main(String[] args) throws IOException {
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
     * Find boolean.
     *
     * @param s the s
     * @return the boolean
     */
    public Boolean find(T s) {
        return set.get(s);
    }

    /**
     * Copy my set.
     *
     * @return the my set
     */
    public MySet copy() {
        return new MySet(this);
    }

    /**
     * Remove void.
     *
     * @param s the s
     */
    public void remove(T s) {
        set.remove(s);
    }

    /**
     * Add void.
     *
     * @param s the s
     */
    public void add(T s) {
        if (!set.containsKey(s)) {
            set.put(s, true);
        }
    }

    public String toString() {
        StringBuilder acm = new StringBuilder();
        for (T s : this.set.keySet()) {
            acm.append(s + "\n");
        }
        return acm.toString();
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return set.size();
    }

    @Override
    public Iterator iterator() {
        return set.keySet().iterator();
    }
}
