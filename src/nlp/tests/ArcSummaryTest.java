package nlp.tests;

import algebra.src.Vec2;
import algebra.src.Vector;
import inputOutput.TextIO;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.seriesSummary.ArcSummarizer;
import nlp.seriesSummary.ArcSummarizerLda;
import nlp.seriesSummary.BaseArcSummarizer;
import nlp.symbolSampler.TopKSymbol;
import nlp.utils.Simplex;
import numeric.src.Distance;
import numeric.src.MyMath;
import numeric.src.MySet;
import org.junit.Test;

import java.util.*;

public class ArcSummaryTest {

    @Test
    public void lowbowLdaStatistics() {
        String seriesAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/";
        String fileExtension = "mkv";
        String output = seriesAddress + "summary";
        int numberOfCluster = 5;
        double heat = 0.04;
        double entropy = 0.10;
        int knn = 5;
        double timeArc = 10;
        boolean cutVideo = true;

        List<BaseArcSummarizer> baseArcSummarizerList = new ArrayList<>();

        BaseArcSummarizer baseArcSummarizer = new ArcSummarizer(seriesAddress, fileExtension, heat, entropy, knn, numberOfCluster, ArcSummarizer.euclideanDist);
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
            String intraDistanceHist = computeIntraDistanceHist(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, ArcSummarizer.cosineDist, 30);
            textIO.write(outputAddress + "/IntraClusterDistanceHist.txt", intraDistanceHist);
            String interClusterDistanceHist = computeInterDistanceHistRandomSample(baseArcSummarizer1.getSegmentedBows(), baseArcSummarizer1.getSegmentIndexByClusterId(), false, 3 * baseArcSummarizer1.getSegmentedBows().size(), ArcSummarizer.cosineDist, 30);
            textIO.write(outputAddress + "/InterClusterDistanceHist.txt", interClusterDistanceHist);
        }


        Map<Integer, Vector> phis = baseArcSummarizerList.get(1).getGraphCentroidByClusterId();
//
        StringBuilder stringBuilder = new StringBuilder();
//        for (int i = 0; i < phis.size(); i++) {
//            int size = baseArcSummarizerList.size();
//            int[] indexCorr = new int[size];
//            double[] distanceCorr = new double[size];
//            for (int j = 0; j < size; j++) {
//                Vec2 ans = findMaxJaccardIndex2Phi(phis.get(i), baseArcSummarizerList.get(j).getGraphCentroidByClusterId(), baseArcSummarizer.getLowBowManager().getSimplex());
//                indexCorr[j] = (int) ans.getX();
//                distanceCorr[j] = ans.getY();
//            }
//            for (int j = 0; j < size; j++) {
//                stringBuilder.append("Topic " + i + ": " + indexCorr[j] + ", " + distanceCorr[j] + " ");
//            }
//            stringBuilder.append("\n");
//        }
//
//        TextIO textIO = new TextIO();
//        textIO.write(seriesAddress + "ldaTopicAssignment", stringBuilder.toString());


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

    private Vec2 findMaxJaccardIndex2Phi(Vector phi, Map<Integer, Vector> graphCentroidByClusterId, Simplex simplex) {
        TopKSymbol topKSymbol = new TopKSymbol(20);
        MySet phiTopKWords = new MySet(topKSymbol.nextSymbol(phi, simplex).split(" "));
        Set<Integer> keySet = graphCentroidByClusterId.keySet();
        Integer[] clusterIds = keySet.toArray(new Integer[keySet.size()]);
        int maxIndex = -1;
        double maxValueDist = Double.MIN_VALUE;
        for (int i = 0; i < clusterIds.length; i++) {
            double jaccard = MySet.jaccardIndex(phiTopKWords, new MySet(topKSymbol.nextSymbol(graphCentroidByClusterId.get(clusterIds[i]), simplex).split(" ")));
            if (maxValueDist < jaccard) {
                maxValueDist = jaccard;
                maxIndex = i;
            }
        }
        return new Vec2(maxIndex, maxValueDist);
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
        double[] hist = new double[bins];
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        for (Double x : data) {
            xmin = Math.min(xmin, x);
            xmax = Math.max(xmax, x);
        }

        double h = (xmax - xmin) / bins;

        for (Double x : data) {
            double z = (x - xmin) / h;
            //because float point errors
            z = MyMath.clamp(z, 0, 30);
            int index = -1;
            // try floor function as index
            for (int i = 0; i < bins; i++) {
                if (i <= z && z <= i + 1) {
                    index = i;
                    break;
                }
            }
            hist[index]++;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hist.length; i++) {
            stringBuilder.append(hist[i] + "\n");
        }
        return stringBuilder.toString();
    }


    private String computeInterDistanceHistRandomSample(List<BaseSegmentedBow> segmentedBows, Map<Integer, List<Integer>> segmentIndexByClusterId, boolean isNormalIndex, int samples, Distance<Vector> distance, int bins) {
        List<Double> data = new ArrayList<>();
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
            data.add(aDouble);
        }
        return buildHist(data, bins);
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
