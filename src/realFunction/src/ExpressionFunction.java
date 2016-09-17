package realFunction.src;

import algebra.src.Vector;
import functionNode.*;
import realFunction.utils.SyntaxErrorException;
import tokenizer.NumbersTokenizer;
import tokenizer.TokenRecognizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ExpressionFunction extends MultiVarFunction {
	private TokenRecognizer tokenRecog;
	/**
	 * maps name of variables to its position in the arguments
	 */
	private Map<String, Integer> varNametoInt;
	/**
	 * maps name of functions to the function object
	 */
	private Map<String, FunctionNode> functionNametoFunc;
	/**
	 * maps name of the operators to its priority
	 * 
	 * priority belongs to the integer number set. higher priority means an
	 * higher number.
	 */
	private Map<String, Integer> operatorNametoPriotity;
	/**
	 * maps constants names to constant node
	 */
	private Map<String, ConstantNode> constantNametoNode;
	private FunctionNode posfixExpr;
	private String[] lexOut;
	/**
	 * input
	 */
	private String expr;
	private String[] vars;
	/**
	 * all tokens that program will use
	 */
	private java.util.Vector<String> tokens;
	/**
	 * maps dummy variable name to its stack
	 */
	private Map<String, Stack<Double>> dummyVarNametoStack;

	/**
	 * 
	 * @param expr
	 *            = mathematical expression. ex: sin(x + 3 * y / exp(pi * z)) ^
	 *            2
	 * @param vars
	 *            = array with the name of variables, order of the name matters.
	 *            Names that are equal to function will not be recognized. Ex
	 *            that works: "x","xyz","x1","mariacachucha". badEx: "exp",
	 *            "sin(x)".
	 */
	public ExpressionFunction(String expr, String[] vars) {
		super((vars != null) ? vars.length : 0);
		this.expr = expr;

		if (vars != null)
			this.vars = vars;
		else
			this.vars = new String[0];

		varNametoInt = new HashMap<String, Integer>();
		operatorNametoPriotity = new HashMap<String, Integer>();
		functionNametoFunc = new HashMap<String, FunctionNode>();
		constantNametoNode = new HashMap<String, ConstantNode>();
		tokens = new java.util.Vector<String>();
		dummyVarNametoStack = new HashMap<String, Stack<Double>>();
	}

	public static void main(String[] args) {
		String[] varTokens = {"u", "x", "y"};
//		ExpressionFunction foo = new ExpressionFunction("sigma(0.5^i,1,99)", varTokens);
		ExpressionFunction foo = new ExpressionFunction("(x + y) + 1.0", varTokens);
		String[] dummyVar = {"i"};
		foo.addFunction("sigma", new Sigma(dummyVar, foo));
		/**
		 * you must create to variables of String[]
		 */
		foo.init();
		double[] vars = {3.141592, 0.5, 0.5};
		Vector x = new Vector(vars);
		double oldTime = System.nanoTime() * 1E-9;
		System.out.println(foo.compute(x));
		System.out.println(System.nanoTime() * 1E-9 - oldTime);
	}

	public void init() {
		// add functions here or in main
		addOperator("+", 1, new AddNode());

		addOperator("-", 1, new SubNode());

		addOperator("*", 2, new MultNode());

		addOperator("/", 2, new DivNode());

		addOperator("^", 5, new PowNode());

		addOperator("u-", 4, new NegNode());

		addFunction("sin", new SinNode());

		addFunction("cos", new CosNode());

		addFunction("exp", new ExpNode());

		addFunction("tan", new TanNode());

		addFunction("ln", new LnNode());

		addFunction("atan", new ATanNode());

		addFunction("acos", new ACosNode());

		addFunction("asin", new ASinNode());

		addConstant("pi", new ConstantNode(Math.PI));

		tokens.add("(");

		tokens.add(")");

		tokens.add(",");

		for (int i = 0; i < vars.length; i++) {
			tokens.add(vars[i]);
			varNametoInt.put(vars[i], i);
		}
		lexAnalysis(expr);

		SyntaxAnalysis();
	}

	private void SyntaxAnalysis() throws SyntaxErrorException {
		java.util.Vector<String> stack = new java.util.Vector<String>();
		java.util.Vector<FunctionNode> output = new java.util.Vector<FunctionNode>();
		int size = lexOut.length;
		String aux;
		try {
			for (int i = 0; i < size; i++) {
				aux = lexOut[i];
				if (aux == "(") {

					stack.add(aux);

				} else if (aux == ")") {

					String s = stack.lastElement();
					while (s != "(") {
						popFunction(stack, output);
						s = stack.lastElement();
					}
					stack.remove(stack.size() - 1);
					if (!stack.isEmpty() && functionNametoFunc.get(stack.lastElement()) != null && operatorNametoPriotity.get(stack.lastElement()) == null) {
						popFunction(stack, output);
					}

				} else if (aux == ",") {

					String s = stack.lastElement();
					while (s != "(") {
						popFunction(stack, output);
						s = stack.lastElement();
					}

				} else if (varNametoInt.get(aux) != null) {

					output.add(new VarNode(varNametoInt.get(aux)));

				} else if (dummyVarNametoStack.get(aux) != null) {

					output.add(new DummyVariableNode(aux, this));

				} else if (operatorNametoPriotity.get(aux) != null && aux != "^") {

					int opPriority = operatorNametoPriotity.get(aux);
					while (!stack.isEmpty() && operatorNametoPriotity.get(stack.lastElement()) != null && opPriority <= operatorNametoPriotity.get(stack.lastElement())) {
						popFunction(stack, output);
					}
					stack.add(aux);
				} else if (operatorNametoPriotity.get(aux) != null && aux == "^") {

					int opPriority = operatorNametoPriotity.get(aux);
					while (!stack.isEmpty() && operatorNametoPriotity.get(stack.lastElement()) != null && opPriority < operatorNametoPriotity.get(stack.lastElement())) {
						popFunction(stack, output);
					}
					stack.add(aux);

				} else if (functionNametoFunc.get(aux) != null) {

					stack.add(aux);

				} else if (constantNametoNode.get(aux) != null) {

					output.add(constantNametoNode.get(aux));

				} else {

					output.add(new ConstantNode(Double.parseDouble(aux)));

				}
			}
			int stackSize = stack.size();
			for (int j = 0; j < stackSize; j++) {
				popFunction(stack, output);
			}

			posfixExpr = output.lastElement();

		} catch (Exception e) {
			e.printStackTrace();
			throw new SyntaxErrorException();
		}
	}

	private void lexAnalysis(String expr) {
		String[] patterns = tokens.toArray(new String[0]);
		tokenRecog = new NumbersTokenizer(patterns);
		tokenRecog.init();
		lexOut = tokenRecog.tokenize(expr);
		checkForUnitaryOp(lexOut);
	}

	private void checkForUnitaryOp(String[] lex) {
		boolean changeToUna = false;
		for (int i = 0; i < lex.length; i++) {
			if (lex[i] == "-") {
				if (i == 0)
					changeToUna = true;
				else if (operatorNametoPriotity.get(lex[i - 1]) != null)
					changeToUna = true;
				else if (lex[i - 1] == "(")
					changeToUna = true;
				else if (lex[i - 1] == ",")
					changeToUna = true;

				if (changeToUna)
					lex[i] = "u-";

				changeToUna = false;
			}
		}
	}

	private void popFunction(java.util.Vector<String> stack, java.util.Vector<FunctionNode> output) {
		String s = stack.lastElement();
		stack.remove(stack.size() - 1);
		FunctionNode f = null;
		f = functionNametoFunc.get(s);
		FunctionNode[] args = new FunctionNode[f.getInputDim()];
		int outsize = output.size();
		for (int j = 0; j < f.getInputDim(); j++) {
			args[f.getInputDim() - j - 1] = output.get(outsize - j - 1);
			output.remove(outsize - j - 1);
		}
		output.add(f.createNode(args));
	}

	public void addOperator(String token, int priority, FunctionNode node) {
		tokens.add(token);
		functionNametoFunc.put(token, node);
		operatorNametoPriotity.put(token, priority);
	}

	public void addFunction(String token, FunctionNode node) {
		tokens.add(token);
		functionNametoFunc.put(token, node);
	}

	public void addConstant(String token, ConstantNode node) {
		tokens.add(token);
		constantNametoNode.put(token, node);
	}

	public void pushDummyVar(String dummyVarName, double x) {
		Stack<Double> aux = dummyVarNametoStack.get(dummyVarName);
		aux.push(x);
	}

	public double popDummyVar(String dummyVarName) {
		Stack<Double> aux = dummyVarNametoStack.get(dummyVarName);
		return aux.pop();
	}

	public double peekDummyVar(String dummyVarName) {
		Stack<Double> aux = dummyVarNametoStack.get(dummyVarName);
		return aux.peek();
	}

	public void addToken(String s) {
		tokens.add(s);
	}

	public void putDummyVarintoMap(String dummyVarName, Stack<Double> stack) {
		if (dummyVarNametoStack.get(dummyVarName) == null) {
			dummyVarNametoStack.put(dummyVarName, stack);
		}
	}

	public double compute(Vector x) {
		return posfixExpr.compute(x);
	}
}
