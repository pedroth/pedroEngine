package algebra.test;

import algebra.src.Matrix;
import algebra.src.Vector;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Pedroth on 11/21/2015.
 */
public class MatrixTest {

    @Test
    public void MatrixTest1() {
        double[][] ls = {{8, 4, 2, 0, 0, 9, 2, 4, 9, 9}, {2, 9, 3, 9, 0, 0, 7, 3, 5, 6}, {0, 7, 5, 2, 4, 1, 2, 2, 5, 4}, {6, 3, 0, 3, 4, 9, 9, 9, 0, 6}, {1, 6, 4, 6, 4, 1, 3, 2, 5, 5}, {7, 7, 2, 7, 1, 3, 1, 3, 8, 7}, {9, 1, 4, 1, 3, 2, 5, 2, 4, 6}, {3, 5, 8, 5, 8, 0, 7, 2, 3, 5}, {2, 8, 3, 9, 7, 1, 4, 4, 1, 9}, {0, 6, 5, 7, 4, 4, 9, 9, 6, 6}};
        Matrix m = new Matrix(ls);
        // m.fillRandom(-1000, 1000);
        double[][] ys = {{6}, {7}, {8}, {3}, {7}, {5}, {3}, {4}, {6}, {2}};
        Vector y = new Vector(new Matrix(ys));
        // y.fillRandom(-100, 100);
        double time = System.currentTimeMillis() * 1E-3;
        Vector x = Matrix.solveLinearSystemSVD(m, y);
        System.out.println("time : " + ((System.currentTimeMillis() * 1E-3) - time));
        // System.out.println(x);
        Vector vector = new Vector(Matrix.diff(Matrix.prod(m, x), y));
        Assert.assertTrue(vector.norm() < 0.001);
        System.out.println(x);
    }
}
