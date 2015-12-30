package nlp.lowbow.src;

import algebra.src.Matrix;
import algebra.src.TridiagonalMatrixSolver;
import algebra.src.Vector;
import numeric.src.MyMath;

/**
 * Created by Pedroth on 11/28/2015.
 */
public class LambdaTestFlow implements HeatMethod {
    private double dt;
    private Vector[] measureCurveOrig;
    private Vector[] measureCurve;
    private Vector[] nullCurve;
    private double maxError;

    public LambdaTestFlow(double dt) {
        this.dt = dt;
    }

    public LambdaTestFlow(double dt, LowBow lowBow) {
        this.dt = dt;

        measureCurveOrig = new Vector[lowBow.getCurve().length];
        nullCurve = new Vector[measureCurveOrig.length];

        for (int i = 0; i < lowBow.curve.length; i++) {
            measureCurveOrig[i] = lowBow.curve[i].copy();
        }

        nullCurve[0] = measureCurveOrig[0].copy();
        nullCurve[nullCurve.length - 1] = measureCurveOrig[measureCurveOrig.length - 1].copy();

        double step = 1.0 / (nullCurve.length - 1);
        Vector diff = Vector.diff(nullCurve[nullCurve.length - 1], nullCurve[0]);
        for (int i = 1; i < nullCurve.length - 1; i++) {
            nullCurve[i] = Vector.add(nullCurve[0], Vector.scalarProd(step * i, diff));
        }

        this.maxError = 0;
        for (int i = 0; i < measureCurveOrig.length; i++) {
            maxError += Vector.diff(measureCurveOrig[i], nullCurve[i]).squareNorm();
        }

        measureCurve = new Vector[nullCurve.length];
    }

    @Override
    public void heatFlow(double lambda, LowBow l) {
        // change
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
        //end change

        for (int i = 0; i < l.curve.length; i++) {
            measureCurve[i] = l.curve[i];
        }
    }

    public double getMaxError() {
        return maxError;
    }

    public double getDt() {
        return dt;
    }

    /**
     * @return sensitivity of lambda
     */
    public double lambdaMeasure() {
        double acc = 0;
        for (int i = 0; i < measureCurve.length; i++) {
            acc += Vector.diff(measureCurve[i], nullCurve[i]).squareNorm();
        }
        return 1.0 - acc / maxError;
    }
}
