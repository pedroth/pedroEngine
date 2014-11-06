package functionNode;

import realFunction.MultiVarFunction;

public abstract class FunctionNode extends MultiVarFunction {
	protected FunctionNode[] args;

	public FunctionNode() {
		super(0);
	}

	public FunctionNode(int nArgs, FunctionNode[] args) {
		super(nArgs);
		this.args = args;
	}
	
	/**
	 * 
	 * @param args
	 * @return create functionNode object with the args;
	 */
	public abstract FunctionNode createNode(FunctionNode[] args);
}
