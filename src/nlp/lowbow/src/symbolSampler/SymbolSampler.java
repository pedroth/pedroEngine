package nlp.lowbow.src.symbolSampler;


import algebra.src.Vector;
import nlp.lowbow.src.Simplex;

public interface SymbolSampler {

    String nextSymbol(Vector simplex, Simplex symTable);
}
