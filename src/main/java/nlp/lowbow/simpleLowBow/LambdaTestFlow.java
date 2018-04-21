package nlp.lowbow.simpleLowBow;

import algebra.Matrix;
import algebra.TridiagonalMatrix;
import algebra.Vector;
import numeric.MatrixExponetial;

/**
 * Created by Pedroth on 11/28/2015.
 * <p>
 * Tests how a change in lambda changes the smoothing
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
        int numWords = l.getNumWords();
        Vector[] zeta = new Vector[numWords];
        Matrix myu = new TridiagonalMatrix(l.samples);
        /*
         * build matrix myu
         */
        for (int i = 1; i <= l.samples; i++) {
            int jminus = Math.max(i - 1, 1);
            int jplus = Math.min(i + 1, l.samples);

            myu.setXY(i, jminus, 1);
            myu.setXY(i, i, -2);
            myu.setXY(i, jplus, 1);
        }

        myu = Matrix.scalarProd(100.0, myu);

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

        int samples = 500;
        for (int j = 1; j <= numWords; j++) {
            Vector v = MatrixExponetial.exp(lambda, myu, zeta[j - 1], samples);
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
