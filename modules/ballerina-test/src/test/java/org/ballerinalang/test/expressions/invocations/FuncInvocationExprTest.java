/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.expressions.invocations;


import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Local function invocation test.
 *
 * @since 0.8.0
 */
public class FuncInvocationExprTest {

    CompileResult funcInvocationExpResult;

    @BeforeClass
    public void setup() {
        funcInvocationExpResult = BTestUtils.compile("test-src/expressions/invocations/function-Invocation-expr.bal");
    }

    @Test
    public void invokeFunctionWithParams() {
        BValue[] args = new BValue[]{new BInteger(1), new BInteger(2)};
        BValue[] values = BTestUtils.invoke(funcInvocationExpResult, "add", args);
        Assert.assertEquals(values.length, 1);
        Assert.assertTrue(values[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) values[0]).intValue(), 3);
    }

    @Test(description = "Test local function invocation expression")
    public void testFuncInvocationExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5), new BInteger(1)};
        BValue[] returns = BTestUtils.invoke(funcInvocationExpResult, "testFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 116;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test recursive function invocation")
    public void testFuncInvocationExprRecursive() {
        BValue[] args = {new BInteger(7)};
        BValue[] returns = BTestUtils.invoke(funcInvocationExpResult, "sum", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 28;
        Assert.assertEquals(actual, expected);

    }

    @Test(description = "Test local function invocation expression advanced")
    public void testFuncInvocationExprAdvanced() {
        BValue[] args = {new BInteger(100), new BInteger(5), new BInteger(1)};
        BValue[] returns = BTestUtils.invoke(funcInvocationExpResult, "funcInvocationWithinFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 322;
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testNativeInvocation() {
        BValue[] args = {new BFloat(2), new BFloat(2)};
        BValue[] returns = BTestUtils.invoke(funcInvocationExpResult, "getPowerOfN", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 4;
        Assert.assertEquals(actual, expected);
    }
}
