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
package org.ballerinalang.test.expressions.unaryoperations;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of unary operator.
 */
public class UnaryExprTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/unaryoperations/unary-operation.bal");
        resultNegative = BCompileUtil.compile(
                "test-src/expressions/unaryoperations/unary-operation-negative.bal");
    }

    @Test(description = "Test unary negative expression")
    public void integerUnaryExprTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "negativeIntTest", args);

        Assert.assertEquals(returns.length, 2);

        BInteger x = (BInteger) returns[0];
        Assert.assertSame(x.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(x.intValue(), (-5), "Invalid value returned.");

        BInteger y = (BInteger) returns[1];
        Assert.assertSame(y.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(y.intValue(), 5, "Invalid value returned.");
    }

    @Test(description = "Test int positive unary expression")
    public void positiveIntegerUnaryExprTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "positiveIntTest", args);

        Assert.assertEquals(returns.length, 2);

        BInteger x = (BInteger) returns[0];
        Assert.assertSame(x.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(x.intValue(), (+5), "Invalid value returned.");

        BInteger y = (BInteger) returns[1];
        Assert.assertSame(y.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(y.intValue(), +5, "Invalid value returned.");
    }

    @Test(description = "Test float unary negative expression")
    public void floatUnaryExprTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "negativeFloatTest", args);

        Assert.assertEquals(returns.length, 2);

        BFloat x = (BFloat) returns[0];
        Assert.assertSame(x.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(x.floatValue(), -5.0D, "Invalid value returned.");

        BFloat y = (BFloat) returns[1];
        Assert.assertSame(y.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(y.floatValue(), 5.0D, "Invalid value returned.");
    }

    @Test(description = "Test float positive unary expression")
    public void positiveFloatUnaryExprTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "positiveFloatTest", args);

        Assert.assertEquals(returns.length, 2);

        BFloat x = (BFloat) returns[0];
        Assert.assertSame(x.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(x.floatValue(), +5D, "Invalid value returned.");

        BFloat y = (BFloat) returns[1];
        Assert.assertSame(y.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(y.floatValue(), +5D, "Invalid value returned.");
    }

    @Test(description = "Test unary boolean not expression")
    public void booleanUnaryExprTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "booleanNotTest", args);

        Assert.assertEquals(returns.length, 3);

        BBoolean x = (BBoolean) returns[0];
        Assert.assertSame(x.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertFalse(x.booleanValue(), "Invalid value returned.");

        BBoolean y = (BBoolean) returns[1];
        Assert.assertSame(y.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertTrue(y.booleanValue(), "Invalid value returned.");

        BBoolean z = (BBoolean) returns[2];
        Assert.assertSame(z.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertTrue(z.booleanValue(), "Invalid value returned.");
    }

    @Test(description = "Test unary boolean not expression in if else")
    public void unaryExprInIfConditionTest() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "unaryExprInIfConditionTest", args);

        Assert.assertEquals(returns.length, 1);

        BBoolean x = (BBoolean) returns[0];
        Assert.assertSame(x.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertTrue(x.booleanValue(), "Invalid value returned.");
    }

    @Test(description = "Test unary negation expression")
    public void unaryNegationTest() {
        long a = 3;
        long b = 2;

        long expectedResult = a - -b;

        BValue[] args = {new BInteger(a), new BInteger(b)};

        BValue[] returns = BRunUtil.invoke(result, "unaryNegationTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Invalid class type returned.");

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult);

    }

    @Test(description = "Test unary positive negation expression")
    public void unaryPositiveNegationTest() {
        long a = 3;

        long expectedResult = +-a;

        BValue[] args = {new BInteger(a)};

        BValue[] returns = BRunUtil.invoke(result, "unaryPositiveNegationTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Invalid class type returned.");

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult);

    }

    @Test(dataProvider = "dataToTestUnaryOperations", description = "test unary operators with types")
    public void testUnaryOperations(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "dataToTestUnaryOperations")
    public Object[] dataToTestUnaryOperations() {
        return new String[] {
                "testComplementOperator",
                "testUnaryOperationsWithIntSubtypes",
                "testUnaryOperationsWithNonBasicTypes"
        };
    }

    @Test(description = "Test unary operators for nullable expressions")
    public void testNullableUnaryExpressions() {
        BRunUtil.invoke(result, "testNullableUnaryExpressions");
    }

    @Test(description = "Test unary operators with user defined subtypes")
    public void testUnaryOperationsWithUserDefinedTypes() {
        BRunUtil.invoke(result, "testUnaryOperationsWithUserDefinedTypes");
    }

    @Test(description = "Test resulting type of unary plus")
    public void testResultingTypeOfUnaryPlus() {
        BRunUtil.invoke(result, "testResultingTypeOfUnaryPlus");
    }

    @Test(description = "Test unary statement with errors")
    public void testUnaryStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 19);
        BAssertUtil.validateError(resultNegative, 0, "operator '+' not defined for 'json'", 5, 10);
        BAssertUtil.validateError(resultNegative, 1, "operator '-' not defined for 'json'", 14, 10);
        BAssertUtil.validateError(resultNegative, 2, "operator '!' not defined for 'json'", 23, 10);
        BAssertUtil.validateError(resultNegative, 3, "operator '!' not defined for 'int'", 29, 13);
        BAssertUtil.validateError(resultNegative, 4, "incompatible types: expected 'int:Unsigned8', found 'int'",
                 34, 24);
        BAssertUtil.validateError(resultNegative, 5, "incompatible types: expected 'int:Signed8', found 'int'",
                35, 22);
        BAssertUtil.validateError(resultNegative, 6, "incompatible types: expected 'byte', found 'int'",
                38, 15);
        BAssertUtil.validateError(resultNegative, 7, "incompatible types: expected 'int:Unsigned8', found 'int'",
                41, 24);
        BAssertUtil.validateError(resultNegative, 8, "operator '~' not defined for 'decimal'", 45, 18);
        BAssertUtil.validateError(resultNegative, 9, "operator '~' not defined for 'float'", 46, 17);
        BAssertUtil.validateError(resultNegative, 10, "operator '~' not defined for 'decimal'", 47, 18);
        BAssertUtil.validateError(resultNegative, 11, "operator '!' not defined for 'decimal'", 48, 18);
        BAssertUtil.validateError(resultNegative, 12, "incompatible types: expected 'A', found 'int'",
                56, 11);
        BAssertUtil.validateError(resultNegative, 13, "incompatible types: expected 'B', found 'float'",
                59, 11);
        BAssertUtil.validateError(resultNegative, 14, "operator '-' not defined for 'C'", 74, 13);
        BAssertUtil.validateError(resultNegative, 15, "operator '-' not defined for 'D'", 77, 13);
        BAssertUtil.validateError(resultNegative, 16, "operator '+' not defined for '(decimal|DecimalType1)'", 80, 24);
        BAssertUtil.validateError(resultNegative, 17, "operator '-' not defined for 'DecimalType1'", 83, 24);
        BAssertUtil.validateError(resultNegative, 18, "operator '+' not defined for '(decimal|DecimalType2)'", 86, 24);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        resultNegative = null;
    }
}
