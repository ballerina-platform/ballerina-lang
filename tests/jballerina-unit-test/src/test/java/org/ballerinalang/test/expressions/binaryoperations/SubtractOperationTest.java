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
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of subtract operator.
 */
public class SubtractOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/subtract-operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/subtract-operation-negative.bal");
    }

    @Test(description = "Test two int subtract expression")
    public void testIntAddExpr() {
        Object[] args = { (1234567891011L), (9876543211110L)};

        Object returns = BRunUtil.invoke(result, "intSubtract", args);
        Assert.assertSame(returns.getClass(), Long.class);
        long actual = (long) returns;
        long expected = -8641975320099L;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int subtract overflow expression", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}NumberOverflow \\{\"message\":\"int range " +
                    "overflow\"\\}.*")
    public void testIntOverflowBySubtraction() {
        BRunUtil.invoke(result, "overflowBySubtraction");
    }

    @Test(description = "Test two float subtract expression")
    public void testFloatAddExpr() {
        Object[] args = { (100.0f), (200.0f)};

        Object returns = BRunUtil.invoke(result, "floatSubtract", args);
        Assert.assertSame(returns.getClass(), Double.class);
        double actual = (double) returns;
        double expected = -100.0f;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test subtracting negative values")
    public void testNegativeValues() {
        int a = -10;
        int b = -20;
        Object[] args = {(a), (b)};
        // Subtract
        long expectedResult = a - b;
        Object returns = BRunUtil.invoke(result, "intSubtract", args);
        Assert.assertSame(returns.getClass(), Long.class);
        long actualResult = (long) returns;
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(dataProvider = "dataToTestSubtractionWithTypes", description = "Test subtraction with types")
    public void testSubtractionWithTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestSubtractionWithTypes() {
        return new Object[]{
                "testSubtractionWithTypes",
                "testSubtractSingleton"
        };
    }

    @Test(description = "Test contextually expected type of numeric literals in subtraction")
    public void testContextuallyExpectedTypeOfNumericLiteralInSubtract() {
        BRunUtil.invoke(result, "testContextuallyExpectedTypeOfNumericLiteralInSubtract");
    }

    @Test(dataProvider = "dataToTestShortCircuitingInSubtraction")
    public void testShortCircuitingInSubtraction(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestShortCircuitingInSubtraction() {
        return new Object[]{
                "testNoShortCircuitingInSubtractionWithNullable",
                "testNoShortCircuitingInSubtractionWithNonNullable"
        };
    }

    @Test(description = "Test subtract statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        BAssertUtil.validateError(resultNegative, 0, "operator '-' not defined for 'float' and 'string'", 4, 9);
        BAssertUtil.validateError(resultNegative, 1, "operator '-' not defined for 'json' and 'json'", 14, 10);
        BAssertUtil.validateError(resultNegative, 2, "operator '-' not defined for 'C' and 'string'", 28, 14);
        BAssertUtil.validateError(resultNegative, 3, "operator '-' not defined for 'C' and '(float|int)'", 29, 14);
        BAssertUtil.validateError(resultNegative, 4, "operator '-' not defined for 'string' and " +
                "'(string|string:Char)'", 30, 17);
        BAssertUtil.validateError(resultNegative, 5, "operator '-' not defined for 'float' and 'decimal'", 37, 14);
        BAssertUtil.validateError(resultNegative, 6, "operator '-' not defined for 'float' and 'decimal'", 38, 14);
        BAssertUtil.validateError(resultNegative, 7, "operator '-' not defined for 'float' and 'int'", 39, 14);
        BAssertUtil.validateError(resultNegative, 8, "operator '-' not defined for 'decimal' and 'int'", 40, 14);
        BAssertUtil.validateError(resultNegative, 9, "operator '-' not defined for 'int' and 'float'", 41, 18);
        BAssertUtil.validateError(resultNegative, 10, "operator '-' not defined for 'C' and 'float'", 45, 14);
        BAssertUtil.validateError(resultNegative, 11, "operator '-' not defined for 'C' and 'float'", 46, 14);
        BAssertUtil.validateError(resultNegative, 12, "'9223372036854775808' is out of range " +
                "for 'int'", 50, 17);
    }

    @Test(description = "Test subtraction of nullable values")
    public void testSubNullable() {
        BRunUtil.invoke(result, "testSubNullable");
    }
}
