package algebra.src;

import algebra.utils.AlgebraException;

public class Diagonal extends Matrix {

    private double[] a;
    private int n;


    public Diagonal(int n) {
        super();
        this.rows = n;
        this.columns = n;
        this.n = n;
        this.a = new double[n];
    }

    public Diagonal(double[] a) {
        super();
        this.n = a.length;
        this.rows = n;
        this.columns = n;
        this.a = a;
    }

    public Diagonal(Diagonal tridiagonalMatrix) {
        super();
        this.n = tridiagonalMatrix.n;
        this.rows = n;
        this.columns = n;
        this.a = new double[this.n];
        for (int i = 0; i < n; i++) {
            a[i] = tridiagonalMatrix.a[i];
        }
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
    }

    @Override
    public Vector prodVector(Vector v) {
        int dim = v.getDim();
        Vector ans = new Vector(dim);
        if (dim == n) {
            for (int i = 0; i < dim; i++) {
                ans.setX(i + 1, a[i] * v.getX(i + 1));
            }
            return ans;
        } else {
            throw new AlgebraException("the number of columns of the first matrix must be equal to the number of lines of the second one");
        }
    }

    @Override
    public Matrix prod(Matrix b) {
        Matrix c;
        if (this.getColumns() == b.getRows()) {
            c = new Matrix(this.getRows(), b.getColumns());
            for (int j = 1; j <= this.getRows(); j++) {
                for (int k = 1; k <= b.getColumns(); k++) {
                    c.setXY(j, k, a[j - 1] * b.getXY(j, k));
                }
            }
        } else {
            throw new AlgebraException("the number of columns of the first matrix must be equal to the number of lines of the second one");
        }
        return c;
    }

    public Vector solveDiagonalSystem(Vector y) {
        if (y.getDim() != n)
            return null;

        Vector aux = new Vector(n);

        for (int i = 1; i <= n; i++) {
            aux.setX(i, 1.0 / a[i - 1]);
        }

        Matrix diagonal = Matrix.diag(aux);
        return diagonal.prodVector(y);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
