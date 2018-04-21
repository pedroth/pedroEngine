package numeric;


import algebra.Matrix;
import algebra.Vector;

import java.util.List;

public class PowerMethodAlgo implements EigenAlgo {


    @Override
    public Vector superEigen(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int maxIte = 10000;
        int ite = 0;
        Vector eigenV = new Vector(initialCondition);
        Vector oldEigenV;
        do {
            oldEigenV = new Vector(eigenV);
            eigenV = symMatrix.prodVector(eigenV);
            eigenV = Vector.normalize(eigenV);
            eigenV = SymmetricEigen.gramSchmitOrtho(eigenV, eigenVectors);
            ite++;
        } while (Vector.diff(eigenV, oldEigenV).norm() > epsilon && ite < maxIte);
        return eigenV;
    }
}
