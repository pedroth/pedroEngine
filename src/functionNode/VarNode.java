package functionNode;

import algebra.src.Vector;

public class VarNode extends FunctionNode {
	int varNum;
	
	public VarNode(int varNum){
		super(0, null);
		this.varNum = varNum;
	}
	
	/**
	 * never will be used
	 */
	@Override
	public FunctionNode createNode(FunctionNode[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double compute(Vector x) {
		return x.getX(varNum + 1);
	}

}
