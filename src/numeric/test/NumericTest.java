package numeric.test;

import algebra.src.Matrix;
import algebra.src.Vec2;
import algebra.src.Vector;
import numeric.src.MatrixExponetial;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Pedroth on 12/30/2015.
 */
public class NumericTest {

    @Test
    public void MatrixExponentialTest() {
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        Vector initial = new Vec2(1, 0);
        double alpha = 0;// 2 * Math.PI;
        int n = 500;
        Vector x = MatrixExponetial.exp(alpha, matrix, initial, n);
        Assert.assertTrue(Vector.diff(x, new Vec2(Math.cos(alpha), Math.sin(alpha))).norm() < 0.01);
    }
}
