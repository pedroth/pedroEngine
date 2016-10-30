package realFunction.test;

import algebra.src.Vector;
import org.junit.Test;
import realFunction.src.ExpressionFunction;

public class ExpressionFunctionTest {

    @Test
    public void test1() {
        ExpressionFunction expressionFunction = new ExpressionFunction("(5 * 4 + 91 * 90 + 5 * 4 + 34 * 33 + 3 * 2 + 5 * 4) / 2", new String[0]);
        expressionFunction.init();
        System.out.println(expressionFunction.compute(new Vector(1)));
    }
}
