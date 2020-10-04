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
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
        BValue[] args = { new BInteger(100), new BInteger(200)};

        BValue[]  returns = BRunUtil.invoke(result, "intSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = -100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float subtract expression")
    public void testFloatAddExpr() {
        BValue[] args = { new BFloat(100.0f), new BFloat(200.0f)};

        BValue[] returns = BRunUtil.invoke(result, "floatSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        double expected = -100.0f;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test subtracting negative values")
    public void testNegativeValues() {
        int a = -10;
        int b = -20;
        BValue[] args = {new BInteger(a), new BInteger(b)};
        // Subtract
        long expectedResult = a - b;
        BValue[] returns = BRunUtil.invoke(result, "intSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actualResult = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test int float subtract expression")
    public void testIntFloatSubtractExpr() {
        int a = 10;
        float b = 1.5f;
        BValue[] args = { new BInteger(a), new BFloat(b)};

        BValue[] returns = BRunUtil.invoke(result, "intFloatSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        double expected = a - b;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test float int subtract expression")
    public void testFloatIntSubtractExpr() {
        float a = 10.5f;
        int b = 1;
        BValue[] args = { new BFloat(a), new BInteger(b)};

        BValue[] returns = BRunUtil.invoke(result, "floatIntSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        double expected = a - b;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test substract statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "operator '-' not defined for 'int' and 'string'", 4, 9);
        BAssertUtil.validateError(resultNegative, 1, "operator '-' not defined for 'json' and 'json'", 14, 10);
    }
}
