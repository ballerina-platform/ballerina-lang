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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative worker action related tests.
 */
public class BasicWorkerActionsNegativeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/actions-negative.bal");
        // TODO: 11/23/18 Need to update the negative tests after worker changes are done
//        Assert.assertEquals(result.getErrorCount(), 31, "Worker actions negative test error count");
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        // TODO: 11/23/18 Need to update the negative tests after worker changes are done
//        BAssertUtil.validateError(result, 0, "invalid type for worker send 'Person', expected anydata",
//                                  28, 9);
//        BAssertUtil.validateError(result, 1, "invalid type for worker send 'Person', expected anydata",
//                                  30, 22);
//        BAssertUtil.validateError(result, 2, "undefined worker 'w4'", 32, 17);
//        BAssertUtil.validateError(result, 3, "invalid worker flush expression for 'w4', there are no worker " +
//                "send statements to 'w4' from 'w1'", 32, 17);
//        BAssertUtil.validateError(result, 4, "variable assignment is required", 42, 9);
//        BAssertUtil.validateError(result, 5, "invalid worker flush expression for 'w1', there are no worker " +
//                "send statements to 'w1' from 'w3'", 42, 9);
//        BAssertUtil.validateError(result, 6, "incompatible types: expected 'future<string>', found 'future<int>'",
//                                  59, 22);
//        BAssertUtil.validateError(result, 7, "variable assignment is required", 62, 5);
//        BAssertUtil.validateError(result, 8, "incompatible types: expected 'future<int>', found 'future" +
//                "<string>'", 76, 34);
//        BAssertUtil.validateError(result, 9, "incompatible types: expected 'future<int|boolean>', found 'future" +
//                "<string>'", 77, 37);
//        BAssertUtil.validateError(result, 10, "incompatible types: expected 'future<map>', found 'future<int>'",
//                                  78, 24);
//        BAssertUtil.validateError(result, 11, "incompatible types: expected 'future<map>', found 'future<string>'",
//                                  78, 29);
//        BAssertUtil.validateError(result, 12, "incompatible types: expected 'future<map>', found 'future<boolean>'",
//                                  78, 34);
//        BAssertUtil.validateError(result, 13, "operator '|' cannot be applied to type 'future'",
//                                  79, 27);
//        BAssertUtil.validateError(result, 14, "operator '|' not defined for 'future<int>' and 'future<string>'",
//                                  80, 33);
//        BAssertUtil.validateError(result, 15, "incompatible types: expected 'future<future<int|string>>', found " +
//                "'future<int>'", 81, 39);
//        BAssertUtil.validateError(result, 16, "incompatible types: expected 'future<future<int|string>>', found " +
//                "'future<string>'", 81, 44);
//        BAssertUtil.validateError(result, 17, "incompatible types: expected 'future<int>', found 'future<string>'",
//                                  99, 34);
//        BAssertUtil.validateError(result, 18, "incompatible types: expected 'future<boolean|string>', found " +
//                                          "'future<int>'", 100, 41);
//        BAssertUtil.validateError(result, 19, "incompatible types: expected 'future<boolean|string>', found " +
//                                          "'future<int>'", 100, 45);
//        BAssertUtil.validateError(result, 20, "incompatible types: expected 'future<int>', found 'future<string>'",
//                                  101, 51);
//        BAssertUtil.validateError(result, 21, "invalid literal for type '$anonType$5'", 102, 45);
//        BAssertUtil.validateError(result, 22, "invalid field name 'f2' in type '$anonType$6'", 103, 45);
//        BAssertUtil.validateError(result, 23, "missing non-defaultable required record field 'f3'", 103, 45);
//        BAssertUtil.validateError(result, 24, "missing non-defaultable required record field 'f2'", 104, 25);
//        BAssertUtil.validateError(result, 25, "worker send/receive interactions are invalid; worker(s) cannot " +
//                "move onwards from the state: '{w1=i -> w2, w2=FINISHED}'", 112, 5);
//        BAssertUtil.validateError(result, 26, "incompatible types: expected 'string', found 'int'", 114, 9);
//        BAssertUtil.validateError(result, 27, "invalid worker send statement position, must be a top level " +
//                "statement in a worker", 120, 13);
//        BAssertUtil.validateError(result, 28, "action invocation as an expression not allowed here", 124, 15);
//        BAssertUtil.validateError(result, 29, "invalid worker receive statement position, must be a top level " +
//                "statement in a worker", 127, 19);
//        BAssertUtil.validateError(result, 30, "invalid worker flush expression for 'w2', there are no worker send " +
//                "statements to 'w2' from 'w1'", 137, 22);
    }
}
