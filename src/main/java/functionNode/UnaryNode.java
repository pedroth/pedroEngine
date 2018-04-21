package functionNode;

import algebra.Vector;

public abstract class UnaryNode extends FunctionNode {
	
	public UnaryNode() {
		super(1, null);
	}

	public UnaryNode(FunctionNode[] args) {
		super(1, args);
	}

	
	public abstract FunctionNode createNode(FunctionNode[] args);

	@Override
	public abstract double compute(Vector x);
}
