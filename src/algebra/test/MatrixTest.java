package algebra.test;

import algebra.src.Matrix;
import algebra.src.Vec3;
import algebra.src.Vector;
import numeric.src.QuadraticFormMinimizer;
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

    @Test
    public void MatrixTest2() {
        Matrix m = new Matrix(new double[][]{{1, -1, 0}, {-1, 2, -1}, {0, -1, 1}});
        int samples = 100;
        for (int i = 0; i < samples; i++) {
            Vec3 v = new Vec3();
            v.fillRandom(-100, 100);
            v = Vec3.normalize(v);
            System.out.println(Vec3.innerProd(v, Vec3.matrixProd(m, v)));
        }
    }

    @Test
    public void MatrixTest3() {
        Matrix m = new Matrix(new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}});
        Vector b = new Vector(new double[]{1, 2, 3});
        QuadraticFormMinimizer quadraticFormMinimizer = new QuadraticFormMinimizer(m, b);
        Vector vector = new Vector(m.getRows());
        vector.fillRandom(0, 1);
        System.out.println(Vector.diff(Vector.matrixProd(m, quadraticFormMinimizer.argMin(1E-16, vector)), b).norm());
    }

    @Test
    public void MatrixTest4() {
        Matrix m = new Matrix(new double[][]{{1, 0, 0, 0, 0}, {1, -2, 1, 0, 0}, {0, 1, -2, 1, 0}, {0, 0, 1, -2, 1}, {0, 0, 0, 0, 1}});
        System.out.println(m);
        m = Matrix.prod(Matrix.scalarProd(1, m), m);
        Vector y = new Vector(new double[]{0, 0, 0, 0, 1});

        System.out.println(y);
        Vector x = Matrix.solveLinearSystem(m, y);
        System.out.println(x);
        Assert.assertTrue(Vector.diff(Vector.matrixProd(m, x), y).norm() < 1E-6);
    }


    @Test
    public void MatrixTest5() {
        Matrix m = new Matrix(new double[][]{{1, 2}, {2, 3.999}});
        System.out.println(m);
        Vector y = new Vector(new double[]{4, 7.999});

        System.out.println(y);
        Vector x = Matrix.solveLinearSystem(m, y);
        System.out.println(x);
        Assert.assertTrue(Vector.diff(Vector.matrixProd(m, x), y).norm() < 1E-6);
    }
}
