package nlp.lowbow.eigenLowbow;


import algebra.src.LineGradient;
import algebra.src.Matrix;
import algebra.src.Vector;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowFactory;
import nlp.textSplitter.SubsSplitter;

import java.util.List;

/**
 * The type Low bow subtitles.
 *
 * @param <T>  the type parameter
 */
public class LowBowSubtitles<T extends SubsSplitter> extends EigenLowBow {
    private String videoAddress;
    private LowBowSegmentator lowBowSegmentator = new MaxDerivativeSegmentator();

    /**
     * Instantiates a new Low bow subtitles.
     *
     * @param in the in
     * @param subtitleSplitter the subtitle splitter
     * @param videoAddress the video address
     */
    public LowBowSubtitles(String in, T subtitleSplitter, String videoAddress) {
        super(in, subtitleSplitter);
        this.videoAddress = videoAddress;
    }

    /**
     * Instantiates a new Low bow subtitles.
     *
     * @param in the in
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
     * Gets segmentation.
     *
     * @param factory the factory
     * @return the segmentation
     */
    public List<BaseSegmentedBow> getSegmentation(SegmentedBowFactory<BaseSegmentedBow> factory) {
        return lowBowSegmentator.getSegmentation(factory, this);
    }

    /**
     * Compute low bow 2 nd derivative.
     *
     * @return the matrix
     */
    public Matrix computeLowBow2ndDerivative() {
        double heatTime = getHeatTime();
        int numberOfLowDimCoeff = getNumberOfLowDimCoeff();
        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        diag = Matrix.diag(getEigenValues().getSubVector(1, numberOfLowDimCoeff)).prod(diag);
        if (textLength > 50) {
            return getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prodParallel(diag.prod(getEigenCoord()));
        }
        return getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prod(diag.prod(getEigenCoord()));
    }

    /**
     * Compute low bow derivative.
     *
     * @return the matrix
     */
    public Matrix computeLowBowDerivative() {
        double heatTime = getHeatTime();
        LineGradient opD = new LineGradient(textLength);
        int numberOfLowDimCoeff = getNumberOfLowDimCoeff();
        Matrix diag = expSt(heatTime, numberOfLowDimCoeff);
        Matrix yt;
        if (textLength > 50) {
            yt = getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prodParallel(diag.prod(getEigenCoord()));
        } else {
            yt = getEigenBasis().getSubMatrix(1, textLength, 1, numberOfLowDimCoeff).prod(diag.prod(getEigenCoord()));
        }
        return opD.prod(yt);
    }

    /**
     * Gets low bow segmentator.
     *
     * @return the low bow segmentator
     */
    public LowBowSegmentator getLowBowSegmentator() {
        return lowBowSegmentator;
    }

    /**
     * Sets low bow segmentator.
     *
     * @param lowBowSegmentator the low bow segmentator
     */
    public void setLowBowSegmentator(LowBowSegmentator lowBowSegmentator) {
        this.lowBowSegmentator = lowBowSegmentator;
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
