package nlp.lowbow;

import inputOutput.MyText;
import numeric.MyMath;
import algebra.Matrix;
import algebra.Vector;

public class MatrixHeatFlow implements HeatFlow {

	@Override
	public void heatFlow(double lambda, LowBow l) {
		l.heatCurve = new Vector[l.curve.length];
		for (int i = 0; i < l.curve.length; i++) {
			l.heatCurve[i] = new Vector(l.numWords);
		}
		/**
		 * aux variables
		 */
		double epsilon = 1E-8;
		Vector zeta = new Vector(l.samples);
		Matrix myu = new Matrix(l.samples, l.samples);
		/**
		 * build matrix myu
		 */
		for (int i = 1; i <= l.samples; i++) {
				/**
				 * ((lambda / step)  * ( dirac(i-j-1) - 2dirac(i-j) + dirac(i-j+1)) - (1-lambda)*step*dirac(i-j)) * (step(i - 2) - step(i - samples))
				 */
				int j = i;
				int jminus = Math.max(j - 1 , 1);
				int jplus = Math.min(j + 1 , l.samples);
				
				myu.setXY(i, jminus, (-(1 - lambda) * l.step * MyMath.dirac(i - jminus) + (l.samples - 1) * lambda * (MyMath.dirac(i - jminus - 1) - 2 * MyMath.dirac(i - jminus) + MyMath.dirac(i - jminus + 1))) * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
				myu.setXY(i, j, (-(1 - lambda) * l.step * MyMath.dirac(i - j) + (l.samples - 1) * lambda * (MyMath.dirac(i - j - 1) - 2 * MyMath.dirac(i - j) + MyMath.dirac(i - j + 1))) * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
				myu.setXY(i, jplus, (-(1 - lambda) * l.step * MyMath.dirac(i - jplus) + (l.samples - 1) * lambda * (MyMath.dirac(i - jplus - 1) - 2 * MyMath.dirac(i - jplus) + MyMath.dirac(i - jplus + 1))) * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
				
				/**
				 * adding boundary conditions
				 */
				myu.setXY(i, j, myu.getXY(i, j) + MyMath.dirac(i - 1) * MyMath.dirac(j - 1) + MyMath.dirac(i - l.samples) * MyMath.dirac(j - l.samples));
		}
		
		MyText t1 = new MyText();
		t1.write("C:/Users/Pedroth/Desktop/Text0.txt", myu.toStringMatlab());

		for (int j = 1; j <= l.numWords; j++) {
			/**
			 * build zeta
			 */
			for (int i = 1; i <= l.samples; i++) {
				zeta.setX(i, (-(1 - lambda) * l.step * l.curve[i - 1].getX(j)) * (MyMath.step(i - 2) - MyMath.step(i - l.samples)));
				zeta.setX(i, zeta.getX(i) +  l.curve[i - 1].getX(j) * (MyMath.dirac(i - 1) + MyMath.dirac(i - l.samples)));
			}
			Vector v = Matrix.solveLinearSystem(myu, zeta,epsilon);
//			System.out.println(v);
//			t1.write("C:/Users/pedro/Desktop/Text"+ (j) + ".txt", zeta.toStringMatlab());
//			System.out.println(Vector.diff(Vector.matrixProd(myu, v),zeta).norm());
			for (int i = 1; i <= l.samples; i++) {
				l.heatCurve[i - 1].setX(j, v.getX(i));
			}
//			System.out.println(1.0 * j /l.numWords);
		}
		
	}
}
