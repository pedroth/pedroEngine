package algebra.src;

import algebra.utils.AlgebraException;

/**
 * Tridiagonal matrix class it simulates matrix of the form:
 * <p>
 * a1 b1 0 ...
 * c1 a2 b2 0 ...
 * 0  c2 a3 b3 0 ...
 * 0  0  c3  ...
 * .             .
 * .             .
 * .             .
 * 0
 * an-1 bn-1
 * ...  0 cn-1 an
 */
public class TridiagonalMatrix extends Matrix {
    private double[] a, b, c;
    private int n;


    public TridiagonalMatrix(int rows, int columns) {
        super();
        this.rows = rows;
        this.columns = columns;
        this.n = Math.min(rows, columns);
        this.a = new double[n];
        this.b = new double[rows < columns ? n : n - 1];
        this.c = new double[rows > columns ? n : n - 1];
    }

    public TridiagonalMatrix(int n) {
        super();
        this.rows = n;
        this.columns = n;
        this.n = n;
        this.a = new double[n];
        this.b = new double[n - 1];
        this.c = new double[n - 1];
    }

    public TridiagonalMatrix(double[] a, double[] b, double[] c) {
        super();
        this.n = a.length;
        this.rows = n;
        this.columns = n;
        this.a = a;
        if (b.length != n - 1 || c.length != n - 1) {
            throw new AlgebraException("b and c vectors must have dimension of " + (n - 1));
        }
        this.b = b;
        this.c = c;
    }

    public TridiagonalMatrix(TridiagonalMatrix tridiagonalMatrix) {
        super();
        this.n = tridiagonalMatrix.n;
        this.rows = n;
        this.columns = n;
        this.a = new double[this.n];
        this.b = new double[this.n - 1];
        this.c = new double[this.n - 1];
        for (int i = 0; i < n - 1; i++) {
            a[i] = tridiagonalMatrix.a[i];
            b[i] = tridiagonalMatrix.b[i];
            c[i] = tridiagonalMatrix.c[i];
        }
        a[n - 1] = tridiagonalMatrix.a[n - 1];
    }

    @Override
    public double getXY(int x, int y) {
        if (!checkInputIndex(x, y)) {
            throw new AlgebraException("index out of matrix. (x,y) : ( " + x + " , " + y + " )");
        }
        int diff = x - y;
        if (diff == 0) {
            return a[x - 1];
        }
        if (diff == -1) {
            return b[x - 1];
        }
        if (diff == 1) {
            return c[x - 2];
        }
        return 0;
    }

    @Override
    public void setXY(int x, int y, double value) {
        if (!checkInputIndex(x, y)) {
            throw new AlgebraException("index out of matrix. (x,y) : ( " + x + " , " + y + " )");
        }
        int diff = x - y;
        if (diff == 0) {
            a[x - 1] = value;
        }
        if (diff == -1) {
            b[x - 1] = value;
        }
        if (diff == 1) {
            c[x - 2] = value;
        }
    }

    @Override
    public Vector prodVector(Vector v) {
        int dim = v.getDim();
        Vector ans = new Vector(dim);
        if (dim == n) {
            for (int i = 0; i < dim; i++) {
                if (i == 0) {
                    ans.setX(i + 1, a[i] * v.getX(i + 1) + b[i] * v.getX(i + 2));
                } else if (i == dim - 1) {
                    ans.setX(i + 1, a[i] * v.getX(i + 1) + c[i - 1] * v.getX(i));
                } else {
                    ans.setX(i + 1, a[i] * v.getX(i + 1) + b[i] * v.getX(i + 2) + c[i - 1] * v.getX(i));
                }
            }
            return ans;
        } else {
            throw new AlgebraException("the number of columns of the first matrix must be equal to the number of lines of the second one");
        }
    }


    public Vector solveTridiagonalSystem(Vector y) {
        if (y.getDim() != n)
            return null;

        Vector x = y.copy();

        //defensive copy
        TridiagonalMatrix matrix = new TridiagonalMatrix(this);
        //forward elimination
        for (int i = 1; i <= n - 1; i++) {
            matrix.setXY(i + 1, i + 1, matrix.getXY(i, i) * matrix.getXY(i + 1, i + 1) - matrix.getXY(i, i + 1) * matrix.getXY(i + 1, i));

            if (i < n - 1)
                matrix.setXY(i + 1, i + 2, matrix.getXY(i, i) * matrix.getXY(i + 1, i + 2));

            x.setX(i + 1, matrix.getXY(i, i) * x.getX(i + 1) - matrix.getXY(i + 1, i) * x.getX(i));
        }
        //backward substitution
        for (int i = n; i >= 2; i--) {
            x.setX(i, x.getX(i) / matrix.getXY(i, i));
            x.setX(i - 1, x.getX(i - 1) - matrix.getXY(i - 1, i) * x.getX(i));
        }

        x.setX(1, x.getX(1) / matrix.getXY(1, 1));

        return x;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}