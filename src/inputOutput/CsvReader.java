package inputOutput;

import table.src.HyperTable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The type Csv reader.
 */
public class CsvReader extends HyperTable<Integer, String> {
    private TextIO textIO;
    private int[] size = new int[]{0, 0};
    private HashMap<String, Boolean> delimiters = new HashMap<>(3);
    private String regex;

    /**
     * Instantiates a new Csv reader.
     */
    public CsvReader() {
        super(2);
        this.delimiters.put("\\t", true);
        this.delimiters.put(",", true);
        this.delimiters.put(";", true);
        this.textIO = new TextIO();
        this.regex = buildRegex();
    }

    /**
     * Read void.
     *
     * @param address the address
     */
    public void read(String address) throws IOException {
        textIO.read(address);
        String text = textIO.getText();
        String[] split = text.split("\n");
        for (int i = 0; i < split.length; i++) {
            String[] elements = split[i].split(regex);
            for (int j = 0; j < elements.length; j++) {
                Integer[] index = {i, j};
                this.set(index, elements[j]);
            }
        }
        size[0] = split.length;
        size[1] = split[0].split(regex).length;
    }

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

    /**
     * Add delimiter. Special characters should be put with an extra left slash e.g \t -> \\t
     * @param delimiter the delimiter
     */
    public void addDelimiter(String delimiter) {
        delimiters.put(delimiter, true);
    }

    /**
     * Remove delimiter.
     * @param delimiter the delimiter
     */
    public void removeDelimiter(String delimiter) {
        delimiters.remove(delimiter);
    }

    /**
     * Get size.
     *
     * @return the int [ ]
     */
    public int[] getSize() {
        return size;
    }

    public <O> O map(Function<CsvReader, O> function) {
        return function.apply(this);
    }
}
