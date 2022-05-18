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
        Object[] args = { (4611686018427387904L), (-2L) };
        Object returns = BRunUtil.invoke(result, "intMultiply", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = -9223372036854775808L;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int multiply overflow expression", expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina}NumberOverflow \\{\"message\":\"int range " +
                    "overflow\"\\}.*")
    public void testIntOverflowByMultiplication() {
        BRunUtil.invoke(result, "overflowByMultiplication");
    }

    @Test(description = "Test two float multiply expression")
    public void testFloatMultiplyExpr() {
        Object[] args = { (40.0f), (40.0f) };
        Object returns = BRunUtil.invoke(result, "floatMultiply", args);

        Assert.assertSame(returns.getClass(), Double.class);

        double actual = (double) returns;
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
        Assert.assertEquals(resultNegative.getErrorCount(), 8);
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'json' and 'json'", 8, 10);
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'float' and 'string'", 14, 9);
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'C' and 'string'", 28, 14);
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'C' and '(float|int)'", 29, 14);
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'string' and " +
                "'(string|string:Char)'", 30, 17);
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'float' and 'decimal'", 37, 14);
        BAssertUtil.validateError(resultNegative, i++, "operator '*' not defined for 'float' and 'decimal'", 38, 14);
        BAssertUtil.validateError(resultNegative, i++, "'9223372036854775808' is out of range " +
                "for 'int'", 51, 17);
    }

    @Test(description = "Test multiplication of nullable values")
    public void testMultiplyNullable() {
        BRunUtil.invoke(result, "testMultiplyNullable");
    }

    @Test(dataProvider = "dataToTestMultiplyFloatInt", description = "Test multiplication float with int")
    public void testMultiplyFloatInt(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestMultiplyFloatInt() {
        return new Object[]{
                "testMultiplyFloatInt",
                "testMultiplyFloatIntSubTypes",
                "testMultiplyFloatIntWithNullableOperands",
                "testMultiplyFloatIntSubTypeWithNullableOperands",
                "testResultTypeOfMultiplyFloatIntByInfering",
                "testResultTypeOfMultiplyFloatIntForNilableOperandsByInfering",
                "testMultiplyFloatIntToInfinityAndNaN"
        };
    }

    @Test(dataProvider = "dataToTestMultiplyDecimalInt", description = "Test multiplication decimal with int")
    public void testMultiplyDecimalInt(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestMultiplyDecimalInt() {
        return new Object[]{
                "testMultiplyDecimalInt",
                "testMultiplyDecimalIntSubTypes",
                "testMultiplyDecimalIntWithNullableOperands",
                "testMultiplyDecimalIntSubTypeWithNullableOperands",
                "testResultTypeOfMultiplyDecimalIntByInfering",
                "testResultTypeOfMultiplyDecimalIntForNilableOperandsByInfering",
        };
    }

    @Test(dataProvider = "dataToTestShortCircuitingInMultiplication")
    public void testShortCircuitingInMultiplication(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestShortCircuitingInMultiplication() {
        return new Object[]{
                "testNoShortCircuitingInMultiplicationWithNullable",
                "testNoShortCircuitingInMultiplicationWithNonNullable"
        };
    }
}
