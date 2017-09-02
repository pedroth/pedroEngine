package algorithms;

/**
 * The interface Sort.
 */
public interface Sort {

    /**
     * Sort elements according to some order defined in implementations
     *
     * @param <T>   the type parameter
     * @param array the array
     */
    <T extends Comparable> void sort(T[] array);
}
