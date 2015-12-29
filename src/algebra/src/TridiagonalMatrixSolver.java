package algebra.src;


public class TridiagonalMatrixSolver {
    /**
     * @param m a tridiagonal matrix
     * @param y
     * @return null if matrix is not square, else the solution to M * x = y
     */
    public static Vector solveTridiagonalSystem(Matrix m, Vector y) {
        int rows = m.getRows();
        if (m.getColumns() != rows || y.getDim() != m.getColumns())
            return null;

        Vector x = y.copy();

        //defensive copy
        Matrix matrix = m.copy();

        for (int i = 1; i <= rows - 1; i++) {
            matrix.setXY(i + 1, i + 1, matrix.getXY(i, i) * matrix.getXY(i + 1, i + 1) - matrix.getXY(i, i + 1) * matrix.getXY(i + 1, i));

            if (i < rows - 1)
                matrix.setXY(i + 1, i + 2, matrix.getXY(i, i) * matrix.getXY(i + 1, i + 2));

            x.setX(i + 1, matrix.getXY(i, i) * x.getX(i + 1) - matrix.getXY(i + 1, i) * x.getX(i));
        }

        for (int i = rows; i >= 2; i--) {
            x.setX(i, x.getX(i) / matrix.getXY(i, i));
            x.setX(i - 1, x.getX(i - 1) - matrix.getXY(i, i - 1) * x.getX(i));
        }

        x.setX(1, x.getX(1) / matrix.getXY(1,1));

        return x;
    }
}
