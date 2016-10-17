package numeric.test;


import algebra.src.*;
import graph.KnnGraph;
import graph.SpectralClustering;
import inputOutput.CsvReader;
import numeric.src.*;
import org.junit.Assert;
import org.junit.Test;
import utils.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NumericTest {

    @Test
    public void matrixExponentialTest() {
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        Vector initial = new Vec2(1, 0);
        double alpha = 8 * Math.PI;
        int n = 50000;
        Vector x = MatrixExponetial.exp(alpha, matrix, initial, n);
        Assert.assertTrue(Vector.diff(x, new Vec2(Math.cos(alpha), Math.sin(alpha))).norm() < 0.01);
    }

    @Test
    public void matrixExponentialTest2() {
        Matrix matrix = new Matrix(new double[][]{{0, 0, 0}, {1, -2, 1}, {0, 0, 0}});
        Vector initial = new Vec3(0, 0, 1);
        double alpha = 1;
        int n = 10000;
        Vector x = MatrixExponetial.exp(alpha, matrix, initial, n);
        System.out.println(x);
    }

    @Test
    public void sVDTest() {
        Matrix matrix = new Matrix(new double[][]{{1, 0, 0}, {-1, 2, -1}, {0, 0, 1}});
        SVD svd = new SVD(matrix);
        svd.computeSVD();
        Matrix V = svd.getV();
        Matrix S = svd.getSigma();
        Matrix U = svd.getU();
        Matrix prod = Matrix.prod(U, Matrix.prod(S, Matrix.transpose(V)));
        System.out.println(U);
        System.out.println(S);
        System.out.println(V);
        System.out.println(prod);
        Assert.assertTrue(Matrix.squareNorm(Matrix.diff(matrix, prod)) < 1E-20);
        double cos45 = Math.sqrt(2) / 2;
        Assert.assertTrue(Vector.diff(new Vec3(-cos45, 0, cos45), new Vector(U.getSubMatrix(1, 3, 2, 2))).norm() < 1E-3 || Vector.diff(new Vec3(cos45, 0, -cos45), new Vector(U.getSubMatrix(1, 3, 2, 2))).norm() < 1E-3);
    }

    @Test
    public void testSvd2() throws Exception {
        Matrix matrix = new Matrix(new double[][]{{0, -1}, {1, 0}});
        SVD svd = new SVD(matrix);
        svd.computeSVD();
        Matrix V = svd.getV();
        Matrix S = svd.getSigma();
        Matrix U = svd.getU();
        Matrix prod = Matrix.prod(U, Matrix.prod(S, Matrix.transpose(V)));
        System.out.println(U);
        System.out.println(Matrix.transpose(V));
        System.out.println(prod);
        Assert.assertTrue(Matrix.squareNorm(Matrix.diff(matrix, prod)) < 1E-20);
    }

    @Test
    public void PCATest() {
        CsvReader table = new CsvReader();
        table.read("src/numeric/test/resource/testData2.csv");
        int[] size = table.getSize();
        Vector[] data = new Vector[size[0]];
        for (int i = 0; i < size[0]; i++) {
            data[i] = new Vector(size[1]);
            for (int j = 0; j < size[1]; j++) {
                data[i].setX(j + 1, Double.valueOf(table.get(new Integer[]{i, j})));
            }
        }
        double time = System.currentTimeMillis();
        Pca pca = new Pca(data);
        System.out.println((System.currentTimeMillis() - time) * 1E-3);
        Vector[] eigenVectors = pca.getEigenVectors();
        double[] eigenValues = pca.getEigenValues();
        double cos45 = -Math.sqrt(2) / 2;
        for (Vector eigenVector : eigenVectors) {
            System.out.println(eigenVector);
        }
        for (double eigenValue : eigenValues) {
            System.out.println(eigenValue);
        }
        Assert.assertTrue(Vector.diff(eigenVectors[0], new Vec2(cos45, cos45)).norm() < 1E-3 || Vector.diff(eigenVectors[0], new Vec2(-cos45, -cos45)).norm() < 1E-3);
    }

    @Test
    public void testEigenValue() {
        Matrix matrix = new Matrix(new double[][]{{1, -1, 0}, {-1, 2, -1}, {0, -1, 1}});
        SymmetricEigen eigen = new SymmetricEigen(matrix);
        Vector[] eigenVectors = eigen.getEigenVectors();
        Double[] eigenValues = eigen.getEigenValues();
        for (Vector eigenVector : eigenVectors) {
            System.out.println(eigenVector);
        }
        for (Double eigenValue : eigenValues) {
            System.out.println(eigenValue);
        }
        double cos45 = Math.sqrt(2) / 2;
        Assert.assertTrue(Vector.diff(eigenVectors[1], new Vec3(cos45, 0, -cos45)).norm() < 1E-3 || Vector.diff(eigenVectors[1], new Vec3(-cos45, 0, cos45)).norm() < 1E-3);
    }

    @Test
    public void testTridiagonalEigen() {
        TridiagonalMatrix matrix = new TridiagonalMatrix(new double[]{1, 2, 1}, new double[]{-1, -1}, new double[]{-1, -1});
        SymmetricEigen eigen = new SymmetricEigen(matrix);
        Assert.assertTrue(matrix.getXY(2, 3) == -1);
        Assert.assertTrue(matrix.getXY(1, 3) == 0);
        Assert.assertTrue(matrix.getColumns() == 3);
        Vector[] eigenVectors = eigen.getEigenVectors();
        Double[] eigenValues = eigen.getEigenValues();
        for (Vector eigenVector : eigenVectors) {
            System.out.println(eigenVector);
        }
        for (Double eigenValue : eigenValues) {
            System.out.println(eigenValue);
        }
        double cos45 = Math.sqrt(2) / 2;
        Assert.assertTrue(Vector.diff(eigenVectors[1], new Vec3(cos45, 0, -cos45)).norm() < 1E-3 || Vector.diff(eigenVectors[1], new Vec3(-cos45, 0, cos45)).norm() < 1E-3);
    }

    @Test
    public void testTridiagonalEigen2() {
        TridiagonalMatrix matrix = new TridiagonalMatrix(new double[]{1, 2, 1}, new double[]{-1, -1}, new double[]{-1, -1});
        Vec3 v = new Vec3(1, 2, 3);
        Vec3 ans = new Vec3(-1, 0, 1);
        Vector test = matrix.prodVector(v);
        System.out.println(test);
        Assert.assertTrue(Vector.diff(ans, test).norm() < 1E-3);
    }


    @Test
    public void eigenVsSvd() {
        double n = 100;
        for (int j = 0; j < n; j++) {
            Matrix matrix = new Matrix(new double[][]{{-1, 1, 0}, {1, -2, 1}, {0, 1, -1}});
            int columns = matrix.getColumns();
            int rows = matrix.getRows();
            Vector data[] = new Vector[columns];
            for (int i = 1; i <= columns; i++) {
                data[i - 1] = new Vector(matrix.getSubMatrix(1, rows, i, i));
            }
            SVD svd = new SVD(matrix);
            double time = System.currentTimeMillis();
            svd.computeSVD();
            System.out.println(1E-3 * (System.currentTimeMillis() - time));
            System.out.println(svd.getSigma());
        }
    }

    @Test
    public void QuadraticFormTest1() {
        Matrix matrix = new Matrix(new double[][]{{1, 2}, {2, 3.999}});
        Vector b = new Vec2(4, 7.999);
        QuadraticFormMinimizer quadraticFormMinimizer = new QuadraticFormMinimizer(matrix, b);
        double time = System.currentTimeMillis();
        quadraticFormMinimizer.argMin(1E-12, b);
        System.out.println((System.currentTimeMillis() - time) * 1E-3);
        time = System.currentTimeMillis();
        Vec2 initial = new Vec2();
        initial.fillRandom(-1, 1);
        quadraticFormMinimizer.argMin(1E-12, initial);
        System.out.println((System.currentTimeMillis() - time) * 1E-3);
    }

    @Test
    public void superEigenTest() {
        int n = 100;
        Matrix laplacian = new LineLaplacian(n);
        SymmetricEigen symmetricEigen = new SymmetricEigen(laplacian);
        StopWatch stopWatch = new StopWatch();
        symmetricEigen.computeEigen(1E-5, n, new IntrinsicEigenAlgo());
        System.out.println("\n" + stopWatch.getEleapsedTime() + "\n");
        symmetricEigen.orderEigenValuesAndVector();
        for (Double eigenValue : symmetricEigen.getEigenValues()) {
            System.out.println(eigenValue);
        }
        Matrix v = new Matrix(symmetricEigen.getEigenVectors());
        System.out.println(v);
    }

    @Test
    public void testKmeans() {
        List<Vector> data = new ArrayList<>(6);
        data.add(new Vec2(-1.1, 0.1));
        data.add(new Vec2(-0.9, -0.1));
        data.add(new Vec2(1.1, 0.1));
        data.add(new Vec2(0.9, -0.1));
        data.add(new Vec2(0.1, -0.9));
        data.add(new Vec2(-0.1, -1.1));
        Kmeans kmeans = new Kmeans(data);
        kmeans.runKmeans(3, 1E-3, 100);
        Map<Integer, Integer> classification = kmeans.getClassification();
        for (Map.Entry<Integer, Integer> entry : classification.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        Vector[] clusters = kmeans.getClusters();
        for (Vector p : clusters) {
            System.out.println(p);
        }
        Assert.assertTrue(Vec2.diff(clusters[classification.get(2)], new Vec2(1, 0)).norm() < 0.5);
    }

    @Test
    public void testSpectralClustering() {
        List<Vector> data = new ArrayList<>(6);
        data.add(new Vec2(-1.1, 0.1));
        data.add(new Vec2(-0.9, -0.1));
        data.add(new Vec2(1.1, 0.1));
        data.add(new Vec2(0.9, -0.1));
        data.add(new Vec2(0.1, -0.9));
        data.add(new Vec2(-0.1, -1.1));
        KnnGraph<Vector> graph = new KnnGraph<>(data, 3, (x, y) -> Vector.diff(x, y).squareNorm());
        SpectralClustering spectralClustering = new SpectralClustering(graph);
        spectralClustering.clustering(3);
        Map<Integer, Integer> classification = spectralClustering.getClassification();
        for (Map.Entry<Integer, Integer> entry : classification.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    @Test
    public void testGaussianMixtureModel() {
        List<Vector> data = new ArrayList<>(6);
        data.add(new Vec2(-1.1, 0.1));
        data.add(new Vec2(-0.9, -0.1));
        data.add(new Vec2(1.1, 0.1));
        data.add(new Vec2(0.9, -0.1));
        data.add(new Vec2(0.1, -0.9));
        data.add(new Vec2(-0.1, -1.1));
        GaussianMixtureClustering kmeans = new GaussianMixtureClustering(data);
        kmeans.runClustering(3, 1E-3);
        Map<Integer, Integer> classification = kmeans.getClassification();
        for (Map.Entry<Integer, Integer> entry : classification.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        Vector[] clusters = kmeans.getClusters();
        for (Vector p : clusters) {
            System.out.println(p);
        }
    }

}
