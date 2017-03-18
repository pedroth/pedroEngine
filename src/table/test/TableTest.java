package table.test;

import inputOutput.CsvReader;
import org.junit.Assert;
import org.junit.Test;

public class TableTest {

    @Test
    public void testTable() {
        CsvReader table = new CsvReader();
        table.read("src/numeric/test/resource/testData.csv");
        System.out.println(table);
        Assert.assertTrue(3.768 == Double.valueOf(table.get(new Integer[] { 14, 1 })));
    }
}
