package nlp.symbolSampler;


import algebra.Vector;
import nlp.utils.Simplex;

public class SymbolAtAverage implements SymbolSampler {

    @Override
    public String nextSymbol(Vector simplex, Simplex symTable) {
        double acc = 0;
        for (int i = 1; i <= simplex.getDim(); i++) {
            acc += i * simplex.getX(i);
        }

        acc = Math.ceil(acc);
        return symTable.get((int) acc);
    }
}
