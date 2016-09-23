package nlp.lowbow.src.simpleLowBow;

import algebra.src.Vec3;
import nlp.simpleDocModel.BaseDocModelManager;

/**
 * @author pedro
 */
public class BaseLowBowManager<L extends BaseLowBow> extends BaseDocModelManager<L> {
    /*
     * x - coordinate is the min text length
     * y - coordinate is the average text length
     * z - coordinate is the max text length
     */
    protected Vec3 textLengthStats = new Vec3(Double.MAX_VALUE, 0, 0);

    public BaseLowBowManager() {
        super();
    }
    /**
     * @param l lowbow curve
     */
    public void add(L l) {
        super.add(l);
        //compute min
        textLengthStats.setX(1, Math.min(l.getTextLength(), textLengthStats.getX(1)));
        //compute avg
        double lastAvg = textLengthStats.getX(2);
        textLengthStats.setX(2, lastAvg + (l.getTextLength() - lastAvg) / getDocModels().size());
        //compute max
        textLengthStats.setX(3, Math.max(l.getTextLength(), textLengthStats.getX(3)));
    }

    public double getMinTextLength() {
        return textLengthStats.getX();
    }

    public double getAverageTextLength() {
        return textLengthStats.getY();
    }

    public double getMaxTextLength() {
        return textLengthStats.getZ();
    }
}
