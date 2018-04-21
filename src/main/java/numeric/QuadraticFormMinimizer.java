package numeric;


import algebra.Matrix;
import algebra.Vector;

public class QuadraticFormMinimizer {
    /**
     * symmetric matrix
     */
    private Matrix matrix;
    private Vector b;

    public QuadraticFormMinimizer(Matrix matrix, Vector b) {
        this.matrix = matrix;
        this.b = b;
    }

    /**
     * @param epsilon      precision
     * @param initialValue  starting point to search for the minimum. Good starting point improves performance.
     * @return Vector which minimizes quadratic form
     */
    public Vector argMin(double epsilon, Vector initialValue) {
        Vector x = initialValue.copy();
        Vector grad;
        do {
            grad = Vector.diff(b, Vector.matrixProd(matrix, x));
            double d2fdt = Vector.innerProd(grad, Vector.matrixProd(matrix, grad));
            double t = (d2fdt != 0) ? (grad.squareNorm() / d2fdt) : 0.5;
            grad = Vector.scalarProd(t, grad);
            x = Vector.add(x, grad);
        } while (grad.norm() > epsilon);
        return x;
    }
}
