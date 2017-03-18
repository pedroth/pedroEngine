package algebra.src;

import algebra.utils.AlgebraException;
import numeric.src.SVD;
import realFunction.src.UniVarFunction;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * The type Matrix.
 */
public class Matrix {
    static final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
    static final DecimalFormat df = new DecimalFormat("0.00#####", otherSymbols);

    static {
        otherSymbols.setDecimalSeparator('.');
    }

    /**
     * Number of rows
     */
    protected int rows;
    /**
     * Number of columns
     */
    protected int columns;
    private double[] matrix;

    /**
     * creates empty matrix
     */
    public Matrix() {
        this.rows = 0;
        this.columns = 0;
    }

    /**
     * Instantiates a new Matrix.
     *
     * @param rows    number of rows of the matrix. Must be a positive integer
     *                bigger than zero,
     *                {
     *                1,2, ...
     *                }
     * @param columns number of columns of the matrix. Must be a positive integer
     *                bigger than zero,
     *                {
     *                1,2, ...
     *                }
     */
    public Matrix(int rows, int columns) {
        if (rows < 1 || columns < 1) {
            throw new AlgebraException("number of rows and columns must be positive integers");
        } else {
            constructMatrix(rows, columns);
            this.fillZeros();
        }
    }

    /**
     * build a matrix with a two dimensional array
     *
     * @param m 2-dim array
     */
    public Matrix(double[][] m) {
        constructMatrix(m.length, m[0].length);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                this.setXY(i + 1, j + 1, m[i][j]);
            }
        }
    }

    /**
     * Builds a row vector
     *
     * @param v 1-dim array
     */
    public Matrix(double[] v) {
        constructMatrix(1, v.length);
        for (int i = 0; i < columns; i++) {
            setXY(1, i + 1, v[i]);
        }
    }

    /**
     * Build a matrix from a set of column vectors
     *
     * @param v the v
     */
    public Matrix(Vector[] v) {
        assert v != null;
        constructMatrix(v[0].getDim(), v.length);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                setXY(i + 1, j + 1, v[j].getX(i + 1));
            }
        }
    }

    /**
     * Instantiates a new Matrix. Copy constructor.
     *
     * @param matrix the matrix
     */
    public Matrix(Matrix matrix) {
        constructMatrix(matrix.getRows(), matrix.getColumns());
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                setXY(i + 1, j + 1, matrix.getXY(i + 1, j + 1));
            }
        }
    }

    /**
     * Instantiates a new Matrix.
     *
     * @param matrix the matrix
     */
    public Matrix(List<Vector> matrix) {
        this(matrix.toArray(new Vector[matrix.size()]));
    }

    /**
     * Add matrix.
     *
     * @param a n * m matrix
     * @param b n * m matrix
     * @return the sum of matrix a and b if input correct null otherwise
     */
    public static Matrix add(Matrix a, Matrix b) {
        return a.add(b);
    }

    /**
     * Diff matrix.
     *
     * @param a n * m matrix
     * @param b n * m matrix
     * @return the subtraction of matrix a and b if input correct null otherwise
     */
    public static Matrix diff(Matrix a, Matrix b) {
        return a.diff(b);
    }

    /**
     * Scalar prod.
     *
     * @param r scalar
     * @param m matrix
     * @return matrix multiplied by r
     */
    public static Matrix scalarProd(double r, Matrix m) {
        return m.scalarProd(r);
    }

    /**
     * Prod matrix.
     *
     * @param a n * m matrix
     * @param b m * l matrix
     * @return return a * b if they fulfill the constraints
     */
    public static Matrix prod(Matrix a, Matrix b) {
        return a.prod(b);
    }

    /**
     * Prod parallel.
     *
     * @param a n * m matrix
     * @param b m * l matrix
     * @return the product of matrix a and b if input correct null otherwise
     */
    public static Matrix prodParallel(Matrix a, Matrix b) {
        return a.prodParallel(b);
    }

    /**
     * Transpose matrix.
     *
     * @param m the m
     * @return the matrix
     */
    public static Matrix transpose(Matrix m) {
        Matrix ans = m.copy();
        ans.transpose();
        return ans;
    }

    /**
     * this solves the following equation m * x = y, where m is a n*n matrix, x
     * and y are n * 1 matrices or vectors of n dimension, using a least square approach
     *
     * @param m       the m
     * @param y       the y
     * @param epsilon convergence error
     * @return vector with solution to equation m * x = y.
     */
    public static Vector leastSquareLinearSystem(Matrix m, Vector y, double epsilon) {
        Matrix normMTrans = Matrix.transpose(m);
        Matrix myu;

        if (m.getRows() >= 100 || m.getColumns() >= 100)
            myu = Matrix.prodParallel(normMTrans, m);
        else
            myu = Matrix.prod(normMTrans, m);

        Vector gamma = Vector.matrixProd(normMTrans, y);
        Vector x = gamma.copy();
        return solveLinearSystem(myu, gamma, epsilon);
    }

    /**
     * this solves the following equation m * x = y, where m is a n*n positive definite matrix, x
     * and y are n * 1 matrices or vectors of n dimension
     *
     * @param m       the m
     * @param y       the y
     * @param epsilon convergence error
     * @return vector with solution to equation m * x = y.
     */
    public static Vector solveLinearSystem(Matrix m, Vector y, double epsilon) {
        Vector x = y.copy();
        Vector grad;
        do {
            grad = Vector.diff(Vector.matrixProd(m, x), y);
            double d2fdt = Vector.innerProd(grad, Vector.matrixProd(m, grad));
            if (d2fdt == 0) {
                return x;
            }
            double t = grad.squareNorm() / d2fdt;
            grad = Vector.scalarProd(-t, grad);
            x = Vector.add(x, grad);
        } while (grad.norm() > epsilon);
        return x;
    }

    /**
     * Solve linear system.
     *
     * @param m the m is n * n positive definite matrix
     * @param y the y
     * @return solution x, to the linear system m*x = y
     */
    public static Vector solveLinearSystem(Matrix m, Vector y) {
        return solveLinearSystem(m, y, 1E-15);
    }

    /**
     * Solve linear system using SVD.
     *
     * @param m the m
     * @param y the y
     * @return the vector
     */
    public static Vector solveLinearSystemSVD(Matrix m, Vector y) {
        SVD svd = new SVD(m);
        svd.computeSVD();
        Matrix U = svd.getU();
        Matrix S = svd.getSigmaInv();
        Matrix V = svd.getV();
        return V.prodVector(S.prodVector(Matrix.transpose(U).prodVector(y)));
    }

    /**
     * Diag matrix.
     *
     * @param v is a matrix
     * @return return a matrix with its diagonal members set to v_i coordinate of v if v is a vector (n by 1 matrix)
     * else returns a vector of the diagonal member of v
     */
    public static Matrix diag(Matrix v) {
        int n = v.getRows();
        int m = v.getColumns();
        if (m == 1) {
            Matrix matrix = new Diagonal(n);
            for (int i = 1; i <= n; i++) {
                matrix.setXY(i, i, v.getXY(i, 1));
            }
            return matrix;
        }
        Vector u = new Vector(n);
        for (int i = 1; i <= n; i++) {
            u.setX(i, v.getXY(i, i));
        }
        return u;
    }


    /**
     * Gets identity.
     *
     * @param n the n
     * @return the identity
     */
    public static Matrix getIdentity(int n) {
        Vector v = new Vector(n);
        v.fill(1.0);
        return Matrix.diag(v);
    }

    /**
     * Square norm.
     *
     * @param m the matrix m
     * @return the Frobenius norm squared
     */
    public static double squareNorm(Matrix m) {
        double acc = 0;
        int rows = m.getRows();
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= m.getColumns(); j++) {
                double mXY = m.getXY(i, j);
                acc += mXY * mXY;
            }
        }

        return acc;
    }

    private void constructMatrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.matrix = new double[rows * columns];
    }

    /**
     * Gets xY.
     *
     * @param x index for the rows where its domain is
     *          {
     *          1,2, ... , number of
     *          rows
     *          }
     * @param y index for the columns where its domain is
     *          {
     *          1,2, ... , number
     *          of columns
     *          }
     * @return value of the matrix at x and y.
     */
    public double getXY(int x, int y) {
        double r;
        if (checkInputIndex(x, y))
            r = matrix[y - 1 + columns * (x - 1)];
        else
            throw new AlgebraException("index out of matrix. (x,y) : ( " + x + " , " + y + " )");
        return r;
    }

    /**
     * Sets xY.
     *
     * @param x index for the rows where its domain is
     *          {
     *          1,2, ... , number of
     *          rows
     *          }
     * @param y index for the columns where its domain is
     *          {
     *          1,2, ... , number
     *          of columns
     *          }
     * @param n value to store at x and y.
     */
    public void setXY(int x, int y, double n) {
        if (checkInputIndex(x, y))
            matrix[y - 1 + columns * (x - 1)] = n;
        else
            throw new AlgebraException("index out of matrix");
    }

    /**
     * Get matrix.
     *
     * @return the double [ ] [ ]
     */
    public double[][] getMatrix() {
        double[][] ans = new double[this.getRows()][this.getColumns()];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                ans[i][j] = this.getXY(i + 1, j + 1);
            }
        }
        return ans;
    }

    /**
     * Sets matrix.
     *
     * @param matrix the matrix
     */
    public void setMatrix(double[][] matrix) {
        constructMatrix(matrix.length, matrix[0].length);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                this.setXY(i + 1, j + 1, matrix[i][j]);
            }
        }
    }

    /**
     * Gets rows.
     *
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets columns.
     *
     * @return the columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Prod vector.
     *
     * @param v the v
     * @return the vector
     */
    public Vector prodVector(Vector v) {
        if (this.getColumns() != v.getDim()) {
            throw new AlgebraException("Matrix columns must be the same as vector dimension");
        }
        int rows = this.getRows();
        // defensive copy
        Vector ans = new Vector(rows);
        for (int i = 0; i < rows; i++) {
            double acc = 0;
            for (int j = 0; j < this.getColumns(); j++) {
                acc += this.getXY(i + 1, j + 1) * v.getX(j + 1);
            }
            ans.setX(i + 1, acc);
        }
        return ans;
    }

    /**
     * Prod matrix.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix prod(Matrix b) {
        Matrix c;
        double sumIdentity;
        if (this.getColumns() == b.getRows()) {
            c = new Matrix(this.getRows(), b.getColumns());
            for (int j = 1; j <= this.getRows(); j++) {
                for (int k = 1; k <= b.getColumns(); k++) {
                    sumIdentity = 0;
                    for (int i = 1; i <= this.getColumns(); i++) {
                        double prod = this.getXY(j, i) * b.getXY(i, k);
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
     * Prod parallel.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix prodParallel(Matrix b) {
        Matrix c = null;

        if (this.getColumns() == b.getRows()) {
            c = new Matrix(this.getRows(), b.getColumns());
            int nCores = Runtime.getRuntime().availableProcessors();
            int quotient = this.getRows() / nCores;
            int remainder = this.getRows() % nCores;
            if (quotient == 0) {
                nCores = this.getRows();
                quotient = this.getRows() / nCores;
                remainder = this.getRows() % nCores;
            }
            Thread[] threads = new Thread[nCores];
            if (remainder != 0) {
                quotient = this.getRows() / (nCores - 1);
                remainder = this.getRows() % (nCores - 1);

                for (int i = 0; i < (nCores - 1); i++) {
                    threads[i] = new Thread(c.new MatrixParallelProd(1 + i * quotient, (i + 1) * quotient, this, b, c));
                    threads[i].start();
                }
                int lastIndex = 1 + (nCores - 1) * quotient;
                threads[nCores - 1] = new Thread(c.new MatrixParallelProd(lastIndex, remainder + lastIndex - 1, this, b, c));
                threads[nCores - 1].start();
            } else {
                for (int i = 0; i < nCores; i++) {
                    threads[i] = new Thread(c.new MatrixParallelProd(1 + i * quotient, (i + 1) * quotient, this, b, c));
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

    /**
     * Binary operation.
     *
     * @param b        the b
     * @param operator the operator
     * @return the matrix
     */
    public Matrix binaryOperation(Matrix b, BinaryOperator<Double> operator) {
        Matrix c = null;
        double r;
        if (this.getRows() == b.getRows() && this.getColumns() == b.getColumns()) {
            c = new Matrix(this.getRows(), this.getColumns());
            for (int j = 1; j <= this.getColumns(); j++) {
                for (int i = 1; i <= this.getRows(); i++) {
                    r = operator.apply(this.getXY(i, j), b.getXY(i, j));
                    c.setXY(i, j, r);
                }
            }
        }
        return c;
    }

    /**
     * Add matrix.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix add(Matrix b) {
        return binaryOperation(b, (x, y) -> x + y);
    }

    /**
     * Diff matrix.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix diff(Matrix b) {
        return binaryOperation(b, (x, y) -> x - y);
    }

    /**
     * Scalar prod.
     *
     * @param r the r
     * @return the matrix
     */
    public Matrix scalarProd(double r) {
        Matrix ans = new Matrix(this);
        return ans.applyFunction((x) -> r * x);
    }

    /**
     * Square norm.
     *
     * @return the Frobenius norm squared
     */
    public double squareNorm() {
        return Matrix.squareNorm(this);
    }

    public String toString() {
        StringBuilder s = new StringBuilder(this.rows * this.columns * 2);
        for (int i = 1; i <= this.getRows(); i++) {
            for (int j = 1; j <= this.getColumns(); j++) {
                s.append(df.format(this.getXY(i, j))).append(j == columns ? "" : "\t");
            }
            s.append("\n");
        }
        return s.toString();
    }

    /**
     * To string.
     *
     * @param function the function
     * @return the string
     */
    public String toString(Function<Matrix, String> function) {
        return function.apply(this);
    }

    /**
     * Fill zeros.
     */
    public void fillZeros() {
        for (int i = 1; i <= this.getRows(); i++) {
            for (int j = 1; j <= this.getColumns(); j++) {
                this.setXY(i, j, 0.0);
            }
        }
    }

    /**
     * Fill void.
     *
     * @param x the x
     */
    public void fill(double x) {
        for (int i = 1; i <= this.getRows(); i++) {
            for (int j = 1; j <= this.getColumns(); j++) {
                this.setXY(i, j, x);
            }
        }
    }

    /**
     * Fill random.
     *
     * @param xmin the xmin
     * @param xmax the xmax
     */
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
     * Apply function.
     *
     * @param f the f
     * @return the matrix
     */
    public Matrix applyFunction(UniVarFunction f) {
        for (int i = 1; i <= this.getRows(); i++) {
            for (int j = 1; j <= this.getColumns(); j++) {
                double x = this.getXY(i, j);
                double y = f.compute(x);
                this.setXY(i, j, y);
            }
        }
        return this;
    }

    /**
     * transpose matrix, this matrix is transposed
     *
     * @return the matrix
     */
    public Matrix transpose() {
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
        return m;
    }

    //static functions

    /**
     * Gets sub matrix.
     *
     * @param xmin lower bound row coordinate
     * @param xmax upper bound row coordinate
     * @param ymin lower bound column coordinate
     * @param ymax upper bound column coordinate
     * @return new Matrix which is the subMatrix M[xmin ... xmax ][ymin ...
     * ymax]
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
     * Sets sub matrix.
     *
     * @param xmin lower bound row coordinate
     * @param xmax upper bound row coordinate
     * @param ymin lower bound column coordinate
     * @param ymax upper bound column coordinate
     * @param m    the m
     * @return new Matrix which is the subMatrix M[xmin ... xmax ][ymin ...
     * ymax]
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

    /**
     * Set matrix to identity matrix
     *
     * @return the matrix
     */
    public Matrix identity() {
        this.fillZeros();
        for (int i = 1; i <= this.getRows(); i++) {
            this.setXY(i, i, 1.0f);
        }
        return this;
    }

    /**
     * Copy matrix.
     *
     * @return the matrix
     */
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
     * To vector.
     *
     * @return vector with dimension rows * columns, whose elements are taken
     * column-wise.
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

    /**
     * Reshape void.
     *
     * @param rows    the rows
     * @param columns the columns
     */
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

    /**
     * Concat matrix.
     *
     * @param m the m
     * @return the matrix
     */
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

    /**
     * Forbenius dist square.
     *
     * @param b the b
     * @return the double
     */
    public double forbeniusDistSquare(Matrix b) {
        if (this.getColumns() != b.getColumns() && this.getRows() != b.getRows()) {
            throw new AlgebraException("b rows and columns must be of the same size as this matrix");
        }
        double acc = 0;
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= columns; j++) {
                double d = this.getXY(i, j) - b.getXY(i, j);
                acc += d * d;
            }
        }
        return acc;
    }

    /**
     * Get vector columns.
     *
     * @return the vector [ ]
     */
    public Vector[] getVectorColumns() {
        int columns = this.getColumns();
        int rows = this.getRows();
        Vector[] vectors = new Vector[columns];
        for (int i = 1; i <= columns; i++) {
            vectors[i - 1] = new Vector(this.getSubMatrix(1, rows, i, i));
        }
        return vectors;
    }

    /**
     * Get rows vectors.
     *
     * @return the vector [ ]
     */
    public Vector[] getRowsVectors() {
        int rows = this.getRows();
        int columns = this.getColumns();
        Vector[] vectors = new Vector[rows];
        for (int i = 1; i <= rows; i++) {
            vectors[i - 1] = new Vector(Matrix.transpose(this.getSubMatrix(i, i, 1, columns)));
        }
        return vectors;
    }

    /**
     * Get matrix data.
     *
     * @return the double [ ]
     */
    public double[] getData() {
        return matrix;
    }


    /**
     * Check input index.
     *
     * @param x the x
     * @param y the y
     * @return the boolean
     */
    protected boolean checkInputIndex(int x, int y) {
        return x <= this.getRows() && x > 0 && y <= this.getColumns() && y > 0;
    }

    private class MatrixParallelProd implements Runnable {
        /**
         * The Up.
         */
        int up, /**
         * The Down.
         */
        down;
        private Matrix a;
        private Matrix b;
        private Matrix output;

        /**
         * Instantiates a new Matrix parallel prod.
         *
         * @param up     the up
         * @param down   the down
         * @param a      the a
         * @param b      the b
         * @param output the output
         */
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
