package numeric.test;

import algebra.src.Matrix;
import algebra.src.Vec2;
import algebra.src.Vec3;
import algebra.src.Vector;
import inputOutput.CsvReader;
import numeric.src.MatrixExponetial;
import numeric.src.SVD;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Pedroth on 12/30/2015.
 */
public class NumericTest {

    @Test
    public void matrixExponentialTest() {
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        Vector initial = new Vec2(1, 0);
        double alpha = 8 * Math.PI;
        int n = 2000;
        Vector x = MatrixExponetial.exp(alpha, matrix, initial, n);
        Assert.assertTrue(Vector.diff(x, new Vec2(Math.cos(alpha), Math.sin(alpha))).norm() < 0.01);
    }

    @Test
    public void matrixExponentialTest2() {
        Matrix matrix = new Matrix(new double[][]{{0, 0, 0}, {1, -2, 1}, {0, 0, 0}});
        Vector initial = new Vec3(0, 0, 1);
        double alpha = 1;
        int n = 5000;
        Vector x = MatrixExponetial.exp(alpha, matrix, initial, n);
    }

    @Test
    public void sVDTest() {
        Matrix matrix = new Matrix(new double[][]{{1, 0, 0}, {-1, 2, -1}, {0, 0, 1}});
        SVD svd = new SVD(matrix);
        svd.computeSVD();
        Matrix V = svd.getV();
        Matrix S = svd.getSigma();
        Matrix U = svd.getU();
        Matrix prod = Matrix.prod(U, Matrix.prod(S, Matrix.transpose(V)));
        System.out.println(prod);
        Assert.assertTrue(Matrix.squareNorm(Matrix.diff(matrix, prod)) < 1E-20);
    }

    @Test
    public void testSvd2() throws Exception {
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        SVD svd = new SVD(matrix);
        svd.computeSVD();
        Matrix V = svd.getV();
        Matrix S = svd.getSigma();
        Matrix U = svd.getU();
        Matrix prod = Matrix.prod(U, Matrix.prod(S, Matrix.transpose(V)));
        System.out.println(U);
        System.out.println(Matrix.transpose(V));
        System.out.println(prod);
        Assert.assertTrue(Matrix.squareNorm(Matrix.diff(matrix, prod)) < 1E-20);
    }

    @Test
    public void PCATest() {
        CsvReader table = new CsvReader();
        table.read("src/numeric/test/resource/testData.csv");

    }

    @Test
    public void testEigenValue() {

    }
}
