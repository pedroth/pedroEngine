package numeric.src;

/**
 * Created by Pedroth on 6/14/2016.
 *
 * @param <X> the type parameter
 */
public interface Norm<X> {
    /**
     * Distance between two objects
     *
     * @param x the x
     * @return the distance between "this" and x
     */
    double dist(X x);
}
