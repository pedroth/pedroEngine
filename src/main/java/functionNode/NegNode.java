package functionNode;

import algebra.Vector;

public class NegNode extends UnaryNode{
	
	public NegNode(FunctionNode[] args) {
		super(args);
	}

	public NegNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new NegNode(args);
	}

	@Override
	public double compute(Vector x) {
		return - args[0].compute(x);
	}
}
