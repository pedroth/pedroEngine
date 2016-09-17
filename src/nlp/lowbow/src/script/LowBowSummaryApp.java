package nlp.lowbow.src.script;

import inputOutput.MyText;
import nlp.lowbow.src.simpleLowBow.HeatMethod;
import nlp.lowbow.src.simpleLowBow.LowBow;
import nlp.lowbow.src.simpleLowBow.LowBowSummaryPrepositions;
import nlp.lowbow.src.simpleLowBow.SparseHeatFlow;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.textSplitter.MyTextSplitter;
import nlp.textSplitter.SpaceSplitter;
import nlp.textSplitter.StopWordsSplitter;

/**
 * 
 * @author pedro
 * 
 * args 
 * 
 * 0 : Text in [String][Address]
 * 1 : Text out [String][Address]
 * 2 : SamplesPerTextLength [positive real number]
 * 3 : Sigma [positive real number][kernel scale]
 * 4 : additive smoothing parameter [positive real number]
 * 5 : Lambda [[0-1] real number][heat parameter]
 * 6 : type of summary [normal, stopWords, prepositional, symbolic]
 * 	normal : separates text in words, and these words are the vocabulary
 *  stopWords : separates text in words, and removes stop words.
 *  prepositional : separates text in words, removes stop words and build a summary with the help of the last preposition seen before that word.
 *  symbolic : words are everything that is between spaces. example : a c c g g t -> [a,c,g,t]
 *  
 */
public class LowBowSummaryApp {
	
	public static void main(String[] args) {
		
		String addressIn = "", addressOut = "";
		double samplesPerTextLength = 1.0, sigma = 0.01, addictiveSmoothCoeff = 0.01, lambda = 0.5;
		String type = "normal";
		
		boolean error = false;
		
		if(args.length < 8) {
			System.out.println("There are missing arguments");
			error = true; 
		}
		if(args.length > 7) {
			System.out.println("Too many arguments");
			error = true; 
		}
		
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				addressIn = args[i];
				break;
			case 1:
				addressOut = args[i];
				break;
			case 2:
				samplesPerTextLength = Double.parseDouble(args[i]);
				break;
			case 3:
				sigma = Double.parseDouble(args[i]);
				break;
			case 4:
				addictiveSmoothCoeff = Double.parseDouble(args[i]);
				break;
			case 5:
				lambda = Double.parseDouble(args[i]);
				break;
			case 6:
				String saux = args[i];
				saux.toLowerCase();
				if(saux.equals("normal") || saux.equals("stopwords") || saux.equals("prepositional")|| saux.equals("symbolic")) {
					type = args[i];
				} else {
					System.out.println("you must insert a valid type");
					error = true; 
				}
				break;
			default:
				System.out.println("Too many arguments,");
				error = true; 
				break;
			}
		}
		if(error) {
			System.exit(1);
		}
		/**
		 * Summary generation
		 */
		try {
			LowBow l;
			MyText text = new MyText();
			text.read(addressIn);
			type.toLowerCase();
			if(type.equals("normal")) {
				l = new LowBow(text.getText(), new MyTextSplitter());
			} else if(type.equals("stopwords")) {
				l = new LowBow(text.getText(), new StopWordsSplitter("src/nlp/resources/wordLists/stopWords.txt"));
			} else if(type.equals("prepositional")) {
				l = new LowBowSummaryPrepositions(text.getText());
			} else {
				l = new LowBow(text.getText(), new SpaceSplitter());
			}
			l.setSamplesPerTextLength(samplesPerTextLength);
			l.setSigma(sigma);
			l.setSmoothingCoeff(addictiveSmoothCoeff);
			l.build();
			System.out.println("computing summary");
			HeatMethod heat = new SparseHeatFlow();
			l.heatFlow(lambda, heat);
			text.write(addressOut, l.generateText(new SymbolAtMax()));
		} catch(RuntimeException e) {
			
		}
	}
}
