package functionNode;

import algebra.src.Vector;

public class CosNode extends UnaryNode {
	
	public CosNode(FunctionNode[] args) {
		super(args);
	}

	public CosNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new CosNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.cos(args[0].compute(x));
	}

}
