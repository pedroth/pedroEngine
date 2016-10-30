package nlp.lowbow.symbolSampler;


import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import nlp.utils.Simplex;
import numeric.src.MyMath;

public class TopKSymbolWithProb implements SymbolSampler {
    private int k;

    public TopKSymbolWithProb(int k) {
        this.k = k;
    }

    @Override
    public String nextSymbol(Vector simplex, Simplex symTable) {
        StringBuilder stringBuilder = new StringBuilder();
        QuickSortWithPermutation quickSortWithPermutation = new QuickSortWithPermutation();
        int n = simplex.size();
        //safe copy
        Double[] v = new Double[n];
        for (int i = 0; i < n; i++) {
            v[i] = simplex.getX(i + 1);
        }
        quickSortWithPermutation.sort(v);
        int[] permutation = quickSortWithPermutation.getPermutation();
        for (int i = 0; i < k; i++) {
            int index = permutation[((int) MyMath.clamp(n - 1 - i, 0, n - 1))] + 1;
            stringBuilder.append(symTable.get(index)).append("( " + String.format("%.6f", simplex.getX(index)) + " )").append(" ");
        }
        return stringBuilder.toString();
    }
}