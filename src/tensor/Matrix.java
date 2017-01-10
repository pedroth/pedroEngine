package tensor;

import table.src.DenseNDArray;

public class Matrix<T extends AlgebraField> extends DenseNDArray<T> {
    public Matrix(int[] dim) {
        super(dim);
        if (dim.length != 2) {
            throw new RuntimeException("Matrices only have 2 dimensions");
        }
    }
}
