/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.worker;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative worker related tests.
 */
public class WorkerFailTest {
    
    @Test
    public void invalidForkJoinJoinResult() {
        CompileResult result = BTestUtils.compile("test-src/workers/invalid-forkjoin-join-result.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }
    
    @Test
    public void invalidForkJoinTimeoutResult() {
        CompileResult result = BTestUtils.compile("test-src/workers/invalid-forkjoin-timeout-result.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }
    
    @Test
    public void invalidWorkerSendReceive() {
        CompileResult result = BTestUtils.compile("test-src/workers/invalid-worker-send-receive.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }
    
}
