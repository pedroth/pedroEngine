package nlp.lowbow.src.symbolSampler;

import algebra.src.Vector;
import algorithms.QuickSortWithPermutation;
import nlp.lowbow.src.Simplex;
import numeric.src.MyMath;

/**
 * Created by Pedroth on 4/10/2016.
 */
public class SymbolAtMaxPos implements SymbolSampler {
    private int pos = 0;

    public SymbolAtMaxPos(int pos) {
        this.pos = pos;
    }

    @Override
    public String nextSymbol(Vector simplex, Simplex symTable) {
        QuickSortWithPermutation quickSortWithPermutation = new QuickSortWithPermutation();
        double[] array = simplex.getArray();
        int n = array.length;
        Double[] v = new Double[n];
        for (int i = 0; i < n; i++) {
            v[i] = array[i];
        }
        quickSortWithPermutation.sort(v);
        int[] permutation = quickSortWithPermutation.getPermutation();
        return symTable.get(permutation[((int) MyMath.clamp(n - 1 - pos, 0, n - 1))] + 1);
    }

}
