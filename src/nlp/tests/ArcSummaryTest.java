package nlp.tests;

import algebra.src.Vector;
import inputOutput.TextIO;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.seriesSummary.ArcSummarizer;
import nlp.seriesSummary.ArcSummarizerLda;
import nlp.seriesSummary.BaseArcSummarizer;
import numeric.src.Distance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ArcSummaryTest {

    @Test
    public void lowbowLdaStatistics() {
        String seriesAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/";
        String fileExtension = "mkv";
        String output = seriesAddress + "summary";
        int numberOfCluster = 6;
        double heat = 0.04;
        double entropy = 0.105;
        int knn = 10;
        double timeArc = 10;

        List<BaseArcSummarizer> baseArcSummarizerList = new ArrayList<>();

        BaseArcSummarizer baseArcSummarizer = new ArcSummarizer(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.simplexDist);
        baseArcSummarizer.setCutVideo(false);
        ((ArcSummarizer) baseArcSummarizer).setNormalized(true);
        baseArcSummarizerList.add(baseArcSummarizer);

        baseArcSummarizer = new ArcSummarizerLda(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.simplexDist);
        baseArcSummarizer.setCutVideo(false);
        baseArcSummarizerList.add(baseArcSummarizer);

        baseArcSummarizer = new ArcSummarizer(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.simplexDist);
        ((ArcSummarizer) baseArcSummarizer).setNormalized(false);
        baseArcSummarizer.setCutVideo(false);
        baseArcSummarizerList.add(baseArcSummarizer);

        // summary
        for (int i = 0; i < baseArcSummarizerList.size(); i++) {
            BaseArcSummarizer baseArcSummarizer1 = baseArcSummarizerList.get(i);
            String outputAddress = output + i;
            baseArcSummarizer1.buildSummary(outputAddress, timeArc);
            TextIO textIO = new TextIO();
            String intraDistanceHist = computeIntraDistanceHist(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, ArcSummarizer.cosineDist);
            textIO.write(outputAddress + "/IntraClusterDistance.txt", intraDistanceHist);
            String interClusterDistanceHist = computeInterDistanceHistRandomSample(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, 2 * baseArcSummarizer1.getSegmentedBows().size(), ArcSummarizer.cosineDist);
            textIO.write(outputAddress + "/InterClusterDistance.txt", interClusterDistanceHist);
        }


    }

    private String computeIntraDistanceHist(List<BaseSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, Distance<Vector> distance) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, List<Integer>> integerListEntry : segmentIndexByClusterId.entrySet()) {
            List<Integer> valueList = integerListEntry.getValue();
            int n = valueList.size();
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Vector segmentBow1 = segmentedBows.get(isNormalIndex ? valueList.get(i) : valueList.get(i) - 1).getSegmentBow();
                    Vector segmentBow2 = segmentedBows.get(isNormalIndex ? valueList.get(j) : valueList.get(j) - 1).getSegmentBow();
                    stringBuilder.append(Double.valueOf(distance.dist(segmentBow1, segmentBow2)) + "\n");
                }
            }
        }
        return stringBuilder.toString();
    }

    private String computeInterDistanceHistRandomSample(List<BaseSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, int samples, Distance<Vector> distance) {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        int n = segmentIndexByClusterId.size();
        for (int i = 0; i < samples; i++) {
            int clusterId1 = random.nextInt(n);
            int clusterId2 = (clusterId1 + random.nextInt(n - 1)) % n;
            List<Integer> cluster1 = segmentIndexByClusterId.get(clusterId1);
            int n1 = cluster1.size();
            List<Integer> cluster2 = segmentIndexByClusterId.get(clusterId2);
            int n2 = cluster2.size();
            int i1 = random.nextInt(n1);
            int i2 = random.nextInt(n2);
            Vector segmentBow = segmentedBows.get(isNormalIndex ? cluster1.get(i1) : cluster1.get(i1) - 1).getSegmentBow();
            Vector segmentBow1 = segmentedBows.get(isNormalIndex ? cluster2.get(i2) : cluster2.get(i2) - 1).getSegmentBow();
            Double aDouble = distance.dist(segmentBow, segmentBow1);
            stringBuilder.append(aDouble + "\n");
        }
        return stringBuilder.toString();
    }

    private List<Vector> readMatrix(String address) {
        List<Vector> ans = new ArrayList<>();
        TextIO textIO = new TextIO();
        textIO.read(address);
        String[] split = textIO.getText().split("\n");
        for (int i = 0; i < split.length; i++) {
            String[] split1 = split[i].split(" ");
            double[] splitdouble = new double[split1.length];
            for (int j = 0; j < split1.length; j++) {
                splitdouble[j] = Double.valueOf(split1[j]);
            }
            ans.add(new Vector(splitdouble));
        }
        return ans;
    }

}
