package nlp.lowbow.simpleLowBow;


import algebra.src.LineLaplacian;
import algebra.src.Matrix;
import algebra.src.Vector;

public class LaplacianEigenFlow implements HeatMethod {
    @Override
    public void heatFlow(double lambda, LowBow l) {
        int n = l.samples;

        LineLaplacian laplacian = new LineLaplacian(n);
        Vector[] eigenVectors = laplacian.getEigenVectors();
        Matrix eigenBasis = new Matrix(eigenVectors);
        Vector eigenValues = new Vector(laplacian.getEigenValues());
        for (int i = 1; i <= eigenValues.getDim(); i++) {
            eigenValues.setX(i, Math.exp(-eigenValues.getX(i) * lambda));
        }
        Vector[] curve = l.getCurve();
        Matrix rawCurve = new Matrix(curve);
        rawCurve.transpose();
        rawCurve = Matrix.transpose(eigenBasis).prod(rawCurve);
        Matrix diag = Matrix.diag(eigenValues);
        rawCurve = diag.prod(rawCurve);
        Matrix prod = eigenBasis.prod(rawCurve);
        l.setCurve(prod.getRowsVectors());
    }
}
