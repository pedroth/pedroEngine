package functionNode;

import algebra.src.Vector;
import realFunction.src.ExpressionFunction;

public class DummyVariableNode extends FunctionNode {
	String dummyVarName;
	ExpressionFunction expr;

	public DummyVariableNode(String dummyVarName, ExpressionFunction expr) {
		super(0, null);
		this.dummyVarName = dummyVarName;
		this.expr = expr;
	}

	/**
	 * never will be used
	 */
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double compute(Vector x) {
		return expr.peekDummyVar(dummyVarName);
	}

}
