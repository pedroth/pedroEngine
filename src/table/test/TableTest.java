package table.test;

import inputOutput.CsvReader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Pedroth on 4/30/2016.
 */
public class TableTest {

    @Test
    public void testTable() {
        CsvReader table = new CsvReader();
        table.read("src/numeric/test/resource/testData.csv");
        System.out.println(table);
        Assert.assertTrue(3.768 == Double.valueOf(table.get(new Integer[] { 14, 1 })));
    }
}
