package nlp.lowbow.simpleLowBow;

import algebra.Vector;
import numeric.Pca;

import java.util.ArrayList;

public class LowBowManagerPca extends LowBowManager<LowBowPca> {

    /**
     * build pca of all curves
     */
    public void buildPca() {
        ArrayList<LowBowPca> lowbows = getDocModels();
        int size = 0;
        for (LowBowPca lowbow : lowbows) {
            size += lowbow.getSamples();
        }
        Vector[] data = new Vector[size];

        int n = lowbows.size();

        int index = 0;
        for (int i = 0; i < n; i++) {
            Vector[] curve = lowbows.get(i).getCurve();
            for (int j = 0; j < curve.length; j++) {
                data[index] = curve[j];
                index++;
            }
        }
        Pca pca = new Pca();
        Vector[] pc = pca.getNPca(data, 3);
        Vector myu = pca.getAverage();
        for (LowBowPca lowbow : lowbows) {
            lowbow.buildPca(pc, myu);
        }
    }
}
