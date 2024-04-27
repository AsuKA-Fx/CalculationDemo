package com.fx;


import org.junit.Assert;
import org.junit.Test;

/**
 * ClassName: CalculationTest
 * Package: com.fx
 * Description:
 * Create: 2024/4/25 - 18:13
 *
 * @author : Fang-
 * @version : v1.0
 */
public class CalculationTest {
    @Test
    public void nullExpressionExceptionTest() {
        Exception exception = Assert.assertThrows(Exception.class, () -> Calculation.doCalculate(null));
        Assert.assertEquals("expression invalid", exception.getMessage());
    }

    @Test
    public void divideZeroExpressionExceptionTest() {
        Exception exception = Assert.assertThrows(ArithmeticException.class, () -> Calculation.doCalculate("1+2/0"));
        Assert.assertEquals("/ by zero", exception.getMessage());
    }

    @Test
    public void missingNumericExpressionExceptionTest() {
        Exception exception = Assert.assertThrows(Exception.class, () -> Calculation.doCalculate("1+2+"));
        Assert.assertEquals("expression invalid", exception.getMessage());
    }

    @Test
    public void simpleTest() throws Exception {
        int expressResult = Calculation.doCalculate("1+2+3");
        Assert.assertEquals(6, expressResult);
    }

    @Test
    public void expressTest() throws Exception {
        int expressResult = Calculation.doCalculate("1+2*3+4/2-3");
        Assert.assertEquals(6, expressResult);
    }

    // TODO: 更多的测试案例
}
