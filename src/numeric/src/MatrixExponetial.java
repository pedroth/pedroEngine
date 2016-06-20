package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vector;

/**
 * Created by Pedroth on 12/30/2015.
 */
public class MatrixExponetial {

    /**
     * @param t
     * @param matrix
     * @param initial
     * @param m
     * @return matrix exponential with "initial" as initial condition, with m iterations
     */
    public static Vector exp(double t, Matrix matrix, Vector initial, int m) {
        Matrix omega = new Matrix(matrix.getRows(), matrix.getColumns());
        omega.identity();

        //defensive copy
        Vector x = initial.copy();
        omega = Matrix.add(omega, Matrix.scalarProd(1.0 / m, matrix));

        int n = (int) Math.floor(t * m);
        for (int i = 0; i < n; i++) {
            x = Vector.matrixProdParallel(omega, x);
        }
        return x;
    }

    /**
     * @param t
     * @param matrix
     * @param initial
     * @param epsilon
     * @return matrix exponential with "initial" as initial condition, with convergence error of epsilon
     */
    public static Vector exp(double t, Matrix matrix, Vector initial, double epsilon) {
        //defensive copy
        Vector x = initial.copy();
        Vector xOld;
        int n = 1;
        do {
            xOld = x.copy();
            x = exp(t, matrix, initial, n++);
        } while (Vector.diff(xOld, x).squareNorm() > epsilon);
        return x;
    }

    public static Vector exp(double t, Matrix matrix, Vector initial) {
        return exp(t, matrix, initial, 0.01);
    }
}
