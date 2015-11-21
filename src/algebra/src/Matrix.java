package algebra.src;

import algebra.utils.AlgebraException;
import numeric.SVD;
import realFunction.src.LinearFunction;
import realFunction.src.UniVarFunction;

import java.util.Random;

public class Matrix {
	private double[][] matrix;
	private int rows;
	private int columns;

	/**
	 * creates empty matrix
	 */
	public Matrix() {
		this.rows = 0;
		this.columns = 0;
	}

	/**
	 * 
	 * @param rows
	 *            number of rows of the matrix. Must be a positive integer
	 *            bigger than zero, {1,2, ...}
	 * @param columns
	 *            number of columns of the matrix. Must be a positive integer
	 *            bigger than zero, {1,2, ...}
	 */
	public Matrix(int rows, int columns) {
		if (rows < 1 && columns < 1) {
			throw new AlgebraException("number of rows and columns must be positive integers");
		} else {
			matrix = new double[rows][columns];
			this.rows = rows;
			this.columns = columns;
			this.fillZeros();
		}
	}

	public Matrix(double[][] m) {
		matrix = m;
		rows = m.length;
		columns = m[0].length;
	}

	public Matrix(double[] v) {
		rows = 1;
		columns = v.length;
		matrix = new double[rows][columns];
		for (int i = 0; i < columns; i++) {
			matrix[0][i] = v[i];
		}
	}

	/**
	 *
	 * @param a
	 *            n * m matrix
	 * @param b
	 *            n * m matrix
	 * @return the sum of matrix a and b if input correct null otherwise
	 */
	public static Matrix add(Matrix a, Matrix b) {
		Matrix c = null;
		double r = 0;
		if (a.getRows() == b.getRows() && a.getColumns() == b.getColumns()) {
			c = new Matrix(a.getRows(), a.getColumns());
			for (int j = 1; j <= a.getColumns(); j++) {
				for (int i = 1; i <= a.getRows(); i++) {
					r = a.getXY(i, j) + b.getXY(i, j);
					c.setXY(i, j, r);
				}
			}
		}
		return c;
	}

	/**
	 * @param a n * m matrix
	 * @param b n * m matrix
	 * @return the subtraction of matrix a and b if input correct null otherwise
	 */
	public static Matrix diff(Matrix a, Matrix b) {
		Matrix r;
		r = Matrix.scalarProd(-1, b);
		r = Matrix.add(a, r);
		return r;
	}

	/**
	 * @param r scalar
	 * @param m matrix
	 * @return matrix multiplied by r
	 */
	public static Matrix scalarProd(double r, Matrix m) {
		Matrix temp = m.copy();
		LinearFunction f = new LinearFunction(r);
		temp.applyFunction(f);
		return temp;
	}

	/**
	 * @param a n * m matrix
	 * @param b m * l matrix
	 * @return return a * b if they fulfill the constraints
	 */
	public static Matrix prod(Matrix a, Matrix b) {
		Matrix c = null;
		double sumIdentity;
		if (a.getColumns() == b.getRows()) {
			c = new Matrix(a.getRows(), b.getColumns());
			for (int j = 1; j <= a.getRows(); j++) {
				for (int k = 1; k <= b.getColumns(); k++) {
					sumIdentity = 0;
					for (int i = 1; i <= a.getColumns(); i++) {
						double prod = a.getXY(j, i) * b.getXY(i, k);
						sumIdentity = sumIdentity + prod;
					}
					c.setXY(j, k, sumIdentity);
				}
			}
		} else {
			throw new AlgebraException("the number of columns of the first matrix must be equal to the number of lines of the second one");
		}
		return c;
	}

	/**
	 * @param a n * m matrix
	 * @param b m * l matrix
	 * @return the product of matrix a and b if input correct null otherwise
	 */
	public static Matrix prodParallel(Matrix a, Matrix b) {
		Matrix c = null;

		if (a.getColumns() == b.getRows()) {
			c = new Matrix(a.getRows(), b.getColumns());
			int nCores = Runtime.getRuntime().availableProcessors();
			int quotient = a.getRows() / nCores;
			int remainder = a.getRows() % nCores;
			if (quotient == 0) {
				nCores = a.getRows();
				quotient = a.getRows() / nCores;
				remainder = a.getRows() % nCores;
			}
			Thread[] threads = new Thread[nCores];
			if (remainder != 0) {
				quotient = a.getRows() / (nCores - 1);
				remainder = a.getRows() % (nCores - 1);

				for (int i = 0; i < (nCores - 1); i++) {
					threads[i] = new Thread(c.new MatrixParallelProd(1 + i * quotient, (i + 1) * quotient, a, b, c));
					threads[i].start();
				}
				int lastIndex = 1 + (nCores - 1) * quotient;
				threads[nCores - 1] = new Thread(c.new MatrixParallelProd(lastIndex, remainder + lastIndex - 1, a, b, c));
				threads[nCores - 1].start();
			} else {
				for (int i = 0; i < nCores; i++) {
					threads[i] = new Thread(c.new MatrixParallelProd(1 + i * quotient, (i + 1) * quotient, a, b, c));
					threads[i].start();
				}
			}
			for (int i = 0; i < nCores; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return c;
	}

	public static Matrix transpose(Matrix m) {
		Matrix ans = m.copy();
		ans.transpose();
		return ans;
	}

	/**
	 * this solves the following equation m * x = y, where m is a n*n matrix, x
	 * and y are n * 1 matrices or vectors of n dimension
	 *
	 * @param m
	 * @param y
	 * @param epsilon convergence error
	 * @return vector with solution to equation m * x = y.
	 */
	public static Vector solveLinearSystem(Matrix m, Vector y, double epsilon) {
		double time = System.nanoTime() * 1E-9;
		Matrix normMTrans = Matrix.transpose(m);
		Matrix myu;

		if (m.getRows() >= 100 || m.getColumns() >= 100)
			myu = Matrix.prodParallel(normMTrans, m);
		else
			myu = Matrix.prod(normMTrans, m);

		Vector gamma = Vector.matrixProd(normMTrans, y);
		Vector x = gamma.copy();
		Vector grad = null;
		do {
			grad = Vector.diff(gamma, Vector.matrixProd(myu, x));
			double d2fdt = Vector.innerProd(grad, Vector.matrixProd(myu, grad));
			double t = (d2fdt != 0) ? (grad.squareNorm() / d2fdt) : 0.5;
			grad = Vector.scalarProd(0.5 * t, grad);
			x = Vector.add(x, grad);
			// System.out.println(grad.norm() + "\t" +
			// Vector.diff(Vector.matrixProd(m, x), y).norm() + "\t" + t);
		} while (grad.norm() > epsilon);
		// System.out.println(System.nanoTime() * 1E-9 - time);
		return x;
	}

	public static Vector solveLinearSystem(Matrix m, Vector y) {
		return solveLinearSystem(m, y, 1E-15);
	}

	public static Vector solveLinearSystemSVD(Matrix m, Vector y) {
		SVD svd = new SVD(m);
		svd.computeSVD();
		Matrix U = svd.getU();
		Matrix S = svd.getSigmaInv();
		Matrix V = svd.getV();
		if (m.getRows() >= 100 || m.getColumns() >= 100) {
			return Vector.matrixProd(Matrix.prodParallel(Matrix.prodParallel(V, S), Matrix.transpose(U)), y);
		} else {
			return Vector.matrixProd(Matrix.prod(Matrix.prod(V, S), Matrix.transpose(U)), y);
		}
	}

	/**
	 *
	 * @param x
	 *            index for the rows where its domain is {1,2, ... , number of
	 *            rows}
	 * @param y
	 *            index for the columns where its domain is {1,2, ... , number
	 *            of columns}
	 * @return value of the matrix at x and y.
	 */
	public double getXY(int x, int y) {
		double r;
		if (checkInputIndex(x, y))
			r = matrix[x - 1][y - 1];
		else
			throw new AlgebraException("index out of matrix. (x,y) : ( " + x + " , " + y +" )" );
		return r;
	}

	/**
	 *
	 * @param x
	 *            index for the rows where its domain is {1,2, ... , number of
	 *            rows}
	 * @param y
	 *            index for the columns where its domain is {1,2, ... , number
	 *            of columns}
	 * @param n
	 *            value to store at x and y.
	 */
	public void setXY(int x, int y, double n) {
		if (checkInputIndex(x, y))
			matrix[x - 1][y - 1] = n;
		else
			throw new AlgebraException("index out of matrix");
	}

	public double[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(double[][] matrix) {
		this.rows = matrix.length;
		this.columns = matrix[0].length;
		this.matrix = matrix;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	public String toString() {
		String s = "";
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				s = s + String.format(" %.10f\t", this.getXY(i, j));
			}
			s += "\n";
		}
		s += "\n";
		return s;
	}

	public String toStringMatlab() {
		String acm = "";
		acm += "[";
		for (int i = 1; i <= rows; i++) {
			for (int j = 1; j <= columns; j++) {
				acm += this.getXY(i, j) + (j <= (columns - 1) ? " , " : " ;");
			}
		}
		acm += "]";
		return acm;
	}

	public void fillZeros() {
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				this.setXY(i, j, 0.0);
			}
		}
	}

	public void fill(double x) {
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				this.setXY(i, j, x);
			}
		}
	}

	public void fillRandom(double xmin, double xmax) {
		Random r = new Random();
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				double x = xmin + (xmax - xmin) * r.nextDouble();
				this.setXY(i, j, x);
			}
		}
	}

	/**
	 * static
	 */

	public void applyFunction(UniVarFunction f) {
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				double x = this.getXY(i, j);
				double y = f.compute(x);
				this.setXY(i, j, y);
			}
		}
	}

	public void transpose() {
		Matrix m;
		m = new Matrix(this.getColumns(), this.getRows());
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				m.setXY(j, i, this.getXY(i, j));
			}
		}
		matrix = m.matrix;
		rows = m.getRows();
		columns = m.getColumns();
	}

	/**
	 *
	 * @param xmin
	 *            lower bound row coordinate
	 * @param xmax
	 *            upper bound row coordinate
	 * @param ymin
	 *            lower bound column coordinate
	 * @param ymax
	 *            upper bound column coordinate
	 * @return new Matrix which is the subMatrix M[xmin ... xmax ][ymin ...
	 *         ymax]
	 */
	public Matrix getSubMatrix(int xmin, int xmax, int ymin, int ymax) {
		Matrix ans;
		if (checkInputIndex(xmin, ymin) && checkInputIndex(xmax, ymax)) {
			ans = new Matrix(xmax - xmin + 1, ymax - ymin + 1);
			for (int i = xmin; i <= xmax; i++) {
				for (int j = ymin; j <= ymax; j++) {
					ans.setXY(i - xmin + 1, j - ymin + 1, this.getXY(i, j));
				}
			}
			return ans;
		} else {
			throw new AlgebraException("index out of matrix");
		}
	}

	/**
	 *
	 * @param xmin
	 *            lower bound row coordinate
	 * @param xmax
	 *            upper bound row coordinate
	 * @param ymin
	 *            lower bound column coordinate
	 * @param ymax
	 *            upper bound column coordinate
	 * @return new Matrix which is the subMatrix M[xmin ... xmax ][ymin ...
	 *         ymax]
	 */
	public void setSubMatrix(int xmin, int xmax, int ymin, int ymax, Matrix m) {
		if (checkInputIndex(xmin, ymin) && checkInputIndex(xmax, ymax) && m.rows == (xmax - xmin + 1) && m.columns == (ymax - ymin + 1)) {
			for (int i = xmin; i <= xmax; i++) {
				for (int j = ymin; j <= ymax; j++) {
					this.setXY(i, j, m.getXY(i - xmin + 1, j - ymin + 1));
				}
			}
		} else {
			throw new AlgebraException("index out of matrix or input doesnt fit");
		}
	}

	public void identity() {
		this.fillZeros();
		for (int i = 1; i <= this.getRows(); i++) {
			this.setXY(i, i, 1.0f);
		}
	}

	public Matrix copy() {
		Matrix r = new Matrix(this.getRows(), this.getColumns());
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getColumns(); j++) {
				r.setXY(i, j, this.getXY(i, j));
			}
		}
		return r;
	}

	/**
	 *
	 * @return vector with dimension rows * columns, whose elements are taken
	 *         column-wise.
	 */
	public Vector toVector() {
		Vector v = new Vector(this.rows * this.columns);
		for (int j = 1; j <= this.columns; j++) {
			for (int i = 1; i <= this.rows; i++) {
				v.setX(i + (j - 1) * rows, this.getXY(i, j));
			}
		}
		return v;

	}

	public void reshape(int rows, int columns) {
		Matrix ans;
		if (rows * columns == this.rows * this.columns) {
			ans = new Matrix(rows, columns);
			Vector v = this.toVector();
			for (int j = 1; j <= columns; j++) {
				for (int i = 1; i <= rows; i++) {
					ans.setXY(i, j, v.getX(i + (j - 1) * rows));
				}
			}
			this.setMatrix(ans.getMatrix());
		} else {
			throw new AlgebraException("not possible reshape, rows * columns differs from original matrix");
		}
	}

	public Matrix concat(Matrix m) {
		Matrix ans = null;
		if (m.rows == this.rows) {
			ans = new Matrix(rows, columns + m.columns);
			ans.setSubMatrix(1, rows, 1, columns, this);
			ans.setSubMatrix(1, rows, columns + 1, columns + m.columns, m);
		} else if (this.rows == 0) {
			ans = m.copy();
		} else if (m.rows == 0) {
			// do nothing
		} else {
			throw new AlgebraException("rows of two matrices must be equal");
		}
		return ans;
	}

	private boolean checkInputIndex(int x, int y) {
		return x <= this.getRows() && x > 0 && y <= this.getColumns() && y > 0;
	}

	private class MatrixParallelProd implements Runnable {
		int up, down;
		private Matrix a;
		private Matrix b;
		private Matrix output;

		public MatrixParallelProd(int up, int down, Matrix a, Matrix b, Matrix output) {
			super();
			this.up = up;
			this.down = down;
			this.a = a;
			this.b = b;
			this.output = output;
		}

		@Override
		public void run() {
			double sumIdentity = 0, prod = 1;
			for (int i = this.up; i <= this.down; i++) {
				for (int k = 1; k <= b.getColumns(); k++) {
					sumIdentity = 0;
					for (int j = 1; j <= a.getColumns(); j++) {
						prod = a.getXY(i, j) * b.getXY(j, k);
						sumIdentity += prod;
					}
					output.setXY(i, k, sumIdentity);
				}
			}
		}
	}
}
