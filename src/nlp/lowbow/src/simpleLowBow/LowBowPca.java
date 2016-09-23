package nlp.lowbow.src.simpleLowBow;


import algebra.src.Vec3;
import algebra.src.Vector;
import nlp.textSplitter.TextSplitter;
import nlp.utils.Simplex;
import numeric.src.Pca;

import javax.management.RuntimeErrorException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class LowBowPca extends LowBow {
    protected Vec3[] pcaCurve;

    public LowBowPca(String in, TextSplitter textSplitter) {
        super(in, textSplitter);
    }

    public LowBowPca(String in, TextSplitter textSplitter, Simplex simplex) {
        super(in, textSplitter, simplex);
    }


    public void buildPca() {
        if (!isBuild)
            throw new RuntimeErrorException(null, "LowBow not initialized");
        /**
         * pca
         */
        Pca pca = new Pca();
        Vector[] pc = pca.getNPca(curve, 3);
        Vector myu = pca.getAverage();
        pcaCurve = new Vec3[samples];
        for (int i = 0; i < samples; i++) {
            Vector v = Vector.diff(curve[i], myu);
            pcaCurve[i] = new Vec3(Vector.innerProd(pc[0], v), Vector.innerProd(pc[1], v), Vector.innerProd(pc[2], v));
        }
        // writeObjFile(false);
    }

    /**
     * @param pc  are the principal components
     * @param myu is the average point
     */
    public void buildPca(Vector[] pc, Vector myu) {
        if (!isBuild)
            throw new RuntimeErrorException(null, "LowBow not initialized");
        /**
         * pca
         */
        pcaCurve = new Vec3[samples];
        for (int i = 0; i < samples; i++) {
            Vector v = Vector.diff(curve[i], myu);
            pcaCurve[i] = new Vec3(Vector.innerProd(pc[0], v), Vector.innerProd(pc[1], v), Vector.innerProd(pc[2], v));
        }
        // writeObjFile(false);
    }

    /**
     * @param isPca if true writes the PCA curve, else writes down the first 3
     *              coordinates of the curve into a .obj file
     */
    @SuppressWarnings("unused")
    private void writeObjFile(boolean isPca) {
        try {
            File file = new File("Line.obj");

            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintStream bw = new PrintStream(file);

            for (int i = 0; i < curve.length; i++) {
                if (isPca)
                    bw.println("v " + pcaCurve[i].getX() + " " + pcaCurve[i].getY() + " " + pcaCurve[i].getZ());
                else
                    bw.println("v " + curve[i].getX(1) + " " + curve[i].getX(2) + " " + curve[i].getX(3));
            }
            for (int i = 0; i < curve.length - 1; i++) {
                bw.println("l " + (i + 1) + " " + (i + 2));
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vec3[] getPcaCurve() {
        return pcaCurve;
    }
}
