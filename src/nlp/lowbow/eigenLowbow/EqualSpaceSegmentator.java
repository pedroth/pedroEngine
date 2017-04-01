package nlp.lowbow.eigenLowbow;

import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.sub.SegmentedBowFactory;
import numeric.src.MyMath;
import utils.Interval;

import java.util.ArrayList;
import java.util.List;

public class EqualSpaceSegmentator<L extends EigenLowBow, B extends BaseSegmentedBow<L>> implements LowBowSegmentator<L, B> {
    private double spacePercent = 0.1;

    @Override
    public List<B> getSegmentation(SegmentedBowFactory<L, B> factory, L lowBowSubtitles) {
        List<B> segmentedBows = new ArrayList<>();
        int size = lowBowSubtitles.getTextLength();
        double rho = 1 - spacePercent;
        int ite = (int) Math.floor(1.0 / rho);
        int step = (int) Math.floor(rho * size);
        step = step == 0 ? 1 : step;
        for (int i = 0; i < ite; i++) {
            int k = step * i;
            Interval interval = new Interval(k + 1, ((i == (ite - 1)) && (k + step != size)) ? size : k + step);
            segmentedBows.add((B) factory.getInstance(interval, lowBowSubtitles));
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
