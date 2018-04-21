package nlp.seriesSummary;


public abstract class SeriesSummarization {
    public static final String SUBTITLE_EXTENSION = "srt";
    private String seriesAddress;
    private String videoExtension;

    public SeriesSummarization(String seriesAddress, String videoExtension) {
        this.seriesAddress = seriesAddress;
        this.videoExtension = videoExtension;
    }

    public abstract void buildSummary(String outputAddress, double timeLengthMinutes);

    public String getSeriesAddress() {
        return seriesAddress;
    }

    public String getVideoExtension() {
        return videoExtension;
    }
}
