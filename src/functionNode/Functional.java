package functionNode;

import algebra.src.Vector;
import realFunction.src.ExpressionFunction;

import java.util.Stack;

/**
 * 
 * @author pedro
 * 
 *         this is a depth search tree
 */

public abstract class Functional extends FunctionNode {
	String[] dummyVarName;
	ExpressionFunction expr;

	public Functional(String[] dummyVar, ExpressionFunction expr) {
		super();
		this.dummyVarName = dummyVar;
		this.expr = expr;
		for (int i = 0; i < dummyVar.length; i++) {
			this.expr.addToken(dummyVar[i]);
			this.expr.putDummyVarintoMap(dummyVar[i], new Stack<Double>());
		}
	}

	public Functional(String[] dummyVar, ExpressionFunction expr, int nArgs,
			FunctionNode[] args) {
		super(nArgs, args);
		this.dummyVarName = dummyVar;
		this.expr = expr;
		/**
		 * some code repetition no big deal.
		 */
		for (int i = 0; i < dummyVar.length; i++) {
			this.expr.addToken(dummyVar[i]);
			this.expr.putDummyVarintoMap(dummyVar[i], new Stack<Double>());
		}
	}

	public abstract FunctionNode createNode(FunctionNode[] args);

	public abstract double compute(Vector x);
}
