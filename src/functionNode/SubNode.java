package functionNode;

import algebra.Vector;

public class SubNode extends BinaryNode {

	public SubNode(FunctionNode[] args) {
		super(args);
	}

	public SubNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new SubNode(args);
	}

	@Override
	public double compute(Vector x) {
		return args[0].compute(x) - args[1].compute(x);
	}

}