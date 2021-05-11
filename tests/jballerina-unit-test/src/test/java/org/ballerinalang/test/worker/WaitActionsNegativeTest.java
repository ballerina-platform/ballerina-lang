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
 * Negative wait action related tests.
 *
 * @since 0.985.0
 */
public class WaitActionsNegativeTest {

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/workers/wait-actions-negative.bal");
        int index = 0;
        Assert.assertEquals(resultNegative.getErrorCount(), 20, "Wait actions negative test error count");
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<string>', found 'future<int>'", 56, 22);
        BAssertUtil.validateError(resultNegative, index++,
                "variable assignment is required", 57, 5);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<int>', found 'future<string>'", 66, 31);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<(int|boolean)>', found 'future<string>'", 67, 36);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<map<(int|boolean)>>', found 'future<int>'", 68, 37);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<map<(int|boolean)>>', found 'future<string>'", 68, 41);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<map<(int|boolean)>>', found 'future<boolean>'", 68, 44);
        // Commented errors won't get captured after (typeChecker, semanticAnalyzer) & codeAnalyzer separation.
        //        BAssertUtil.validateError(resultNegative, index++,
        //                "operator '|' cannot be applied to type 'future'", 69, 27);
        //        BAssertUtil.validateError(resultNegative, index++,
        //                "operator '|' cannot be applied to type 'future'", 70, 33);
        BAssertUtil.validateError(resultNegative, index++,
                "operator '|' not defined for 'future<int>' and 'future<string>'", 70, 33);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<future<(int|string)>>', found 'future<int>'", 71, 40);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<future<(int|string)>>', found 'future<string>'", 71, 43);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<int>', found 'future<string>'", 81, 34);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<(boolean|string)>', found 'future<int>'", 82, 41);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<(boolean|string)>', found 'future<int>'", 82, 45);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<int>', found 'future<string>'", 83, 51);
        BAssertUtil.validateError(resultNegative, index++,
                "missing non-defaultable required record field 'f3'", 85, 50);
        BAssertUtil.validateError(resultNegative, index++,
                "missing non-defaultable required record field 'f2'", 86, 30);
        BAssertUtil.validateError(resultNegative, index++,
                                  "incompatible types: expected 'sealedRec', found 'record {| int id; string name; " +
                                          "boolean status; |}'", 87, 31);
        BAssertUtil.validateError(resultNegative, index++,
                "invalid field name 'status' in type 'sealedRec'", 88, 31);
        BAssertUtil.validateError(resultNegative, index++,
                "incompatible types: expected 'future<int>', found 'future<string>'", 89, 55);
        BAssertUtil.validateError(resultNegative, index,
                "incompatible types: expected 'future<string>', found 'future<int>'", 90, 54);
    }

    @Test
    public void testWorkerMessagePassingAfterWaitNegative() {
        CompileResult result = BCompileUtil.compile("test-src/workers/after-wait-action-negative.bal");
        int index = 0;
        String expectedErrMsg = "invalid worker message passing after waiting for the same worker";
        BAssertUtil.validateError(result, index++, expectedErrMsg, 22, 13);
        BAssertUtil.validateError(result, index++, expectedErrMsg, 33, 17);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 41, 13);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 44, 13);
        BAssertUtil.validateError(result, index++, expectedErrMsg, 50, 17);
        BAssertUtil.validateError(result, index++, expectedErrMsg, 51, 13);
        BAssertUtil.validateError(result, index++, expectedErrMsg, 61, 25);

        Assert.assertEquals(result.getErrorCount(), index);
    }

    @Test
    public void testWaitCausingADeadlockNegative() {
        CompileResult result = BCompileUtil.compile("test-src/workers/wait-deadlock-negative.bal");
        int index = 0;
        String msg = "worker send/receive interactions are invalid; worker(s) cannot move onwards from the state: '%s'";
        BAssertUtil.validateError(result, index++, String.format(msg, "[wait v, wait w, FINISHED]"), 19, 14);
        BAssertUtil.validateError(result, index++, String.format(msg, "[wait v, wait w, wait x, FINISHED]"), 29, 14);
        BAssertUtil.validateError(result, index++, String.format(msg, "[wait w2,  <- w, wait w1, FINISHED]"), 43, 14);
        BAssertUtil.validateError(result, index++, String.format(msg, "[wait v, wait w, wait x, FINISHED]"), 61, 18);
        BAssertUtil.validateError(result, index++,
                String.format(msg, "[wait vi, wait wi, wait xi, FINISHED, FINISHED]"), 78, 23);
        Assert.assertEquals(result.getErrorCount(), index);
    }
}
