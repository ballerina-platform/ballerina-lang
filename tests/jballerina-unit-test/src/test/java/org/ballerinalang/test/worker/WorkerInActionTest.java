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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for usages of worker in actions.
 */
public class WorkerInActionTest {

    private CompileResult result;

    @BeforeClass()
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/worker-in-action.bal");
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        Object returns = JvmRunUtil.invoke(result, "testAction1");

        Assert.assertEquals(returns.toString(), "result from sampleWorker");
    }

    @Test(description = "Test TestConnector action2")
    public void testConnectorAction2() {
        Object returns = JvmRunUtil.invoke(result, "testAction2");

        Assert.assertEquals(returns.toString(), "request");
    }

    @Test(description = "Test default strand error before send action")
    public void testDefaultErrorBeforeSend() {
        Object returns = JvmRunUtil.invoke(result, "testDefaultError");

        Assert.assertEquals(returns.toString(), "REACHED");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
