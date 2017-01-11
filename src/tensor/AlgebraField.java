package tensor;

/**
 * The interface Algebra field.
 * @param <T>                the type parameter
 */
public interface AlgebraField<T> {
    /**
     * Sum t.
     *
     * @param y the y
     * @return the t
     */
    T sum( T y);

    /**
     * Diff t.
     *
     * @param y the y
     * @return the t
     */
    T diff(T y);

    /**
     * Prod t.
     *
     * @param y the y
     * @return the t
     */
    T prod(T y);

    /**
     * Div t.
     *
     * @param y the y
     * @return the t
     */
    T div(T y);

    /**
     * Sum identity.
     *
     * @return the t
     */
    T sumIdentity();

    /**
     * Prod identity.
     *
     * @return the t
     */
    T prodIdentity();

    /**
     * Symmetric t.
     *
     * @return the t
     */
    T symmetric();

    /**
     * Reciprocal t.
     *
     * @return the t
     */
    T reciprocal();

    T clone();


}
