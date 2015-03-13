package numeric;

import algebra.Matrix;
import algebra.Vector;

public class Pca {
	private Vector average;
	private Vector[] eigenVectors;
	private double[] eigenValues;

	public Pca() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Data does not change in the function
	 * 
	 * @param Data
	 * @param n
	 * @return
	 */
	public Vector[] getNPca(Vector[] Data, int n) {
		if (n < 1 || n > Data[0].getDim())
			return null;

		Vector[] ans = new Vector[n];
		/**
		 * compute average
		 */
		Vector myu = new Vector(Data[0].getDim());
		for (int i = 0; i < Data.length; i++) {
			myu = Vector.add(myu, Data[i]);
		}
		myu = Vector.scalarProd(1 / Data.length, myu);
		average = myu;
		/**
		 * subtract the average to the data, and compute convariance matrix
		 */

		Vector[] myData = new Vector[Data.length];
		Matrix conv = new Matrix(myu.getDim(), myu.getDim());
		for (int i = 0; i < Data.length; i++) {
			myData[i] = Vector.diff(Data[i], myu);
			conv = Matrix.add(conv, Matrix.prod(myData[i], Matrix.transpose(myData[i])));
		}
		/**
		 * compute eigenVectors
		 */
		for (int i = 0; i < n; i++) {
			Vector v = superEigen(conv);
			ans[i] = v;
			conv.fillZeros();
			for (int j = 0; j < Data.length; j++) {
				myData[j] = Vector.orthoProjection(myData[j], v);
				conv = Matrix.add(conv, Matrix.prod(myData[j], Matrix.transpose(myData[j])));
			}
			// System.out.println(conv);
		}
		eigenVectors = ans;
		return ans;
	}

	/**
	 * it works but it is too slow and depends on matrix condition number bah!!
	 * 
	 * @param conv
	 * @return
	 */
	private static Vector eigen(Matrix conv) {
		double time = 1E-3 * System.currentTimeMillis();
		int ite = 0;
		double epsilon = 1E-8;
		Vector eigenV = new Vector(conv.getRows());
		eigenV.fillRandom(-1, 1);
		eigenV = Vector.normalize(eigenV);
		Vector grad = null;
		Vector eta = null;
		do {
			grad = Vector.matrixProd(conv, eigenV);
			eta = Vector.orthoProjection(grad, eigenV);
			double alpha = 1E-2;
			Vector dotEigen = Vector.scalarProd(alpha, eta);
			eigenV = Vector.add(eigenV, dotEigen);
			eigenV = Vector.normalize(eigenV);
			ite++;
		} while (eta.norm() > epsilon);
		// System.out.println(ite + " time : " + (1E-3 *
		// System.currentTimeMillis() - time) + " error : " + eta.norm());
		return eigenV;
	}

	private static Vector superEigen(Matrix conv) {
		int ite = 0;
		double time = 1E-3 * System.currentTimeMillis();
		double epsilon = 1E-10;
		Vector eigenV = new Vector(conv.getRows());
		eigenV.fillRandom(-1, 1);
		eigenV = Vector.normalize(eigenV);
		Vector grad = null;
		Vector eta = null;
		do {
			grad = Vector.matrixProd(conv, eigenV);
			double beta = -(grad.squareNorm() / Vector.innerProd(grad, Vector.matrixProd(conv, grad)));
			/**
			 * you must put a minus since conv matrix is positive semi-definite
			 */
			eta = Vector.scalarProd(-beta, grad);
			eta = Vector.orthoProjection(eta, eigenV);
			eigenV = Vector.add(eigenV, eta);
			eigenV = Vector.normalize(eigenV);
			ite++;
		} while (eta.norm() > epsilon);
		// System.out.println(ite + " time : " + (1E-3 *
		// System.currentTimeMillis() - time) + " error : " + eta.norm() +
		// " <eta,eigen> : " + Vector.innerProd(eta, eigenV));
		return eigenV;
	}

	public Vector getAverage() {
		return average;
	}

	public Vector[] getEigenVectors() {
		return eigenVectors;
	}

	public double[] getEigenValues() {
		return eigenValues;
	}

	public static void main(String[] args) {
		double[][] m = { { 0.8147, 0.1576, 0.6557, 0.7060, 0.4387, 0.2760, 0.7513, 0.8407, 0.3517, 0.0759 }, { 0.9058, 0.9706, 0.0357, 0.0318, 0.3816, 0.6797, 0.2551, 0.2543, 0.8308, 0.0540 }, { 0.1270, 0.9572, 0.8491, 0.2769, 0.7655, 0.6551, 0.5060, 0.8143, 0.5853, 0.5308 }, { 0.9134, 0.4854, 0.9340, 0.0462, 0.7952, 0.1626, 0.6991, 0.2435, 0.5497, 0.7792 }, { 0.6324, 0.8003, 0.6787, 0.0971, 0.1869, 0.1190, 0.8909, 0.9293, 0.9172, 0.9340 }, { 0.0975, 0.1419, 0.7577, 0.8235, 0.4898, 0.4984, 0.9593, 0.3500, 0.2858, 0.1299 }, { 0.2785, 0.4218, 0.7431, 0.6948, 0.4456, 0.9597, 0.5472, 0.1966, 0.7572, 0.5688 }, { 0.5469, 0.9157, 0.3922, 0.3171, 0.6463, 0.3404, 0.1386, 0.2511, 0.7537, 0.4694 }, { 0.9575, 0.7922, 0.6555, 0.9502, 0.7094, 0.5853, 0.1493, 0.6160, 0.3804, 0.0119 }, { 0.9649, 0.9595, 0.1712, 0.0344, 0.7547, 0.2238, 0.2575, 0.4733, 0.5678, 0.3371 } };
		Matrix mat = new Matrix(m);
		// mat.fillRandom(-1000, 1000);
		Vector[] v = new Vector[10];
		for (int i = 1; i <= v.length; i++) {
			v[i - 1] = new Vector(Matrix.transpose(mat.getSubMatrix(i, i, 1, 10)));
		}
		Pca pca = new Pca();
		Vector[] eigen = pca.getNPca(v, 10);
		for (int i = 0; i < eigen.length; i++) {
			System.out.println(eigen[i]);
		}
	}
}
