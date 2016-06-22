package nlp.lowbow.src;

import algebra.src.TridiagonalMatrix;
import algebra.src.Vector;
import numeric.src.MyMath;

/**
 * Solves the heat equation with Dirichlet Boundary condition using Matrix method (solves a Tridiagonal System super-fast)
 */
public class MatrixHeatFlow implements HeatMethod {

    @Override
    public void heatFlow(double lambda, LowBow l) {
        /*
         * aux variables
         */
        Vector zeta = new Vector(l.samples);
        TridiagonalMatrix myu = new TridiagonalMatrix(l.samples);
        /*
         * build matrix myu
         */
        for (int i = 1; i <= l.samples; i++) {

            int j = i;
            int jminus = Math.max(j - 1, 1);
            int jplus = Math.min(j + 1, l.samples);

            myu.setXY(i, jminus, lambda * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
            myu.setXY(i, j, -(lambda + 1) * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
            myu.setXY(i, jplus, lambda * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));

            /*
             * adding boundary conditions
             */
            myu.setXY(i, j, myu.getXY(i, j) + MyMath.dirac(i - 1) * MyMath.dirac(j - 1) + MyMath.dirac(i - l.samples) * MyMath.dirac(j - l.samples));
        }
        for (int j = 1; j <= l.numWords; j++) {
            /*
             * build zeta
             */
            for (int i = 1; i <= l.samples; i++) {
                zeta.setX(i, ((lambda - 1) * l.curve[i - 1].getX(j)) * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
                /*
                 * boundary condition
                 */
                zeta.setX(i, zeta.getX(i) + l.curve[i - 1].getX(j) * (MyMath.dirac(i - 1) + MyMath.dirac(i - l.samples)));
            }

            Vector v = myu.solveTridiagonalSystem(zeta);
            for (int i = 1; i <= l.samples; i++) {
                l.curve[i - 1].setX(j, v.getX(i));
            }
        }
    }
}
