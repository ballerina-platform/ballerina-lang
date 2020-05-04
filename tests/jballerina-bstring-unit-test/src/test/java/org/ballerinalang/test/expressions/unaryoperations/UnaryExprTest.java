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
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

    @Test(description = "Test uanry statement with errors")
    public void testUnaryStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 3);
        BAssertUtil.validateError(resultNegative, 0, "operator '+' not defined for 'json'", 5, 10);
        BAssertUtil.validateError(resultNegative, 1, "operator '-' not defined for 'json'", 14, 10);
        BAssertUtil.validateError(resultNegative, 2, "operator '!' not defined for 'json'", 23, 10);
    }
}
