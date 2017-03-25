package nlp.utils;


public final class RemoveStopWordsPredicate implements RemoveWordsPredicate {

    private static RemoveWordsPredicate instance = new RemoveStopWordsPredicate();
    private static StopWordPredicate stopWordPredicate = StopWordPredicate.getInstance();

    private RemoveStopWordsPredicate() {
        // prohibits instance creation
    }

    public static RemoveWordsPredicate getInstance() {
        return instance;
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
