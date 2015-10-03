package nlp.lowbow;

import java.util.Stack;

import numeric.MyMath;
import algebra.Vector;
/**
 * 
 * @author pedro
 * 
 * just a test didnt work
 *
 */
public class FisherHeat implements HeatMethod {

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
		l.curve = new Vector[l.curve.length];
		Vector[] auxCurve = new Vector[l.curve.length];
		Vector[] grad = new Vector[l.curve.length];
		/**
		 * initial condition u(x,0) = u_init(x)
		 */
		for (int i = 0; i < l.curve.length; i++) {
			l.curve[i] = l.curve[i].copy();
			grad[i] = new Vector(l.numWords);
			auxCurve[i] = l.curve[i].copy();
		}

		stack.push(Double.MAX_VALUE);

		do {
			acmGrad = 0;
			for (int i = 1; i < l.curve.length - 1; i++) {
				/**
				 * second derivative
				 */
				Vector d2x = new Vector(l.curve[0].getDim());
				for (int j = 1; j <= d2x.getDim(); j++) {
					double a = l.curve[i+1].getX(j);
					double b = l.curve[i].getX(j);
					double c = l.curve[i-1].getX(j);
					double aux = ((c * c * (a * a - a * b) + b * b * (b - 2 * c) * (b - c))) / (b * b * c * c);
					d2x.setX(j, aux);
				}
				d2x = Vector.scalarProd(lambda * (l.samples - 1), d2x);
				/**
				 * distance from the original l.curve
				 */
				double dot = 0;
				for (int j = 1; j <= l.curve[i].getDim(); j++) {
					double aux = Math.max(0, l.curve[i].getX(j) * l.curve[i].getX(j));
					dot += Math.sqrt(aux);
				}
				dot = MyMath.clamp(dot * dot,1E-9,0.99999);
				grad[i] = Vector.scalarProd( (1-lambda) * l.step * (1 / Math.sqrt(1 - dot)), l.curve[i]);
				
				/**
				 * gradient calculation
				 */
				grad[i] = Vector.add(d2x, grad[i]);
				System.out.println(grad[i]);
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
//			double[] acmCost = { 0, 0, 0 };
//			for (int j = 0; j < 3; j++) {
//				double delta = j * h;
//				for (int i = 1; i < l.curve.length - 1; i += 2) {
//					/**
//					 * update l.curve
//					 */
//					auxCurve[i] = Vector.add(l.curve[i], Vector.scalarProd(delta, grad[i]));
//					auxCurve[i + 1] = Vector.add(l.curve[i + 1], Vector.scalarProd(delta, grad[i + 1]));
//					/**
//					 * compute cost functions
//					 */
//					Vector dx = Vector.scalarProd((l.samples - 1), Vector.diff(auxCurve[i + 1], auxCurve[i]));
//					
//					acmCost[j] += ((1 - lambda) * Vector.diff(l.curve[i], auxCurve[i]).squareNorm() + lambda  * dx.squareNorm());
//				}
//				acmCost[j] *= 0.5 * l.step;
//			}
//			/**
//			 * choosing dt, baah
//			 */
//			double lastAcmGrad = stack.pop();
//			if (acmGrad - lastAcmGrad > 0) {
//				eta *= eta;
//			} else {
//				eta += 0.1 * (0.99 - eta);
//			}
			/**
			 * more cool
			 */
			dt = 0.0001;//eta * acmGrad / ((acmCost[2] - 2 * acmCost[1] + acmCost[0]) / (h * h));
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
