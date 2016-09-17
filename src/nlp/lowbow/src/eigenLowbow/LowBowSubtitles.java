package nlp.lowbow.src.eigenLowbow;


import nlp.textSplitter.SubsSplitter;

public class LowBowSubtitles extends EigenLowBow {
    private String videoAddress;
    private int seasonNumber;
    private int episodeNumber;

    public LowBowSubtitles(String in) {
        super(in, new SubsSplitter());
    }
}
