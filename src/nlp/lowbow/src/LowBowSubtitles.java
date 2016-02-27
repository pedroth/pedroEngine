package nlp.lowbow.src;


import nlp.textSplitter.TextSplitter;

public class LowBowSubtitles extends LowBow {
    public LowBowSubtitles(String in, TextSplitter textSplitter) {
        super(in, textSplitter);
    }

    public LowBowSubtitles(String in, TextSplitter textSplitter, Simplex simplex) {
        super(in, textSplitter, simplex);
    }
}
