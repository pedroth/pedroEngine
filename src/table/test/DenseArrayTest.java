package table.test;

import org.junit.Assert;
import org.junit.Test;
import table.src.DenseNDArray;

public class DenseArrayTest {

    @Test
    public void denseTest() {
        DenseNDArray<Integer> table = new DenseNDArray(new int[] { 3, 3, 3 });
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    table.set(new int[] { i, j, k }, i + 3 * j  + 9 * k);
                }
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    System.out.println(table.get(new int[] { i, j, k }));
                }
            }
        }
        Assert.assertTrue(table.get("1,:,:").get(new int[] { 0, 0 }) == 1);
        Assert.assertTrue(table.get("1,:,:").get(new int[] { 1, 1 }) == 13);
        Assert.assertTrue(table.get("1,:,:").get(new int[] { 2, 2 }) == 25);
    }

}
