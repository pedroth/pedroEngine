package inputOutput;

import table.src.HyperTable;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Csv reader.
 */
public class CsvReader extends HyperTable<Integer, String> {
    private MyText textReader;
    private HashMap<String, Boolean> delimiters = new HashMap<>(3);

    /**
     * Instantiates a new Csv reader.
     */
    public CsvReader() {
        super(2);
        this.delimiters.put("\\t", true);
        this.delimiters.put(",", true);
        this.delimiters.put(";", true);
        this.textReader = new MyText();
    }

    /**
     * Read void.
     *
     * @param address the address
     */
    public void read(String address) {
        textReader.read(address);
        String text = textReader.getText();
        String[] split = text.split("\n");
        String regex = buildRegex();
        for (int i = 0; i < split.length; i++) {
            String[] elements = split[i].split(regex);
            for (int j = 0; j < elements.length; j++) {
                Integer[] index = {i, j};
                this.set(index, elements[j]);
            }
        }
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
     */
    public void addDelimiter(String delimiter) {
        delimiters.put(delimiter, true);
    }

    /**
     * Remove delimiter.
     */
    public void removeDelimiter(String delimiter) {
        delimiters.remove(delimiter);
    }
}
