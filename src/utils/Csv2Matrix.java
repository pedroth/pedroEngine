package utils;

import algebra.src.Matrix;
import inputOutput.CsvReader;

import java.util.function.Function;

public class Csv2Matrix implements Function<CsvReader, Matrix> {
    private static Csv2Matrix instance = new Csv2Matrix();

    private Csv2Matrix() {
    }

    public static Csv2Matrix getInstance() {
        return instance;
    }

    @Override
    public Matrix apply(CsvReader csvReader) {
        int[] size = csvReader.getSize();
        Matrix matrix = new Matrix(size[0], size[1]);
        for (int i = 0; i < size[0]; i++) {
            for (int j = 0; j < size[1]; j++) {
                matrix.setXY(i + 1, j + 1, Double.valueOf(csvReader.get(new Integer[]{i, j})));
            }
        }
        return matrix;
    }
}
