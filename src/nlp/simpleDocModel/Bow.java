package nlp.simpleDocModel;


import algebra.src.Vector;
import nlp.textSplitter.TextSplitter;

/**
 * The type Bow.
 */
public class Bow extends BaseDocumentModel {

    /**
     * The Pmf probability mass function.Is a |V|* 1 vector that sums to 1.
     */
    protected Vector pmf;

    /**
     * Instantiates a new Bow.
     *
     * @param originalText the original text
     * @param textSplitter the text splitter
     */
    public Bow(String originalText, TextSplitter textSplitter) {
        super(originalText, textSplitter);
    }

    public Bow(Bow bow) {
        super(bow);
        this.pmf = bow.pmf;
    }

    protected void processText(String in) {
        text = textSplitter.split(in);
        int acmIndex = 0;
        for (int i = 0; i < text.length; i++) {
            Integer aux = simplex.get(text[i]);
            if (aux == null) {
                acmIndex++;
                simplex.put(text[i], acmIndex);
            }
        }
        textLength = text.length;
        numWords = simplex.size();
    }

    public void build() {
        pmf = new Vector(numWords);
        int n = textLength;
        int m = numWords;
        smoothingCoeff = Math.max(0.0, smoothingCoeff);
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                pmf.setX(j, ((simplex.get(text[i - 1]) == j) ? 1.0 : 0.0) + pmf.getX(j));
            }
        }
        Vector ones = new Vector(m);
        ones.fill(1.0);
        pmf.applyFunction(x -> x + smoothingCoeff);
        pmf = Vector.scalarProd(1.0 / Vector.innerProd(pmf, ones), pmf);
    }
}
