package nlp.utils;


import java.io.IOException;

public final class RemoveStopWordsPredicate implements RemoveWordsPredicate {
    private StopWordPredicate stopWordPredicate;

    public RemoveStopWordsPredicate() throws IOException {
        this.stopWordPredicate = new StopWordPredicate();
    }

    public RemoveStopWordsPredicate(String stopWordsAddress) throws IOException {
        this.stopWordPredicate = new StopWordPredicate(stopWordsAddress);
    }

    @Override
    public String getNotNecessaryWordString() {
        return stopWordPredicate.getNotNecessaryWordString();
    }

    @Override
    public boolean test(String s) {
        return !stopWordPredicate.test(s);
    }
}
