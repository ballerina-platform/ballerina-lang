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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for usages of worker in functions.
 */
public class WorkerInFunctionTest {

    @Test(description = "Test worker in function")
    public void testWorkerInFunction() {
        CompileResult result = BCompileUtil.compile("test-src/workers/worker-in-function-test.bal");
        BValue[] args = {new BString("hello")};
        BValue[] returns = BRunUtil.invoke(result, "testSimpleWorker", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test(description = "Test worker interactions with each other")
    public void testWorkerMultipleInteractions() {
        CompileResult result = BCompileUtil.compile("test-src/workers/worker-multiple-interactions.bal");
        BValue[] args = {new BInteger(100)};
        BValue[] returns = BRunUtil.invoke(result, "testMultiInteractions", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "1103";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

}
