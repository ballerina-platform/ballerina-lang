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
package org.ballerinalang.test.worker;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Negative worker related tests.
 */
public class WorkerFailTest {
    
    @Test(enabled = false)
    public void invalidForkJoinJoinResult() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-forkjoin-join-result.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }

    @Test(enabled = false)
    public void invalidForkJoinTimeoutResult() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-forkjoin-timeout-result.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }
    
    @Test(enabled = false)
    public void invalidWorkerSendReceive() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-worker-send-receive.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }
    
    @Test(enabled = false)
    public void invalidForkJoinWithReturn() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-forkjoin-with-return.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
    }

    @Test
    public void invalidWorkSendWithoutWorker() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-worksend-without-worker.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("undefined worker "), message);
    }

    @Test
    public void invalidWorkReceiveWithoutWorker() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-workreceive-without-worker.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("undefined worker"), message);
    }

    @Test
    public void invalidReceiveBeforeWorkers() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-before-workers.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("undefined worker"), message);
    }

    @Test
    public void invalidSendBeforeWorkers() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-before-workers.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("undefined worker"), message);
    }

    @Test
    public void invalidSendInLambda() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-in-lambda.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("undefined worker"), message);
    }

    @Test
    public void invalidSendWithReturnTest() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-with-return.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        String message = result.getDiagnostics()[0].getMessage();
        Assert.assertTrue(message.contains("can not be used after a non-error return"), message);
    }

    @Test
    public void invalidSendWithErrorReturnTest() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-with-error-return.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        String message = result.getDiagnostics()[0].getMessage();
        Assert.assertTrue(message.contains("expected 'int', found 'error|int'"), message);
    }

    @Test
    public void invalidSendWithErrorCheckTest() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-with-error-check.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        String message = result.getDiagnostics()[0].getMessage();
        Assert.assertTrue(message.contains("can not be used after a non-error return"), message);
    }


}
