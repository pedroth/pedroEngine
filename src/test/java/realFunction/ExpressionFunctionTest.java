package realFunction;

import algebra.Vector;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionFunctionTest {

    @Test
    public void test1() {
        ExpressionFunction expressionFunction = new ExpressionFunction("(5 * 4 + 91 * 90 + 5 * 4 + 34 * 33 + 3 * 2 + 5 * 4) / 2", new String[0]);
        expressionFunction.init();
        Assert.assertTrue(expressionFunction.compute(new Vector(1))== 4689.0);
    }
}
