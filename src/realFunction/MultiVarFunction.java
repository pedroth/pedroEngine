package realFunction;

import algebra.Vector;


public abstract class MultiVarFunction {
	private int inputDim;

	public MultiVarFunction(int inputDim) {
		super();
		this.inputDim = inputDim;
	}

	public int getInputDim() {
		return inputDim;
	}
	
	public void setInputDim(int inputDim) {
		this.inputDim = inputDim;
	}
		
	public abstract double compute(Vector x);
}
