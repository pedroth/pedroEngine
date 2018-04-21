package numeric;

import algebra.Matrix;
import algebra.Vector;

import java.util.List;


/**
 * The interface Eigen algo.
 */
public interface EigenAlgo {
    /**
     * Super eigen.
     *
     * @param symMatrix        the symmetric matrix
     * @param initialCondition the initial condition vector in Sphere
     * @param eigenVectors     the eigen vectors
     * @param epsilon          the epsilon
     * @return vector vector
     */
    Vector superEigen(Matrix symMatrix, Vector initialCondition, List<Vector> eigenVectors, double epsilon);
}
