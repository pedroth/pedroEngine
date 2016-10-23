package nlp.tests;

import inputOutput.TextIO;
import nlp.segmentedBow.BaseSegmentedBow;
import nlp.seriesSummary.ArcSummarizer;
import nlp.utils.LdaWrapper;
import org.junit.Test;

public class ArcSummaryTest {

    @Test
    public void lowbowLdaStatistics() {
        ArcSummarizer arcSummarizer = new ArcSummarizer("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/", "mkv", 0.045, 0.1, 3, 5, ArcSummarizer.simplexDist);
        arcSummarizer.buildSummary("C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/summary", 10);
        StringBuilder stringBuilder = new StringBuilder();
        for (BaseSegmentedBow baseSegmentedBow : arcSummarizer.getSegmentedBows()) {
            stringBuilder.append(baseSegmentedBow.cutSegmentSubtitleWords() + "\n");
        }
        TextIO textIO = new TextIO();
        String adress = "C:/pedro/escolas/ist/Tese/Series/OverTheGardenWall/summary/segmentsCorpus.txt";
        textIO.write(adress, stringBuilder.toString());
        LdaWrapper.computeLda(adress, 5, 0.1, 0.01, 2000, "ldaModel");
    }

}
