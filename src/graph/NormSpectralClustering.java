package graph;

import algebra.src.Diagonal;
import algebra.src.Matrix;

public class NormSpectralClustering extends SpectralClustering {

    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public NormSpectralClustering(KnnGraph graph) {
        super(graph);
    }

    @Override
    protected Matrix transformMatrix(Matrix u, Diagonal d, Matrix w) {
        Diagonal sqrt = d.inverse().sqrt();
        return sqrt.prod(u).transpose();
    }

    @Override
    protected Matrix getLaplacian(Diagonal d, Matrix w) {
        Diagonal sqrt = d.inverse().sqrt();
        return Matrix.scalarProd(0.5, sqrt.prod(Matrix.diff(d, w).prod(sqrt)));
    }
}
