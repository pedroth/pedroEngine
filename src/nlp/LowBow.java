package nlp;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import numeric.Pca;
import numeric.SimplexPointSampler;
import algebra.Matrix;
import algebra.Vec3;
import algebra.Vector;

/**
 * Lowbow implementation
 * 
 * @author pedro
 * 
 */
public class LowBow {
	private String originalText;
	private String[] text;
	private HashMap<String, Integer> wordsIndex;
	private HashMap<Integer, String> wordsIndexInv;
	private Matrix x;
	private int textLength;
	private int numWords;
	private int samples;
	private double sigma;
	private double samplesPerTextLength;
	private double step;
	private Vector[] curve;
	private Vector[] heatCurve;
	private Vec3[] pcaCurve;
	private double smoothingCoeff;

	public LowBow(String in) {
		smoothingCoeff = 0.0;
		originalText = in;
		wordsIndex = new HashMap<String, Integer>();
		wordsIndexInv = new HashMap<Integer, String>();
		this.processText(in);
	}

	public LowBow(String in, HashMap<String, Integer> wordIndex, HashMap<Integer, String> wordIndexInv) {
		smoothingCoeff = 0.0;
		originalText = in;
		this.wordsIndex = wordIndex;
		this.wordsIndexInv = wordIndexInv;
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
		TextSplitter txtSplit = new StopWordsSplitter("C:/Users/pedro/Desktop/stopWords.txt");
		text = txtSplit.split(in);
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
			Vector ones = new Vector(numWords);
			ones.fill(1.0);
			// System.out.println(Vector.innerProd(curve[i], ones));
			myu += step;
		}
	}

	public void buildPca() {
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
		writeObjFile(false);
	}

	/**
	 * 
	 * @param pc
	 *            are the principal components
	 * @param myu
	 *            is the average point
	 */
	public void buildPca(Vector[] pc, Vector myu) {
		/**
		 * pca
		 */
		pcaCurve = new Vec3[samples];
		for (int i = 0; i < samples; i++) {
			Vector v = Vector.diff(curve[i], myu);
			pcaCurve[i] = new Vec3(Vector.innerProd(pc[0], v), Vector.innerProd(pc[1], v), Vector.innerProd(pc[2], v));
		}
		writeObjFile(false);
	}

	public void heatFlow(double lambda) {
		boolean firstIte = true;
		double acm = 0;
		double epsilon = 1E-1;
		double d2Norm = 0;
		heatCurve = new Vector[curve.length];
		Vector[] grad = new Vector[curve.length];
		Vector[] lastGrad = new Vector[curve.length];
		for (int i = 0; i < curve.length; i++) {
			heatCurve[i] = curve[i].copy();
			grad[i] = new Vector(numWords);
			lastGrad[i] = new Vector(numWords);
		}
		do {
			acm = 0;
			d2Norm = 0;
			for (int i = 1; i < curve.length - 1; i++) {
				Vector d2x = Vector.add(Vector.diff(heatCurve[i + 1], Vector.scalarProd(2, heatCurve[i])), heatCurve[i - 1]);
				d2x = Vector.scalarProd(lambda * (samples - 1), d2x);
				grad[i] = Vector.scalarProd(1 - lambda, Vector.diff(curve[i], heatCurve[i]));
				grad[i] = Vector.add(d2x, grad[i]);
				if(!firstIte) {
					d2Norm += Vector.scalarProd(samples - 1, Vector.diff(grad[i],lastGrad[i])).norm();
				}
				acm += grad[i].norm();
			}
			/**
			 * Future work use Wolfe conditions instead or something else
			 */
			double dt = (firstIte)? 0.001 : ((( 1.0 / d2Norm)));
			
			for (int i = 1; i < curve.length - 1; i++) {
				heatCurve[i] = Vector.add(heatCurve[i], Vector.scalarProd(dt, grad[i]));
				lastGrad[i] = grad[i].copy();
			}
			firstIte = false;
			System.out.println(dt + "  " + acm);
		} while (acm > epsilon);
	}
	
	private Vector getHeatStep() {
		
	}

	public void curve2Heat() {
		if (heatCurve == null) {
			return;
		}
		for (int i = 0; i < curve.length; i++) {
			curve[i] = heatCurve[i];
		}
	}

	private void writeObjFile(boolean isPca) {
		try {
			File file = new File("Line.obj");

			// if file doesnt exists, then create it
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

			// System.out.println("Done" + ((isPca) ? "Pca" : "XYZ"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

	public void setSmoothingCoeff(double smoothingCoeff) {
		this.smoothingCoeff = smoothingCoeff;
	}

	public double getSigma() {
		return sigma;
	}

	public double getSamplesPerTextLength() {
		return samplesPerTextLength;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public void setSamplesPerTextLength(double samplesPerTextLength) {
		this.samplesPerTextLength = samplesPerTextLength;
	}

	public class MyTextSplitter implements TextSplitter {
		public String[] split(String in) {
			/**
			 * ugly but it is the best way I found.
			 */
			return in.replaceAll("[^(\\p{L}|\\s+)]|\\(|\\)", " ").toLowerCase().replaceAll("[^(\\p{L}|\\s+)]|\\(|\\)", "").split("\\s+");
		};
	}
}
