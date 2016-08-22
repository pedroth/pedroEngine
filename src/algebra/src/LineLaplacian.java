package algebra.src;


import algebra.utils.AlgebraException;
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
        int n = 100;
        LineLaplacian laplacian = null;
        for (int i = n; i <= n; i++) {
            stopWatch.resetTime();
            laplacian = new LineLaplacian(i);
            Double[] eigenValues = laplacian.getEigenValues();
            System.out.println(i + "\t" + stopWatch.getEleapsedTime());
        }
        System.out.println(laplacian.getEigenVectors()[99]);
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
        int n = this.getRows();
        eigenVectors = new Vector[n];
        eigenValues = new Double[n];

        for (int j = 1; j <= n; j++) {
            //construct eigen value
            double theta = Math.PI * (j - 1.0) / (2.0 * n);
            eigenValues[j - 1] = 4.0 * Math.sin(theta) * Math.sin(theta);

            //construct eigen vector
            eigenVectors[j - 1] = new Vector(n);
            if (j == 1) {
                double sqrt = 1 / Math.sqrt(n);
                for (int i = 1; i <= n; i++) {
                    eigenVectors[j - 1].setX(i, sqrt);
                }
            } else {
                for (int i = 1; i <= n; i++) {
                    theta = (Math.PI * (i - 0.5) * (j - 1.0)) / n;
                    eigenVectors[j - 1].setX(i, Math.sqrt(2.0 / n) * Math.cos(theta));
                }
            }
        }
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
