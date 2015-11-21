package functionNode;

import algebra.src.Vector;

public abstract class BinaryNode extends FunctionNode{
	
	public BinaryNode(){
		super(2,null);
	}
	
	public BinaryNode(FunctionNode[] args){
		super(2,args);
	}

	@Override
	public abstract FunctionNode createNode(FunctionNode[] args) ;

	@Override
	public abstract double compute(Vector x);

}
