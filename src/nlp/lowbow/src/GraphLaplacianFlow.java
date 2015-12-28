package nlp.lowbow.src;

import algebra.src.Matrix;
import algebra.src.Vec3;
import algebra.src.Vector;
import numeric.QuadraticFormMinimizer;

/**
 * Created by Pedroth on 12/20/2015.
 */
public class GraphLaplacianFlow implements HeatMethod {
    @Override
    public void heatFlow(double lambda, LowBow l) {
        /*
         * aux variables
         */
        double epsilon = 1E-8;
        Vector zeta = new Vector(l.samples);
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
        myu.setXY(1, 2, -lambda);
        myu.setXY(l.samples, l.samples - 1, -lambda);
        myu.setXY(l.samples, l.samples, 1.0);

        for (int j = 1; j <= l.numWords; j++) {
            /*
             * build zeta
             */
            for (int i = 1; i <= l.samples; i++) {
                zeta.setX(i, ((lambda - 1) * l.curve[i - 1].getX(j)));
            }
            Vector v = Matrix.solveLinearSystem(myu, Vector.scalarProd(-1, zeta), epsilon);
            for (int i = 1; i <= l.samples; i++) {
                l.curve[i - 1].setX(j, v.getX(i));
            }
        }
    }
}
