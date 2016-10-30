package graph;

import algebra.src.Matrix;
import algebra.src.Vector;

public class RandomWalkGraph {
    private Graph graph;

    public RandomWalkGraph(Graph graph) {
        this.graph = graph;
    }

    public Vector getRandomWalkDistribution(Vector initialDistribution, double p, int k) {
        //deep copy
        Vector v = new Vector(initialDistribution);
        Matrix weightMatrix = graph.getWeightMatrix(0);
        Matrix degreeMatrix = getDegreeMatrix(weightMatrix);
        degreeMatrix.applyFunction(x -> 1 / x);
        Matrix ones = new Matrix(weightMatrix.getRows(), weightMatrix.getColumns());
        ones.fill(1.0);
        Matrix b = Matrix.diag(Vector.scalarProd(1 - p, v)).prod(ones);
        // kernel matrix p * D^-1 * W + (1 - p) * diag(initialDistribution) * ones
        weightMatrix = Matrix.transpose(Matrix.add(Matrix.scalarProd(p, degreeMatrix.prod(weightMatrix)), b));
        for (int i = 0; i < k; i++) {
            v = weightMatrix.prodVector(v);
        }
        return v;
    }

    private Matrix getDegreeMatrix(Matrix w) {
        double acc;
        int columns = w.getColumns();
        int rows = w.getRows();
        Vector degrees = new Vector(rows);
        for (int i = 1; i <= rows; i++) {
            acc = 0;
            for (int j = 1; j <= columns; j++) {
                acc += w.getXY(i, j);
            }
            degrees.setX(i, acc);
        }
        return Matrix.diag(degrees);
    }

    public Vector getStationaryDistribution(Vector initialDistribution, double p, double convError) {
        //deep copy
        Vector v = new Vector(initialDistribution);
        Matrix weightMatrix = graph.getWeightMatrix(0);
        Matrix degreeMatrix = getDegreeMatrix(weightMatrix);
        degreeMatrix.applyFunction(x -> x == 0.0 ? 1.0 : 1 / x);
        Matrix ones = new Matrix(weightMatrix.getRows(), weightMatrix.getColumns());
        ones.fill(1.0);
        Matrix b = Matrix.diag(Vector.scalarProd(1 - p, v)).prod(ones);
        // kernel matrix p * D^-1 * W + (1 - p) * diag(initialDistribution) * ones
        weightMatrix = Matrix.transpose(Matrix.add(Matrix.scalarProd(p, degreeMatrix.prod(weightMatrix)), b));
        Vector u;
        int i = 0;
        do {
            u = new Vector(v);
            v = weightMatrix.prodVector(v);
            i++;
        } while (Vector.diff(u, v).squareNorm() > convError);
//        System.out.println(i);
        return v;
    }
}
