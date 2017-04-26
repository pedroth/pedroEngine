package nlp.tests;

import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.*;
import nlp.segmentedBow.SegmentBow;
import nlp.segmentedBow.sub.SegmentedBowHeat;
import nlp.segmentedBow.sub.SubSegmentedBow;
import nlp.symbolSampler.TopKSymbol;
import nlp.textSplitter.StopWordsSplitter;
import nlp.textSplitter.SubsSplitter;
import nlp.textSplitter.TextSplitter;
import nlp.utils.RemoveStopWordsPredicate;
import numeric.src.MyMath;
import org.junit.Test;
import utils.FilesCrawler;
import utils.Interval;

import java.util.*;

public class SegmentationTest {


    @Test
    public void segmentTestVideo() {
        final String desktopAddress = System.getProperty("user.home") + "/Desktop/";
        final String baseAddress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/";
        final double heatTime = 0.04;

        List<String> subtitles = FilesCrawler.listFilesWithExtension(baseAddress, "srt");
        Collections.sort(subtitles);
        List<String> videos = FilesCrawler.listFilesWithExtension(baseAddress, "mkv");
        Collections.sort(videos);

        SubsSplitter textSplitter = null;
        TextIO textIO = new TextIO();
        SummaryGenLowBowManager<LowBowSubtitles, SubSegmentedBow> lowBowManager = new SummaryGenLowBowManager<>();

        //lowbow representation
        for (int i = 0; i < 1; i++) {
            textIO.read(subtitles.get(i));
            textSplitter = new SubsSplitter(RemoveStopWordsPredicate.getInstance());
            LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(textIO.getText(), textSplitter, videos.get(i));
            LowBowSegmentator lowBowSegmentator = MaxDerivativeSegmentator.getInstance();
            low.setLowBowSegmentator(lowBowSegmentator);
            lowBowManager.add(low);
        }
        lowBowManager.buildModel(heatTime);
        final LowBowSubtitles lowBowSubtitles = lowBowManager.getDocModels().get(0);
        final String text = lowBowSubtitles.generateText(new TopKSymbol(10, ","));

        lowBowManager.buildSegmentations(SegmentedBowHeat::new);
        List<SubSegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Set<Integer> boundaryIndex = new HashSet<>();
        for (SubSegmentedBow segmentedBow : segmentedBows) {
            segmentedBow.cutSegment(desktopAddress + segmentedBow.getInterval() + ".mp4");
            boundaryIndex.add(segmentedBow.getInterval().getXmax() - 1);
        }
        final String[] split = text.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        final List<String> subtitleWords = textSplitter.getSubtitleWords();
        for (int i = 0; i < split.length; i++) {
            stringBuilder.append(split[i] + "," + subtitleWords.get(i) + (boundaryIndex.contains(i) ? ",*\n" : "\n"));
        }
        textIO.write(desktopAddress + "segmentation.csv", stringBuilder.toString());
    }

    @Test
    public void segmentC99() {
        final String baseAddress = "C:/pedro/escolas/ist/Tese/C99/";
//        final String baseAddress = "C:/Users/Pedroth/Downloads/C99-1.2-release/C99-1.2-release/naacl00Exp/data/";
        final TextIO textIO = new TextIO();
        final TextSplitter textSplitter = x -> {
            final String[] split = new StopWordsSplitter().split(x);
            ArrayList<String> list = new ArrayList<>(split.length);
            for (String s : split) {
                if (s.length() > 2) {
                    list.add(s);
                }
            }
            return list.toArray(new String[list.size()]);
        };
        final List<String> texts = FilesCrawler.listFilesWithExtension(baseAddress, "ref");

        final double maxKPercent = 0.1;
        final int samples = 50;
        double kPercent = 0.0;
        for (int i = 0; i < samples; i++) {
            computeAverageFilePk(textIO, textSplitter, texts, kPercent);
            kPercent += maxKPercent / (samples - 1);
        }
    }

    private void computeAverageFilePk(TextIO textIO, TextSplitter textSplitter, List<String> texts, double kPercent) {
        double averagePk = 0;
        final int n = texts.size();
        for (int i = 0; i < n; i++) {
            textIO.read(texts.get(i));
            List<Interval<Integer>> hypList = getLowbowSeg(textIO, textSplitter, kPercent);
            List<Interval<Integer>> refList = getRefSeg(textIO, textSplitter);
            averagePk += computePk(hypList, refList);
        }
        System.out.println(kPercent + "\t" + averagePk / n);
    }

    private double computePk(List<Interval<Integer>> hypList, List<Interval<Integer>> refList) {
        int numOfElements = 0;
        final int numOfSegments = refList.size();
        for (final Interval<Integer> integerInterval : refList) {
            numOfElements += integerInterval.getXmax() - integerInterval.getXmin() + 1;
        }
        int k = numOfElements / (2 * numOfSegments) - 1;
        double pk = 0;
        for (int i = 1; i <= numOfElements - k; i++) {
            pk += diracAuxiliar(i, i + k, refList) != diracAuxiliar(i, i + k, hypList) ? 1 : 0;
        }
        return 0.5 * pk / (numOfElements - k);
    }

    /**
     * @param i
     * @param j
     * @param intervals
     * @return 1 if both element i and j are in the same segment or 0 otherwise.
     */
    private int diracAuxiliar(int i, int j, List<Interval<Integer>> intervals) {
        final Interval<Integer> baseInterval = new Interval<>(i, j);
        Stack<Interval<Integer>> stack = new Stack<>();
        for (Interval<Integer> interval : intervals) {
            final Interval<Integer> intersection = baseInterval.intersection(interval);
            if (!intersection.isEmptyInterval()) {
                stack.push(intersection);
            }
        }
        return stack.size() > 1 ? 0 : 1;
    }

    private List<Interval<Integer>> getRefSeg(TextIO textIO, TextSplitter textSplitter) {
        final String text = textIO.getText();
        final String[] split = text.split("==========");
        List<Interval<Integer>> ans = new ArrayList<>(split.length);
        int index = 1;
        for (String s : split) {
            if (s.isEmpty() || s.equals("\n"))
                continue;
            final String[] processedSegment = textSplitter.split(s);
            final int newIndex = index + processedSegment.length - 1;
            ans.add(new Interval<>(index, newIndex));
            index = newIndex + 1;
        }
        return ans;
    }

    private List<Interval<Integer>> getLowbowSeg(TextIO textIO, TextSplitter textSplitter, double kPercent) {
        EigenLowBow lowBow = new EigenLowBow(textIO.getText(), textSplitter);
        final int maxTextLength = lowBow.getTextLength();
        final int k = (int) MyMath.clamp(kPercent * maxTextLength + 1, 1, maxTextLength);
        lowBow.buildHeatRepresentation(k);
        lowBow.setLowBowSegmentator(MaxDerivativeSegmentator.getInstance());
        final List<SegmentBow<EigenLowBow>> segmentation = lowBow.getSegmentation(SegmentBow<EigenLowBow>::new);
        List<Interval<Integer>> ans = new ArrayList<>(segmentation.size());
        for (SegmentBow<EigenLowBow> eigenLowBowSegmentBow : segmentation) {
            final Interval<Integer> interval = eigenLowBowSegmentBow.getInterval();
            ans.add(interval);
        }
        return ans;
    }
}
