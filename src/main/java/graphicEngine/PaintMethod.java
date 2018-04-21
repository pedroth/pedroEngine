package graphicEngine;

/**
 * The interface Paint method.
 *
 * @param <E> the type parameter
 */
public interface PaintMethod<E> {
    /**
     * Initialize void.
     */
    void initialize();

    /**
     * Paint void.
     *
     * @param element the element
     */
    void paint(E element);
}
