package nlp.lowbow.src.symbolSampler;


import algebra.src.Vector;
import nlp.lowbow.src.Simplex;

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
