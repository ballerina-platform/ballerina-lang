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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
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
        Assert.assertEquals(result.getErrorCount(), 27, "Worker actions negative test error count");
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        BAssertUtil.validateError(result, 0, "invalid type for worker send 'Person', expected anydata",
                                  28, 9);
        BAssertUtil.validateError(result, 1, "invalid type for worker send 'Person', expected anydata",
                                  30, 22);
        BAssertUtil.validateError(result, 2, "undefined worker 'w4'", 32, 17);
        BAssertUtil.validateError(result, 3, "invalid worker flush expression for 'w4', there are no worker " +
                "send statements to 'w4' from 'w1'", 32, 17);
        BAssertUtil.validateError(result, 4, "invalid type for worker receive 'Person', expected anydata",
                                  36, 21);
        BAssertUtil.validateError(result, 5, "invalid type for worker receive 'Person', expected anydata",
                                  38, 14);
        BAssertUtil.validateError(result, 6, "variable assignment is required", 42, 9);
        BAssertUtil.validateError(result, 7, "invalid worker flush expression for 'w1', there are no worker " +
                "send statements to 'w1' from 'w3'", 42, 9);
        BAssertUtil.validateError(result, 8, "redeclared symbol 's1'", 59, 5);
        BAssertUtil.validateError(result, 9, "variable assignment is required", 62, 5);
        BAssertUtil.validateError(result, 10, "incompatible types: expected 'future<int>', found 'future<string>'",
                                  76, 34);
        BAssertUtil.validateError(result, 11, "incompatible types: expected 'future<int|boolean>', found 'future" +
                "<string>'", 77, 37);
        BAssertUtil.validateError(result, 12, "incompatible types: expected 'future<map>', found 'future<int>'",
                                  78, 24);
        BAssertUtil.validateError(result, 13, "incompatible types: expected 'future<map>', found 'future<string>'",
                                  78, 29);
        BAssertUtil.validateError(result, 14, "incompatible types: expected 'future<map>', found 'future<boolean>'",
                                  78, 34);
        BAssertUtil.validateError(result, 15, "operator '|' cannot be applied to type 'future'",
                                  79, 27);
        BAssertUtil.validateError(result, 16, "operator '|' not defined for 'future<int>' and 'future<string>'",
                                  80, 33);
        BAssertUtil.validateError(result, 17, "incompatible types: expected 'future<future<int|string>>', found " +
                "'future<int>'", 81, 39);
        BAssertUtil.validateError(result, 18, "incompatible types: expected 'future<future<int|string>>', found " +
                "'future<string>'", 81, 44);
        BAssertUtil.validateError(result, 19, "incompatible types: expected 'future<int>', found 'future<string>'",
                                  99, 34);
        BAssertUtil.validateError(result, 20, "incompatible types: expected 'future<boolean|string>', found " +
                                          "'future<int>'", 100, 41);
        BAssertUtil.validateError(result, 21, "incompatible types: expected 'future<boolean|string>', found " +
                                          "'future<int>'", 100, 45);
        BAssertUtil.validateError(result, 22, "incompatible types: expected 'future<int>', found 'future<string>'",
                                  101, 51);
        BAssertUtil.validateError(result, 23, "invalid literal for type '$anonType$5'", 102, 45);
        BAssertUtil.validateError(result, 24, "invalid field name 'f2' in type '$anonType$6'", 103, 45);
        BAssertUtil.validateError(result, 25, "missing non-defaultable required record field 'f3'", 103, 45);
        BAssertUtil.validateError(result, 26, "missing non-defaultable required record field 'f2'", 104, 25);
    }
}
