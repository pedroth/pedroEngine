package numeric;

import algebra.src.Vector;
import realFunction.src.ExpressionFunction;

public class MyMath {
	public static double clamp(double x, double xmin, double xmax) {
		return Math.max(xmin, Math.min(xmax, x));
	}

	public static double dirac(double x) {
		if (x == 0)
			return 1.0;
		else
			return 0;
	}

	public static double heaviside(double x) {
		if (x > 0)
			return 1.0;
		else if (x == 0)
			return 0.5;
		else
			return 0;
	}
	
	public static double step(double x) {
		if (x >= 0)
			return 1.0;
		else 
			return 0;
	}
	
	/**
	 * http://www.johndcook.com/blog/cpp_phi/
	 * 
	 * @param x
	 * @return gaussian cumulative distribution function
	 */
	public static double phi(double x) {
		// constants
		double a1 = 0.254829592;
		double a2 = -0.284496736;
		double a3 = 1.421413741;
		double a4 = -1.453152027;
		double a5 = 1.061405429;
		double p = 0.3275911;

		// Save the sign of x
		int sign = 1;
		if (x < 0)
			sign = -1;
		x = Math.abs(x) / Math.sqrt(2.0);

		// A&S formula 7.1.26
		double t = 1.0 / (1.0 + p * x);
		double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

		return 0.5 * (1.0 + sign * y);
	}
	
	/**
	 * warning does not accept variables
	 * 
	 * @param s
	 *            string to be read.
	 * @return computation of the expression in s
	 */
	public static double numericRead(String s) {
		ExpressionFunction in;
		in = new ExpressionFunction(s, new String[] {});
		in.init();
		return in.compute(new Vector(new double[] {}));
	}
}
