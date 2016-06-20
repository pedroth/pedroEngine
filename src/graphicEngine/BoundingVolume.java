package graphicEngine;

import numeric.src.Norm;

/**
 * The type Bounding volume.
 *
 * @param <B> the type parameter
 */
public abstract class BoundingVolume<B extends BoundingVolume> implements Norm<B> {
    /**
     * The Is empty.
     */
    protected boolean isEmpty;

    /**
     * Instantiates a new Bounding volume.
     */
    public BoundingVolume() {
        isEmpty = true;
    }

    /**
     * Is empty.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Union b.
     *
     * @param boundingVolume the bounding volume
     * @return the b
     */
    public abstract B union(B boundingVolume);

    /**
     * Intersection b.
     *
     * @param boundingVolume the bounding volume
     * @return the b
     */
    public abstract B intersection(B boundingVolume);
}
