package nlp.lowbow.eigenLowbow;


import nlp.segmentedBow.sub.SegmentedBowFactory;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.textSplitter.SubsSplitter;
import utils.Interval;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SubtitleSegmentator implements LowBowSegmentator<LowBowSubtitles, SubSegmentedBow> {
    private static SubtitleSegmentator instance = new SubtitleSegmentator();

    private SubtitleSegmentator() {
        //black constructor prevents instance creation
    }

    public static SubtitleSegmentator getInstance() {
        return instance;
    }

    @Override
    public List<SubSegmentedBow> getSegmentation(SegmentedBowFactory<LowBowSubtitles, SubSegmentedBow> factory, LowBowSubtitles lowBow) {
        List<SubSegmentedBow> segmentedBows = new ArrayList<>();
        SubsSplitter textSplitter = lowBow.getTextSplitter();
        List<SubsSplitter.Subtitle> subtitles = textSplitter.getSubtitles();
        List<Integer> indexWords2Subtitle = textSplitter.getIndexWords2Subtitle();
        int minIndex = 1;
        for (int i = 0; i < indexWords2Subtitle.size() - 1; i++) {
            Integer subplus = indexWords2Subtitle.get(i + 1);
            Integer sub = indexWords2Subtitle.get(i);
            int dx = subplus - sub;
            long dt = subtitles.get(sub).getInterval().getXmax().until(subtitles.get(subplus).getInterval().getXmin(), ChronoUnit.SECONDS);
            if (dx > 0 && dt > 2) {
                segmentedBows.add(factory.getInstance(new Interval(minIndex, i), lowBow));
                minIndex = i + 1;
            }
        }
        int textLength = lowBow.getTextLength();
        if (minIndex != textLength) {
            segmentedBows.add(factory.getInstance(new Interval(minIndex, textLength), lowBow));
        }
        return segmentedBows;
    }
}
