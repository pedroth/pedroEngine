package functionNode;

import algebra.Vector;

public class SinNode extends UnaryNode {
	
	public SinNode(FunctionNode[] args) {
		super(args);
	}

	public SinNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new SinNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.sin(args[0].compute(x));
	}

}
