/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of division operation.
 */
public class DivisionOperationTest {

    CompileResult result;
    CompileResult resultNegative;
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/division-operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/division-operation-negative.bal");
    }

    @Test(description = "Test two int divide expression")
    public void testIntDivideExpr() {
        BValue[] args = { new BInteger(2000), new BInteger(50) };
        BValue[] returns = BRunUtil.invoke(result, "intDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int divide expression", expectedExceptions = BLangRuntimeException.class)
    public void testIntDivideByZeroExpr() {
        BValue[] args = { new BInteger(2000), new BInteger(0) };
        BRunUtil.invoke(result, "intDivide", args);
    }

    @Test(description = "Test two float divide expression")
    public void testFloatDivideExpr() {
        float a = 8.5f;
        float b = 4.1f;

        double expectedResult = a / b;

        BValue[] args = { new BFloat(a), new BFloat(b) };
        BValue[] returns = BRunUtil.invoke(result, "floatDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expectedResult, DELTA);
    }

    @Test(description = "Test integer division by float")
    public void testIntDivideByFloat() {
        int a = Integer.MAX_VALUE;
        double b = 1.23456789d;

        double expectedResult = a / b;

        BValue[] args = { new BInteger(a), new BFloat(b) };
        BValue[] returns = BRunUtil.invoke(result, "intDivideByFloat", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");

        double actualResult = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actualResult, expectedResult, DELTA, "Result of the division operation is incorrect");
    }

    @Test(description = "Test float number division by integer")
    public void testFloatDivideByInt() {
        double a = Float.MAX_VALUE;
        int b = 123456789;

        double expectedResult = a / b;

        BValue[] args = { new BFloat(a), new BInteger(b) };
        BValue[] returns = BRunUtil.invoke(result, "floatDivideByInt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");

        double actualResult = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actualResult, expectedResult, DELTA, "Result of the division operation is incorrect");
    }

    @Test(description = "Test float by zero")
    public void testFloatDivideByZeroExpr() {
        BValue[] args = { new BFloat(300.0f), new BFloat(0) };
        BValue[] returns = BRunUtil.invoke(result, "floatDivide", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");
        Assert.assertTrue(Double.isInfinite(((BFloat) returns[0]).floatValue()),
                "Result of the division operation is incorrect");
    }

    @Test(description = "Test devide statement with errors")
    public void testDivideStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "operator '/' not defined for 'json' and 'json'", 8, 10);
        BAssertUtil.validateError(resultNegative, 1, "operator '/' not defined for 'string' and 'float'", 14, 11);
    }

    @Test
    public void testIntDivisionFloat() {
        BValue[] args = {new BInteger(110), new BFloat(22L)};
        BValue[] returns = BRunUtil.invoke(result, "intDivideByFloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "5.0";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatDivisionInt() {
        BValue[] args = {new BFloat(110f), new BInteger(22)};
        BValue[] returns = BRunUtil.invoke(result, "floatDivideByInt", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "5.0";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}NumberOverflow \\{\"message\":\" int range " +
                    "overflow\"\\}.*")
    public void testIntOverflowByDivision() {
        BRunUtil.invoke(result, "overflowByDivision");
    }
}
