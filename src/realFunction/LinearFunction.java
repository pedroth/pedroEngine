package realFunction;


public class LinearFunction implements UniVarFunction {
	private double lambda;
	public LinearFunction(double r) {
		lambda = r;
	}
	@Override
	public double compute(double x) {
		return x * lambda;
	}
}
