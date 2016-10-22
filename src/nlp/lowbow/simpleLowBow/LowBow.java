package nlp.lowbow.simpleLowBow;

import algebra.src.Vector;
import nlp.lowbow.symbolSampler.SymbolSampler;
import nlp.textSplitter.TextSplitter;
import nlp.utils.Simplex;
import numeric.src.MyMath;

import javax.management.RuntimeErrorException;
import java.util.function.Function;

/**
 * Lowbow implementation as described in http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
 *
 * @author pedro
 */
public class LowBow extends BaseLowBow {
    protected Vector[] curve;

    protected int samples;
    /**
     * 1.0 / (samples - 1)
     */
    protected double step;

    protected double sigma;
    protected double samplesPerTextLength = 1.0;

    protected boolean isBuild;

    public LowBow(String in, TextSplitter textSplitter) {
        super(in, textSplitter);
        isBuild = false;
        sigma = getSigmaAuto();
    }

    public LowBow(String in, TextSplitter textSplitter, Simplex simplex) {
        super(in, textSplitter, simplex);
        sigma = getSigmaAuto();
    }

    public LowBow(LowBow lowBow) {
        super(lowBow);
        isBuild = false;
        sigma = lowBow.getSigma();
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
        super.build();
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
        return "text length : " + textLength + " number of distinct words : " + numWords;
    }

    public String toString(Function<LowBow, String> function) {
        return function.apply(this);
    }


    public void setSimplex(Simplex simplex) {
        super.setSimplex(simplex);
        isBuild = false;
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
        isBuild = false;
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
        isBuild = false;
    }

    public TextSplitter getTextSplitter() {
        return textSplitter;
    }

    public void setTextSplitter(TextSplitter textSplitter) {
        this.textSplitter = textSplitter;
        this.processText(this.originalText);
        isBuild = false;
    }

    /**
     * set sigma acoording to the folowing rule: 1.0 / (2 * textLength)
     */
    public void setSigmaAuto() {
        this.setSigma(getSigmaAuto());
    }

    /**
     * get sigma acoording to the folowing rule: 1.0 / (2 * textLength)
     */
    public double getSigmaAuto() {
        return 1.0 / (2 * this.getTextLength());
    }

    /**
     * @return text based on the symbol sampler
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
