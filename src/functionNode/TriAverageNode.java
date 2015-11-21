package functionNode;

import algebra.src.Vector;

public class TriAverageNode extends FunctionNode {
	
	public TriAverageNode(FunctionNode[] args) {
		super(3,args);
	}

	public TriAverageNode() {
		super();
	}

	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new TriAverageNode(args);
	}

	@Override
	public double compute(Vector x) {
		return (args[0].compute(x) + args[1].compute(x) + args[2].compute(x)) / 3.0;
	}

}