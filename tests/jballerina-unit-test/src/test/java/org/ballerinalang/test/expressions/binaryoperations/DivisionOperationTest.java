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

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
        Object[] args = { (2000), (50) };
        Object returns = JvmRunUtil.invoke(result, "intDivide", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int divide expression", expectedExceptions = BLangRuntimeException.class)
    public void testIntDivideByZeroExpr() {
        Object[] args = { (2000), (0) };
        JvmRunUtil.invoke(result, "intDivide", args);
    }

    @Test(description = "Test two float divide expression")
    public void testFloatDivideExpr() {
        float a = 8.5f;
        float b = 4.1f;

        double expectedResult = a / b;

        Object[] args = { (a), (b) };
        Object returns = JvmRunUtil.invoke(result, "floatDivide", args);

        Assert.assertSame(returns.getClass(), Double.class);

        double actual = (double) returns;
        Assert.assertEquals(actual, expectedResult, DELTA);
    }

    @Test(description = "Test float by zero")
    public void testFloatDivideByZeroExpr() {
        Object[] args = { (300.0f), (0) };
        Object returns = JvmRunUtil.invoke(result, "floatDivide", args);
        Assert.assertSame(returns.getClass(), Double.class, "Return type of the division is invalid");
        Assert.assertTrue(Double.isInfinite((Double) returns),
                "Result of the division operation is incorrect");
    }

    @Test(description = "Test divide statement with errors")
    public void testDivideStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 12);
        BAssertUtil.validateError(resultNegative, 0, "operator '/' not defined for 'json' and 'json'", 8, 10);
        BAssertUtil.validateError(resultNegative, 1, "operator '/' not defined for 'string' and 'float'", 14, 11);
        BAssertUtil.validateError(resultNegative, 2, "operator '/' not defined for 'C' and 'string'", 28, 14);
        BAssertUtil.validateError(resultNegative, 3, "operator '/' not defined for 'C' and '(float|int)'", 29, 14);
        BAssertUtil.validateError(resultNegative, 4, "operator '/' not defined for 'string' and " +
                "'(string|string:Char)'", 30, 17);
        BAssertUtil.validateError(resultNegative, 5, "operator '/' not defined for 'float' and 'decimal'", 37, 14);
        BAssertUtil.validateError(resultNegative, 6, "operator '/' not defined for 'float' and 'decimal'", 38, 14);
        BAssertUtil.validateError(resultNegative, 7, "operator '/' not defined for 'float' and 'int'", 39, 14);
        BAssertUtil.validateError(resultNegative, 8, "operator '/' not defined for 'decimal' and 'int'", 40, 14);
        BAssertUtil.validateError(resultNegative, 9, "operator '/' not defined for 'int' and 'float'", 41, 18);
        BAssertUtil.validateError(resultNegative, 10, "operator '/' not defined for 'C' and 'float'", 45, 14);
        BAssertUtil.validateError(resultNegative, 11, "operator '/' not defined for 'C' and 'float'", 46, 14);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}NumberOverflow \\{\"message\":\"int range " +
                    "overflow\"\\}.*")
    public void testIntOverflowByDivision() {
        JvmRunUtil.invoke(result, "overflowByDivision");
    }

    @Test(dataProvider = "dataToTestDivisionWithTypes", description = "Test division with types")
    public void testDivisionWithTypes(String functionName) {
        JvmRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestDivisionWithTypes() {
        return new Object[]{
                "testDivisionWithTypes",
                "testDivisionSingleton"
        };
    }

    @Test(description = "Test contextually expected type of numeric literals in division")
    public void testContextuallyExpectedTypeOfNumericLiteralInDivision() {
        JvmRunUtil.invoke(result, "testContextuallyExpectedTypeOfNumericLiteralInDivision");
    }

    @Test(description = "Test division of nullable values")
    public void testDivisionNullable() {
        JvmRunUtil.invoke(result, "testDivisionNullable");
    }
}
