package algebra.utils;

import algebra.src.Matrix;

import java.util.function.Function;

/**
 * The interface Matrix printer.
 */
public interface MatrixPrinter extends Function<Matrix, String> {
    /**
     * Before string.
     *
     * @return the string
     */
    String before();

    /**
     * After string.
     *
     * @return the string
     */
    String after();

    /**
     * Line string.
     *
     * @return the string
     */
    String line();

    /**
     * Separator string.
     *
     * @return the string
     */
    String separator();

    @Override
    default String apply(Matrix matrix) {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();
        StringBuilder acc = new StringBuilder(rows * columns);
        acc.append(before());
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= columns; j++) {
                acc.append(matrix.getXY(i, j)).append(j <= (columns - 1) ? " " + separator() + " " : " " + line());
            }
        }
        acc.append(after());
        return acc.toString();
    }
}
