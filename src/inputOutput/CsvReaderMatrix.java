package inputOutput;

import algebra.src.Matrix;

import java.util.HashMap;
import java.util.Map;

public class CsvReaderMatrix {
    private TextIO textIO;

    private HashMap<String, Boolean> delimiters = new HashMap<>(3);

    private String regex;

    private Matrix matrix;

    private String buildRegex() {
        StringBuilder stringBuilder = new StringBuilder(3);
        int index = 0;
        int size = delimiters.size();
        stringBuilder.append("(");
        for (Map.Entry<String, Boolean> stringBooleanEntry : delimiters.entrySet()) {
            stringBuilder.append(stringBooleanEntry.getKey());
            if (index++ < size - 1) {
                stringBuilder.append("|");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public CsvReaderMatrix(String address) {
        super();
        this.delimiters.put("\\t", true);
        this.delimiters.put(",", true);
        this.delimiters.put(";", true);
        this.textIO = new TextIO(address);
        this.regex = buildRegex();
        buildMatrix();
    }

    private void buildMatrix() {
        String text = textIO.getText();
        String[] split = text.split("\n");
        if (split.length <= 0) {
            throw new RuntimeException("There is nothing to parse");
        }
        String[] elements = split[0].split(regex);
        this.matrix = new Matrix(split.length, elements.length);

        for (int i = 0; i < split.length; i++) {
            for (int j = 0; j < elements.length; j++) {
                elements = split[0].split(regex);
                matrix.setXY(i+1,j+1,Double.valueOf(elements[j]));
            }
        }
    }

    public Matrix getMatrix() {
        return matrix;
    }
}
