package nlp.textSplitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class MyTextSplitter implements TextSplitter {
	public String[] split(String in) {
		/**
		 * ugly but it is the best way I found.
		 */
		return in.replaceAll("[^(\\p{L}|\\s+)]|\\(|\\)", " ").toLowerCase().replaceAll("[^(\\p{L}|\\s+)]|\\(|\\)", "").split("\\s+");
	};
	
	public static void main(String[] args) {
		BufferedReader br = null;
		String text = "";
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader("C:/Users/pedro/Desktop/Dummy.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				text+= sCurrentLine;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		TextSplitter spl = new CharacterSplitter();
		String[] splTxt = spl.split(text); 
		for (int i = 0; i < splTxt.length; i++) {
			System.out.println(splTxt[i]);
		}
	}
}
