package nlp.utils;

import algebra.Vector;
import nlp.lowbow.simpleLowBow.LowBow;

import java.util.function.Function;

public class LowBowPrinter implements Function<LowBow, String> {

    @Override
    public String apply(LowBow lowBow) {
        Vector[] curve = lowBow.getCurve();
        StringBuilder acc = new StringBuilder(curve.length * curve[0].getDim());
        for (int i = 0; i < curve.length; i++) {
            for (int j = 1; j <= curve[0].getDim(); j++) {
                acc.append(curve[i].getX(j) + (j == curve[0].getDim() ? "\n" : ","));
            }
        }
        return acc.toString();
    }
}
