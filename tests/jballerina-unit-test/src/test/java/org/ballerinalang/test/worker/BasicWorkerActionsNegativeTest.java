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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative worker action related tests.
 */
public class BasicWorkerActionsNegativeTest {

    private CompileResult resultNegative, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        resultNegative = BCompileUtil.compile("test-src/workers/actions-negative.bal");
        resultSemanticsNegative = BCompileUtil.compile("test-src/workers/actions-semantics-negative.bal");
    }

    @Test(description = "Test negative scenarios of worker actions", groups = {"disableOnOldParser"})
    public void testWorkerActionsSemanticsNegative() {
        int index = 0;
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 8, "Worker actions semantics negative test error" +
                " count");
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "invalid type for worker send 'Person', expected value:Cloneable", 44, 22);
        BAssertUtil.validateError(resultSemanticsNegative, index++, "undefined worker 'w4'", 46, 17);
        BAssertUtil.validateError(resultSemanticsNegative, index++, "variable assignment is required",
                61, 9);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "action invocation as an expression not allowed here", 78, 15);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "invalid usage of receive expression, var not allowed", 112, 21);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "missing identifier", 139, 13);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "missing identifier", 143, 12);
        BAssertUtil.validateError(resultSemanticsNegative, index,
                "invalid token 'int'", 143, 16);
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        int index = 0;

        BAssertUtil.validateError(resultNegative, index++, "invalid worker flush expression for 'w1', there are no " +
                "worker send statements to 'w1' from 'w3'", 62, 17);
        BAssertUtil.validateError(resultNegative, index++, "invalid worker send statement position, must be a top " +
                "level statement in a worker", 76, 13);
        BAssertUtil.validateError(resultNegative, index++, "invalid worker receive statement position, must be a " +
                "top level statement in a worker", 83, 19);
        BAssertUtil.validateError(resultNegative, index++, "invalid worker flush expression for 'w2', there are no " +
                "worker send statements to 'w2' from 'w1'", 93, 25);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 144, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 161, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 162, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 166, 29);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 167, 25);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 168, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 169, 21);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 190, 17);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 198, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 200, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 218, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 219, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 220, 75);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 226, 33);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 227, 29);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 228, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 229, 25);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 231, 21);

        Assert.assertEquals(resultNegative.getErrorCount(), index, "Worker actions negative test error count");
    }

    private String formatMessage(String workerName) {
        return String.format(
                "multiple references to a named worker '%s' as a variable reference is not allowed", workerName);
    }

    @Test
    public void testAsyncSendAsExpression() {
        // TODO: support async send as expression issue #24849
        CompileResult compileResult = BCompileUtil.compile("test-src/workers/worker_async_send_as_expression.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "async send action not yet supported as expression",
                19, 16);

        Assert.assertEquals(compileResult.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        resultNegative = null;
        resultSemanticsNegative = null;
    }
}
