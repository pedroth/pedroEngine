package algebra;

import numeric.QuadraticFormMinimizer;
import numeric.SymmetricEigen;
import org.junit.Assert;
import org.junit.Test;
import utils.StopWatch;


public class MatrixTest {

    @Test
    public void MatrixTest1() {
        double[][] ls = {{8, 4, 2, 0, 0, 9, 2, 4, 9, 9}, {2, 9, 3, 9, 0, 0, 7, 3, 5, 6}, {0, 7, 5, 2, 4, 1, 2, 2, 5, 4}, {6, 3, 0, 3, 4, 9, 9, 9, 0, 6}, {1, 6, 4, 6, 4, 1, 3, 2, 5, 5}, {7, 7, 2, 7, 1, 3, 1, 3, 8, 7}, {9, 1, 4, 1, 3, 2, 5, 2, 4, 6}, {3, 5, 8, 5, 8, 0, 7, 2, 3, 5}, {2, 8, 3, 9, 7, 1, 4, 4, 1, 9}, {0, 6, 5, 7, 4, 4, 9, 9, 6, 6}};
        Matrix m = new Matrix(ls);
        double[][] ys = {{6}, {7}, {8}, {3}, {7}, {5}, {3}, {4}, {6}, {2}};
        Vector y = new Vector(new Matrix(ys));
        StopWatch stopWatch = new StopWatch();
        Vector x0 = Matrix.solveLinearSystemSVD(m, y);
        System.out.println("time : " + stopWatch.getEleapsedTime());
        stopWatch.resetTime();
        Vector x2 = Matrix.leastSquareLinearSystem(m, y, 1E-15);
        System.out.println("time : " + stopWatch.getEleapsedTime());

        Vector diff0 = Vector.diff(m.prodVector(x0), y);
        Vector diff2 = Vector.diff(m.prodVector(x2), y);

        System.out.println(diff0.norm());
        Assert.assertTrue(diff0.norm() < 0.00001);
        Assert.assertTrue(diff2.norm() < 0.00001);
    }

    @Test
    public void MatrixTest2() {
        Matrix m = new Matrix(new double[][]{{1, -1, 0}, {-1, 2, -1}, {0, -1, 1}});
        SymmetricEigen eigen = new SymmetricEigen(m);
        Double[] eigenValues = eigen.getEigenValues();
        for (double eigenValue : eigenValues) {
            System.out.println(eigenValue);
        }
        Assert.assertTrue(Math.abs(eigenValues[0] - 3) < 1E-3);
    }

    @Test
    public void MatrixTest3() {
        Matrix m = new Matrix(new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}});
        Vector b = new Vector(new double[]{1, 2, 3});
        QuadraticFormMinimizer quadraticFormMinimizer = new QuadraticFormMinimizer(m, b);
        Vector vector = new Vector(m.getRows());
        vector.fillRandom(0, 1);
        Assert.assertTrue(Vector.diff(Vector.matrixProd(m, quadraticFormMinimizer.argMin(1E-16, vector)), b).norm() < 1E-16);
    }

    @Test
    public void MatrixTest4() {
        Matrix m = new Matrix(new double[][]{{1, 0, 0, 0, 0}, {1, -2, 1, 0, 0}, {0, 1, -2, 1, 0}, {0, 0, 1, -2, 1}, {0, 0, 0, 0, 1}});
        System.out.println(m);
        m = Matrix.prod(Matrix.scalarProd(1, m), m);
        Vector y = new Vector(new double[]{1, 0, 0, 0, 1});
        StopWatch stopWatch = new StopWatch();
        Vector x = Matrix.solveLinearSystem(m, y);
        System.out.println(stopWatch.getEleapsedTime());
        System.out.println(x);
        Assert.assertTrue(Vector.diff(Vector.matrixProd(m, x), y).norm() < 1E-6);
    }


    @Test
    public void MatrixTest5() {
        Matrix m = new Matrix(new double[][]{{100000000000.0, 2}, {2, 0.00001}});
        System.out.println(m);
        Vector y = new Vector(new double[]{4, 7.999});
        StopWatch stopWatch = new StopWatch();
        Vector x = Matrix.solveLinearSystem(m, y);
        System.out.println(stopWatch.getEleapsedTime());
        Assert.assertTrue(Vector.diff(Vector.matrixProd(m, x), y).norm() < 1E-3);
    }

    @Test
    public void DiagonalMatrixTest() {
        Matrix diag = Matrix.diag(new Vec3(1, 2, 3));
        Matrix b = new Matrix(3, 3);
        b.fill(1);
        Matrix matrix = new Matrix(new double[][]{{1, 1, 1}, {2, 2, 2}, {3, 3, 3}});
        Matrix diff = Matrix.diff(matrix, diag.prod(b));
        Assert.assertTrue(Matrix.squareNorm(diff) < 1E-3);
        Diagonal diagonal = (Diagonal) diag;
        Vec3 y = new Vec3(1, 1, 1);
        Vector x = diagonal.solveDiagonalSystem(y);
        Assert.assertTrue(Vector.diff(diagonal.prodVector(x), y).norm() < 1E-3);

    }

    @Test
    public void TridiagonalTest() {
        TridiagonalMatrix matrix = new TridiagonalMatrix(new double[]{1, 2, 2, 2}, new double[]{2, 3, 3}, new double[]{1, 1, 1});
        Matrix test = new Matrix(new double[][]{{1, 1}, {1, 1}, {1, 1}, {1, 1}});
        Assert.assertTrue(Matrix.diff(matrix.prod(test), new Matrix(new double[][]{{3, 3}, {6, 6}, {6, 6}, {3, 3}})).squareNorm() < 1E-3);
        LineGradient lineGradient = new LineGradient(4);
        System.out.println(lineGradient.prod(test));
    }

}
