package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.Random;
/**
 * 
 * @author pedro
 *
 */
public class SimplexPointSampler {
	/**
	 * simplex point, a.k.a discrete probability distribution
	 */
	private double[] simplex;
	private double[] cdf;
	private Random random;

	/**
	 * 
	 * @param simplex
	 *            is a vector \mathbf{v} such that \sum_{i=1}^{D} v_i = 1, if
	 *            not it normalized so that it fits the simplex definition
	 */
	public SimplexPointSampler(double[] simplex) {
		this.simplex = new double[simplex.length];
		this.cdf = new double[simplex.length];
		/**
		 * copy array
		 */
		double acm = 0;
		for (int i = 0; i < simplex.length; i++) {
			this.simplex[i] = simplex[i];
			acm += simplex[i];
		}
		for (int i = 0; i < simplex.length; i++) {
			this.simplex[i] /= acm;
			int iBound = Math.max(0, i - 1);
			cdf[i] = cdf[iBound] + this.simplex[i];
		}
		random = new Random();
	}

	public static void main(String[] args) {
		//test1();
		test2();
	}

	private static void test2() {
		double[] v = {0.75, 0, 0, 0.25};
		SimplexPointSampler simplexPointSampler = new SimplexPointSampler(v);
		int n = 10000;
		for (int i = 0; i < n; i++) {
			System.out.println(simplexPointSampler.nextSymbol());
		}
	}

	private static void test1() {
		Vector v = new Vector(10);
		v.fill(1.0);
		Vector ones = new Vector(10);
		ones.fill(1.0);
		v = Vector.scalarProd(1.0 / Vector.innerProd(ones, v), v);
		double[] simplexPoint = v.getArray();
		SimplexPointSampler random = new SimplexPointSampler(simplexPoint);
		int n = 10000;
		System.out.println(Matrix.transpose(v));
		for(int i = 0; i < n; i++) {
			System.out.println(random.nextSymbol());
		}
	}

	/**
	 * @return symbol, a number between 0 and [dimension of simplex - 1] with
	 * probability given by the simplex point
	 */
	public int nextSymbol() {
		int symbol = 0;
		double r = random.nextDouble();
		for (int i = 0; i < cdf.length; i++) {
			double xBound = (i - 1 < 0) ? 0.0 : cdf[i - 1];
			if (r >= xBound && r < cdf[i]) {
				symbol = i;
				break;
			}
		}
		return symbol;
	}
}
