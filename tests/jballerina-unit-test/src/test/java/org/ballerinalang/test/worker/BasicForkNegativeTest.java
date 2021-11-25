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
 * Basic fork negative test.
 *
 * @since 0.990.0
 */
public class BasicForkNegativeTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/basic_fork_negative.bal");
    }

    @Test
    public void testBasicForkNegative() {
        Assert.assertEquals(result.getErrorCount(), 1, "Incorrect error count");
        BAssertUtil.validateError(result, 0, "empty fork statement is not allowed", 18, 5);
    }

    @Test
    public void workerPeerCommunicationNegativeTests() {
        CompileResult result = BCompileUtil.compile("test-src/workers/worker-peer-communication-negative.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 20, 24);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 21, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 'm'", 29, 13);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 32, 13);
        BAssertUtil.validateWarning(result, index++, "unused variable 's'", 41, 13);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 41, 24);
        BAssertUtil.validateWarning(result, index++, "unused variable 'm'", 46, 13);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 52, 5);
        BAssertUtil.validateWarning(result, index++, "unused variable 'k'", 53, 5);
        BAssertUtil.validateError(result, index++, "worker interactions are only allowed between peers", 53, 16);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
