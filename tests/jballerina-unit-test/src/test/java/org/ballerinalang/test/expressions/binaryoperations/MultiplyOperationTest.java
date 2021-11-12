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
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of multiply operator.
 */
public class MultiplyOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/multiply-operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/multiply-operation-negative.bal");
    }

    @Test(description = "Test two int multiply expression")
    public void testIntMultiplyExpr() {
        BValue[] args = { new BInteger(4611686018427387904L), new BInteger(-2L) };
        BValue[] returns = BRunUtil.invoke(result, "intMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = -9223372036854775808L;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float multiply expression")
    public void testFloatMultiplyExpr() {
        BValue[] args = { new BFloat(40.0f), new BFloat(40.0f) };
        BValue[] returns = BRunUtil.invoke(result, "floatMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 1600.0f;
        Assert.assertEquals(actual, expected);
    }

    @Test(dataProvider = "dataToTestMultiplicationWithTypes", description = "Test multiplication with types")
    public void testMultiplicationWithTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestMultiplicationWithTypes() {
        return new Object[]{
                "testMultiplicationWithTypes",
                "testMultiplySingleton"
        };
    }

    @Test(description = "Test contextually expected type of numeric literals in multiplication")
    public void testContextuallyExpectedTypeOfNumericLiteralInMultiply() {
        BRunUtil.invoke(result, "testContextuallyExpectedTypeOfNumericLiteralInMultiply");
    }

    @Test(description = "Test binary statement with errors")
    public void testMultiplyStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        BAssertUtil.validateError(resultNegative, 0, "operator '*' not defined for 'json' and 'json'", 8, 10);
        BAssertUtil.validateError(resultNegative, 1, "operator '*' not defined for 'float' and 'string'", 14, 9);
        BAssertUtil.validateError(resultNegative, 2, "operator '*' not defined for 'C' and 'string'", 28, 14);
        BAssertUtil.validateError(resultNegative, 3, "operator '*' not defined for 'C' and '(float|int)'", 29, 14);
        BAssertUtil.validateError(resultNegative, 4, "operator '*' not defined for 'string' and " +
                "'(string|string:Char)'", 30, 17);
        BAssertUtil.validateError(resultNegative, 5, "operator '*' not defined for 'float' and 'decimal'", 37, 14);
        BAssertUtil.validateError(resultNegative, 6, "operator '*' not defined for 'float' and 'decimal'", 38, 14);
        BAssertUtil.validateError(resultNegative, 7, "operator '*' not defined for 'float' and 'int'", 39, 14);
        BAssertUtil.validateError(resultNegative, 8, "operator '*' not defined for 'decimal' and 'int'", 40, 14);
        BAssertUtil.validateError(resultNegative, 9, "operator '*' not defined for 'int' and 'float'", 41, 18);
        BAssertUtil.validateError(resultNegative, 10, "operator '*' not defined for 'C' and 'float'", 45, 14);
        BAssertUtil.validateError(resultNegative, 11, "operator '*' not defined for 'C' and 'float'", 46, 14);
        BAssertUtil.validateError(resultNegative, 12, "Integer '-9223372036854775808' too small", 51, 16);
    }

    @Test(description = "Test multiplication of nullable values")
    public void testMultiplyNullable() {
        BRunUtil.invoke(result, "testMultiplyNullable");
    }
}
