package nlp.lowbow.src;

import algebra.src.Vector;

import java.util.Stack;

public class SparseHeatFlow implements HeatMethod {

    @Override
    public void heatFlow(double lambda, LowBow l) {
        double acmGrad;
        int maxIte = 10000;
        int ite = 0;
        /**
         * convergence error
         */
        double epsilon = 1E-8;
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

            dt = 1.0 / (lambda + 1);
            /*
             * update l.curve
             */
            for (int i = 1; i < l.curve.length - 1; i += 2) {
                l.curve[i] = Vector.add(l.curve[i], Vector.scalarProd(dt, grad[i]));
                l.curve[i + 1] = Vector.add(l.curve[i + 1], Vector.scalarProd(dt, grad[i + 1]));
            }
            ite++;
			System.out.println(acmGrad);
        } while (acmGrad > epsilon && ite < maxIte);
    }
}
