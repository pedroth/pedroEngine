package functionNode;

import algebra.src.Vector;

public class ExpNode extends UnaryNode {
	
	public ExpNode(FunctionNode[] args) {
		super(args);
	}

	public ExpNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ExpNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.exp(args[0].compute(x));
	}

}