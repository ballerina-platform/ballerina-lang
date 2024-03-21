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
import org.testng.annotations.Test;

/**
 * Negative worker action related tests.
 */
public class BasicWorkerActionsNegativeTest {

    @Test(description = "Test negative scenarios of worker actions")
    public void testWorkerActionsSemanticsNegative() {
        int index = 0;
        CompileResult resultSemanticsNegative = BCompileUtil.compile("test-src/workers/actions-semantics-negative.bal");
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 10, "Worker actions semantics negative test " +
                "error count");
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "invalid type for worker send 'Person', expected value:Cloneable", 42, 9);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "invalid type for worker send 'Person', expected value:Cloneable", 44, 22);
        BAssertUtil.validateError(resultSemanticsNegative, index++, "undefined worker 'w4'", 46, 17);
        BAssertUtil.validateError(resultSemanticsNegative, index++, "variable assignment is required",
                61, 9);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "action invocation as an expression not allowed here", 78, 15);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "receive action not supported wth 'var' type", 112, 21);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "missing identifier", 139, 13);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "invalid token 'int'", 143, 12);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "missing identifier", 143, 12);
        BAssertUtil.validateError(resultSemanticsNegative, index,
                "incomplete quoted identifier", 147, 12);
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        int index = 0;
        CompileResult resultNegative = BCompileUtil.compile("test-src/workers/actions-negative.bal");
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'val'", 45, 9);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'p3'", 56, 9);
        BAssertUtil.validateError(resultNegative, index++, "invalid worker flush expression for 'w1', there are no " +
                "worker send statements to 'w1' from 'w3'", 62, 17);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'val'", 63, 9);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'msg'", 78, 9);
        BAssertUtil.validateError(resultNegative, index++, "invalid worker receive statement position, must be a " +
                "top level statement in a worker", 80, 19);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'x1'", 88, 9);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'x2'", 89, 9);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'result'", 90, 9);
        BAssertUtil.validateError(resultNegative, index++, "invalid worker flush expression for 'w2', there are no " +
                "worker send statements to 'w2' from 'w1'", 90, 25);
        BAssertUtil.validateWarning(resultNegative, index++, "unused variable 'j'", 97, 9);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 141, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 158, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 159, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 163, 25);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 164, 22);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 165, 25);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 166, 21);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 187, 17);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 195, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 197, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 215, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 216, 30);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 217, 75);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 223, 29);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wix"), 224, 26);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 225, 29);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wx"), 226, 25);
        BAssertUtil.validateError(resultNegative, index++, formatMessage("wy"), 228, 21);

        String notSupportedMsg = "worker send statement position not supported yet, " +
                "must be a top level statement in a worker";

        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 249, 13);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 250, 13);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 254, 13);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 255, 13);
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 258, 9);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 260, 17);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 263, 17);
        BAssertUtil.validateError(resultNegative, index++, "undefined worker 'w1'", 268, 26);
        BAssertUtil.validateError(resultNegative, index++,
                "invalid worker receive statement position, must be a top level statement in a worker", 275, 21);
        BAssertUtil.validateError(resultNegative, index++,
                "invalid worker receive statement position, must be a top level statement in a worker", 280, 17);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 295, 26);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 296, 26);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 300, 26);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 301, 26);
        BAssertUtil.validateError(resultNegative, index++, "unreachable code", 304, 22);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 306, 30);
        BAssertUtil.validateError(resultNegative, index++, notSupportedMsg, 309, 30);
        BAssertUtil.validateError(resultNegative, index++, "undefined worker 'w1'", 314, 39);
        BAssertUtil.validateError(resultNegative, index++,
                "invalid worker receive statement position, must be a top level statement in a worker", 321, 34);
        BAssertUtil.validateError(resultNegative, index++,
                "invalid worker receive statement position, must be a top level statement in a worker", 326, 30);

        Assert.assertEquals(resultNegative.getDiagnostics().length, index,
                            "Worker actions negative test diagnostic count");
    }

    private String formatMessage(String workerName) {
        return String.format(
                "multiple references to a named worker '%s' as a variable reference is not allowed", workerName);
    }
}
