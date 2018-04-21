package nlp.textSplitter;

public class SpaceSplitter implements TextSplitter{

	@Override
	public String[] split(String in) {
		return in.split("\\s+");
	}

}
