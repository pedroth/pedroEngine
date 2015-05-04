package numeric;

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
}
