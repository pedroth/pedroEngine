package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.List;

public class SuperEigenAlgo implements EigenAlgo {

    @Override
    public Vector superEigen(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int maxIte = 10000;
        int ite = 0;
        Vector eigenV = new Vector(initialCondition);
        Vector grad;
        Vector eta;
        /*
         * It maximizes positive definite matrices and minimizes negative definite matrices
         */
        do {
            grad = symMatrix.prodVector(eigenV);
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
            eta = SymmetricEigen.gramSchmitOrtho(eta, eigenVectors);
            /*
             * gradient ascend if matrix is negative definite
             */
            eigenV = Vector.diff(eigenV, eta);
            eigenV = SymmetricEigen.gramSchmitOrtho(eigenV, eigenVectors);
            eigenV = Vector.normalize(eigenV);
            ite++;
        } while (eta.norm() > epsilon && ite < maxIte);
        return eigenV;
    }

}
