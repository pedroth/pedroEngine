package functionNode;

import algebra.src.Vector;
import realFunction.src.ExpressionFunction;

/**
 * 
 * @author pedro
 * 
 *         first input must be a function of the dummy variable plus everything
 *         you like
 * 
 *         ex: sigma(1/2^<dummyVariable>,0, (x - y ) / h);
 * 
 *         how to use in code:
 * 
 *         String[] varTokens = { "u", "x", "y" }; ExpressionFunction foo = new
 *         ExpressionFunction("sigma(sigma(u,x,i),x,y)", varTokens); String[]
 *         dummyVar ={"i"}; foo.addFunction("sigma", new Sigma(dummyVar,foo));
 *         foo.init(); Double[] vars = { 3.141592, 1.0, 100.0 };
 *         System.out.print(foo.compute(vars));
 * 
 * 
 */
public class Sigma extends Functional {
	/**
	 * 
	 * @param args
	 * @param dummyVar
	 *            must be of size 1
	 * @param expr
	 */
	public Sigma(FunctionNode[] args, String[] dummyVar, ExpressionFunction expr) {
		super(dummyVar, expr, 3, args);
	}

	public Sigma(String[] dummyVar, ExpressionFunction expr) {
		super(dummyVar, expr);
		setInputDim(3);
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new Sigma(args, this.dummyVarName, this.expr);
	}

	@Override
	public double compute(Vector x) {
		int lower = (int) Math.floor(args[1].compute(x));
		int upper = (int) Math.floor(args[2].compute(x));
		double acm = 0;
		for (int i = lower; i <= upper; i++) {
			expr.pushDummyVar(this.dummyVarName[0], i);
			acm += args[0].compute(x);
			expr.popDummyVar(this.dummyVarName[0]);
		}
		return acm;
	}

}
