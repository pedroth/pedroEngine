package functionNode;

import algebra.Vector;

public class PowNode extends BinaryNode {
	public PowNode(FunctionNode[] args) {
		super(args);
	}

	public PowNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new PowNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.pow(args[0].compute(x), args[1].compute(x));
	}
}
