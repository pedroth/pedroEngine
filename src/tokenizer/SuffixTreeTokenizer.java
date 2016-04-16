package tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * roughly speaking to construct state machine it takes O(|p| * 2 * m )
 * 
 * where |p| is the number of pattern, and m average size of each pattern = (1 /
 * |P|) * sigma(i = 0, |p|, |p[i]|).
 * 
 * @author pedro
 * 
 */
public class SuffixTreeTokenizer extends TokenRecognizer {
	protected Node root;

	public SuffixTreeTokenizer(String[] patterns) {
		super(patterns);
		root = new Node("root");
	}

	/**
	 * ---------------------------------
	 **/

	public static void main(String args[]) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String[] s = {"x", "exp", "gauss", "euler", "pedro", "sin", "cos",
				"ln", "sinh", "cosh", "tanh", "acos", "asin", "acosh", "asinh",
				"she", "hers"};
		SuffixTreeTokenizer st = new SuffixTreeTokenizer(s);
		st.init();
		String line = null;
		try {
			line = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] tokens = st.tokenize(line);
		for (int i = 0; i < tokens.length; i++) {
			System.out.println(tokens[i]);
		}
	}

	public void init() {
		String[] p = this.getPatterns();
		int psize = p.length;
		for (int i = 0; i < psize; i++)
			ConstructDSM(p[i], root);
		ConstructFailFunction();
		// printStateMachine(root);
	}

	private void ConstructDSM(String pattern, Node n) {
		int patternSize = pattern.length();
		for (int i = 0; i < patternSize; i++) {
			Character c = pattern.charAt(i);
			if (n.get(c) == null) {
				n.put(c);
			}
			n = n.get(c);
		}
		n.setFinalState(true);
		n.setToken(pattern);
	}

	private void ConstructFailFunction() {
		Set<Character> keys = root.map.keySet();
		for (Character k : keys) {
			Node n = root.get(k);
			buildFailFunction(n);
		}
	}

	private void buildFailFunction(Node n) {
		String[] p = this.getPatterns();
		Node oldState = n;
		int psize = p.length;
		String stack = "";
		for (int j = 0; j < psize; j++) {
			int strSize = p[j].length();
			for (int k = 0; k < strSize; k++) {
				Character aux = p[j].charAt(k);
				stack += aux;
				if (n.get(aux) != null) {
					n = n.get(aux);
				} else {
					n.putFail(aux, searchNode(stack));
					stack = "";
					n = oldState;
					break;
				}
			}
		}
	}

	private Node searchNode(String s) {
		int size = s.length();
		Node n = root;
		for (int i = 0; i < size; i++) {
			n = n.get(s.charAt(i));
		}
		return n;
	}

	@Override
	public String[] tokenize(String s) {
		Node state = root;
		int textSize = s.length();
		Vector<String> answer = new Vector<String>();

		for (int i = 0; i < textSize; i++) {
			Character c = s.charAt(i);
			if (state.hasNext(c)) {
				state = state.get(c);
			} else if (state.isFinalState()) {
				answer.add(state.getToken());
				i--;
				state = root;
			} else if (state.hasNextFail(c)) {
				state = state.getFail(c);
			}
		}
		if (state.isFinalState())
			answer.add(state.getToken());
		return answer.toArray(new String[0]);
	}

	/** --------------------------------- **/
	/** debug stuff */
	public void printStateMachine(Node n) {
		try {
			Set<Character> keys = n.map.keySet();
			for (Character k : keys) {
				printFailFunction(n);
				printStateMachine(n.get(k));
			}
		} catch (NullPointerException e) {

		}
	}

	public void printFailFunction(Node n) {
		try {
			Set<Character> keys = n.failFunction.keySet();
			for (Character k : keys) {
				System.out.println("state : " + n.getName() + " from : "
						+ n.getToken() + " with : " + k + " err--> "
						+ n.getFail(k).getName() + " from : "
						+ n.getFail(k).getToken());
			}
		} catch (NullPointerException e) {

		}
	}

	/**
	 * class Node, it has a map <Char,Node>
	 *
	 * @author pedro
	 */
	class Node {
		private Map<Character, Node> map;
		private Map<Character, Node> failFunction;
		private boolean finalState;
		private String token;
		private String name;

		Node(String name) {
			map = new HashMap<Character, Node>();
			failFunction = new HashMap<Character, Node>();
			this.name = name;
			finalState = false;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public boolean isFinalState() {
			return finalState;
		}

		public void setFinalState(boolean finalState) {
			this.finalState = finalState;
		}

		public void put(Character c) {
			map.put(c, new Node("" + c));
		}

		public Node get(Character c) {
			return map.get(c);
		}

		public boolean hasNext(Character c) {
			return this.get(c) != null;
		}

		public void putFail(Character c, Node n) {
			failFunction.put(c, n);
		}

		public Node getFail(Character c) {
			return failFunction.get(c);
		}

		public boolean hasNextFail(Character c) {
			return failFunction.get(c)!= null;
		}
	}

}
