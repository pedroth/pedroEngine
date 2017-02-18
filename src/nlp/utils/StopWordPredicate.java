package nlp.utils;

import inputOutput.TextIO;

import java.util.HashSet;

/**
 * The type Stop word predicate.
 */
public class StopWordPredicate implements RemoveWordsPredicate {

    private static HashSet<String> stopWordsSet;
    private static StopWordPredicate stopWordPredicate = new StopWordPredicate();


    private StopWordPredicate() {
        TextIO text = new TextIO();
        text.read("src/nlp/resources/wordLists/stopWords.txt");
        String[] split = text.getText().split("\n");
        stopWordsSet = new HashSet<>(split.length);
        for (int i = 0; i < split.length; i++) {
            stopWordsSet.add(split[i]);
        }

    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static StopWordPredicate getInstance() {
        return stopWordPredicate;
    }

    public static void main(String[] args) {
        StopWordPredicate stopWordPredicate = StopWordPredicate.getInstance();
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
