package nlp;

import algebra.Vector;
import nlp.lowbow.eigenLowbow.LowBowSegmentator;
import nlp.utils.RemoveWordsPredicate;
import numeric.Distance;

import java.lang.reflect.Field;
import java.util.Optional;

class TestParameters {
    String seriesAddress;
    String fileExtension;
    String output;
    int numberOfCluster;
    double heat;
    double entropy;
    int knn;
    double timeArc;
    boolean cutVideo;
    boolean concatVideo;
    LowBowSegmentator lowBowSegmentator;
    RemoveWordsPredicate necessaryWordPredicate;
    Distance<Vector> distance;

    public TestParameters(int numberOfCluster, double heat, double entropy, int knn, double timeArc, boolean cutVideo, boolean concatVideo, LowBowSegmentator lowBowSegmentator, RemoveWordsPredicate necessaryWordPredicate, Distance<Vector> distance, String seriesAddress, String fileExtension) {
        this.numberOfCluster = numberOfCluster;
        this.heat = heat;
        this.entropy = entropy;
        this.knn = knn;
        this.timeArc = timeArc;
        this.cutVideo = cutVideo;
        this.concatVideo = concatVideo;
        this.lowBowSegmentator = lowBowSegmentator;
        this.necessaryWordPredicate = necessaryWordPredicate;
        this.seriesAddress = seriesAddress;
        this.fileExtension = fileExtension;
        this.output = seriesAddress + "summary";
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "TestParameters{" +
                "seriesAddress='" + seriesAddress + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", output='" + output + '\'' +
                ", numberOfCluster=" + numberOfCluster +
                ", heat=" + heat +
                ", entropy=" + entropy +
                ", knn=" + knn +
                ", timeArc=" + timeArc +
                ", cutVideo=" + cutVideo +
                ", concatVideo=" + concatVideo +
                ", lowBowSegmentator=" + lowBowSegmentator +
                ", necessaryWordPredicate=" + necessaryWordPredicate +
                '}';
    }

    static class TestParametersBuilder {
        private String seriesAddress;
        private String fileExtension;
        private String output = seriesAddress + "summary";
        private Integer numberOfCluster;
        private Double heat;
        private Double entropy;
        private Integer knn;
        private Double timeArc;
        private Boolean cutVideo;
        private Boolean concatVideo;
        private LowBowSegmentator lowBowSegmentator;
        private RemoveWordsPredicate necessaryWordPredicate;
        private Distance<Vector> distance;

        public TestParametersBuilder() {
            // blank constructor
        }

        public TestParametersBuilder numberOfCluster(int numberOfCluster) {
            this.numberOfCluster = numberOfCluster;
            return this;
        }

        public TestParametersBuilder heat(double heat) {
            this.heat = heat;
            return this;
        }

        public TestParametersBuilder entropy(double entropy) {
            this.entropy = entropy;
            return this;
        }

        public TestParametersBuilder knn(int knn) {
            this.knn = knn;
            return this;
        }

        public TestParametersBuilder timeArc(double timeArc) {
            this.timeArc = timeArc;
            return this;
        }

        public TestParametersBuilder cutVideo(boolean cutVideo) {
            this.cutVideo = cutVideo;
            return this;
        }

        public TestParametersBuilder concatVideo(boolean concatVideo) {
            this.concatVideo = concatVideo;
            return this;
        }

        public TestParametersBuilder lowBowSegmentator(LowBowSegmentator lowBowSegmentator) {
            this.lowBowSegmentator = lowBowSegmentator;
            return this;
        }

        public TestParametersBuilder necessaryWordPredicate(RemoveWordsPredicate necessaryWordPredicate) {
            this.necessaryWordPredicate = necessaryWordPredicate;
            return this;
        }

        public TestParametersBuilder seriesAddress(String seriesAddress) {
            this.seriesAddress = seriesAddress;
            return this;
        }

        public TestParametersBuilder fileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
            return this;
        }

        public TestParametersBuilder distance(Distance<Vector> distance) {
            this.distance = distance;
            return this;
        }

        public Optional<TestParameters> build() {
            for (Field f : this.getClass().getFields()) {
                if (f == null) {
                    return Optional.empty();
                }
            }
            return Optional.of(new TestParameters(this.numberOfCluster, this.heat, this.entropy, this.knn, this.timeArc, this.cutVideo, this.concatVideo, this.lowBowSegmentator, this.necessaryWordPredicate, this.distance, this.seriesAddress, this.fileExtension));
        }
    }
}
