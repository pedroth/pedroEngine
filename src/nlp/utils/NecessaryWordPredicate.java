package nlp.utils;

import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.simpleDocModel.BaseDocModelManager;
import nlp.simpleDocModel.Bow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

public class NecessaryWordPredicate implements Predicate<String> {
    private BaseDocModelManager<Bow> bowManager;
    // vector of dimension |W| (vocabulary size), representing the entropy of p(d|W=w), where w is a coordinate of wordEntropy;
    private Vector wordEntropy;
    private double cutOffPercentage;
    private double maxEntropy;
    private HashSet<String> notNecessaryWords;
    private Vector wordProb;

    public NecessaryWordPredicate(BaseDocModelManager<Bow> bowManager, double cutOffPercentage) {
        this.bowManager = bowManager;
        this.wordEntropy = computeWordEntropyPerDoc();
        this.cutOffPercentage = cutOffPercentage;
        this.maxEntropy = computeMaxEntropy();
        this.notNecessaryWords = computeNotNecessaryWords();
    }

    private HashSet<String> computeNotNecessaryWords() {
        HashSet<String> notNecessaryWords = new HashSet<>();
        for (String s : bowManager.getSimplex().wordsIndex.keySet()) {
            if (!isWordNecessary(s)) {
                notNecessaryWords.add(s);
            }
        }
        return notNecessaryWords;
    }

    private double computeMaxEntropy() {
        int size = bowManager.getDocModels().size();
        size = size == 1 ? 2 : size;
        return -Math.log(1.0 / size);
    }

    private Vector computeWordEntropyPerDoc() {
        int size = bowManager.getSimplex().size();
        Vector entropy = new Vector(size);
        wordProb = new Vector(size);
        List<Vector> aux = new ArrayList<>();
        for (Bow bow : bowManager.getDocModels()) {
            aux.add(bow.getPmf());
        }
        Matrix pwd = normalize(new Matrix(aux));
        for (int i = 1; i <= entropy.size(); i++) {
            double acc = 0;
            double acc2 = 0;
            for (int j = 1; j <= pwd.getColumns(); j++) {
                double p = pwd.getXY(i, j);
                acc += -p * Math.log(p);
                acc2 += p;
            }
            entropy.setX(i, acc);
            wordProb.setX(i, acc2);
        }
        return entropy;
    }

    private Matrix normalize(Matrix pwd) {
        Vector ones = new Vector(pwd.getColumns());
        ones.fill(1);
        Vector diag = pwd.prodVector(ones);
        diag.applyFunction(x -> 1.0 / x);
        return Matrix.diag(diag).prod(pwd);
    }

    @Override
    public boolean test(String s) {
        return !notNecessaryWords.contains(s);
    }

    private boolean isWordNecessary(String s) {
        Simplex simplex = bowManager.getSimplex();
        Integer index = simplex.get(s);
        if (index != null) {
            double entropy = wordEntropy.getX(index);
            return entropy < (1 - cutOffPercentage) * maxEntropy && entropy > maxEntropy * cutOffPercentage;
        }
        return true;
    }

    public Vector getWordEntropyForSimplex(Simplex simplex) {
        int size = simplex.size();
        Vector p = new Vector(size);
        Simplex mySimplex = bowManager.getSimplex();
        String[] keySet = simplex.getKeySet().toArray(new String[simplex.size()]);
        for (int i = 0; i < keySet.length; i++) {
            Integer index = mySimplex.get(keySet[i]);
            if (index != null) {
                p.setX(i + 1, wordEntropy.getX(index));
            }
        }
        return p;
    }

    public Vector getWordProbForSimplex(Simplex simplex) {
        int size = simplex.size();
        Vector p = new Vector(size);
        Simplex mySimplex = bowManager.getSimplex();
        String[] keySet = simplex.getKeySet().toArray(new String[simplex.size()]);
        for (int i = 0; i < keySet.length; i++) {
            Integer index = mySimplex.get(keySet[i]);
            if (index != null) {
                p.setX(i + 1, wordProb.getX(index));
            }
        }
        return p;
    }

    public double getCutOffPercentage() {
        return cutOffPercentage;
    }

    public void setCutOffPercentage(double cutOffPercentage) {
        this.cutOffPercentage = cutOffPercentage;
    }

    public Vector getWordEntropy() {
        return wordEntropy;
    }

    public HashSet<String> getNotNecessaryWords() {
        return notNecessaryWords;
    }

    public String getNotNecessaryWordString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String notNecessaryWord : notNecessaryWords) {
            stringBuilder.append(notNecessaryWord + "\n");
        }
        return stringBuilder.toString();
    }

    public void setBowManager(BaseDocModelManager<Bow> bowManager) {
        this.bowManager = bowManager;
    }
}
