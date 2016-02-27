package nlp.utils;

import algebra.src.Vector;

public class LowBowPrinter {

    public static String printMatlabVector(Vector[] curve) {
        StringBuilder acc = new StringBuilder(curve.length * curve[0].getDim());
        acc.append("[\t");
        for (int i = 0; i < curve.length; i++) {
            for (int j = 1; j <= curve[0].getDim(); j++) {
                acc.append(curve[i].getX(j) + ";\t");
            }
            acc.append(";\t");
        }
        acc.append("]\n");
        return acc.toString();
    }

    public static String printMatlabMatrix(Vector[] curve) {
        StringBuilder acc = new StringBuilder(curve.length * curve[0].getDim());
        acc.append("[\t");
        for (int i = 0; i < curve.length; i++) {
            for (int j = 1; j <= curve[0].getDim(); j++) {
                acc.append(curve[i].getX(j) + ",\t");
            }
            acc.append(";\t");
        }
        acc.append("]\n");
        return acc.toString();
    }
}
