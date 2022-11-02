/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.worker;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Wait for any action related tests.
 */
public class WaitForAnyActionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait-for-any-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "Wait for any actions test error count");
    }

    @Test(dataProvider = "waitForAnyActionsTestFunctions")
    public void testWaitForAnyActionsTest(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] waitForAnyActionsTestFunctions() {
        return new Object[]{
                "waitTest1",
                "waitTest2",
                "waitTest3",
                "waitTest4",
                "waitTest5",
                "waitTest6",
                "waitTest7",
                "waitTest8",
                "waitTest9",
                "waitTest10",
                "waitTest11",
                "waitTest12",
                "waitTest13",
                "waitTest14",
                "waitTest15",
                "waitTest16",
                "waitTest17",
                "waitTest18",
                "waitTest19",
                "waitTest20",
                "waitTest21"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
