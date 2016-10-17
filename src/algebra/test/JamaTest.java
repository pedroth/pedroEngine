package algebra.test;


import Jama.EigenvalueDecomposition;
import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import org.junit.Test;
import utils.StopWatch;

public class JamaTest {
    @Test
    public void eigenTest() {
        int n = 100;
        Matrix laplacian = new LineLaplacian(n);
        StopWatch stopWatch = new StopWatch();
        Jama.Matrix matrix = new Jama.Matrix(laplacian.getMatrix());
        EigenvalueDecomposition eigenvalueDecomposition = new EigenvalueDecomposition(matrix);
        Jama.Matrix v = eigenvalueDecomposition.getV();
        double[] realEigenvalues = eigenvalueDecomposition.getRealEigenvalues();
        System.out.println("\n" + stopWatch.getEleapsedTime() + "\n");
        for (double realEigenvalue : realEigenvalues) {
            System.out.println(realEigenvalue);
        }
        double[][] array = matrix.getArray();
        System.out.println("hello");
    }
}
