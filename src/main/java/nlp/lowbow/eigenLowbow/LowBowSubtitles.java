package nlp.lowbow.eigenLowbow;


import algebra.Matrix;
import algebra.Vector;
import nlp.textSplitter.SubsSplitter;

/**
 * The type Low bow subtitles.
 *
 * @param <T> the type parameter
 */
public class LowBowSubtitles<T extends SubsSplitter> extends EigenLowBow {
    private String videoAddress;

    /**
     * Instantiates a new Low bow subtitles.
     *
     * @param in               the in
     * @param subtitleSplitter the subtitle splitter
     * @param videoAddress     the video address
     */
    public LowBowSubtitles(String in, T subtitleSplitter, String videoAddress) {
        super(in, subtitleSplitter);
        this.videoAddress = videoAddress;
    }

    /**
     * Instantiates a new Low bow subtitles.
     *
     * @param in               the in
     * @param subtitleSplitter the subtitle splitter
     */
    public LowBowSubtitles(String in, T subtitleSplitter) {
        super(in, subtitleSplitter);
    }


    /**
     * Gets video address.
     *
     * @return the video address
     */
    public String getVideoAddress() {
        return videoAddress;
    }

    @Override
    public T getTextSplitter() {
        return (T) super.getTextSplitter();
    }


    /**
     * Gets heat energy.
     *
     * @return the heat energy
     */
    public double getHeatEnergy() {
        double heatTime = getHeatTime();
        int numberOfLowDimCoeff = getNumberOfLowDimCoeff();

        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        Matrix prod = diag.prod(getEigenCoord());
        Vector[] rowsVectors = prod.getRowsVectors();
        double acc = 0;
        for (int i = 0; i < rowsVectors.length; i++) {
            acc += rowsVectors[i].squareNorm();
        }
        return acc;
    }
}
