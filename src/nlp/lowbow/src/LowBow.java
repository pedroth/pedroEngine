package nlp.lowbow.src;

import algebra.src.Matrix;
import algebra.src.Vec3;
import algebra.src.Vector;
import inputOutput.MyText;
import nlp.lowbow.src.symbolSampler.SymbolSampler;
import nlp.textSplitter.StopWordsSplitter;
import nlp.textSplitter.TextSplitter;
import numeric.src.MyMath;
import numeric.src.Pca;

import javax.management.RuntimeErrorException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Lowbow implementation as described in http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
 *
 * @author pedro
 */
public class LowBow {
    protected String originalText;
    protected String[] text;
    protected int textLength;
    protected TextSplitter textSplitter;
    /**
     * vocabulary represented as a simplex
     */
    protected Simplex simplex;
    protected int numWords;
    /**
     * n by m matrix where n is the number of words and m is the number of words
     * in the dictionary
     * <p>
     * is the delta in definition 2 of http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     */
    protected Matrix rawCurve;
    protected Vector[] curve;
    protected Vec3[] pcaCurve;

    protected int samples;
    /**
     * 1.0 / (samples - 1)
     */
    protected double step;

    protected double sigma;
    protected double samplesPerTextLength = 1.0;
    /**
     * parameter c described in definition 2
     * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     */
    protected double smoothingCoeff = 1E-2;

    protected boolean isBuild;

    public LowBow(String in, TextSplitter textSplitter) {
        originalText = in;
        this.simplex = new Simplex();
        this.textSplitter = textSplitter;
        isBuild = false;
        this.processText(in);
        sigma = getSigmaAuto();
    }

    public LowBow(String in, TextSplitter textSplitter, Simplex simplex) {
        originalText = in;
        this.simplex = simplex;
        this.textSplitter = textSplitter;
        isBuild = false;
        this.processText(in);
        sigma = getSigmaAuto();
    }

    /**
     * Example
     */
    public static void main(String[] args) {
        MyText t = new MyText();
        t.read("C:/Users/pedro/Desktop/research/Text.txt");
//		LowBow low = new LowBow("a c c c b b a c c", new SpaceSplitter());
        LowBow low = new LowBow(t.getText(), new StopWordsSplitter("wordsLists/stopWords.txt"));
        low.setSamplesPerTextLength(1.0);
        low.setSigma(0.08);
        low.setSmoothingCoeff(0.003);
        low.build();
        HeatMethod heat = new MatrixHeatFlow();
        low.heatFlow(0.01, heat);
//		low.writeMatrixFile();
        System.out.println(low);
    }

    private void processText(String in) {
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

    /**
     * As described in the definition 6 of
     * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     */
    protected double gamma(double myu, double sigma, int samples, int j) {
        double acc = 0;
        double h = 1.0 / (samples - 1);
        double x = 0;
        /**
         * trapezoidal method of integration
         */
        for (int i = 0; i < samples - 1; i++) {
            acc += MyMath.kernel(x, myu, sigma) * psi(x, j) + MyMath.kernel(x + h, myu, sigma) * psi(x + h, j);
            x += h;
        }
        return acc * 0.5 * h;
    }

    /**
     * As described in the definition 6 of
     * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     */
    protected double gammaEfficient(double myu, double sigma, int samples, int j) {
        double acc = 0;
        double h = 1.0 / (samples - 1);
        int scale = 3;
        int indexLowerBound = (int) (Math.floor(myu * (samples - 1)) + Math.floor(-(scale * sigma) * (samples - 1)));
        int indexUpperBound = (int) (Math.floor(myu * (samples - 1)) + Math.ceil((scale * sigma) * (samples - 1)));
        indexLowerBound = indexLowerBound <= 0 ? 0 : indexLowerBound;
        indexUpperBound = indexUpperBound >= samples - 1 ? samples - 1 : indexUpperBound;
        double x = indexLowerBound * h;
        /**
         * trapezoidal method of integration
         */
        for (int i = indexLowerBound; i < indexUpperBound; i++) {
            acc += MyMath.kernel(x, myu, sigma) * psi(x, j) + MyMath.kernel(x + h, myu, sigma) * psi(x + h, j);
            x += h;
        }
        return acc * 0.5 * h;
    }

    public void build() {
        build(samplesPerTextLength, sigma);
    }

    public void build(double samplesPerTextLength, double sigma) {
        rawCurve = new Matrix(textLength, numWords);
        int n = rawCurve.getRows();
        int m = rawCurve.getColumns();
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

        resample(samplesPerTextLength, sigma);
        isBuild = true;
    }

    public void resample(double samplesPerTextLength, double sigma) {
        this.samplesPerTextLength = samplesPerTextLength;
        this.sigma = sigma;
        /**
         * construct curve
         */
        samples = (int) Math.floor(samplesPerTextLength * textLength);
        step = 1.0 / (samples - 1);
        curve = new Vector[samples];
        double myu = 0;
        for (int i = 0; i < samples; i++) {
            curve[i] = new Vector(numWords);
            for (int j = 1; j <= numWords; j++) {
                curve[i].setX(j, gammaEfficient(myu, sigma, samples, j));
            }
            /**
             * normalization, because the integration is just an approximation
             * error decreases as number of samples increases
             */
            Vector ones = new Vector(numWords);
            ones.fill(1.0);
            double dot = Vector.innerProd(curve[i], ones);
            curve[i] = Vector.scalarProd(1 / dot, curve[i]);
            myu += step;
        }
    }

    public void buildPca() {
        if (!isBuild)
            throw new RuntimeErrorException(null, "LowBow not initialized");
        /**
         * pca
         */
        Pca pca = new Pca();
        Vector[] pc = pca.getNPca(curve, 3);
        Vector myu = pca.getAverage();
        pcaCurve = new Vec3[samples];
        for (int i = 0; i < samples; i++) {
            Vector v = Vector.diff(curve[i], myu);
            pcaCurve[i] = new Vec3(Vector.innerProd(pc[0], v), Vector.innerProd(pc[1], v), Vector.innerProd(pc[2], v));
        }
        // writeObjFile(false);
    }

    /**
     * @param pc  are the principal components
     * @param myu is the average point
     */
    public void buildPca(Vector[] pc, Vector myu) {
        if (!isBuild)
            throw new RuntimeErrorException(null, "LowBow not initialized");
        /**
         * pca
         */
        pcaCurve = new Vec3[samples];
        for (int i = 0; i < samples; i++) {
            Vector v = Vector.diff(curve[i], myu);
            pcaCurve[i] = new Vec3(Vector.innerProd(pc[0], v), Vector.innerProd(pc[1], v), Vector.innerProd(pc[2], v));
        }
        // writeObjFile(false);
    }

    /**
     * @param isPca if true writes the PCA curve, else writes down the first 3
     *              coordinates of the curve into a .obj file
     */
    @SuppressWarnings("unused")
    private void writeObjFile(boolean isPca) {
        try {
            File file = new File("Line.obj");

            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintStream bw = new PrintStream(file);

            for (int i = 0; i < curve.length; i++) {
                if (isPca)
                    bw.println("v " + pcaCurve[i].getX() + " " + pcaCurve[i].getY() + " " + pcaCurve[i].getZ());
                else
                    bw.println("v " + curve[i].getX(1) + " " + curve[i].getX(2) + " " + curve[i].getX(3));
            }
            for (int i = 0; i < curve.length - 1; i++) {
                bw.println("l " + (i + 1) + " " + (i + 2));
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void writeMatrixFile(String address) {
        MyText t1 = new MyText();

        String acc = "";

        acc += " x = [\t";
        for (int i = 1; i <= rawCurve.getRows(); i++) {
            for (int j = 1; j <= rawCurve.getColumns(); j++) {
                acc += rawCurve.getXY(i, j) + ",\t";
            }
            acc += ";\t";
        }
        acc += "]\n";
        acc += "curve = [\t";
        for (int i = 0; i < curve.length; i++) {
            for (int j = 1; j <= curve[0].getDim(); j++) {
                acc += curve[i].getX(j) + ",\t";
            }
            acc += ";\t";
        }
        acc += "]\n";
        t1.write(address, acc);
    }

    /**
     * As described in the definition 4 of
     * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
     *
     * @param t belongs to the interval [0,1]
     * @param j Belongs to the set {1,..., numWords}
     * @return
     */
    private double psi(double t, int j) throws IndexOutOfBoundsException {
        t = Math.min(t, 1.0);
        if (t >= 0 && j >= 1 && j <= numWords) {
            int index = (int) Math.min(Math.floor(textLength * t), textLength - 1);
            return rawCurve.getXY(index + 1, j);
        } else {
            throw new IndexOutOfBoundsException("t:" + t + "  j : " + j + " numWords : " + numWords);
        }
    }

    public String toString() {
        return "text length : " + textLength + " number of wordsIndex : " + numWords;
    }

    public String getOriginalText() {
        return originalText;
    }

    public String[] getText() {
        return text;
    }

    public Simplex getSimplex() {
        return simplex;
    }

    /**
     * @return lowbow representation without smoothing.
     */
    public Matrix rawCurve() {
        return rawCurve;
    }

    public int getTextLength() {
        return textLength;
    }

    public int getNumWords() {
        return numWords;
    }

    public int getSamples() {
        return samples;
    }

    public double getStep() {
        return step;
    }

    public Vector[] getCurve() {
        if (isBuild) {
            return curve;
        } else {
            build();
            return curve;
        }
    }

    public void setCurve(Vector[] curve) {
        this.curve = curve;
    }

    public Vec3[] getPcaCurve() {
        return pcaCurve;
    }

    public double getSmoothingCoeff() {
        return smoothingCoeff;
    }

    /**
     * @param smoothingCoeff positive real value
     */
    public void setSmoothingCoeff(double smoothingCoeff) {
        if (smoothingCoeff <= 0)
            throw new RuntimeErrorException(null, "smoothing coeff must be > 0");
        this.smoothingCoeff = smoothingCoeff;
    }

    public double getSigma() {
        return sigma;
    }

    /**
     * @param sigma positive real value
     */
    public void setSigma(double sigma) {
        if (sigma <= 0)
            throw new RuntimeErrorException(null, "sigma must be > 0");
        this.sigma = sigma;
    }

    public double getSamplesPerTextLength() {
        return samplesPerTextLength;
    }

    /**
     * @param samplesPerTextLength positive real value
     */
    public void setSamplesPerTextLength(double samplesPerTextLength) {
        if (samplesPerTextLength <= 0)
            throw new RuntimeErrorException(null, "samples per text length must be > 0");
        this.samplesPerTextLength = samplesPerTextLength;

    }

    public TextSplitter getTextSplitter() {
        return textSplitter;
    }

    public void setTextSplitter(TextSplitter textSplitter) {
        this.textSplitter = textSplitter;
    }

    /**
     * set sigma acoording to the folowing rule: 1.0 / (2 * textLength)
     */
    public void setSigmaAuto() {
        this.sigma = getSigmaAuto();
    }

    /**
     * get sigma acoording to the folowing rule: 1.0 / (2 * textLength)
     */
    public double getSigmaAuto() {
        return 1.0 / (0.8 * this.getTextLength());
    }

    /**
     * @return text based on the maximum probable word at each sample.
     */
    public String generateText(SymbolSampler symbolSampler) {
        if (!isBuild)
            throw new RuntimeErrorException(null, "LowBow not initialized");
        int n = this.getTextLength();
        int samples = this.getSamples();
        double s = (samples - 1.0) / (n - 1.0);
        StringBuilder acc = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int k = (int) Math.floor(MyMath.clamp(s * i, 0, samples - 1));
            acc.append(symbolSampler.nextSymbol(curve[k], simplex) + "\n");
        }
        return acc.toString();
    }

    public void heatFlow(double lambda, HeatMethod heatM) {
        if (!isBuild) {
            build();
        }
        heatM.heatFlow(lambda, this);
    }
}
