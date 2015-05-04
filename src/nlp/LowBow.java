package nlp;

import inputOutput.MyText;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Stack;

import javax.management.RuntimeErrorException;

import nlp.textSplitter.StopWordsSplitter;
import nlp.textSplitter.TextSplitter;
import numeric.MyMath;
import numeric.Pca;
import numeric.SimplexPointSampler;
import tools.simple.TextFrame;
import algebra.Matrix;
import algebra.Vec3;
import algebra.Vector;

/**
 * Lowbow implementation
 * 
 * @author pedro
 * 
 *         NOTE : try the horrible linear algebra technique to solve the heat
 *         equation
 * 
 */
public class LowBow {
	protected String originalText;
	protected String[] text;
	/**
	 * maps words to the corresponding coordinate on the simplex index of the
	 * coordinates starts at 1.
	 */
	protected HashMap<String, Integer> wordsIndex;
	protected HashMap<Integer, String> wordsIndexInv;
	/**
	 * n by m matrix where n is the number of words and m is the number of words
	 * in the dictionary
	 */
	protected Matrix x;
	protected int textLength;
	protected int numWords;
	protected int samples;
	protected double sigma;
	protected double samplesPerTextLength;
	protected double step;
	protected Vector[] curve;
	protected Vector[] heatCurve;
	protected Vec3[] pcaCurve;
	/**
	 * parameter c described in definition 2
	 * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
	 */
	protected double smoothingCoeff;
	protected TextSplitter textSplitter;
	protected boolean isInitialized;

	public LowBow(String in, TextSplitter textSplitter) {
		smoothingCoeff = 0.0;
		originalText = in;
		wordsIndex = new HashMap<String, Integer>();
		wordsIndexInv = new HashMap<Integer, String>();
		this.textSplitter = textSplitter;
		isInitialized = false;
		this.processText(in);
	}

	public LowBow(String in, TextSplitter textSplitter, HashMap<String, Integer> wordIndex, HashMap<Integer, String> wordIndexInv) {
		smoothingCoeff = 0.0;
		originalText = in;
		this.wordsIndex = wordIndex;
		this.wordsIndexInv = wordIndexInv;
		this.textSplitter = textSplitter;
		isInitialized = false;
		this.processText(in);
	}

	private double kernel(double x, double myu, double sigma) {
		double normalization = phi((1 - myu) / sigma) - phi(-myu / sigma);
		double gaussian = (1 / (Math.sqrt(2 * Math.PI) * sigma)) * Math.exp(-0.5 * ((x - myu) / sigma) * ((x - myu) / sigma));
		return gaussian / normalization;
	}

	/**
	 * http://www.johndcook.com/blog/cpp_phi/
	 * 
	 * @param x
	 * @return gaussian cumulative distribution function
	 */
	private double phi(double x) {
		// constants
		double a1 = 0.254829592;
		double a2 = -0.284496736;
		double a3 = 1.421413741;
		double a4 = -1.453152027;
		double a5 = 1.061405429;
		double p = 0.3275911;

		// Save the sign of x
		int sign = 1;
		if (x < 0)
			sign = -1;
		x = Math.abs(x) / Math.sqrt(2.0);

		// A&S formula 7.1.26
		double t = 1.0 / (1.0 + p * x);
		double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

		return 0.5 * (1.0 + sign * y);
	}

	private void processText(String in) {
		text = textSplitter.split(in);
		int acmIndex = 0;
		for (int i = 0; i < text.length; i++) {
			Integer aux = wordsIndex.get(text[i]);
			if (aux == null) {
				acmIndex++;
				wordsIndex.put(text[i], acmIndex);
				wordsIndexInv.put(acmIndex, text[i]);
			}
		}
		textLength = text.length;
		numWords = wordsIndex.size();
	}

	/**
	 * As described in the definition 6 of
	 * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
	 */
	private double gamma(double myu, double sigma, int samples, int j) {
		double acm = 0;
		double h = 1.0 / (samples - 1);
		double x = 0;
		/**
		 * trapezoidal method of integration
		 */
		for (int i = 0; i < samples - 1; i++) {
			acm += kernel(x, myu, sigma) * psi(x, j) + kernel(x + h, myu, sigma) * psi(x + h, j);
			x += h;
		}
		return acm * 0.5 * h;
	}

	public void init() {
		init(samplesPerTextLength, sigma);
	}

	public void init(double samplesPerTextLength, double sigma) {
		x = new Matrix(textLength, numWords);
		int n = x.getRows();
		int m = x.getColumns();
		double norm = 1 + smoothingCoeff * m;
		for (int i = 1; i <= n; i++) {
			if (smoothingCoeff == 0.0) {
				x.setXY(i, wordsIndex.get(text[i - 1]), 1.0);
			} else {
				for (int j = 1; j <= m; j++) {
					x.setXY(i, j, (wordsIndex.get(text[i - 1]) == j) ? ((1.0 + smoothingCoeff) / norm) : (smoothingCoeff / norm));
				}
			}
		}

		resample(samplesPerTextLength, sigma);
		isInitialized = true;
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
				curve[i].setX(j, gamma(myu, sigma, samples, j));
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
		if (!isInitialized)
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
	 * 
	 * @param pc
	 *            are the principal components
	 * @param myu
	 *            is the average point
	 */
	public void buildPca(Vector[] pc, Vector myu) {
		if (!isInitialized)
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
	 * solves the following PDE: u_t(x,t) = (1 - lambda) * (u_init(x,t) -
	 * u(x,t)) + lambda * (u_xx(x,t)); where u_t is the time derivative of the
	 * curve, u_xx is the second derivative of the curve relative to the
	 * parameter of the parameterization x , u(x,t) is the curve parameterized
	 * by x in time t
	 * 
	 * this solves the PDE using Dirichlet boundary conditions
	 * 
	 * following the notation of the
	 * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf u(x,t)
	 * corresponds to the curve gamma(myu,t)
	 * 
	 * TODO Try use Wolfe's conditions to choose dt.
	 * 
	 * @param lambda
	 *            \in [0,1]
	 */
	public void heatFlow(double lambda) {
		if (!isInitialized)
			throw new RuntimeErrorException(null, "LowBow not initialized");

		heatCurve = new Vector[curve.length];
		for (int i = 0; i < curve.length; i++) {
			heatCurve[i] = new Vector(numWords);
		}
		/**
		 * aux variables
		 */
		double epsilon = 1E-8;
		Vector zeta = new Vector(samples);
		Matrix myu = new Matrix(samples, samples);
		/**
		 * build matrix myu
		 */
		for (int i = 1; i <= samples; i++) {
			for (int j = 1; j <= samples; j++) {
				/**
				 * ((lambda / step)  * ( dirac(i-j-1) - 2dirac(i-j) + dirac(i-j+1)) - (1-lambda)*step*dirac(i-j)) * (step(i - 2) - step(i - samples))
				 */
				myu.setXY(i, j, (-(1 - lambda) * step * MyMath.dirac(i - j) + (samples - 1) * lambda * (MyMath.dirac(i - j - 1) - 2 * MyMath.dirac(i - j) + MyMath.dirac(i - j + 1))) * (MyMath.step(i - 2) - MyMath.step(i - samples)));
				/**
				 * adding boundary conditions
				 */
				myu.setXY(i, j, myu.getXY(i, j) + MyMath.dirac(i - 1) * MyMath.dirac(j - 1) + MyMath.dirac(i - samples) * MyMath.dirac(j - samples));
			}
		}
		
//		MyText t1 = new MyText();
//
//		t1.write("C:/Users/pedro/Desktop/Text1.txt", acm);
		

		for (int j = 1; j <= numWords; j++) {
			/**
			 * build zeta
			 */
			for (int i = 1; i <= samples; i++) {
				zeta.setX(i, -(1 - lambda) * step * curve[i - 1].getX(j));
			}
			Vector v = Matrix.solveLinearSystemSVD(myu, zeta);
//			System.out.println(v);
//			System.out.println(Vector.diff(Vector.matrixProd(myu, v),zeta).norm());
			for (int i = 1; i <= samples; i++) {
				heatCurve[i - 1].setX(j, v.getX(i));
			}
		}
	}

	/**
	 * changes the original curve to the smoothed one
	 */
	public void curve2Heat() {
		if (heatCurve == null) {
			return;
		}
		for (int i = 0; i < curve.length; i++) {
			curve[i] = heatCurve[i];
		}
	}

	/**
	 * 
	 * @param isPca
	 *            if true writes the PCA curve, else writes down the first 3
	 *            coordinates of the curve into a .obj file
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
	public void writeMatrixFile() {
		MyText t1 = new MyText();

		String acm = "";

		acm += " x = [\t";
		for (int i = 1; i <= x.getRows(); i++) {
			for (int j = 1; j <= x.getColumns(); j++) {
				acm += x.getXY(i, j) + ",\t";
			}
			acm += ";\t";
		}
		acm += "]\n";
		acm += "curve = [\t";
		for (int i = 0; i < curve.length; i++) {
			for (int j = 1; j <= curve[0].getDim(); j++) {
				acm += curve[i].getX(j) + ",\t";
			}
			acm += ";\t";
		}
		acm += "]\n";

		acm += "heat = [\t";
		for (int i = 0; i < heatCurve.length; i++) {
			for (int j = 1; j <= curve[0].getDim(); j++) {
				acm += heatCurve[i].getX(j) + ",\t";
			}
			acm += ";\t";
		}
		acm += "]\n";

		t1.write("C:/Users/pedro/Desktop/Text1.txt", acm);
	}

	/**
	 * generates word based on the maximum probable word in the curve at myu =
	 * (1 / (samples-1)) * k
	 * 
	 * @param k
	 * @return
	 */
	public String generateText(int k) {
		String ans = "";
		if (k < 0 || k > (samples - 1)) {
			return ans;
		}
		Vector v = curve[k];
		int n = v.getDim();
		int key = 0;
		double max = 0;
		for (int i = 1; i <= n; i++) {
			double value = v.getX(i);
			if (max < value) {
				max = value;
				key = i;
			}
		}
		ans = wordsIndexInv.get(key);
		return ans;
	}

	/**
	 * generates word in the curve at myu = (1 / (samples-1)) * k, by sampling
	 * the probability distribution of words in the curve at sample k.
	 * 
	 * @param k
	 * @return
	 */
	public String generateTextRandom(int k) {
		String ans = "";
		if (k < 0 || k > (samples - 1)) {
			return ans;
		}
		Vector v = curve[k];
		SimplexPointSampler r = new SimplexPointSampler(v.getArray());
		int key = r.nextSymbol();
		ans = wordsIndexInv.get(key + 1);
		return ans;
	}

	/**
	 * As described in the definition 4 of
	 * http://www.jmlr.org/papers/volume8/lebanon07a/lebanon07a.pdf
	 * 
	 * @param t
	 *            belongs to the interval [0,1]
	 * @param j
	 *            Belongs to the set {1,..., numWords}
	 * @return
	 */
	private double psi(double t, int j) throws IndexOutOfBoundsException {
		t = Math.min(t, 1.0);
		if (t >= 0 && j >= 1 && j <= numWords) {
			int index = (int) Math.min(Math.floor(textLength * t), textLength - 1);
			return x.getXY(index + 1, j);
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

	public HashMap<String, Integer> getWordsIndex() {
		return wordsIndex;
	}

	public HashMap<Integer, String> getWordsIndexInv() {
		return wordsIndexInv;
	}

	/**
	 * 
	 * @return lowbow representation without smoothing. Each line represents
	 */
	public Matrix getCategoricText() {
		return x;
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
		return curve;
	}

	public Vec3[] getPcaCurve() {
		return pcaCurve;
	}

	public double getSmoothingCoeff() {
		return smoothingCoeff;
	}

	public Vector[] getHeatCurve() {
		return heatCurve;
	}

	/**
	 * 
	 * @param smoothingCoeff
	 *            positive real value
	 */
	public void setSmoothingCoeff(double smoothingCoeff) {
		if (smoothingCoeff <= 0)
			throw new RuntimeErrorException(null, "smoothing coeff must be > 0");
		this.smoothingCoeff = smoothingCoeff;
	}

	public double getSigma() {
		return sigma;
	}

	public double getSamplesPerTextLength() {
		return samplesPerTextLength;
	}

	public TextSplitter getTextSplitter() {
		return textSplitter;
	}

	public void setTextSplitter(TextSplitter textSplitter) {
		this.textSplitter = textSplitter;
	}

	/**
	 * 
	 * @param sigma
	 *            positive real value
	 */
	public void setSigma(double sigma) {
		if (sigma <= 0)
			throw new RuntimeErrorException(null, "sigma must be > 0");
		this.sigma = sigma;
	}

	/**
	 * 
	 * @param samplesPerTextLength
	 *            positive real value
	 */
	public void setSamplesPerTextLength(double samplesPerTextLength) {
		if (samplesPerTextLength <= 0)
			throw new RuntimeErrorException(null, "samples per text length must be > 0");
		this.samplesPerTextLength = samplesPerTextLength;
	}

	public String getSummary(double lambda) {
		if (!isInitialized)
			throw new RuntimeErrorException(null, "LowBow not initialized");
		this.heatFlow(lambda);
		this.curve2Heat();
		int n = this.getTextLength();
		int samples = this.getSamples();
		double s = (samples - 1.0) / (n - 1.0);
		String acm = "";
		for (int i = 0; i < n; i++) {
			int k = (int) Math.floor(MyMath.clamp(s * i, 0, samples - 1));
			acm += this.generateText(k) + "\n";
		}
		return acm;
	}

	/**
	 * Example
	 */
	public static void main(String[] args) {
		MyText t = new MyText();
		t.read("C:/Users/pedro/Desktop/research/Text.txt");
		LowBow low = new LowBow(t.getText(), new StopWordsSplitter("wordsLists/stopWords.txt"));
		low.setSamplesPerTextLength(1.0);
		low.setSigma(0.008);
		low.setSmoothingCoeff(0.01);
		low.init();
		low.heatFlow(0.25);
//		low.writeMatrixFile();
		System.out.println(low);
	}
}
