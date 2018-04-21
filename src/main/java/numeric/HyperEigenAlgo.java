package numeric;


import algebra.Matrix;
import algebra.Vector;

import java.util.List;

public class HyperEigenAlgo implements EigenAlgo {

    @Override
    public Vector superEigen(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int maxIte = 10000;
        int ite = 0;
        Vector eigenV = new Vector(initialCondition);
        Vector grad;
        double alpha;
        double residual;
        /*
         * It maximizes positive definite matrices and minimizes negative definite matrices
         */
        do {
            /*
             * grad is the direction that maximizes quadratic form
             */
            grad = symMatrix.prodVector(eigenV);
            /*
             * make grad orthogonal to eigenVectors
             */
            Vector v = SymmetricEigen.gramSchmitOrtho(grad, eigenVectors);

            double dotvEigenV = Vector.innerProd(v, eigenV);
            /*
             * compute speed vector tangent to n-1 sphere
             */
            Vector dx = Vector.scalarProd(1, Vector.diff(v, Vector.scalarProd(dotvEigenV, eigenV)));
            /*
             * compute acceleration vector
             */
            Vector dxx = Vector.scalarProd(-1, Vector.add(Vector.scalarProd(dotvEigenV, dx), Vector.scalarProd(Vector.innerProd(v, dx), eigenV)));

            double quadraticForm = Vector.innerProd(dx, symMatrix.prodVector(dx));// + Vector.innerProd(grad, dxx);
            residual = Vector.innerProd(grad, dx);
            alpha = -(residual / (quadraticForm == 0.0 ? epsilon * Math.random() : quadraticForm));
            /*
             * update eigenV
             */
            eigenV = Vector.add(Vector.add(eigenV, Vector.scalarProd(alpha, dx)), Vector.scalarProd(0.5 * alpha * alpha, dxx));
            /*
             * constraint eigenV
             */
            eigenV = SymmetricEigen.gramSchmitOrtho(eigenV, eigenVectors);
            eigenV = Vector.normalize(eigenV);
            ite++;
        } while (Math.abs(residual) > epsilon && ite < maxIte);
        return eigenV;
    }
}
