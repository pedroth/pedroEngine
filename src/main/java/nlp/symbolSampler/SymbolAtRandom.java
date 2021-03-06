package nlp.symbolSampler;


import algebra.Vector;
import nlp.utils.Simplex;
import numeric.SimplexPointSampler;

public class SymbolAtRandom implements SymbolSampler {

    /**
     * generates word by sampling simplex of words.
     *
     * @param simplex
     * @param symTable
     * @return
     */
    @Override
    public String nextSymbol(Vector simplex, Simplex symTable) {
        SimplexPointSampler r = new SimplexPointSampler(simplex.getArray());
        int key = r.nextSymbol();
        return symTable.get(key + 1);
    }
}
