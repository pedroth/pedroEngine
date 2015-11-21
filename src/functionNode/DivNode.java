package functionNode;

import algebra.src.Vector;

public class DivNode extends BinaryNode{
	public DivNode(FunctionNode[] args) {
		super(args);
	}

	public DivNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new DivNode(args);
	}

	@Override
	public double compute(Vector x) {
		return args[0].compute(x) / args[1].compute(x);
	}
}
