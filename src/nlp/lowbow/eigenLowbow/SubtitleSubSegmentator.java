package nlp.lowbow.eigenLowbow;


import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import nlp.textSplitter.SubsSplitter;
import utils.Interval;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SubtitleSubSegmentator implements LowBowSubSegmentator {
    private static SubtitleSubSegmentator instance = new SubtitleSubSegmentator();

    private SubtitleSubSegmentator() {
        //black constructor prevents instance creation
    }

    public static SubtitleSubSegmentator getInstance() {
        return instance;
    }

    @Override
    public List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory, LowBowSubtitles lowBowSubtitles) {
        List<BaseSegmentedBow> segmentedBows = new ArrayList<>();
        SubsSplitter textSplitter = lowBowSubtitles.getTextSplitter();
        List<SubsSplitter.Subtitle> subtitles = textSplitter.getSubtitles();
        List<Integer> indexWords2Subtitle = textSplitter.getIndexWords2Subtitle();
        int minIndex = 1;
        for (int i = 0; i < indexWords2Subtitle.size() - 1; i++) {
            Integer subplus = indexWords2Subtitle.get(i + 1);
            Integer sub = indexWords2Subtitle.get(i);
            int dx = subplus - sub;
            long dt = subtitles.get(sub).getInterval().getXmax().until(subtitles.get(subplus).getInterval().getXmin(), ChronoUnit.SECONDS);
            if (dx > 0 && dt > 2) {
                segmentedBows.add(factory.getInstance(new Interval(minIndex, i), lowBowSubtitles));
                minIndex = i + 1;
            }
        }
        int textLength = lowBowSubtitles.getTextLength();
        if (minIndex != textLength) {
            segmentedBows.add(factory.getInstance(new Interval(minIndex, textLength), lowBowSubtitles));
        }
        return segmentedBows;
    }
}
