/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.tuples;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Basic Test cases for tubles.
 *
 * @since 0.966.0
 */
public class BasicTupleTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/tuples/tuple_basic_test.bal");
        resultNegative = BCompileUtil.compile("test-src/types/tuples/tuple_negative_test.bal");
    }


    @Test(description = "Test basics of tuple types")
    public void testTupleTypeBasics() {
        BValue[] returns = BRunUtil.invoke(result, "basicTupleTest", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), " test1 expr \n" +
                " test2 \n" +
                " test3 foo test3 \n" +
                " test4 4 \n" +
                " test5 5 \n" +
                " test6 foo test6 \n ");
    }

    @Test(description = "Test Function invocation using tuples")
    public void testFunctionInvocationUsingTuples() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionInvocation", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "xy5.0z");
    }

    @Test(description = "Test Function Invocation return values using tuples")
    public void testFunctionReturnValue() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionReturnValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "x5.0z");

        returns = BRunUtil.invoke(result, "testFunctionReturnValue2", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "xz");
        Assert.assertEquals(returns[1].stringValue(), "5.0");
    }

    @Test(description = "Test Function Invocation return values using tuples")
    public void testIgnoredValue() {
        BValue[] returns = BRunUtil.invoke(result, "testIgnoredValue1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "foo");

        returns = BRunUtil.invoke(result, "testIgnoredValue2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "foo");

        returns = BRunUtil.invoke(result, "testIgnoredValue3");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "foo");
    }

    @Test(description = "Test index based access of tuple type")
    public void testIndexBasedAccess() {
        BValue[] returns = BRunUtil.invoke(result, "testIndexBasedAccess");
        Assert.assertEquals(returns[0].stringValue(), "def");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 4);
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test(description = "Test negatives of tuple type")
    public void testNegativesOfTupleType() {
        BAssertUtil.validateError(resultNegative, 0, "tuple and expression size does not match", 2, 30);
        BAssertUtil.validateError(resultNegative, 1, "array index out of range: index: '-1', size: '3'", 7, 14);
        BAssertUtil.validateError(resultNegative, 2, "array index out of range: index: '3', size: '3'", 8, 14);
        BAssertUtil.validateError(resultNegative, 3, "invalid index expression: expected integer literal", 10, 18);
        BAssertUtil.validateError(resultNegative, 4, "incompatible types: expected 'int', found 'string'", 11, 16);
    }
}
