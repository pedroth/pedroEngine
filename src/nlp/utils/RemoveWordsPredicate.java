package nlp.utils;


import java.util.function.Predicate;

public interface RemoveWordsPredicate extends Predicate<String> {
    String getNotNecessaryWordString();
}
