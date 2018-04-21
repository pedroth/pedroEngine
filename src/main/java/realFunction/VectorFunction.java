package realFunction;

import algebra.Vector;

public abstract class VectorFunction {
	private int inputDim, outputDim;
	
	public VectorFunction(int inputDim, int outputDim) {
		super();
		this.inputDim = inputDim;
		this.outputDim = outputDim;
	}
	/**
	 * 
	 * @return
	 * 	dimension of output vector
	 */
	public int getInputDim() {
		return inputDim;
	}
	/**
	 * 
	 * @return
	 * 	dimension of the input vector
	 */	
	public int getOutputDim() {
		return outputDim;
	}
	
	public abstract Vector compute(Vector x);
		
}
