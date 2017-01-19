package tensor;

import algebra.utils.AlgebraException;
import table.src.DenseNDArray;

import java.util.Arrays;
import java.util.function.Function;

/**
 * The type Matrix.
 *
 * @param <I> the type parameter
 */
public class Matrix<I extends AlgebraField> extends DenseNDArray<I> {
    /**
     * Instantiates a new Matrix.
     *
     * @param dim the dim
     */
    public Matrix(int[] dim) {
        super(dim);
        if (dim.length != 2) {
            throw new RuntimeException("Matrices only have 2 dimensions");
        }
    }

    /**
     * Instantiates a new Matrix.
     *
     * @param b the b
     */
    public Matrix(Matrix<I> b) {
        this(b.dim);
        int size = size();
        for (int i = 0; i < size; i++) {
            this.denseNDArray.set(i, (I) b.denseNDArray.get(i).copy());
        }

    }

    /**
     * Instantiates a new Matrix.
     *
     * @param matrix the matrix. Example of the format of a matrix : "1 2 3 ; 4 5 6"
     * @param parser the parser
     */
    public Matrix(String matrix, Function<String, I> parser) {
        this(getDimFromText(matrix));
        parseMatrix(matrix, parser);
    }

    private void parseMatrix(String matrix, Function<String, I> parser) {
        String[] split = matrix.split(";");
        for (int i = 0; i < split.length; i++) {
            String[] strings = split[i].split("\\s+");
            int index = 0;
            for (int j = 0; j < strings.length; j++) {
                if (strings[j] != null && strings[j].isEmpty()) {
                    continue;
                }
                I elem = parser.apply(strings[j]);
                this.set(i, index++, elem);
            }
        }
    }

    private static int[] getDimFromText(String matrix) {
        int[] dimAns = new int[2];
        String[] split = matrix.split(";");
        dimAns[0] = split.length;
        if (dimAns[0] == 0) {
            throw new RuntimeException("syntax error : empty columns");
        }
        dimAns[1] = split[0].split("\\s+").length;
        if (dimAns[1] == 0) {
            throw new RuntimeException("syntax error : empty rows");
        }
        return dimAns;
    }

    /**
     * Sum matrix.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix<I> sum(Matrix<I> b) {
        if (!Arrays.equals(this.dim, b.dim)) {
            throw new RuntimeException("In the sum of matrices dimensions should be equal");
        }

        int size = this.size();
        for (int i = 0; i < size; i++) {
            this.denseNDArray.set(i, (I) this.denseNDArray.get(i).sum(b.denseNDArray.get(i)));
        }
        return this;
    }

    /**
     * Diff matrix.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix<I> diff(Matrix<I> b) {
        if (!Arrays.equals(this.dim, b.dim)) {
            throw new RuntimeException("In the sum of matrices dimensions should be equal");
        }

        int size = this.size();
        for (int i = 0; i < size; i++) {
            this.denseNDArray.set(i, (I) this.denseNDArray.get(i).diff(b.denseNDArray.get(i)));
        }
        return this;
    }

    /**
     * Scalar prod.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix<I> scalarProd(I b) {
        this.forEach((x) -> (I) x.prod(b));
        return this;
    }

    /**
     * Get i.
     *
     * @param x the x
     * @param y the y
     * @return the i
     */
    public I get(int x, int y) {
        return this.denseNDArray.get(x + y * powers[1]);
    }

    /**
     * Set void.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void set(int x, int y, I z) {
        this.denseNDArray.set(x + y * powers[1], z);
    }

    /**
     * Transpose matrix.
     *
     * @return the matrix
     */
    public Matrix<I> transpose() {
        int size = this.size();
        for (int i = 0; i < size; i++) {
            int x = i % powers[1];
            int y = i % powers[2] / powers[1];
            I t = this.get(x, y);
            this.set(x, y, get(y, x));
            set(y, x, t);
        }
        return this;
    }

    /**
     * Prod matrix. doesn't change the matrix, it returns a new one.
     *
     * @param b the b
     * @return the matrix
     */
    public Matrix<I> prod(Matrix<I> b) {
        Matrix<I> c;
        if (this.dim[1] != b.dim[0]) {
            throw new AlgebraException("the number of columns of the first matrix must be equal to the number of lines of the second one");
        }

        c = new Matrix(new int[] { dim[0], b.dim[1] });
        int size = c.size();
        for (int i = 0; i < size; i++) {
            int x = i % c.powers[1];
            int y = i % c.powers[2] / c.powers[1];
            I sumIdentity = (I) this.denseNDArray.get(0).sumIdentity();
            for (int k = 0; k < this.dim[0]; k++) {
                I prod = (I) this.get(x, k).prod(b.get(k, y));
                sumIdentity = (I) sumIdentity.sum(prod);
            }
            c.set(x, y, sumIdentity);
        }
        return c;
    }

    /**
     * Square norm.
     *
     * @return the i
     */
    public I squareNorm() {
        return this.innerProd(this);
    }

    /**
     * Inner prod.
     *
     * @param b the b
     * @return the i
     */
    public I innerProd(Matrix<I> b) {
        if (this.dim[0] != b.dim[0]) {
            throw new RuntimeException("rows must be of same dimension");
        }
        I acc = (I) this.denseNDArray.get(0).sumIdentity();
        int[] index = new int[2];
        int min = Integer.min(dim[1], b.dim[1]);
        for (int j = 0; j < min; j++) {
            I acc2 = (I) this.denseNDArray.get(0).sumIdentity();
            for (int i = 0; i < dim[0]; i++) {
                index[0] = i;
                index[1] = j;
                acc2 = (I) acc2.sum(this.get(index).prod(b.get(index)));
            }
            acc = (I) acc.sum(acc2);
        }
        return acc;
    }

}
