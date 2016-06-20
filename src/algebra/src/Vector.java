package algebra.src;

import algebra.utils.AlgebraException;

/**
 * @author pedro
 *         <p>
 *         <p>
 *         vectors are n * 1 matrices
 */
public class Vector extends Matrix {
    public Vector(int n) {
        super(n, 1);
    }

    /**
     * @param v receives 1 * n array but stores as a n * 1 vector
     */
    public Vector(double[] v) {
        super(v);
        this.transpose();
    }

    /**
     * @param v n * 1 matrix, can be used as copy method
     */
    public Vector(Matrix v) {
        super(v.getRows(), 1);
        if (v.getColumns() == 1) {
            this.setMatrix(v.getMatrix());
        } else {
            throw new AlgebraException(
                    "number of columns of matrix v must be one");
        }

    }

    /**
     * static
     */
    public static double innerProd(Vector v1, Vector v2) {
        Matrix ans = transpose(v1);
        Matrix m = prod(ans, v2);
        return m.getXY(1, 1);
    }

    public static Vector matrixProd(Matrix m, Vector v) {
        Vector ret;
        Matrix m1 = prod(m, v);
        ret = new Vector(m1);
        return ret;
    }

    public static Vector matrixProdParallel(Matrix m, Vector v) {
        Vector ret;
        Matrix m1 = prodParallel(m, v);
        ret = new Vector(m1);
        return ret;
    }

    public static Vector add(Vector v1, Vector v2) {
        Matrix m = Matrix.add(v1, v2);
        Vector ret = new Vector(m);
        return ret;
    }

    public static Vector diff(Vector v1, Vector v2) {
        Matrix m = Matrix.diff(v1, v2);
        Vector ret = new Vector(m);
        return ret;
    }

    /**
     * multiplication of real number with a vector, not the inner product
     *
     * @param x real number
     * @param v vector
     * @return
     */
    public static Vector scalarProd(double x, Vector v) {
        Matrix m = Matrix.scalarProd(x, v);
        Vector ret = new Vector(m);
        return ret;
    }

    public static Vector normalize(Vector v) {
        Vector ret = Vector.scalarProd(1 / v.norm(), v);
        return ret;
    }

    /**
     * @param u
     * @param v
     * @return projection of u on v;
     */
    public static Vector projection(Vector u, Vector v) {
        Vector ret;
        double dot = Vector.innerProd(u, v);
        double norm = Vector.innerProd(v, v);
        ret = Vector.scalarProd(dot / norm, v);
        return ret;
    }

    /**
     * @param u
     * @param v
     * @return return the orthogonal projection of u on v
     */
    public static Vector orthoProjection(Vector u, Vector v) {
        Vector ret;
        Vector proj = Vector.projection(u, v);
        ret = Vector.diff(u, proj);
        return ret;
    }

    public static void main(String[] args) {
        Vector v = new Vector(100);
        v.fillRandom(-1, 1);
        Matrix m = new Matrix(100, 100);
        Matrix r = prodParallel(m, v);
        v = Vector.matrixProd(m, v);
        System.out.println(diff(r, v));
        Vector u1 = new Vector(10);
        u1.fill(1);
        double[] u2array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Vector u2 = new Vector(u2array);
        System.out.println(Vector.innerProd(u1, u2));
    }

    public static double squareNorm(Vector v) {
        return Vector.innerProd(v, v);
    }

    /**
     * Gets the x'th value of the vector
     *
     * @param x index from 1 to dimension of vector
     * @return value of the vector at index x
     */
    public double getX(int x) {
        return this.getXY(x, 1);
    }

    /**
     * Sets the x'th value of the vector to n
     *
     * @param x index from 1 to dimension of vector
     * @param n
     */
    public void setX(int x, double n) {
        this.setXY(x, 1, n);
    }

    public double norm() {
        Vector aux = new Vector(this);
        return Math.sqrt(Vector.innerProd(aux, aux));
    }

    public double squareNorm() {
        Vector aux = new Vector(this);
        return Vector.innerProd(aux, aux);
    }

    public Vector copy() {
        Matrix m = super.copy();
        Vector r = new Vector(m);
        return r;
    }

    /**
     * @return dimension/size of the vector
     */
    public int getDim() {
        return this.getRows();
    }

    /**
     * @return dimension/size of vector
     */
    public int size() {
        return this.getRows();
    }

    public double[] getArray() {
        int n = this.getDim();
        double[] ans = new double[n];
        for (int i = 0; i < n; i++) {
            ans[i] = this.getX(i + 1);
        }
        return ans;
    }
}
