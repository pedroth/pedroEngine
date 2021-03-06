package numeric;

import algebra.Matrix;
import algebra.Vector;

/**
 * @author pedro
 *         <p>
 *         warning some code repetition with Pca
 *         <p>
 *         V is the new orthonormal matrix which maximizes variance / minimizes projection of the row vectors of matrix M
 *         U is the matrix of coordinates of the row vectors of the matrix M in the transpose(V) basis
 *         S matrix of singular values
 */
public class SVD {
    private Matrix sigma, sigmaInv, u, v, m;
    private Vector[] eigenVectors;
    private double[] eigenValues;
    private Vector[] data;

    public SVD(Matrix m) {
        this.m = m;
        int rows = m.getRows();
        int col = m.getColumns();
        data = new Vector[col];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Vector(m.getSubMatrix(1, rows, i + 1, i + 1));
        }
    }

    private static Vector superEigen(Matrix conv) {
        int maxIte = 10000;
        int ite = 0;
        double time = 1E-9 * System.nanoTime();
        double epsilon = 1E-15;
        Vector eigenV = new Vector(conv.getRows());
        eigenV.fillRandom(-1, 1);
        eigenV = Vector.normalize(eigenV);
        Vector grad = null;
        Vector eta = null;
        do {
            grad = Vector.matrixProd(conv, eigenV);
            double beta = -(grad.squareNorm() / Vector.innerProd(grad, Vector.matrixProd(conv, grad)));
            /**
             * you must put a minus since you want to maximize.
             */
            eta = Vector.scalarProd(-0.5 * beta, grad);
            eta = Vector.orthoProjection(eta, eigenV);
            eigenV = Vector.add(eigenV, eta);
            eigenV = Vector.normalize(eigenV);
            ite++;
        } while (eta.norm() > epsilon && ite < maxIte);
        // System.out.println(ite + " time : " + (1E-9 *
        // System.nanoTime() - time) + " error : " + eta.norm() +
        // " <eta,eigen> : " + Vector.innerProd(eta, eigenV));
        return eigenV;
    }

    public static void main(String[] args) {
        double[][] m = {{7, 8, 1, 0, 6}, {1, 6, 6, 6, 7}, {1, 4, 8, 2, 4}, {3, 2, 8, 2, 8}, {8, 2, 8, 3, 9}, {5, 4, 7, 1, 7}, {5, 2, 8, 5, 2}, {9, 0, 7, 9, 7}, {5, 1, 2, 3, 1}, {0, 2, 9, 5, 5}};
        Matrix M = new Matrix(m);
        SVD svd = new SVD(M);
        svd.computeSVD();
        Matrix U = svd.getU();
        Matrix S = svd.getSigma();
        Matrix V = svd.getV();
        System.out.println(Matrix.prod(Matrix.transpose(U), U));
//		System.out.println(S.toStringMatlab());
//		System.out.println(V.toStringMatlab());
//		System.out.println(Matrix.diff(M, Matrix.prod(U, Matrix.prod(S, Matrix.transpose(V)))).toStringMatlab());
    }

    /**
     * @param n is the rank of the svd
     */
    public void computeSVD(int n) {
        if (n < 1 || n > data[0].getDim())
            return;

        this.eigenVectors = new Vector[n];
        this.eigenValues = new double[n];
        /**
         * compute covariance matrix, or m * transpose(m);
         */
        Vector[] myData = new Vector[data.length];
        Matrix conv = new Matrix(data[0].getDim(), data[0].getDim());
        for (int i = 0; i < data.length; i++) {
            myData[i] = data[i].copy();
            conv = Matrix.add(conv, Matrix.prod(myData[i], Matrix.transpose(myData[i])));
        }

        SymmetricEigen symmetricEigen = new SymmetricEigen(conv);
        eigenVectors = symmetricEigen.getEigenVectors();
        Double[] eigenValues = symmetricEigen.getEigenValues();
        for (int i = 0; i < eigenValues.length; i++) {
            double sqrt = Math.sqrt(Math.abs(eigenValues[i]));
            this.eigenValues[i] = sqrt;
        }

        /**
         * Construct sigma and v
         */
        this.u = new Matrix();
        this.sigma = new Matrix(n, n);
        this.sigmaInv = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            u = u.concat(eigenVectors[i]);
            /**
             * there should be a square root on the eigenvalues,
             */
            double singularValue = (this.eigenValues[i]);
            sigma.setXY(i + 1, i + 1, singularValue);
            sigmaInv.setXY(i + 1, i + 1, 1 / singularValue);
        }

        if (m.getRows() > 100 || m.getColumns() > 100) {
            v = Matrix.prodParallel(Matrix.transpose(m), Matrix.prodParallel(u, sigmaInv));
        } else {
            v = Matrix.prod(Matrix.transpose(m), Matrix.prod(u, sigmaInv));
        }
    }

    public void computeSVD() {
        this.computeSVD(data[0].getDim());
    }

    /**
     * @return diagonal matrix with singular values
     */
    public Matrix getSigma() {
        return sigma;
    }

    /**
     * @return inverse of sigma matrix
     */
    public Matrix getSigmaInv() {
        return sigmaInv;
    }

    /**
     * @return U matrix from SVD decomposition
     */
    public Matrix getU() {
        return u;
    }

    /**
     * @return V matrix from SVD decomposition
     */
    public Matrix getV() {
        return v;
    }

    /**
     * @return original matrix
     */
    public Matrix getM() {
        return m;
    }

    /**
     * @return array of column vectors of the V matrix
     */
    public Vector[] getEigenVectors() {
        if (eigenVectors == null) computeSVD();
        return eigenVectors;
    }

    /**
     * @return array of singular values squared
     */
    public double[] getEigenValues() {
        return eigenValues;
    }
}
