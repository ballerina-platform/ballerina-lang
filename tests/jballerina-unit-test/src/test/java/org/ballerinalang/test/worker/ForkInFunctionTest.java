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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test cases for usages of fork in functions.
 */
public class ForkInFunctionTest {

    @Test(description = "Test Fork and wait for all workers")
    public void testForkAndWaitForAll() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-in-all.bal");
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "testForkAndWaitForAll", args);
        Assert.assertTrue(returns instanceof BArray);
        Assert.assertEquals(((BArray) returns).size(), 2);
        Assert.assertEquals(((BArray) returns).getInt(0), 234);
        Assert.assertEquals(((BArray) returns).getInt(1), 500);
    }

    @Test(description = "Test fork and wait for any")
    public void testForkAndWaitForAny() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-some.bal");
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "testForkAndWaitForAny", args);
        Assert.assertTrue(returns instanceof BString);
        String returnStr = returns.toString();
        Assert.assertTrue(returnStr.equals("abc") || returnStr.equals("xyz"), returnStr);
    }

    @Test(description = "Test Fork With Workers in same function")
    public void testForkWithWorkersInSameFunction() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-workers-under-same-funtion.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
        Object[] args = {};
        Object returns = BRunUtil.invoke(result, "forkWithWorkers", args);
        Assert.assertEquals(returns.getClass(), Long.class);
        long returnInt = (long) returns;
        Assert.assertEquals(returnInt, 10);
    }
}
