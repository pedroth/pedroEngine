package nlp.tests;

import algebra.src.Vector;
import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.LowBowSegmentator;
import nlp.lowbow.eigenLowbow.MaxDerivativeSegmentator;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.seriesSummary.ArcSummarizerDiffusion;
import nlp.seriesSummary.ArcSummarizerLda;
import nlp.seriesSummary.ArcSummarizerSpectral;
import nlp.seriesSummary.BaseArcSummarizer;
import nlp.utils.RemoveStopWordsPredicate;
import nlp.utils.RemoveWordsPredicate;
import numeric.src.Distance;
import org.junit.Test;
import utils.Histogram;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class ArcSummaryTest {

    @Test
    public void arcSummaryExperiment() throws IOException {
        final String baseVideoAddress = "C:/pedro/escolas/ist/Tese/Series/";
        RemoveWordsPredicate necessaryWordPredicate = new RemoveStopWordsPredicate();

        List<TestParameters> testParametersList = new ArrayList<>(4);
        final int numberOfCluster = 6;
        final double heat = 0.07;
        final double entropy = 0.0;
        final int knn = 1;
        final int timeArc = 10;
        final boolean cutVideo = true;
        final boolean concatVideo = true;
        final MaxDerivativeSegmentator instance = MaxDerivativeSegmentator.getInstance();
        final Distance<Vector> distance = BaseArcSummarizer.simplexDist;
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "OverTheGardenWall/")
                .fileExtension("mkv")
                .numberOfCluster(numberOfCluster)
                .heat(heat)
                .entropy(entropy)
                .knn(knn)
                .timeArc(timeArc)
                .cutVideo(cutVideo)
                .concatVideo(concatVideo)
                .lowBowSegmentator(instance)
                .necessaryWordPredicate(necessaryWordPredicate)
                .distance(distance)
                .build()
                .get());
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "BattleStarGalactica/")
                .fileExtension("avi")
                .numberOfCluster(numberOfCluster)
                .heat(heat)
                .entropy(entropy)
                .knn(knn)
                .timeArc(timeArc)
                .cutVideo(cutVideo)
                .concatVideo(concatVideo)
                .lowBowSegmentator(instance)
                .necessaryWordPredicate(necessaryWordPredicate)
                .distance(distance)
                .build()
                .get());
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "BreakingBad/")
                .fileExtension("mp4")
                .numberOfCluster(numberOfCluster)
                .heat(heat)
                .entropy(entropy)
                .knn(knn)
                .timeArc(timeArc)
                .cutVideo(cutVideo)
                .concatVideo(concatVideo)
                .lowBowSegmentator(instance)
                .necessaryWordPredicate(necessaryWordPredicate)
                .distance(distance)
                .build()
                .get());
        testParametersList.add(new TestParameters.TestParametersBuilder()
                .seriesAddress(baseVideoAddress + "MrRobot/")
                .fileExtension("mkv")
                .numberOfCluster(numberOfCluster)
                .heat(heat)
                .entropy(entropy)
                .knn(knn)
                .timeArc(timeArc)
                .cutVideo(cutVideo)
                .concatVideo(concatVideo)
                .lowBowSegmentator(instance)
                .necessaryWordPredicate(necessaryWordPredicate)
                .distance(distance)
                .build()
                .get());
        testParametersList.forEach(this::summaryBaseExp);
    }

    private void summaryBaseExp(TestParameters testParameters) {
        System.out.println(testParameters);
        Stack<BaseArcSummarizer> stack = new Stack<>();

        // spectral clustering andrew et al
        BaseArcSummarizer baseArcSummarizer = new ArcSummarizerSpectral(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, testParameters.distance);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.ANDREW_ET_AL);
        stack.add(baseArcSummarizer);

//        // spectral clustering Shi and Malik
        baseArcSummarizer = new ArcSummarizerSpectral(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, testParameters.distance);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.NORM);
        stack.add(baseArcSummarizer);

        //  Latent Dirichlet Allocation
        baseArcSummarizer = new ArcSummarizerLda(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, testParameters.distance);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        stack.add(baseArcSummarizer);

        // spectral clustering not normalized
        baseArcSummarizer = new ArcSummarizerSpectral(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, testParameters.distance);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.NNORM);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        stack.add(baseArcSummarizer);

        // diffusion clustering
        baseArcSummarizer = new ArcSummarizerDiffusion(testParameters.seriesAddress, testParameters.fileExtension, testParameters.heat, testParameters.entropy, testParameters.knn, testParameters.numberOfCluster, testParameters.distance);
        baseArcSummarizer.setNecessaryWordPredicate(testParameters.necessaryWordPredicate);
        ((ArcSummarizerDiffusion) baseArcSummarizer).setHeatTime(-1);
        ((ArcSummarizerDiffusion) baseArcSummarizer).setNormalized(false);
        baseArcSummarizer.setLowBowSegmentator(testParameters.lowBowSegmentator);
        baseArcSummarizer.setCutVideo(testParameters.cutVideo);
        baseArcSummarizer.setVideoConcat(testParameters.concatVideo);
        stack.add(baseArcSummarizer);

        // summary
        while (!stack.isEmpty()) try {
            BaseArcSummarizer baseArcSummarizer1 = stack.pop();
            System.out.println(baseArcSummarizer1.toString());
            String outputAddress = testParameters.output + Math.random();
            baseArcSummarizer1.buildSummary(outputAddress, testParameters.timeArc);
            TextIO textIO = new TextIO();
            String intraDistanceHist = computeIntraDistanceHist(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, ArcSummarizerSpectral.cosineDist, 30);
            textIO.write(outputAddress + "/IntraClusterDistanceHist.txt", intraDistanceHist);
            String interClusterDistanceHist = computeInterDistanceHistRandomSample(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, 3 * baseArcSummarizer1.getSegmentedBows().size(), ArcSummarizerSpectral.cosineDist, 30);
            textIO.write(outputAddress + "/InterClusterDistanceHist.txt", interClusterDistanceHist);
        } catch (IOException e) {
            e.printStackTrace();
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
        ((ArcSummarizerSpectral) baseArcSummarizer).setSpectralType(ArcSummarizerSpectral.SpectralTypeEnum.ANDREW_ET_AL);

        baseArcSummarizer.buildSummary(output, timeArc);
        baseArcSummarizer.getSegmentLengthData().forEach(System.out::println);

    }

    private String computeIntraDistanceHist(List<SubSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, Distance<Vector> distance, int bins) {
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
        Function<List<Double>, String> dataPrinterHist = x -> buildHist(x, bins);
        Function<List<Double>, String> dataPrinter = this::buildData;
        return dataPrinterHist.apply(data);
    }

    private String buildHist(List<Double> data, int bins) {
        Histogram histogram = new Histogram(data, bins);
        List<Integer> hist = histogram.getHistogram();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hist.size(); i++) {
            stringBuilder.append(hist.get(i) + "\n");
        }
        stringBuilder.append("===\n");
        stringBuilder.append("min\tmax\n");
        stringBuilder.append(histogram.getXmin() + "\t");
        stringBuilder.append(histogram.getXmax() + "\n");
        return stringBuilder.toString();
    }

    private String buildData(List<Double> data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Double point : data) {
            stringBuilder.append(point).append("\n");
        }
        return stringBuilder.toString();
    }


    private String computeInterDistanceHistRandomSample(List<SubSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, int samples, Distance<Vector> distance, int bins) {
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
        Function<List<Double>, String> dataPrinterHist = x -> buildHist(x, bins);
        Function<List<Double>, String> dataPrinter = this::buildData;
        return dataPrinterHist.apply(data);
    }
}
