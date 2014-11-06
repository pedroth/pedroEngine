package functionNode;

import algebra.Vector;

public class TanNode extends UnaryNode {
	
	public TanNode(FunctionNode[] args) {
		super(args);
	}

	public TanNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new TanNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.tan(args[0].compute(x));
	}

}