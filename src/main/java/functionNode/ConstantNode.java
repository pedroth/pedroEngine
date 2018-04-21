package functionNode;

import algebra.Vector;

public class ConstantNode extends FunctionNode {
	private Double value;
	
	public ConstantNode(Double value) {
		super(0, null);
		this.value = value;
	}

	/**
	 * never will be used
	 */
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		return new ConstantNode(1.0);
	}

	@Override
	public double compute(Vector x) {
		return value;
	}
}
