package nlp.lowbow.src.simpleLowBow;

import algebra.src.Vec3;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author pedro
 */
public class BaseLowBowManager<L extends BaseLowBow> {
    protected ArrayList<L> lowbows;
    protected Simplex simplex;
    /*
     * x - coordinate is the min text length
     * y - coordinate is the average text length
     * z - coordinate is the max text length
     */
    protected Vec3 textLengthStats = new Vec3(Double.MAX_VALUE, 0, 0);

    public BaseLowBowManager() {
        lowbows = new ArrayList<>();
        simplex = new Simplex();
    }

    /**
     * @param l lowbow curve
     */
    public void add(L l) {
        lowbows.add(l);

        //compute min
        textLengthStats.setX(1, Math.min(l.getTextLength(), textLengthStats.getX(1)));
        //compute avg
        double lastAvg = textLengthStats.getX(2);
        textLengthStats.setX(2, lastAvg + (l.getTextLength() - lastAvg) / lowbows.size());
        //compute max
        textLengthStats.setX(3, Math.max(l.getTextLength(), textLengthStats.getX(3)));

        Set<String> keys = l.getSimplex().getKeySet();
        int accIndex = simplex.size();
        for (String s : keys) {
            Integer aux = simplex.get(s);
            if (aux == null) {
                accIndex++;
                simplex.put(s, accIndex);
            }
        }
        for (L lowbow : lowbows) {
            lowbow.setSimplex(simplex);
        }
    }


    public ArrayList<L> getLowbows() {
        return lowbows;
    }


    public void removeAll() {
        lowbows.removeAll(lowbows);
        simplex = new Simplex();
    }

    public Simplex getSimplex() {
        return simplex;
    }

    public Vec3 getTextLengthStats() {
        return textLengthStats;
    }

    /**
     * Initializes all curves with the same vocabulary
     */
    public void build() {
        int n = lowbows.size();
        for (int i = 0; i < n; i++) {
            lowbows.get(i).build();
        }
    }
}
