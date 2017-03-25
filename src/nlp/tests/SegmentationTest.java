package nlp.tests;

import inputOutput.TextIO;
import nlp.lowbow.eigenLowbow.*;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.segmentedBow.SegmentedBowHeat;
import nlp.symbolSampler.TopKSymbol;
import nlp.textSplitter.StopWordsSplitter;
import nlp.textSplitter.SubsSplitter;
import nlp.utils.RemoveStopWordsPredicate;
import org.junit.Test;
import utils.FilesCrawler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        SummaryGenLowBowManager<LowBowSubtitles> lowBowManager = new SummaryGenLowBowManager<>();

        //lowbow representation
        for (int i = 0; i < 1; i++) {
            textIO.read(subtitles.get(i));
            textSplitter = new SubsSplitter(RemoveStopWordsPredicate.getInstance());
            LowBowSubtitles<SubsSplitter> low = new LowBowSubtitles<>(textIO.getText(), textSplitter, videos.get(i));
            LowBowSubSegmentator lowBowSubSegmentator = MaxDerivativeSubSegmentator.getInstance();
            low.setLowBowSubSegmentator(lowBowSubSegmentator);
            lowBowManager.add(low);
        }
        lowBowManager.buildModel(heatTime);
        final LowBowSubtitles lowBowSubtitles = lowBowManager.getDocModels().get(0);
        final String text = lowBowSubtitles.generateText(new TopKSymbol(10, ","));

        lowBowManager.buildSegmentations(SegmentedBowHeat::new);
        List<BaseSegmentedBow> segmentedBows = lowBowManager.getSegmentedBows();
        Set<Integer> boundaryIndex = new HashSet<>();
        for (BaseSegmentedBow segmentedBow : segmentedBows) {
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
    public void segmentTest() {
        final String desktopAddress = System.getProperty("user.home") + "/Desktop/";
        final String baseAddress = "C:/pedro/escolas/ist/Tese/C99/";
        final double heatTime = 0.04;

        List<String> textAddresss = FilesCrawler.listFilesWithExtension(baseAddress, "srt");
        Collections.sort(textAddresss);

        StopWordsSplitter textSplitter = new StopWordsSplitter("src/nlp/resources/wordLists/stopWords.txt");
        TextIO textIO = new TextIO();

        //lowbow representation
        for (int i = 0; i < 1; i++) {
            textIO.read(textAddresss.get(i));
            EigenLowBow low = new EigenLowBow(textIO.getText(), textSplitter);
        }
    }
}
