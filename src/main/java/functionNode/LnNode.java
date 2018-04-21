package functionNode;

import algebra.Vector;

public class LnNode extends UnaryNode {
	
	public LnNode(FunctionNode[] args) {
		super(args);
	}

	public LnNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new LnNode(args);
	}

	@Override
	public double compute(Vector x) {
		return Math.log(args[0].compute(x));
	}

}