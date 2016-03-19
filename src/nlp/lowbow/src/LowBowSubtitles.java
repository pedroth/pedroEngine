package nlp.lowbow.src;


import nlp.textSplitter.SubsSplitter;

public class LowBowSubtitles extends LowBow {
    public LowBowSubtitles(String in) {
        super(in, new SubsSplitter());
    }

    public LowBowSubtitles(String in, Simplex simplex) {
        super(in, new SubsSplitter(), simplex);
    }
}
