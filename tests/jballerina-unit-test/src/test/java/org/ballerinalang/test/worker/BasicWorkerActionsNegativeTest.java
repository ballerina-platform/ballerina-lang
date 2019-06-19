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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
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
        Assert.assertEquals(result.getErrorCount(), 11, "Worker actions negative test error count");
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        int index = 0;
        BAssertUtil.validateError(result, index, "invalid type for worker send 'Person', expected anydata",
                                  42, 9);
        index++;
        BAssertUtil.validateError(result, index, "invalid type for worker send 'Person', expected anydata",
                                  44, 22);
        index++;
        BAssertUtil.validateError(result, index, "undefined worker 'w4'", 46, 17);
        index++;
        BAssertUtil.validateError(result, index, "invalid worker flush expression for 'w4', there are " +
                "no worker send statements to 'w4' from 'w1'", 46, 17);
        index++;
        BAssertUtil.validateError(result, index, "variable assignment is required",
                                  61, 9);
        index++;
        BAssertUtil.validateError(result, index, "invalid worker flush expression for 'w1', there are no " +
                "worker send statements to 'w1' from 'w3'", 61, 9);
        index++;
        BAssertUtil.validateError(result, index, "invalid worker send statement position, must be a top " +
                "level statement in a worker", 74, 13);
        index++;
        BAssertUtil.validateError(result, index, "action invocation as an expression not allowed here",
                                  78, 15);
        index++;
        BAssertUtil.validateError(result, index, "invalid worker receive statement position, must be a " +
                "top level statement in a worker", 81, 19);
        index++;
        BAssertUtil.validateError(result, index, "invalid worker flush expression for 'w2', there are no " +
                "worker send statements to 'w2' from 'w1'", 91, 22);
        index++;
        BAssertUtil.validateError(result, index, "invalid usage of receive expression, var not allowed",
                                  112, 21);
    }
}
