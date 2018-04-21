package table;

/**
 * The interface Table.
 *
 * @param <I> the type parameter of the Indexing variable
 * @param <E> the type parameter of the stored object
 */
public interface Table<I, E> {

    /**
     * Gets dimension.
     *
     * @return the dimension
     */
    int getDimension();

    /**
     * Get e.
     *
     * @param x the index variable
     * @return the element stored with index x
     */
    E get(I[] x);

    /**
     * Set void.
     *
     * @param x       the index variable
     * @param element the element to store with index x
     */
    void set(I[] x, E element);


}
