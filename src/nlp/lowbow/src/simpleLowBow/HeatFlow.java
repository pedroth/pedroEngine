package nlp.lowbow.src.simpleLowBow;

import algebra.src.Matrix;
import algebra.src.TridiagonalMatrix;
import algebra.src.Vector;
import numeric.src.MatrixExponetial;

/**
 * Solves the heat equation with Dirichlet Boundary condition using exponential matrix method
 */
public class HeatFlow implements HeatMethod {

    @Override
    public void heatFlow(double lambda, LowBow l) {
        int numWords = l.getNumWords();
        Vector[] zeta = new Vector[numWords];
        Matrix myu = new TridiagonalMatrix(l.samples);
        /*
         * build laplacian matrix
         */
        for (int i = 1; i <= l.samples; i++) {
            int jminus = Math.max(i - 1, 1);
            int jplus = Math.min(i + 1, l.samples);

            myu.setXY(i, jminus, 1);
            myu.setXY(i, i, -2);
            myu.setXY(i, jplus, 1);
        }

        myu.setXY(1, 1, 0.0);
        myu.setXY(1, 2, 0);
        myu.setXY(l.samples, l.samples - 1, 0);
        myu.setXY(l.samples, l.samples, 0.0);

        // build zeta
        for (int j = 1; j <= numWords; j++) {

            zeta[j - 1] = new Vector(l.samples);

            for (int i = 1; i <= l.samples; i++) {
                zeta[j - 1].setX(i, l.curve[i - 1].getX(j));
            }
        }

        int samples = 100;
        for (int j = 1; j <= numWords; j++) {
            Vector v = MatrixExponetial.exp(2 * lambda, myu, zeta[j - 1], samples);
            for (int i = 1; i <= l.samples; i++) {
                l.curve[i - 1].setX(j, v.getX(i));
            }
        }
    }
}
