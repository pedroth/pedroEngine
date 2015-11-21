package functionNode;

import algebra.src.Vector;

public class AddNode extends BinaryNode {

	public AddNode(FunctionNode[] args) {
		super(args);
	}

	public AddNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new AddNode(args);
	}

	@Override
	public double compute(Vector x) {
		return args[0].compute(x) + args[1].compute(x);
	}

}
