package numeric;


import algebra.src.Matrix;
import algebra.src.Vector;

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
     *
     * @param epsilon precision
     * @param initialValue
     * @return
     */
    public Vector argMin(double epsilon, Vector initialValue) {
        Vector x = initialValue.copy();
        Vector grad;
        do {
            grad = Vector.diff(b, Vector.matrixProd(matrix, x));
            double d2fdt = Vector.innerProd(grad, Vector.matrixProd(matrix, grad));
            double t = (d2fdt != 0) ? (grad.squareNorm() / d2fdt) : 0.5;
            grad = Vector.scalarProd(0.5 * t, grad);
            x = Vector.add(x, grad);
//            System.out.println((Vector.innerProd(x,Vector.matrixProd(matrix,x)) + Vector.innerProd(b,x)) + "\t" + t + "\t" + d2fdt);
        } while (grad.norm() > epsilon);
        // System.out.println(System.nanoTime() * 1E-9 - time);
        return x;
    }
}
