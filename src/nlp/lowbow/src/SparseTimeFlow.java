package nlp.lowbow.src;

import algebra.src.Vector;

/**
 * Created by Pedroth on 11/28/2015.
 */
public class SparseTimeFlow implements HeatMethod {
    private double dt;
    private Vector[] measureCurveOrig;
    private Vector[] measureCurve;
    private Vector[] nullCurve;
    private double maxError;

    public SparseTimeFlow(double dt) {
        this.dt = dt;
    }

    public SparseTimeFlow(double dt, LowBow lowBow) {
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
        double acmGrad;
        int maxIte = 10000;
        int ite = 0;
//        lambda = (1- lambda) / (lambda == 0.0 ? 1E-9 : lambda);
        /**
         * convergence error
         */
        double epsilon = 1E-16;
        /**
         * time l.step of heat
         */
        double dt;
        Vector[] auxCurve = new Vector[l.curve.length];
        Vector[] grad = new Vector[l.curve.length];
        /**
         * initial condition u(x,0) = u_init(x)
         */
        for (int i = 0; i < l.curve.length; i++) {
            grad[i] = new Vector(l.numWords);
            auxCurve[i] = l.curve[i].copy();
        }
        do {
            acmGrad = 0;
            for (int i = 1; i < l.curve.length - 1; i++) {
                Vector lambdaFactor = Vector.scalarProd(lambda, Vector.add(l.curve[i + 1], l.curve[i - 1]));
                Vector lambdaPlusFactor = Vector.scalarProd(-(lambda + 1), l.curve[i]);
                Vector originalFactor = Vector.scalarProd(1 - lambda, auxCurve[i]);

                grad[i] = Vector.add(lambdaFactor, Vector.add(lambdaPlusFactor, originalFactor));
                acmGrad += grad[i].squareNorm();
            }

           dt = 0.5 / (lambda + 1);
            /*
             * update l.curve
             */
            for (int i = 1; i < l.curve.length - 1; i += 2) {
                l.curve[i] = Vector.add(l.curve[i], Vector.scalarProd(dt, grad[i]));
                l.curve[i + 1] = Vector.add(l.curve[i + 1], Vector.scalarProd(dt, grad[i + 1]));
            }
            ite++;
        } while (acmGrad > epsilon && ite < maxIte);

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
