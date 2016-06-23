package algebra.src;


import algebra.utils.AlgebraException;
import numeric.src.SymmetricEigen;
import utils.StopWatch;

/**
 * The type Line laplacian.
 */
public class LineLaplacian extends TridiagonalMatrix {
    private Vector[] eigenVectors;
    private Double[] eigenValues;
    private boolean hasEigen = false;

    /**
     * Instantiates a new Line laplacian.
     *
     * @param n the n
     */
    public LineLaplacian(int n) {
        super(n);
        if (n > 1) {
            for (int i = 2; i <= n - 1; i++) {
                setXY(i, i - 1, -1);
                setXY(i, i + 1, -1);
                setXY(i, i, 2);
            }
            setXY(1, 1, 1);
            setXY(1, 2, -1);
            setXY(n, n - 1, -1);
            setXY(n, n, 1);
        } else {
            throw new AlgebraException("LineLaplacian matrix exists for matrix of size > 1");
        }
    }

    public static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        int n = 10000;
        for (int i = 10000; i <= n; i++) {
            stopWatch.resetTime();
            LineLaplacian laplacian = new LineLaplacian(i);
            System.out.println(stopWatch.getEleapsedTime());
            Double[] eigenValues = laplacian.getEigenValues();
            System.out.println(i + "\t" + stopWatch.getEleapsedTime());
        }
    }

    /**
     * Get eigen vector.
     *
     * @return the vector [ ]
     */
    public Vector[] getEigenVectors() {
        if (!hasEigen) {
            buildEigen();
        }
        return eigenVectors;
    }

    private void buildEigen() {
        SymmetricEigen symmetricEigen = new SymmetricEigen(this);
        this.eigenValues = symmetricEigen.getEigenValues();
        this.eigenVectors = symmetricEigen.getEigenVectors();
        hasEigen = true;
    }

    /**
     * Get eigen value.
     *
     * @return the double [ ]
     */
    public Double[] getEigenValues() {
        if (!hasEigen) {
            buildEigen();
        }
        return eigenValues;
    }
}
