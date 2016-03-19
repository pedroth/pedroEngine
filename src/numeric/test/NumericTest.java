package numeric.test;

import algebra.src.Matrix;
import algebra.src.Vec2;
import algebra.src.Vec3;
import algebra.src.Vector;
import numeric.src.MatrixExponetial;
import numeric.src.Pca;
import numeric.src.SVD;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        SVD svd = new SVD(matrix);
        svd.computeSVD();
        double[] eigenValues = svd.getEigenValues();
        for (int i = 0; i < eigenValues.length; i++) {
            System.out.println(eigenValues[i]);
        }
    }

    @Test
    public void testSvdPca() throws Exception {
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        SVD svd = new SVD(matrix);
        List<Vector> data = new ArrayList<>(2);
        data.add(matrix.getSubMatrix(1, 2, 1, 1).toVector());
        data.add(matrix.getSubMatrix(1, 2, 2, 2).toVector());
        Pca pca = new Pca(data.toArray(new Vector[2]));
        Vector[] eigenVectors = pca.getEigenVectors();
        Vector[] eigenVectors1 = svd.getEigenVectors();
        for (int i = 0; i < eigenVectors.length; i++) {
            System.out.println(Vector.diff(eigenVectors[i], eigenVectors1[i]).norm());
        }
    }
}
