package nlp.lowbow.src;

import algebra.src.Vector;

import java.util.Stack;

public class SparseHeatFlow implements HeatMethod {

	@Override
	public void heatFlow(double lambda, LowBow l) {
		Stack<Double> stack = new Stack<Double>();
		double acmGrad = 0;
		double maxCost = Double.MIN_VALUE;
		int maxIte = 10000;
		int ite = 0;
		/**
		 * convergence error
		 */
		double epsilon = 1E-3;
		double h = 1E-4;
		/**
		 * time l.step of heat
		 */
		double dt = 1E-5;
		/**
		 * horrible parameter, to compute time l.step
		 */
		double eta = 0.5;
		Vector[] auxCurve = new Vector[l.curve.length];
		Vector[] grad = new Vector[l.curve.length];
		/**
		 * initial condition u(x,0) = u_init(x)
		 */
		for (int i = 0; i < l.curve.length; i++) {
			grad[i] = new Vector(l.numWords);
			auxCurve[i] = l.curve[i].copy();
		}

		stack.push(Double.MAX_VALUE);
		
		if(lambda == 0.0) {
			return;
		}
		
		do {
			acmGrad = 0;
			for (int i = 1; i < l.curve.length - 1; i++) {
				/**
				 * second derivative
				 */
				Vector d2x = Vector.add(Vector.diff(l.curve[i + 1], Vector.scalarProd(2, l.curve[i])), l.curve[i - 1]);
				d2x = Vector.scalarProd(lambda * (l.samples - 1), d2x);
				/**
				 * distance from the original l.curve
				 */
				grad[i] = Vector.scalarProd((1 - lambda) * l.step, Vector.diff(l.curve[i], l.curve[i]));
				/**
				 * gradient calculation
				 */
				grad[i] = Vector.add(d2x, grad[i]);
				acmGrad += grad[i].squareNorm();
			}
			/**
			 * choosing dt
			 * 
			 * tried armijo condition and it didnt work
			 */
			/**
			 * compute second time derivative in gradient direction
			 */
			double[] acmCost = { 0, 0, 0 };
			for (int j = 0; j < 3; j++) {
				double delta = j * h;
				for (int i = 1; i < l.curve.length - 1; i += 2) {
					/**
					 * update l.curve
					 */
					auxCurve[i] = Vector.add(l.curve[i], Vector.scalarProd(delta, grad[i]));
					auxCurve[i + 1] = Vector.add(l.curve[i + 1], Vector.scalarProd(delta, grad[i + 1]));
					/**
					 * compute cost functions
					 */
					Vector dx = Vector.scalarProd((l.samples - 1), Vector.diff(auxCurve[i + 1], auxCurve[i]));
					acmCost[j] += ((1 - lambda) * Vector.diff(l.curve[i], auxCurve[i]).squareNorm() + lambda  * dx.squareNorm());
				}
				acmCost[j] *= 0.5 * l.step;
			}
			/**
			 * choosing dt, baah
			 */
			double lastAcmGrad = stack.pop();
			if (acmGrad - lastAcmGrad > 0) {
				eta *= eta;
			} else {
				eta += 0.1 * (0.99 - eta);
			}
			/**
			 * more cool
			 */
			dt = eta * acmGrad / ((acmCost[2] - 2 * acmCost[1] + acmCost[0]) / (h * h));
			/**
			 * end choosing dt
			 */
			
			/**
			 * update l.curve
			 */
			for (int i = 1; i < l.curve.length - 1; i += 2) {
				l.curve[i] = Vector.add(l.curve[i], Vector.scalarProd(dt, grad[i]));
				l.curve[i + 1] = Vector.add(l.curve[i + 1], Vector.scalarProd(dt, grad[i + 1]));
			}

			stack.push(new Double(acmGrad));
			
			maxCost = Math.max(maxCost, acmGrad);
			ite++;
			
//			System.out.println("" + acmCost[0] + "\t" + dt + "\t" + eta + "\t" + (1 - (1.0 / maxCost) * acmGrad) + "\t" + ite);
//			System.out.printf("%.5f\n" , 1 - (1.0 / maxCost) * acmGrad);
		} while (acmGrad > epsilon && ite < maxIte);
		
	}
}
