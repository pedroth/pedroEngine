package nlp.simpleDocModel;


import nlp.textSplitter.TextSplitter;
import nlp.utils.Simplex;

import javax.management.RuntimeErrorException;

public abstract class BaseDocumentModel {
    protected final String originalText;
    protected String[] text;
    protected int textLength;
    protected TextSplitter textSplitter;
    /**
     * vocabulary represented as a simplex
     */
    protected Simplex simplex;
    /**
     * number of distinct words
     */
    protected int numWords;
    /**
     * parameter c described in definition 2
     * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     */
    protected double smoothingCoeff = 1E-2;

    public BaseDocumentModel(String originalText, TextSplitter textSplitter) {
        this.originalText = originalText;
        this.simplex = new Simplex();
        this.textSplitter = textSplitter;
        this.processText(originalText);
    }

    public BaseDocumentModel(BaseDocumentModel baseDocumentModel) {
        this.originalText = baseDocumentModel.getOriginalText();
        this.simplex = baseDocumentModel.getSimplex();
        this.textSplitter = baseDocumentModel.getTextSplitter();
        this.smoothingCoeff = baseDocumentModel.getSmoothingCoeff();
        this.processText(this.originalText);
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

    public String getOriginalText() {
        return originalText;
    }

    public String[] getText() {
        return text;
    }

    public int getTextLength() {
        return textLength;
    }

    public TextSplitter getTextSplitter() {
        return textSplitter;
    }

    public Simplex getSimplex() {
        return simplex;
    }

    public void setSimplex(Simplex simplex) {
        this.simplex = simplex;
        this.processText(this.originalText);
    }

    public int getNumWords() {
        return numWords;
    }

    public double getSmoothingCoeff() {
        return smoothingCoeff;
    }

    public void setSmoothingCoeff(double smoothingCoeff) {
        if (smoothingCoeff <= 0)
            throw new RuntimeErrorException(null, "smoothing coeff must be > 0");

        this.smoothingCoeff = smoothingCoeff;
        this.processText(originalText);
    }

    public abstract void build();
}
