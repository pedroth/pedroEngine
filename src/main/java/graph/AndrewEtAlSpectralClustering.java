package graph;

import algebra.Diagonal;
import algebra.Matrix;
import algebra.Vector;

public class AndrewEtAlSpectralClustering extends SpectralClustering {
    /**
     * Instantiates a new Spectral clustering.
     *
     * @param graph the graph
     */
    public AndrewEtAlSpectralClustering(KnnGraph graph) {
        super(graph);
    }

    @Override
    protected Matrix transformMatrix(Matrix u, Diagonal d, Matrix w) {
        return normalizeRows(u);
    }

    @Override
    protected Matrix getLaplacian(Diagonal d, Matrix w) {
        Diagonal sqrt = d.inverse().sqrt();
        return Matrix.scalarProd(0.5, sqrt.prod(Matrix.diff(d, w).prod(sqrt)));
    }

    private Matrix normalizeRows(Matrix subMatrix) {
        Vector[] rowsVectors = subMatrix.getRowsVectors();
        for (int i = 0; i < rowsVectors.length; i++) {
            rowsVectors[i] = Vector.normalize(rowsVectors[i]);
        }
        return new Matrix(rowsVectors);
    }
}
