package nlp.lowbow.simpleLowBow;

import algebra.src.TridiagonalMatrix;
import algebra.src.Vector;

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
        for (int i = 2; i <= l.samples - 1; i++) {

            int j = i;
            int jminus = Math.max(j - 1, 1);
            int jplus = Math.min(j + 1, l.samples);

            myu.setXY(i, jminus, lambda);
            myu.setXY(i, j, -(lambda + 1));
            myu.setXY(i, jplus, lambda);
        }
        /*
         * adding boundary conditions
         */
        myu.setXY(1, 1, 1.0);
        myu.setXY(l.samples, l.samples, 1.0);

        for (int j = 1; j <= l.getNumWords(); j++) {
            /*
             * build zeta
             */
            for (int i = 2; i <= l.samples - 1; i++) {
                zeta.setX(i, (lambda - 1) * l.curve[i - 1].getX(j));
            }
            /*
             * boundary condition
             */
            zeta.setX(1, l.curve[0].getX(j));
            zeta.setX(l.samples, l.curve[l.samples - 1].getX(j));

            Vector v = myu.solveTridiagonalSystem(zeta);
            for (int i = 1; i <= l.samples; i++) {
                l.curve[i - 1].setX(j, v.getX(i));
            }
        }
    }
}
