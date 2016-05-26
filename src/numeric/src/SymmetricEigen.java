package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pedroth on 4/23/2016.
 */
public class SymmetricEigen {
    private final Matrix symMatrix;
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

    private boolean checkIfSym(Matrix symMatrix) {
        int rows = symMatrix.getRows();
        int columns = symMatrix.getColumns();

        for (int i = 1; i <= rows; i++) {
            for (int j = i + 1; j <= columns; j++) {
                if (symMatrix.getXY(i, j) != symMatrix.getXY(j, i)) {
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
        computeEigen(1E-10, symMatrix.getRows());
    }

    /**
     * Compute eigen.
     *
     * @param epsilon the epsilon
     */
    public void computeEigen(double epsilon, int dim) {
        int n = Math.min(symMatrix.getRows(), dim);
        List<Vector> eigenVectors = new ArrayList<>(n);
        List<Double> eigenValues = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Vector initial = new Vector(symMatrix.getRows());
            initial.fillRandom(-1, 1);
            initial = Vector.normalize(initial);
            gramSchmitOrtho(initial, eigenVectors);
            Vector v = superEigen(initial, eigenVectors, epsilon);
            eigenValues.add(computeEigenValue(v));
            eigenVectors.add(v);
        }
        this.eigenVectors = eigenVectors.toArray(new Vector[eigenVectors.size()]);
        this.eigenValues = eigenValues.toArray(new Double[eigenValues.size()]);
        isUpdated = true;
    }

    private Double computeEigenValue(Vector v) {
        return Vector.innerProd(v, Vector.matrixProd(symMatrix, v));
    }

    /**
     * Super eigen.
     *
     * @param initialCondition the initial condition vector in Sphere
     * @param eigenVectors     the eigen vectors
     * @param epsilon          the epsilon
     * @return vector
     */
    public Vector superEigen(Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int maxIte = 10000;
        int ite = 0;
        Vector eigenV = new Vector(initialCondition);
        Vector grad;
        Vector eta;
        /*
         * It maximizes positive definite matrices and minimizes negative definite matrices
         */
        do {
            grad = Vector.matrixProd(symMatrix, eigenV);
            double quadraticForm = Vector.innerProd(grad, Vector.matrixProd(symMatrix, grad));
            /*
             * beta is negative when matrix is positive definite and positive when it is negative definite.
             */
            double beta = -(grad.squareNorm() / (quadraticForm == 0.0 ? epsilon * Math.random() : quadraticForm));
            eta = Vector.scalarProd(beta, grad);
            /*
             * project differential to constraint space
             */
            eta = Vector.orthoProjection(eta, eigenV);
            eta = gramSchmitOrtho(eta, eigenVectors);
            /*
             * gradient ascend if is positive
             */
            eigenV = Vector.diff(eigenV, eta);
            eigenV = gramSchmitOrtho(eigenV, eigenVectors);
            eigenV = Vector.normalize(eigenV);
            ite++;
        } while (eta.norm() > epsilon && ite < maxIte);
        return eigenV;
    }

    private Vector gramSchmitOrtho(Vector x, List<Vector> eigenVectors) {
        for (Vector eigenVector : eigenVectors) {
            x = Vector.orthoProjection(x, eigenVector);
        }
        return x;
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
}
