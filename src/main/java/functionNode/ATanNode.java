package functionNode;

import algebra.Vector;

public class ATanNode extends UnaryNode {
	public ATanNode(FunctionNode[] args) {
		super(args);
	}

	public ATanNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ATanNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.atan(args[0].compute(x));
	}
}
