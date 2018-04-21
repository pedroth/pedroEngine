package numeric;

import algebra.Diagonal;
import algebra.Matrix;
import algebra.Vector;

public class MatrixExponetial {

    /**
     * Exp vector.
     *
     * @param t the t
     * @param matrix the matrix
     * @param initial the initial
     * @param m the m
     * @return matrix exponential with "initial" as initial condition, with m iterations
     */
    public static Vector exp(double t, Matrix matrix, Vector initial, int m) {
        Matrix omega = new Matrix(matrix.getRows(), matrix.getColumns());
        omega.identity();

        //defensive copy
        Vector x = initial.copy();
        omega = Matrix.add(omega, Matrix.scalarProd((t / (m - 1)), matrix));
        for (int i = 0; i < m; i++) {
            x = omega.prodVector(x);
        }
        return x;
    }

    /**
     * Exp vector.
     *
     * @param t the t
     * @param matrix the matrix
     * @param initial the initial
     * @param epsilon the epsilon
     * @return matrix exponential with "initial" as initial condition, with convergence error of epsilon
     */
    public static Vector exp(double t, Matrix matrix, Vector initial, double epsilon) {
        //defensive copy
        Vector x = initial.copy();
        Vector xOld;
        int n = 1;
        do {
            xOld = x.copy();
            x = exp(t, matrix, initial, n << 1);
            n = n << 1;
        } while (Vector.diff(xOld, x).squareNorm() > epsilon);
        return x;
    }

    /**
     * Exp vector.
     *
     * @param t the t
     * @param diagonal the diagonal
     * @return the vector
     */
    public static Matrix exp(double t, Diagonal diagonal) {
        Matrix diag = Matrix.diag(diagonal);
        diag.applyFunction((x) -> Math.exp(x * t));
        return Matrix.diag(diag);
    }

    /**
     * Exp vector.
     *
     * @param t the t
     * @param matrix the matrix
     * @param initial the initial
     * @return the vector
     */
    public static Vector exp(double t, Matrix matrix, Vector initial) {
        return exp(t, matrix, initial, 0.01);
    }
}
