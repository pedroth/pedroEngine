package functionNode;

import algebra.src.Vector;

public class ASinNode extends UnaryNode {

	public ASinNode(FunctionNode[] args) {
		super(args);
	}

	public ASinNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ASinNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.asin(args[0].compute(x));
	}

}
