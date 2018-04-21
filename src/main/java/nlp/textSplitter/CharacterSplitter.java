package nlp.textSplitter;

public class CharacterSplitter implements TextSplitter{

	@Override
	public String[] split(String in) {
		return in.toLowerCase().split("");
	}

}
