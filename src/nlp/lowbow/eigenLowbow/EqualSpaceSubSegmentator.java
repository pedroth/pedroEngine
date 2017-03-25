package nlp.lowbow.eigenLowbow;

import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import numeric.src.MyMath;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public class EqualSpaceSubSegmentator implements LowBowSubSegmentator {
    private double spacePercent = 0.1;

    @Override
    public List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory, LowBowSubtitles lowBowSubtitles) {
        List<BaseSegmentedBow> segmentedBows = new ArrayList<>();
        int size = lowBowSubtitles.getTextLength();
        double rho = 1 - spacePercent;
        int ite = (int) Math.floor(1.0 / rho);
        int step = (int) Math.floor(rho * size);
        step = step == 0 ? 1 : step;
        for (int i = 0; i < ite; i++) {
            int k = step * i;
            Interval interval = new Interval(k + 1, ((i == (ite - 1)) && (k + step != size)) ? size : k + step);
            segmentedBows.add(factory.getInstance(interval, lowBowSubtitles));
        }
        return segmentedBows;
    }

    public double getSpacePercent() {
        return spacePercent;
    }

    public void setSpacePercent(double spacePercent) {
        this.spacePercent = MyMath.clamp(spacePercent, 0.0, 1.0);
    }
}
