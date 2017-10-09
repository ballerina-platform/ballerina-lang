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

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for usages of fork-join in functions.
 */
public class ForkJoinInFunctionTest {

    @Test(description = "Test Fork Join All", enabled = false)
    public void testForkJoinAll() {
        CompileResult result = BTestUtils.compile("test-src/workers/fork-join-in-all.bal");
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testForkJoinAll", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 2);
        Assert.assertEquals(((BRefValueArray) returns[0]).get(0).value(), 234);
        Assert.assertEquals(((BRefValueArray) returns[0]).get(1).value(), 500);
    }

    @Test(description = "Test Fork Join Any", enabled = false)
    public void testForkJoinAny() {
        CompileResult result = BTestUtils.compile("test-src/workers/fork-join-some.bal");
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testForkJoinAny", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 1);
    }

    @Test(description = "Test Fork Join All of specific", enabled = false)
    public void testForkJoinAllOfSpecific() {
        CompileResult result = BTestUtils.compile("test-src/workers/fork-join-all-specific.bal");
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testForkJoinAllOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 2);
    }

    @Test(description = "Test Fork Join Any of specific", enabled = false)
    public void testForkJoinAnyOfSpecific() {
        CompileResult result = BTestUtils.compile("test-src/workers/fork-join-any-specific.bal");
        BValue[] args = {};
        BValue[] returns = BTestUtils.invoke(result, "testForkJoinAnyOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 1);
    }

    @Test(description = "Test Fork Join Without Timeout Expression", enabled = false)
    public void testForkJoinWithoutTimeoutExpression() {
        CompileResult result = BTestUtils.compile("test-src/workers/fork-join-without-timeout.bal");
        BValue[] returns = BTestUtils.invoke(result, "testForkJoinWithoutTimeoutExpression");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.23);
    }

    @Test(description = "Test Fork Join With Workers in same function")
    public void testForkJoinWithWorkersInSameFunction() {
        CompileResult result = BTestUtils.compile("test-src/workers/fork-join-and-workers-under-same-funtion.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }
}
