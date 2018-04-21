package numeric;

import algebra.Matrix;
import algebra.Vector;
import algorithms.QuickSortWithPermutation;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Symmetric eigen.
 */
public class SymmetricEigen {
    private final Matrix symMatrix;
    private EigenAlgo eigenAlgo = new SuperEigenAlgo();
    private Vector[] eigenVectors;
    private Double[] eigenValues;
    private boolean isUpdated = false;

    /**
     * Instantiates a new Symmetric eigen.
     *
     * @param symMatrix the sym matrix
     */
    public SymmetricEigen(Matrix symMatrix) {
        if (symMatrix.getRows() != symMatrix.getColumns()) {
            throw new RuntimeException("Number of rows must be equal to the number of columns in the matrix");
        }
        if (!checkIfSym(symMatrix)) {
            throw new RuntimeException("Matrix must be symmetric");
        }
        this.symMatrix = symMatrix;
    }

    /**
     * Gram schmit ortho.
     *
     * @param x            the x
     * @param eigenVectors the eigen vectors
     * @return the vector
     */
    public static Vector gramSchmitOrtho(Vector x, List<Vector> eigenVectors) {
        for (Vector eigenVector : eigenVectors) {
            x = Vector.orthoProjection(x, eigenVector);
        }
        return x;
    }

    private boolean checkIfSym(Matrix symMatrix) {
        int rows = symMatrix.getRows();
        int columns = symMatrix.getColumns();

        for (int i = 1; i <= rows; i++) {
            for (int j = i + 1; j <= columns; j++) {
                if (Math.abs(symMatrix.getXY(i, j) - symMatrix.getXY(j, i)) > 0.001) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compute eigen.
     */
    public void computeEigen() {
        computeEigen(1E-10, symMatrix.getRows(), eigenAlgo);
    }

    /**
     * Compute eigen.
     *
     * @param epsilon the epsilon
     * @param dim     the dim
     */
    public void computeEigen(double epsilon, int dim) {
        computeEigen(epsilon, dim, eigenAlgo);
    }

    /**
     * Compute eigen.
     *
     * @param epsilon   the epsilon
     * @param dim       the dim
     * @param eigenAlgo the eigen algo
     */
    public void computeEigen(double epsilon, int dim, EigenAlgo eigenAlgo) {
        int rows = symMatrix.getRows();
        int n = Math.min(rows, dim);
        List<Vector> eigenVectors = new ArrayList<>(n);
        List<Double> eigenValues = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Vector initial = new Vector(rows);
            initial.fillRandom(-1, 1);
            initial = gramSchmitOrtho(initial, eigenVectors);
            initial = Vector.normalize(initial);
            Vector v = eigenVectors.size() != rows - 1 ? eigenAlgo.superEigen(symMatrix, initial, eigenVectors, epsilon) : initial;
            eigenValues.add(computeEigenValue(v));
            eigenVectors.add(v);
        }
        this.eigenVectors = eigenVectors.toArray(new Vector[eigenVectors.size()]);
        this.eigenValues = eigenValues.toArray(new Double[eigenValues.size()]);
        isUpdated = true;
    }

    private Double computeEigenValue(Vector v) {
        return Vector.innerProd(v, symMatrix.prodVector(v));
    }

    /**
     * Gets sym matrix.
     *
     * @return the sym matrix
     */
    public Matrix getSymMatrix() {
        return symMatrix;
    }

    /**
     * Get eigen vectors.
     * <p>
     * Order of eigen vector is of descending eigen values if matrix is positive definite and ascending otherwise ( negative definite ).
     *
     * @return the vector [ ]
     */
    public Vector[] getEigenVectors() {
        if (!isUpdated) {
            computeEigen();
        }
        return eigenVectors;
    }

    /**
     * Get eigen values.
     * <p>
     * Order of eigen values is descending if matrix is positive definite and ascending otherwise ( negative definite ).
     *
     * @return the double [ ]
     */
    public Double[] getEigenValues() {
        if (!isUpdated) {
            computeEigen();
        }
        return eigenValues;
    }

    /**
     * Order eigen values and vectors in a natural order
     */
    public void orderEigenValuesAndVector() {
        Double[] eigenValues = getEigenValues();
        QuickSortWithPermutation quickSortWithPermutation = new QuickSortWithPermutation();
        quickSortWithPermutation.sort(eigenValues);
        int[] permutation = quickSortWithPermutation.getPermutation();
        Vector[] eigenVectors = getEigenVectors();
        Vector[] permutedEigenVectors = new Vector[eigenVectors.length];
        for (int i = 0; i < eigenVectors.length; i++) {
            permutedEigenVectors[i] = eigenVectors[permutation[i]];
        }
        this.eigenVectors = permutedEigenVectors;
    }

    public void setEigenAlgo(EigenAlgo eigenAlgo) {
        this.eigenAlgo = eigenAlgo;
    }
}
