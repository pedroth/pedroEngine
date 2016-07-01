package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vector;

import java.util.List;

/**
 * The type Intrinsic eigen algo.
 */
public class IntrinsicEigenAlgo implements EigenAlgo {

    private Vector computeV(Matrix symMatrix, Vector u, List<Vector> eigenVectors) {
        Vector v = symMatrix.prodVector(u);
        v = SymmetricEigen.gramSchmitOrtho(v, eigenVectors);
        v = Vector.orthoProjection(v, u);
        return Vector.normalize(v);
    }

    private double[] dfD2f(double t, Vector u, Vector v, Matrix symMatrix) {
        double[] ans = new double[2];
        double alpha = Math.cos(2 * t);
        double beta = Math.sin(2 * t);
        double uAu = Vector.innerProd(u, symMatrix.prodVector(u));
        double uAv = Vector.innerProd(u, symMatrix.prodVector(v));
        double vAv = Vector.innerProd(v, symMatrix.prodVector(v));
        double x = (vAv - uAu);
        double y = uAv;
        ans[0] = beta * x + 2 * alpha * y;
        ans[1] = 2 * alpha * x - 4 * beta * y;
        return ans;
    }

    private Vector gamma(Vector u, Vector v, double t) {
        return Vector.add(Vector.scalarProd(Math.cos(t), u), Vector.scalarProd(Math.sin(t), v));
    }

    private double costFunction(Matrix symMatrix, Vector u, Vector v, double t) {
        Vector gamma = gamma(u, v, t);
        return Vector.innerProd(gamma, symMatrix.prodVector(gamma));
    }

    @Override
    public Vector superEigen(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int ite = 0;
        Vector u = new Vector(initialCondition);
        Vector v = computeV(symMatrix, u, eigenVectors);
        double t = 0;
        do {
            if (v.norm() < epsilon) {
                break;
            }
            t = getOptimumT(symMatrix, u, v, t, epsilon);
            u = gamma(u, v, t);
            v = computeV(symMatrix, u, eigenVectors);
            ite++;
        } while (v.norm() > epsilon && ite < 10000);
        System.out.println(ite);
        return u;
    }

    private double getOptimumT(Matrix symMatrix, Vector u, Vector v, double t, double epsilon) {
        double df, d2f;
        do {
            double[] dfD2f = dfD2f(t, u, v, symMatrix);
            df = dfD2f[0];
            d2f = dfD2f[1];
            t = t - df / d2f;
        } while (Math.abs(df) > epsilon);
        return t;
    }

    public Vector test(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int ite = 0;
        Vector u = new Vector(initialCondition);
        Vector v = computeV(symMatrix, u, eigenVectors);
        double residual = Vector.innerProd(v, v);
        while (residual > epsilon && ite < 10000) {
            double uSu = Vector.innerProd(u, symMatrix.prodVector(u));
            double uSv = Vector.innerProd(u, symMatrix.prodVector(v));
            double vSv = Vector.innerProd(v, symMatrix.prodVector(v));
            double t = 0.5 * Math.atan2(-(2 * uSv), (vSv - uSu));
            u = gamma(u, v, t);
//            v = computeV(symMatrix, u, eigenVectors);
            residual = Vector.innerProd(v, v);
            ite++;
        }
        System.out.println(ite);
        return u;
    }
}
