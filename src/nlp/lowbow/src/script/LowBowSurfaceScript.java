package nlp.lowbow.src.script;

import algebra.src.Vector;
import inputOutput.MyText;
import nlp.lowbow.src.LowBow;
import nlp.lowbow.src.symbolSampler.SymbolAtMax;
import nlp.textSplitter.MyTextSplitter;

public class LowBowSurfaceScript {
	
	public static void main(String[] args) {
		MyText text = new MyText();
		text.read("src/nlp/resources/texts/TextExample.txt");
		LowBow low = new LowBow(text.getText(), new MyTextSplitter());
		low.setSmoothingCoeff(0.03);
		low.build();
//		int samples = 20;
//		double lambda = 0.0;
//		HeatMethod sparse = new SparseHeatFlow();
//		String acc = "";
//		System.out.println(low);
//		for (int i = 0; i < samples; i++) {
//			acc+= "x(:," + (i+1) + ") = ";
//			low.heatFlow(lambda*lambda*lambda, sparse);
//			acc+= LowBowSurfaceScript.printMatlabVector(low.getCurve());
//			System.out.println(lambda*lambda*lambda);
//			lambda += (1.0 / (samples - 1)); 
//			acc += ";\n";
//		}
//		text.write("C:/Users/Pedroth/Desktop/XMatrix.txt", acc);
		text.read("C:/Users/Pedroth/Desktop/NMF3.txt");
		String[] aux = text.getText().split("\n");
		Vector[] curve = new Vector[aux.length];
		for (int i = 0; i < curve.length; i++) {
			String[] x = aux[i].split(",");
			curve[i] = new Vector(x.length);
			for (int j = 0; j < x.length; j++) {
				curve[i].setX(j+1,Double.parseDouble(x[j]));
			}
		}
		low.setCurve(curve);
		System.out.println(low.generateText(new SymbolAtMax()));
	}
}
