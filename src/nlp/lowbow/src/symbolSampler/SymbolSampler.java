package nlp.lowbow.src.symbolSampler;


import algebra.src.Vector;
import nlp.lowbow.src.simpleLowBow.Simplex;

public interface SymbolSampler {

    String nextSymbol(Vector simplex, Simplex symTable);
}
