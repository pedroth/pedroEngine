package nlp.utils;

import inputOutput.TextIO;

import java.io.IOException;
import java.util.HashSet;

/**
 * The type Stop word predicate.
 */
public class StopWordPredicate implements RemoveWordsPredicate {
    private static final String DEEFAULT_ADDRESS = "src/main/java/nlp/resources/wordLists/stopWords.txt";
    private HashSet<String> stopWordsSet;

    public StopWordPredicate() throws IOException {
        this(DEEFAULT_ADDRESS);
    }

    public StopWordPredicate(String stopWordListAddress) throws IOException {
        TextIO text = new TextIO();
        text.read(stopWordListAddress);
        String[] split = text.getText().split("\n");
        stopWordsSet = new HashSet<>(split.length);
        for (int i = 0; i < split.length; i++) {
            stopWordsSet.add(split[i]);
        }
    }

    public static void main(String[] args) throws IOException {
        StopWordPredicate stopWordPredicate = new StopWordPredicate();
        System.out.println(stopWordPredicate.test("a"));
    }

    /**
     * detects stop words.
     *
     * @param s string
     * @return
     */
    @Override
    public boolean test(String s) {
        return stopWordsSet.contains(s);
    }

    @Override
    public String getNotNecessaryWordString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String notNecessaryWord : stopWordsSet) {
            stringBuilder.append(notNecessaryWord + "\n");
        }
        return stringBuilder.toString();
    }
}
