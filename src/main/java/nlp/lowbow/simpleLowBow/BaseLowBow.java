package nlp.lowbow.simpleLowBow;


import algebra.Matrix;
import algebra.Vector;
import nlp.simpleDocModel.BaseDocumentModel;
import nlp.symbolSampler.SymbolSampler;
import nlp.textSplitter.TextSplitter;
import nlp.utils.Simplex;

public abstract class BaseLowBow extends BaseDocumentModel {
    /**
     * n by m matrix where n is the number of words and m is the number of words
     * in the dictionary
     * <p>
     * is the delta in definition 2 of http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     */
    protected Matrix rawCurve;

    public BaseLowBow(String originalText, TextSplitter textSplitter) {
        super(originalText, textSplitter);
    }

    public BaseLowBow(String originalText, TextSplitter textSplitter, Simplex simplex) {
        super(originalText, textSplitter);
        setSimplex(simplex);
    }

    public BaseLowBow(BaseLowBow lowBow) {
        super(lowBow);
    }

    public void build() {
        rawCurve = new Matrix(textLength, numWords);
        int n = rawCurve.getRows();
        int m = rawCurve.getColumns();
        smoothingCoeff = Math.max(0.0, smoothingCoeff);
        double norm = 1 + smoothingCoeff * m;
        for (int i = 1; i <= n; i++) {
            if (smoothingCoeff == 0.0) {
                rawCurve.setXY(i, simplex.get(text[i - 1]), 1.0);
            } else {
                for (int j = 1; j <= m; j++) {
                    rawCurve.setXY(i, j, (simplex.get(text[i - 1]) == j) ? ((1.0 + smoothingCoeff) / norm) : (smoothingCoeff / norm));
                }
            }
        }
    }

    public Matrix getRawCurve() {
        return rawCurve;
    }

    /**
     * @return text based on the symbol sampler
     */
    public abstract String generateText(SymbolSampler symbolSampler);

    public Vector[] getCurve() {
        return getRawCurve().getRowsVectors();
    }
}
