package nlp.symbolSampler;


import algebra.Vector;
import nlp.utils.Simplex;

public interface SymbolSampler {

    String nextSymbol(Vector simplex, Simplex symTable);
}
