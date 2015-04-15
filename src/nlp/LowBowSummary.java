package nlp;

import javax.management.RuntimeErrorException;

import tools.simple.TextFrame;
import nlp.textSplitter.TextSplitter;
import numeric.MyMath;

public class LowBowSummary extends LowBow {

	public LowBowSummary(String in, TextSplitter textSplitter) {
		super(in, textSplitter);
	}
	
	public String getSummary(double lambda) {
		if(!isInitialized)
			throw new RuntimeErrorException(null, "LowBow not initialized");
		this.heatFlow(lambda);
		this.curve2Heat();
		int n = this.getTextLength();
		double s = this.getSamplesPerTextLength();
		int samples = this.getSamples();
		String acm = "";
		for (int i = 0; i < n; i++) {
			int k = (int) Math.floor(MyMath.clamp(s * i, 0, samples));
			acm += this.generateText(k) + "\n";
		}
		return acm;
	}
	
	public static void main(String[] args) {
		LowBowSummary low = new LowBowSummary("a b b b c c a a b", new TextSplitter() {

			@Override
			public String[] split(String in) {
				return in.split("\\s+");
			}
		});
		low.setSamplesPerTextLength(1.0);
		low.setSigma(0.005);
		low.setSmoothingCoeff(0.01);
		low.init();
		TextFrame frame = new TextFrame("Generated Text", low.getSummary(0.25));
	}

}
