package tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
/**
 * 
 * @author pedro
 * 
 * little dirty the code
 *
 */
public class NumbersTokenizer extends SufixTreeTokenizer {

	public NumbersTokenizer(String[] patterns) {
		super(patterns);
	}

	
	
	@Override
	public String[] tokenize(String s) {
		Node state = super.root;
		int textSize = s.length();
		Vector<String> answer = new Vector<String>();
		for (int i = 0; i < textSize; i++) {
			Character c = s.charAt(i);
			/**
			 * tokens recognition
			 */
			if(state.hasNext(c)) {
				state = state.get(c);
			}else if(state.isFinalState()) {
				answer.add(state.getToken());
				i--;
				state = root;
			}else if(state.hasNextFail(c)) {
				state = state.getFail(c);
			}
			/**
			 * numbers recognition
			 */
			else if(Character.isDigit(c)) {
				int j = DoublesSM(s, i);
				answer.add(s.substring(i, i + j));
				i += j - 1;
				continue;
			}
		}
		if (state.isFinalState())
			answer.add(state.getToken());
		return answer.toArray(new String[0]);

	}

	int DoublesSM(String s, int i) {
		int state = 0;
		int size = s.length();
		int j;
		for (j = i; j < size; j++) {
			Character c = s.charAt(j);
			if (state == 0) {
				if (Character.isDigit(c)) {
					continue;
				} else if (c == '.') {
					state = 1;
					continue;
				} else {
					return j - i;
				}
			}
			if (state == 1) {
				if (Character.isDigit(c)) {
					continue;
				} else {
					return j - i;
				}
			}
		}
		return j - i;
	}

	public static void main(String args[]) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String[] s = { "x1" };
		NumbersTokenizer st = new NumbersTokenizer(s);
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
}
