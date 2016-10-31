package nlp.tests;

import algebra.src.Vec2;
import algebra.src.Vector;
import inputOutput.TextIO;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.seriesSummary.ArcSummarizer;
import nlp.seriesSummary.ArcSummarizerLda;
import nlp.seriesSummary.BaseArcSummarizer;
import numeric.src.Distance;
import org.junit.Test;

import java.util.*;

public class ArcSummaryTest {

    @Test
    public void lowbowLdaStatistics() {
        String seriesAddress = "C:/pedro/escolas/ist/Tese/Series/BattleStarGalactica/";
        String fileExtension = "avi";
        String output = seriesAddress + "summary";
        int numberOfCluster = 8;
        double heat = 0.04;
        double entropy = 0.105;
        int knn = 10;
        double timeArc = 10;
        boolean cutVideo = true;

        List<BaseArcSummarizer> baseArcSummarizerList = new ArrayList<>();

        BaseArcSummarizer baseArcSummarizer = new ArcSummarizer(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.simplexDist);
        baseArcSummarizer.setCutVideo(cutVideo);
        ((ArcSummarizer) baseArcSummarizer).setNormalized(true);
        baseArcSummarizerList.add(baseArcSummarizer);

        baseArcSummarizer = new ArcSummarizerLda(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.simplexDist);
        baseArcSummarizer.setCutVideo(cutVideo);
        baseArcSummarizerList.add(baseArcSummarizer);

        baseArcSummarizer = new ArcSummarizer(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.simplexDist);
        ((ArcSummarizer) baseArcSummarizer).setNormalized(false);
        baseArcSummarizer.setCutVideo(cutVideo);
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

        TextIO textIO = new TextIO();
        textIO.read(output + 1 + "/ldaModel.phi");
        String[] phisSplit = textIO.getText().split("\n");
        Vector[] phis = new Vector[phisSplit.length];
        for (int i = 0; i < phisSplit.length; i++) {
            String[] split = phisSplit[i].split(" ");
            phis[i] = new Vector(split.length);
            for (int j = 0; j < split.length; j++) {
                phis[i].setX(j + 1, Double.valueOf(split[j]));
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < phis.length; i++) {
            int size = baseArcSummarizerList.size();
            int[] indexCorr = new int[size];
            double[] distanceCorr = new double[size];
            for (int j = 0; j < size; j++) {
                Vec2 ans = findMinDistance2Phi(phis[i], baseArcSummarizerList.get(j).getGraphCentroidByClusterId(), (x, y) -> {
                    double acc = 0;
                    for (int k = 0; k < x.size(); k++) {
                        acc += x.getX(k + 1) * Math.log(x.getX(k + 1) / y.getX(k + 1));
                    }
                    return acc;
                });
                indexCorr[j] = (int) ans.getX();
                distanceCorr[j] = ans.getY();
            }
            for (int j = 0; j < size; j++) {
                stringBuilder.append("Topic " + i + ": " + indexCorr[j] + ", " + distanceCorr[j] + " ");
            }
            stringBuilder.append("\n");
        }

        textIO.write(seriesAddress + "ldaTopicAssignment", stringBuilder.toString());


    }

    private Vec2 findMinDistance2Phi(Vector phi, Map<Integer, Vector> graphCentroidByClusterId, Distance<Vector> distance) {
        Set<Integer> keySet = graphCentroidByClusterId.keySet();
        Integer[] clusterIds = keySet.toArray(new Integer[keySet.size()]);
        int minIndex = -1;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < clusterIds.length; i++) {
            double norm = distance.dist(phi, graphCentroidByClusterId.get(clusterIds[i]));
            if (norm < minDist) {
                minDist = norm;
                minIndex = i;
            }
        }
        return new Vec2(minIndex, minDist);
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
