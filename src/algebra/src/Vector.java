package algebra.src;

import algebra.utils.AlgebraException;

/**
 * The type Vector.
 * @author pedro <p>
 *         <p>
 *         vectors are n * 1 matrices
 */
public class Vector extends Matrix {
    /**
     * Instantiates a new Vector.
     *
     * @param n the n
     */
    public Vector(int n) {
        super(n, 1);
    }

    /**
     * Instantiates a new Vector.
     *
     * @param v receives 1 * n array but stores as a n * 1 vector
     */
    public Vector(double[] v) {
        super(v);
        this.transpose();
    }

    /**
     * Instantiates a new Vector.
     *
     * @param v the v
     */
    public Vector(Double[] v) {
        super(v.length, 1);
        for (int i = 0; i < v.length; i++) {
            this.setX(i + 1, v[i]);
        }
    }

    /**
     * Instantiates a new Vector.
     *
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
     * @param v1 the v 1
     * @param v2 the v 2
     * @return the double
     */
    public static double innerProd(Vector v1, Vector v2) {
        Matrix ans = transpose(v1);
        Matrix m = prod(ans, v2);
        return m.getXY(1, 1);
    }

    /**
     * Matrix prod.
     *
     * @param m the m
     * @param v the v
     * @return the vector
     */
    public static Vector matrixProd(Matrix m, Vector v) {
        return m.prodVector(v);
    }

    /**
     * Matrix prod parallel.
     *
     * @param m the m
     * @param v the v
     * @return the vector
     */
    public static Vector matrixProdParallel(Matrix m, Vector v) {
        Vector ret;
        Matrix m1 = prodParallel(m, v);
        ret = new Vector(m1);
        return ret;
    }

    /**
     * Add vector.
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @return the vector
     */
    public static Vector add(Vector v1, Vector v2) {
        Matrix m = Matrix.add(v1, v2);
        Vector ret = new Vector(m);
        return ret;
    }

    /**
     * Diff vector.
     *
     * @param v1 the v 1
     * @param v2 the v 2
     * @return the vector
     */
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
     * @return vector vector
     */
    public static Vector scalarProd(double x, Vector v) {
        Matrix m = Matrix.scalarProd(x, v);
        Vector ret = new Vector(m);
        return ret;
    }

    /**
     * Normalize vector.
     *
     * @param v the v
     * @return the vector
     */
    public static Vector normalize(Vector v) {
        double norm = v.norm();
        Vector ret = norm == 0.0 ? new Vector(v) : Vector.scalarProd(1 / norm, v);
        return ret;
    }

    /**
     * Projection vector.
     *
     * @param u the u
     * @param v the v
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
     * Ortho projection.
     *
     * @param u the u
     * @param v the v
     * @return return the orthogonal projection of u on v
     */
    public static Vector orthoProjection(Vector u, Vector v) {
        Vector ret;
        Vector proj = Vector.projection(u, v);
        ret = Vector.diff(u, proj);
        return ret;
    }


    /**
     * Point mult.
     *
     * @param u the u
     * @param v the v
     * @return the vector
     */
    public static Vector pointMult(Vector u, Vector v) {
        if (u.getDim() != v.getDim()) {
            throw new AlgebraException("In a pointMult vectors must be of the same size");
        }
        Vector ret = new Vector(u);
        for (int i = 1; i <= ret.getDim(); i++) {
            ret.setX(i, ret.getX(i) * v.getX(i));
        }
        return ret;
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
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

    /**
     * Square norm.
     *
     * @param v the v
     * @return the double
     */
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
     * @param n the n
     */
    public void setX(int x, double n) {
        this.setXY(x, 1, n);
    }

    /**
     * Norm double.
     *
     * @return the double
     */
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
     * Gets dim.
     *
     * @return dimension /size of the vector
     */
    public int getDim() {
        return this.getRows();
    }

    /**
     * Size int.
     *
     * @return dimension /size of vector
     */
    public int size() {
        return this.getRows();
    }

    /**
     * Get array.
     *
     * @return the double [ ]
     */
    public double[] getArray() {
        int n = this.getDim();
        double[] ans = new double[n];
        for (int i = 0; i < n; i++) {
            ans[i] = this.getX(i + 1);
        }
        return ans;
    }

    /**
     * Gets sub vector.
     *
     * @param xmin the xmin
     * @param xmax the xmax
     * @return the sub vector
     */
    public Vector getSubVector(int xmin, int xmax) {
        return new Vector(this.getSubMatrix(xmin, xmax, 1, 1));
    }

    /**
     * Left prod.
     *
     * @param m the m
     * @return the vector
     */
    public Vector leftProd(Matrix m) {
        return Vector.matrixProd(m, this);
    }


    /**
     * Gets max.
     *
     * @return the max, where 1st coordinate is the value and the second coordinate is the index;
     */
    public Vec2 getMax() {
        int dim = this.getDim();
        double max = Double.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 1; i <= dim; i++) {
            double x = this.getX(i);
            if (x > max) {
                max = x;
                maxIndex = i;
            }
        }
        return new Vec2(max, maxIndex);
    }

    /**
     * Gets min.
     *
     * @return the min, where 1st coordinate is the value and the second coordinate is the index;
     */
    public Vec2 getMin() {
        int dim = this.getDim();
        double min = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 1; i <= dim; i++) {
            double x = this.getX(i);
            if (min > x) {
                min = x;
                minIndex = i;
            }
        }
        return new Vec2(min, minIndex);
    }
}
