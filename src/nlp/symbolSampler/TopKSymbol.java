package nlp.symbolSampler;

import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import nlp.utils.Simplex;
import numeric.src.MyMath;


public class TopKSymbol implements SymbolSampler {
    private final String separator;
    private int k;

    public TopKSymbol(int k) {
        this.k = k;
        this.separator = " ";
    }

    public TopKSymbol(int k, String separator) {
        this.k = k;
        this.separator = separator;
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
            stringBuilder.append(symTable.get(permutation[((int) MyMath.clamp(n - 1 - i, 0, n - 1))] + 1)).append(i < k - 1 ? separator : "");
        }
        return stringBuilder.toString();
    }
}
