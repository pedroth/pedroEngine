package nlp.lowbow.symbolSampler;


import algebra.src.Vector;
import nlp.utils.Simplex;

public interface SymbolSampler {

    String nextSymbol(Vector simplex, Simplex symTable);
}
