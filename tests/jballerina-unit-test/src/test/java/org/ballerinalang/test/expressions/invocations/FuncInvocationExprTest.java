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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Local function invocation test.
 *
 * @since 0.8.0
 */
public class FuncInvocationExprTest {

    private CompileResult funcInvocationExpResult;
    private CompileResult funcInvocationNegative;
    private CompileResult methodInvocationNegative;

    @BeforeClass
    public void setup() {
        funcInvocationExpResult = BCompileUtil.compile("test-src/expressions/invocations/function-invocation-expr.bal");
        funcInvocationNegative = BCompileUtil.compile("test-src/expressions/invocations/function_call_negative.bal");
        methodInvocationNegative = BCompileUtil.compile("test-src/expressions/invocations/method_call_negative.bal");
    }

    @Test(enabled = false)
    public void invokeFunctionWithParams() {
        BValue[] args = new BValue[]{new BInteger(1), new BInteger(2)};
        BValue[] values = BRunUtil.invoke(funcInvocationExpResult, "add", args);
        Assert.assertEquals(values.length, 1);
        Assert.assertTrue(values[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) values[0]).intValue(), 3);
    }

    @Test(enabled = false, description = "Test local function invocation expression")
    public void testFuncInvocationExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5), new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(funcInvocationExpResult, "testFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 116;
        Assert.assertEquals(actual, expected);
    }

    @Test(enabled = false, description = "Test recursive function invocation")
    public void testFuncInvocationExprRecursive() {
        BValue[] args = {new BInteger(7)};
        BValue[] returns = BRunUtil.invoke(funcInvocationExpResult, "sum", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 28;
        Assert.assertEquals(actual, expected);

    }

    @Test(enabled = false, description = "Test local function invocation expression advanced")
    public void testFuncInvocationExprAdvanced() {
        BValue[] args = {new BInteger(100), new BInteger(5), new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(funcInvocationExpResult, "funcInvocationWithinFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 322;
        Assert.assertEquals(actual, expected);
    }

    @Test(enabled = false)
    public void testReturnFuncInvocationWithinFuncInvocation() {
        BValue[] args = {new BInteger(2), new BInteger(3)};
        BValue[] returns =
                BRunUtil.invoke(funcInvocationExpResult, "testReturnFuncInvocationWithinFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 8;
        Assert.assertEquals(actual, expected);
    }

    @Test(enabled = false)
    public void testReturnNativeFuncInvocationWithinNativeFuncInvocation() {
        BValue[] args = {new BFloat(2)};
        BValue[] returns = BRunUtil.invoke(funcInvocationExpResult,
                                             "testReturnNativeFuncInvocationWithinNativeFuncInvocation", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(enabled = false)
    public void testNativeInvocation() {
        BValue[] args = {new BFloat(2), new BFloat(2)};
        BValue[] returns = BRunUtil.invoke(funcInvocationExpResult, "getPowerOfN", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 4;
        Assert.assertEquals(actual, expected);
    }

    @Test(enabled = false)
    public void testInvocationWithArgVarargMix() {
        BRunUtil.invoke(funcInvocationExpResult, "testInvocationWithArgVarargMix");
    }

    @Test(enabled = false, groups = { "brokenOnNewParser" })
    public void testFunctionCallNegativeCases() {
        int i = 0;
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'int', found 'string'", 3, 16);
        validateError(funcInvocationNegative, i++, "undefined function 'foo'", 11, 5);
        validateError(funcInvocationNegative, i++, "function 'testMyFunc' can not have private visibility", 14, 1);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'string', found 'int'", 22, 16);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'boolean[]', found '[int,boolean," +
                "boolean]'", 22, 26);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'boolean', found 'string'", 24, 24);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'boolean', found 'int'", 26, 27);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'boolean', found 'string'", 26, 30);
        validateError(funcInvocationNegative, i++, "variable assignment is required", 28, 5);
        validateError(funcInvocationNegative, i++, "incompatible types: expected '[int,boolean...]', found '[float," +
                "string...]'", 28, 12);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'string', found 'int'", 31, 9);
        validateError(funcInvocationNegative, i++, "incompatible types: expected '[float,boolean...]', found '[int," +
                "boolean,boolean]'", 31, 15);
        validateError(funcInvocationNegative, i++, "incompatible types: expected '[string,float,boolean...]', found " +
                "'[float,string...]'", 33, 12);
        validateError(funcInvocationNegative, i++, "rest argument not allowed after named arguments", 39, 20);
        validateError(funcInvocationNegative, i++, "incompatible types: expected '[float,boolean...]', found " +
                "'boolean[]'", 41, 23);
        validateError(funcInvocationNegative, i++, "incompatible types: expected 'boolean', found 'float'", 47, 20);
        Assert.assertEquals(i,  funcInvocationNegative.getErrorCount());
    }

    @Test(enabled = false)
    public void testMethodCallNegativeCases() {
        int i = 0;
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'string', found 'int'", 23, 16);
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'boolean[]', found '[int,boolean," +
                "boolean]'", 23, 28);
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'boolean', found 'string'", 25, 26);
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'boolean', found 'int'", 27, 29);
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'boolean', found 'string'", 27, 32);
        validateError(methodInvocationNegative, i++, "variable assignment is required", 29, 5);
        validateError(methodInvocationNegative, i++, "incompatible types: expected '[int,boolean...]', found '[float," +
                "string...]'", 29, 14);
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'string', found 'int'", 32, 12);
        validateError(methodInvocationNegative, i++, "incompatible types: expected '[float,boolean...]', found '[int," +
                "boolean,boolean]'", 32, 18);
        validateError(methodInvocationNegative, i++, "incompatible types: expected '[string,float,boolean...]', " +
                "found '[float,string...]'", 34, 15);
        validateError(methodInvocationNegative, i++, "rest argument not allowed after named arguments", 42, 22);
        validateError(methodInvocationNegative, i++, "incompatible types: expected '[float,boolean...]', found " +
                "'boolean[]'", 44, 26);
        validateError(methodInvocationNegative, i++, "incompatible types: expected 'boolean', found 'float'", 50, 22);
        Assert.assertEquals(i,  methodInvocationNegative.getErrorCount());
    }
}
