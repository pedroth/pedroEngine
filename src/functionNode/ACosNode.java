package functionNode;

import algebra.src.Vector;

public class ACosNode extends UnaryNode {
	
	public ACosNode(FunctionNode[] args) {
		super(args);
	}

	public ACosNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ACosNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.acos(args[0].compute(x));
	}
}
