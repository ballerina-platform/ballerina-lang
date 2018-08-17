/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.worker;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BIntArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for usages of fork-join in functions.
 */
public class ForkJoinInFunctionTest {

    @Test(description = "Test Fork Join All")
    public void testForkJoinAll() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-in-all.bal");
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testForkJoinAll", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(((BIntArray) returns[0]).size(), 2);
        Assert.assertEquals(((BIntArray) returns[0]).get(0), 234);
        Assert.assertEquals(((BIntArray) returns[0]).get(1), 500);
    }

    @Test(description = "Test Fork Join with empty timeout block")
    public void testForkJoinWithEmptyTimeoutBlock() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-in-all.bal");
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testForkJoinWithEmptyTimeoutBlock", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BIntArray);
        Assert.assertEquals(((BIntArray) returns[0]).size(), 2);
        Assert.assertEquals(((BIntArray) returns[0]).get(0), 234);
        Assert.assertEquals(((BIntArray) returns[0]).get(1), 500);
    }

    @Test(description = "Test Fork Join Any")
    public void testForkJoinAny() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-some.bal");
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testForkJoinAny", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).size(), 1);
    }

    @Test(description = "Test Fork Join All of specific")
    public void testForkJoinAllOfSpecific() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-all-specific.bal");
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testForkJoinAllOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).size(), 2);
    }

    @Test(description = "Test Fork Join Any of specific")
    public void testForkJoinAnyOfSpecific() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-any-specific.bal");
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testForkJoinAnyOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).size(), 1);
    }

    @Test(description = "Test Fork Join Without Timeout Expression")
    public void testForkJoinWithoutTimeoutExpression() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-without-timeout.bal");
        BValue[] returns = BRunUtil.invoke(result, "testForkJoinWithoutTimeoutExpression");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.23);
    }

    @Test(description = "Test Fork Join With Workers in same function")
    public void testForkJoinWithWorkersInSameFunction() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-and-workers-under-same-funtion.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }
}
