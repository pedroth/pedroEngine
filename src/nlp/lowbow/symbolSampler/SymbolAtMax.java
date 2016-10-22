package nlp.lowbow.symbolSampler;

import algebra.src.Vector;
import nlp.utils.Simplex;

public class SymbolAtMax implements SymbolSampler {
    /**
     * generates word based on the maximum probable word in the simplex
     *
     * @param simplex
     * @param symTable
     * @return
     */
    @Override
    public String nextSymbol(Vector simplex, Simplex symTable) {
        int n = simplex.getDim();
        int key = 0;
        double max = Double.MIN_VALUE;
        for (int i = 1; i <= n; i++) {
            double value = simplex.getX(i);
            if (max < value) {
                max = value;
                key = i;
            }
        }
        return symTable.get(key);
    }
}
