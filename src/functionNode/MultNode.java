package functionNode;

import algebra.src.Vector;

public class MultNode extends BinaryNode{
	public MultNode(FunctionNode[] args) {
		super(args);
	}

	public MultNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new MultNode(args);
	}

	@Override
	public double compute(Vector x) {
		return args[0].compute(x) * args[1].compute(x);
	}
}
