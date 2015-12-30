package nlp.lowbow.src;

import algebra.src.Matrix;
import algebra.src.Vector;
import numeric.src.MatrixExponetial;

/**
 * Created by Pedroth on 12/29/2015.
 */
public class HeatFlow implements HeatMethod {

    @Override
    public void heatFlow(double lambda, LowBow l) {
        Vector[] zeta = new Vector[l.numWords];
        Matrix myu = new Matrix(l.samples, l.samples);
        /*
         * build matrix myu
         */
        for (int i = 1; i <= l.samples; i++) {
            int j = i;
            int jminus = Math.max(j - 1, 1);
            int jplus = Math.min(j + 1, l.samples);

            myu.setXY(i, jminus, -lambda);
            myu.setXY(i, j, (lambda + 1));
            myu.setXY(i, jplus, -lambda);
        }

        myu.setXY(1, 1, 1.0);
        myu.setXY(1, 2, 0.0);
        myu.setXY(l.samples, l.samples - 1, 0.0);
        myu.setXY(l.samples, l.samples, 1.0);

        // build zeta
        for (int j = 1; j <= l.numWords; j++) {

            zeta[j - 1] = new Vector(l.samples);

            for (int i = 1; i <= l.samples; i++) {
                zeta[j - 1].setX(i, l.curve[i - 1].getX(j));
            }
        }

        int samples = 100;
        for (int j = 1; j <= l.numWords; j++) {
            Vector v = MatrixExponetial.exp(lambda, myu, zeta[j - 1], samples);
            for (int i = 1; i <= l.samples; i++) {
                l.curve[i - 1].setX(j, v.getX(i));
            }
        }

    }
}
