package nlp.lowbow.src;

import algebra.src.Matrix;
import algebra.src.TridiagonalMatrixSolver;
import algebra.src.Vector;
import numeric.src.MyMath;

public class MatrixHeatFlow implements HeatMethod {

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

            Vector v = TridiagonalMatrixSolver.solveTridiagonalSystem(myu, zeta);
            //System.out.println(Vector.diff(Vector.matrixProd(myu,v),zeta));
            for (int i = 1; i <= l.samples; i++) {
                l.curve[i - 1].setX(j, v.getX(i));
            }
        }
    }
}
