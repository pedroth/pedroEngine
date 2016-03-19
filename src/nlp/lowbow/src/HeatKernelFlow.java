package nlp.lowbow.src;

import algebra.src.Vector;

/**
 * Created by Pedroth on 11/23/2015.
 *
 * Solves the heat equation with a kernel method
 */
public class HeatKernelFlow implements HeatMethod {
    @Override
    public void heatFlow(double lambda, LowBow l) {
        double myu = 0;
        for (int i = 0; i < l.samples; i++) {
            for (int j = 1; j <= l.numWords; j++) {
                l.curve[i].setX(j, i == 0 || i == (l.samples-1) ? l.curve[i].getX(j) : l.gamma(myu, lambda, l.samples, j));
            }
            /**
             * normalization, because the integration is just an approximation
             * error decreases as number of samples increases
             */
            Vector ones = new Vector(l.numWords);
            ones.fill(1.0);
            double dot = Vector.innerProd(l.curve[i], ones);
            l.curve[i] = Vector.scalarProd(1 / dot, l.curve[i]);
            myu += l.step;
        }
    }
}
