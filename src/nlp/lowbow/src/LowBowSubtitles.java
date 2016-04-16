package nlp.lowbow.src;


import nlp.textSplitter.SubsSplitter;

public class LowBowSubtitles extends LowBow {
    public LowBowSubtitles(String in) {
        super(in, new SubsSplitter());
    }
}
