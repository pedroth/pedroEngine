package nlp.lowbow.src.simpleLowBow;

import algebra.src.Vector;

import java.util.ArrayList;

public class LowBowManager<L extends LowBow> extends BaseLowBowManager<L> {
    /**
     * resamples each curve with the maximum samples of the curves in the collection
     */
    public void maxSamplesInit() {
        ArrayList<L> lowbows = getDocModels();
        int n = lowbows.size();
        double maxSamples = 0;
        for (int i = 0; i < n; i++) {
            LowBow l = lowbows.get(i);
            double samples = l.samplesPerTextLength * l.getTextLength();
            if (samples > maxSamples) {
                maxSamples = samples;
            }
        }
        for (int i = 0; i < n; i++) {
            LowBow low = lowbows.get(i);
            low.setSamplesPerTextLength(maxSamples / low.getTextLength());
            low.build();
        }
    }


    /**
     * @param i curve at index i
     * @param j curve at index j
     * @return euclidean distance between curves i and j
     */
    public double getDistance(int i, int j) {
        ArrayList<L> lowbows = getDocModels();
        LowBow l1 = lowbows.get(i);
        LowBow l2 = lowbows.get(j);
        int l1TextLength = l1.getTextLength();
        int l2TextLength = l2.getTextLength();
        double maxSamples = Math.max(l1.samplesPerTextLength * l1TextLength, l2.samplesPerTextLength * l2TextLength);
        if (l1.samplesPerTextLength * l1TextLength >= l2.samplesPerTextLength * l2TextLength) {
            l2.resample(maxSamples / l2TextLength, l2.getSigma());
        } else {
            l1.resample(maxSamples / l1TextLength, l1.getSigma());
        }
        /**
         * trapezoidal method
         */
        double acm = 0;
        double h = 1.0 / (l1.samples - 1);
        for (int k = 0; k < l1.samples - 1; k++) {
            acm += Vector.diff(l1.curve[k], l2.curve[k]).norm();
            acm += Vector.diff(l1.curve[k + 1], l2.curve[k + 1]).norm();
        }
        return acm * 0.5 * h;
    }
}
