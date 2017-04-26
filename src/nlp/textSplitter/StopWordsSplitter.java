package nlp.textSplitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;


public class StopWordsSplitter implements TextSplitter {
    private static final String DEFAULT_STOP_WORD_LIST = "src/nlp/resources/wordLists/stopWords.txt";
    private TreeMap<String, Boolean> stopWords;

    public StopWordsSplitter() {
        this(DEFAULT_STOP_WORD_LIST);
    }

    public StopWordsSplitter(String address) {
        this.stopWords = new TreeMap<>();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(address));
            while ((sCurrentLine = br.readLine()) != null) {
                stopWords.put(sCurrentLine, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * code Horror
     */
    @Override
    public String[] split(String in) {
        String[] text = in.replaceAll("[^(\\p{L}|\\s+)]|\\(|\\)", " ").toLowerCase().replaceAll("[^(\\p{L}|\\s+)]|\\(|\\)", "").split("\\s+");
        ArrayList<String> ans = new ArrayList<>();
        for (int i = 0; i < text.length; i++) {
            if (!isStopWord(text[i])) {
                ans.add(text[i]);
            }
        }
        return ans.toArray(new String[0]);
    }

    public boolean isStopWord(String s) {
        return stopWords.containsKey(s);
    }
}