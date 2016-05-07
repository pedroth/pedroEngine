package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pedroth on 4/23/2016.
 */
public class SymmetricEigen {
    private Matrix symMatrix;
    private Vector[] eigenVectors;
    private Double[] eigenValues;
    private boolean isChanged = false;

    public SymmetricEigen(Matrix symMatrix) {
        assert symMatrix.getRows() == symMatrix.getColumns();
        assert checkIfSym(symMatrix);
        this.symMatrix = symMatrix;
    }

    private boolean checkIfSym(Matrix symMatrix) {
        int rows = symMatrix.getRows();
        int columns = symMatrix.getColumns();

        for (int i = 1; i <= rows; i++) {
            for (int j = i + 1; j < columns; j++) {
                if (symMatrix.getXY(i, j) != symMatrix.getXY(j, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void computeEigen() {
        computeEigen(1E-10);
    }

    public void computeEigen(double epsilon) {
        int n = symMatrix.getRows();
        List<Vector> eigenVectors = new ArrayList<>(n);
        List<Double> eigenValues = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Vector v = superEigen(eigenVectors, epsilon);
            eigenValues.add(computeEigenValue(v));
            eigenVectors.add(v);
        }
        this.eigenVectors = eigenVectors.toArray(new Vector[eigenVectors.size()]);
        this.eigenValues = eigenValues.toArray(new Double[eigenValues.size()]);
    }

    private Double computeEigenValue(Vector v) {
        return Vector.innerProd(v, Vector.matrixProd(symMatrix, v));
    }

    /**
     * @param eigenVectors
     * @param epsilon
     * @return
     */
    private Vector superEigen(List<Vector> eigenVectors, double epsilon) {
        int maxIte = 10000;
        int ite = 0;
        Vector eigenV = new Vector(symMatrix.getRows());
        eigenV.fillRandom(-1, 1);
        eigenV = Vector.normalize(eigenV);
        Vector grad;
        Vector eta;
        do {
            grad = Vector.matrixProd(symMatrix, eigenV);
            double beta = -(grad.squareNorm() / Vector.innerProd(grad, Vector.matrixProd(symMatrix, grad)));
            /*
             * you must put a minus since you want to maximize.
			 */
            eta = Vector.scalarProd(beta, grad);
            eta = Vector.orthoProjection(eta, eigenV);
            eta = gramSchmitOrtho(eta, eigenVectors);
            eigenV = Vector.diff(eigenV, eta);
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

    public Matrix getSymMatrix() {
        return symMatrix;
    }

    public Vector[] getEigenVectors() {
        return eigenVectors;
    }

    public Double[] getEigenValues() {
        return eigenValues;
    }
}
