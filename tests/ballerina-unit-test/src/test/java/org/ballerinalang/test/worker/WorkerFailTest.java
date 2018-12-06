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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Negative worker related tests.
 */
public class WorkerFailTest {

    @Test
    public void invalidWorkerSendReceive() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-worker-send-receive.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1);
        Assert.assertTrue(message.contains(" interactions are invalid"), message);
    }

    @Test
    public void invalidWorkSendWithoutWorker() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-worksend-without-worker.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined worker 'worker1'", 3, 12);
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
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined worker 'w1'", 2, 12);
    }

    @Test
    public void invalidSendBeforeWorkers() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-before-workers.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined worker 'w1'", 3, 3);
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
    public void invalidReciveWithErrorReturnTest() {
        CompileResult result =
                BCompileUtil.compile("test-src/workers/invalid-receive-with-error-return.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("incompatible types"), message);
    }

    @Test
    public void invalidSendWithErrorCheckTest() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-with-error-check.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        String message = result.getDiagnostics()[0].getMessage();
        Assert.assertTrue(message.contains("can not be used after a non-error return"), message);
    }

    @Test
    public void invalidSendInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position"), message);
    }

    @Test
    public void invalidSyncSendInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position"), message);
    }

    @Test
    public void invalidReceiveInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker receive statement position"), message);
    }

}
