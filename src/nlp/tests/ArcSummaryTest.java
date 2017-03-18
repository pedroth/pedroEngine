package nlp.tests;

import algebra.src.Vector;
import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.LowBowSegmentator;
import nlp.lowbow.eigenLowbow.MaxDerivativeSegmentator;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.seriesSummary.ArcSummarizerDiffusion;
import nlp.seriesSummary.ArcSummarizerLda;
import nlp.seriesSummary.ArcSummarizerSpectral;
import nlp.seriesSummary.BaseArcSummarizer;
import nlp.utils.RemoveWordsPredicate;
import nlp.utils.StopWordPredicate;
import numeric.src.Distance;
import org.junit.Test;
import utils.Histogram;

import java.util.*;

public class ArcSummaryTest {

    @Test
    public void arcSummaryExperiment() {
        final String baseVideoAddress = "C:/pedro/escolas/ist/Tese/Series/";
        StopWordPredicate stopWordPredicate = StopWordPredicate.getInstance();
        RemoveWordsPredicate necessaryWordPredicate = new RemoveWordsPredicate() {
            @Override
            public String getNotNecessaryWordString() {
                return stopWordPredicate.getNotNecessaryWordString();
            }

            @Override
            public boolean test(String s) {
                return !stopWordPredicate.test(s);
            }
        };

        List<TestParameters> testParametersList = new ArrayList<>(4);
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "BattleStarGalactica/")
                .fileExtension("avi")
                .numberOfCluster(6)
                .heat(0.04)
                .entropy(0)
                .knn(5)
                .timeArc(10)
                .cutVideo(true)
                .concatVideo(true)
                .lowBowSegmentator(MaxDerivativeSegmentator.getInstance())
                .necessaryWordPredicate(necessaryWordPredicate)
                .build()
                .get());
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "BreakingBad/")
                .fileExtension("mp4")
                .numberOfCluster(6)
                .heat(0.04)
                .entropy(0)
                .knn(5)
                .timeArc(10)
                .cutVideo(true)
                .concatVideo(true)
                .lowBowSegmentator(MaxDerivativeSegmentator.getInstance())
                .necessaryWordPredicate(necessaryWordPredicate)
                .build()
                .get());
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "MrRobot/")
                .fileExtension("mkv")
                .numberOfCluster(6)
                .heat(0.04)
                .entropy(0)
                .knn(5)
                .timeArc(10)
                .cutVideo(true)
                .concatVideo(true)
                .lowBowSegmentator(MaxDerivativeSegmentator.getInstance())
                .necessaryWordPredicate(necessaryWordPredicate)
                .build()
                .get());
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "OverTheGardenWall/")
                .fileExtension("mkv")
                .numberOfCluster(6)
                .heat(0.04)
                .entropy(0)
                .knn(5)
                .timeArc(10)
                .cutVideo(true)
                .concatVideo(true)
                .lowBowSegmentator(MaxDerivativeSegmentator.getInstance())
                .necessaryWordPredicate(necessaryWordPredicate)
                .build()
                .get());
        testParametersList.forEach(this::summaryBaseExp);
    }

    private void summaryBaseExp(TestParameters testParameters) {
        Stack<BaseArcSummarizer> stack = new Stack<>();

        // spectral clustering andrew et al
        BaseArcSummarizer baseArcSummarizer = new ArcSummarizerSpectral(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, ArcSummarizerSpectral.simplexDist);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        ((ArcSummarizerSpectral) baseArcSummarizer).setNormalized(true);
        stack.add(baseArcSummarizer);

        // spectral clustering Shi and Malik
        baseArcSummarizer = new ArcSummarizerSpectral(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, ArcSummarizerSpectral.simplexDist);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        ((ArcSummarizerSpectral) baseArcSummarizer).setNormalized(true);
        ((ArcSummarizerSpectral) baseArcSummarizer).setAdrewEtAl(false);
        stack.add(baseArcSummarizer);

        //  Latent Dirichlet Allocation
        baseArcSummarizer = new ArcSummarizerLda(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, ArcSummarizerSpectral.simplexDist);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        stack.add(baseArcSummarizer);

        // spectral clustering not normalized
        baseArcSummarizer = new ArcSummarizerSpectral(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, ArcSummarizerSpectral.simplexDist);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        ((ArcSummarizerSpectral) baseArcSummarizer).setNormalized(false);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        stack.add(baseArcSummarizer);

        // diffusion clustering
        baseArcSummarizer = new ArcSummarizerDiffusion(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, ArcSummarizerSpectral.simplexDist);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        ((ArcSummarizerDiffusion) baseArcSummarizer).setHeatTime(50);
        ((ArcSummarizerDiffusion) baseArcSummarizer).setNormalized(false);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        stack.add(baseArcSummarizer);

        // summary
        int i = 0;
        while (!stack.isEmpty()) {
            BaseArcSummarizer baseArcSummarizer1 = stack.pop();
            System.out.println(baseArcSummarizer1.toString());
            String outputAddress = testParameters.output + Math.random();
            baseArcSummarizer1.buildSummary(outputAddress, testParameters.timeArc);
            TextIO textIO = new TextIO();
            String intraDistanceHist = computeIntraDistanceHist(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, ArcSummarizerSpectral.cosineDist, 30);
            textIO.write(outputAddress + "/IntraClusterDistanceHist.txt", intraDistanceHist);
            String interClusterDistanceHist = computeInterDistanceHistRandomSample(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, 3 * baseArcSummarizer1.getSegmentedBows().size(), ArcSummarizerSpectral.cosineDist, 30);
            textIO.write(outputAddress + "/InterClusterDistanceHist.txt", interClusterDistanceHist);
            i++;
        }
    }

    @Test
    public void lowbowTimeLengthStatistics() {
        String seriesAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/";
        String fileExtension = "mkv";
        String output = seriesAddress + "summary";
        int numberOfCluster = 6;
        double heat = 0.04;
        double entropy = 0.01;
        int knn = 5;
        double timeArc = 100000;
        boolean cutVideo = false;
        boolean concatVideo = true;
        LowBowSegmentator lowBowSegmentator = MaxDerivativeSegmentator.getInstance();

        BaseArcSummarizer baseArcSummarizer = new ArcSummarizerSpectral(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizerSpectral.euclideanDist);
        baseArcSummarizer.setCutVideo(cutVideo);
        baseArcSummarizer.setVideoConcat(concatVideo);
        baseArcSummarizer.setLowBowSegmentator(lowBowSegmentator);
        ((ArcSummarizerSpectral) baseArcSummarizer).setNormalized(true);

        baseArcSummarizer.buildSummary(output, timeArc);
        baseArcSummarizer.getSegmentLengthData().forEach(System.out::println);

    }

    private String computeIntraDistanceHist(List<BaseSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, Distance<Vector> distance, int bins) {
        List<Double> data = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> integerListEntry : segmentIndexByClusterId.entrySet()) {
            List<Integer> valueList = integerListEntry.getValue();
            int n = valueList.size();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Vector segmentBow1 = segmentedBows.get(isNormalIndex ? valueList.get(i) : valueList.get(i) - 1).getSegmentBow();
                    Vector segmentBow2 = segmentedBows.get(isNormalIndex ? valueList.get(j) : valueList.get(j) - 1).getSegmentBow();
                    data.add(distance.dist(segmentBow1, segmentBow2));
                }
            }
        }
        return buildHist(data, bins);
    }

    private String buildHist(List<Double> data, int bins) {
        Histogram histogram = new Histogram(data, bins);
        List<Integer> hist = histogram.getHistogram();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hist.size(); i++) {
            stringBuilder.append(hist.get(i) + "\n");
        }
        stringBuilder.append("min:" + histogram.getXmin() + "\n");
        stringBuilder.append("max:" + histogram.getXmax() + "\n");
        return stringBuilder.toString();
    }


    private String computeInterDistanceHistRandomSample(List<BaseSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, int samples, Distance<Vector> distance, int bins) {
        List<Double> data = new ArrayList<>();
        Random random = new Random();
        int n = segmentIndexByClusterId.size();
        for (int i = 0; i < samples; i++) {
            int clusterId1 = random.nextInt(n);
            int clusterId2 = (clusterId1 + random.nextInt(n - 1) + 1) % n;
            List<Integer> cluster1 = segmentIndexByClusterId.get(clusterId1);
            int n1 = cluster1.size();
            List<Integer> cluster2 = segmentIndexByClusterId.get(clusterId2);
            int n2 = cluster2.size();
            int i1 = random.nextInt(n1);
            int i2 = random.nextInt(n2);
            Vector segmentBow = segmentedBows.get(isNormalIndex ? cluster1.get(i1) : cluster1.get(i1) - 1).getSegmentBow();
            Vector segmentBow1 = segmentedBows.get(isNormalIndex ? cluster2.get(i2) : cluster2.get(i2) - 1).getSegmentBow();
            Double aDouble = distance.dist(segmentBow, segmentBow1);
            data.add(aDouble);
        }
        return buildHist(data, bins);
    }
}
