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

import io.ballerina.runtime.api.utils.StringUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for usages of worker in functions.
 */
public class WorkerInFunctionTest {

    @Test(description = "Test worker in function")
    public void testWorkerInFunction() {
        CompileResult result = BCompileUtil.compile("test-src/workers/worker-in-function-test.bal");
        Object[] args = {StringUtils.fromString("hello")};
        Object returns = BRunUtil.invoke(result, "testSimpleWorker", args);
        Assert.assertEquals(returns.toString(), "hello");
    }

    @Test(description = "Test worker interactions with each other")
    public void testWorkerMultipleInteractions() {
        CompileResult result = BCompileUtil.compile("test-src/workers/worker-multiple-interactions.bal");
        Object[] args = {(100)};
        Object returns = BRunUtil.invoke(result, "testMultiInteractions", args);
        Assert.assertTrue(returns instanceof Long);
        final String expected = "1103";
        Assert.assertEquals(returns.toString(), expected);
    }

}
