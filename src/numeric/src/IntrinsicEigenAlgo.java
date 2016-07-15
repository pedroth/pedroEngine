package numeric.src;

import algebra.src.Matrix;
import algebra.src.Vec2;
import algebra.src.Vector;

import java.util.List;

/**
 * The type Intrinsic eigen algo.
 */
public class IntrinsicEigenAlgo implements EigenAlgo {

    private Vector computeV(Matrix symMatrix, Vector u, List<Vector> eigenVectors) {
        return Vector.normalize(computeResidual(symMatrix, u, eigenVectors));
    }


    private Vector computeResidual(Matrix symMatrix, Vector u, List<Vector> eigenVectors) {
        Vector v = symMatrix.prodVector(u);
        v = SymmetricEigen.gramSchmitOrtho(v, eigenVectors);
        v = Vector.orthoProjection(v, u);
        return v;
    }

    private Matrix computeS(Matrix symMatrix, Vector u, Vector v) {
        double uSu = Vector.innerProd(u, symMatrix.prodVector(u));
        double uSv = Vector.innerProd(u, symMatrix.prodVector(v));
        double vSv = Vector.innerProd(v, symMatrix.prodVector(v));
        return new Matrix(new double[][]{{uSu, uSv}, {uSv, vSv}});
    }

    private double f(Matrix S, double t) {
        Vec2 alpha = new Vec2(Math.cos(t), Math.sin(t));
        return Vector.innerProd(alpha, S.prodVector(alpha));
    }

    private double df(Matrix S, double t) {
        Vec2 alpha = new Vec2(Math.cos(t), Math.sin(t));
        Matrix R = new Matrix(new double[][]{{0, -1}, {1, 0}});
        return 2 * Vector.innerProd(S.prodVector(alpha), R.prodVector(alpha));
    }

    private double d2f(Matrix S, double t) {
        Vec2 alpha = new Vec2(Math.cos(t), Math.sin(t));
        Matrix R2 = new Matrix(new double[][]{{-1, 0}, {0, -1}});
        return 4 * Vector.innerProd(S.prodVector(alpha), R2.prodVector(alpha));
    }

    private Vector gamma(Vector u, Vector v, double t) {
        return Vector.add(Vector.scalarProd(Math.cos(t), u), Vector.scalarProd(Math.sin(t), v));
    }

    @Override
    public Vector superEigen(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon) {
        int ite = 0;
        Vector u = new Vector(initialCondition);
        Vector v = computeV(symMatrix, u, eigenVectors);
        Vector residual = computeResidual(symMatrix, u, eigenVectors);
        double t = 0;
        while (residual.norm() > epsilon && ite < 10000) {
            Matrix S = computeS(symMatrix, u, v);
            t = getOptimumT(S, t, epsilon);
            u = gamma(u, v, t);
            u = Vector.normalize(u);
            v = computeV(symMatrix, u, eigenVectors);
            residual = computeResidual(symMatrix, u, eigenVectors);
            ite++;
        }
        return u;
    }

    private double getOptimumT(Matrix S, double t, double epsilon) {
        double df;
        do {
            df = df(S, t);
            double d2f = d2f(S, t);
            t = t - df / d2f;
        } while (Math.abs(df) > 0.1);
        return t;
    }
}
