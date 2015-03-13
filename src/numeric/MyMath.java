package numeric;

public class MyMath {
	public static double clamp(double x, double xmin, double xmax) {
		return Math.max(xmin, Math.min(xmax, x));
	}
}
