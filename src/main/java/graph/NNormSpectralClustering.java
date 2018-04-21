package graph;

import algebra.Diagonal;
import algebra.Matrix;

public class NNormSpectralClustering extends SpectralClustering {
    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public NNormSpectralClustering(KnnGraph graph) {
        super(graph);
    }

    @Override
    protected Matrix transformMatrix(Matrix u, Diagonal d, Matrix w) {
        return u.transpose();
    }

    @Override
    protected Matrix getLaplacian(Diagonal d, Matrix w) {
        return Matrix.scalarProd(0.5, Matrix.diff(d, w));
    }
}
