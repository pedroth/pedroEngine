package tensor;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

public class MatrixTest {

    public static final String[] PATTERNS = new String[0];

    @Test
    public void basicTest() {
        double epsilon = 1E-4;
        Function<String, Real> stringRealFunction = x -> new Real(Double.valueOf(x));

        Matrix<Real> R = new Matrix<>(new int[] { 2, 2 });
        Matrix<Real> v = new Matrix<>(new int[] { 2, 1 });

        R.set(0, 0, new Real(0));
        R.set(0, 1, new Real(-1));
        R.set(1, 0, new Real(1));
        R.set(1, 1, new Real(0));

        v.set(0, 0, new Real(2));
        v.set(1, 0, new Real(3));

        Matrix<Real> ans = new Matrix<>(R.prod(v));
        Matrix<Real> sol = new Matrix<>("-3 ; 2", stringRealFunction);
        Assert.assertTrue(ans.diff(sol).squareNorm().getValue() < epsilon);
        Assert.assertTrue(R.squareNorm().getValue() - 2 < epsilon);
        Assert.assertTrue(v.innerProd(v).getValue() - 13.0 < epsilon);
        Assert.assertTrue(ans.innerProd(v).getValue() < epsilon);

        sol = new Matrix<>("0 -2; 2 0", stringRealFunction);

        Assert.assertTrue(R.sum(R).diff(sol).squareNorm().getValue() < epsilon);

    }
}
