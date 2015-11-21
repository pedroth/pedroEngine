package nlp.lowbow.src;

import algebra.src.Vector;
import numeric.Pca;

import java.util.ArrayList;
import java.util.Set;

/**
 * 
 * @author pedro
 * 
 */
public class LowBowManager {
	private ArrayList<LowBow> lowbows;
	private Simplex simplex;

	public LowBowManager() {
		lowbows = new ArrayList<LowBow>();
		simplex = new Simplex();
	}
	/**
	 * 
	 * @param l lowbow curve
	 * 
	 */
	public void add(LowBow l) {
		lowbows.add(l);
		Set<String> keys = simplex.getKeySet();
		int acmIndex = simplex.size();
		for (String s : keys) {
			Integer aux = simplex.get(s);
			if (aux == null) {
				acmIndex++;
				simplex.put(s, acmIndex);
			}
		}
	}

	private LowBow myUnitAuxiliarInit(LowBow temp, double samplesPerTextLength) {
		LowBow ans = new LowBow(temp.getOriginalText(), temp.getTextSplitter(), simplex);
		ans.setSmoothingCoeff(temp.getSmoothingCoeff());
		ans.build(samplesPerTextLength, temp.getSigma());
		return ans;
	}
	
	/**
	 * Initializes all curves with the same vocabulary
	 */
	public void build() {
		int n = lowbows.size();
		for (int i = 0; i < n; i++) {
			LowBow temp = lowbows.get(i);
			LowBow lowbow = myUnitAuxiliarInit(temp, temp.samplesPerTextLength);
			lowbows.set(i, lowbow);
		}
	}
	/**
	 * resamples each curve with the maximum samples of the curves in the collection
	 */
	public void maxSamplesInit() {
		int n = lowbows.size();
		double maxSamples = 0;
		for (int i = 0; i < n; i++) {
			LowBow l = lowbows.get(i);
			double samples = l.samplesPerTextLength * l.textLength;
			if(samples > maxSamples) {
				maxSamples = samples;
			}
		}
		for (int i = 0; i < n; i++) {
			LowBow temp = lowbows.get(i);
			LowBow lowbow = myUnitAuxiliarInit(temp, maxSamples / temp.textLength);
			lowbows.set(i, lowbow);
		}
	}
	/**
	 * build pca of all curves
	 */
	public void buildPca() {
		int size = 0;
		for (LowBow lowbow : lowbows) {
			size += lowbow.getSamples();
		}
		Vector[] data = new Vector[size];

		int n = lowbows.size();

		int index = 0;
		for (int i = 0; i < n; i++) {
			Vector[] curve = lowbows.get(i).getCurve();
			for (int j = 0; j < curve.length; j++) {
				data[index] = curve[j];
				index++;
			}
		}
		Pca pca = new Pca();
		Vector[] pc = pca.getNPca(data, 3);
		Vector myu = pca.getAverage();
		for (LowBow lowbow : lowbows) {
			lowbow.buildPca(pc, myu);
		}
	}

	public ArrayList<LowBow> getLowbows() {
		return lowbows;
	}
	
	/**
	 * 
	 * @param i curve at index i
	 * @param j curve at index j
	 * @return euclidean distance between curves i and j
	 */
	public double getDistance(int i, int j) {
		LowBow l1 = lowbows.get(i);
		LowBow l2 = lowbows.get(j);
		double maxSamples = Math.max(l1.samplesPerTextLength * l1.textLength, l2.samplesPerTextLength * l2.textLength);
		if(l1.samplesPerTextLength * l1.textLength >= l2.samplesPerTextLength * l2.textLength) {
			l2.resample(maxSamples / l2.textLength, l2.getSigma());
		} else {
			l1.resample(maxSamples / l1.textLength, l1.getSigma());
		}
		/**
		 * trapezoidal method
		 */
		double acm = 0;
		double h = 1.0 / (l1.samples - 1);
		for (int k = 0; k < l1.samples - 1; k++) {
			acm += Vector.diff(l1.curve[k],l2.curve[k]).norm();
			acm += Vector.diff(l1.curve[k + 1],l2.curve[k + 1]).norm();
		}
		return acm * 0.5 * h;
	}

	public void removeAll() {
		lowbows.removeAll(lowbows);
		simplex = new Simplex();
	}
}
