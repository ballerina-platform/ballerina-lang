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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

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
        validateError(result, 0, "undefined worker 'worker1'", 3, 12);
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
        validateError(result, 0, "undefined worker 'w1'", 2, 12);
    }

    @Test
    public void invalidSendBeforeWorkers() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-before-workers.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "undefined worker 'w1'", 3, 3);
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
        String message = result.getDiagnostics()[0].message();
        Assert.assertTrue(message.contains("can not be used after a non-error return"), message);
    }

    @Test
    public void invalidSendWithErrorReturnTest() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-with-error-return.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        String message = result.getDiagnostics()[0].message();
        Assert.assertTrue(message.contains("expected 'int', found '(error|int)'"), message);
    }

    @Test(groups = { "brokenOnNewParser" })
    public void invalidReceiveWithErrorReturnTest() {
        CompileResult result =
                BCompileUtil.compile("test-src/workers/invalid-receive-with-error-return.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("incompatible types"), message);
    }

    @Test(groups = { "brokenOnNewParser" })
    public void invalidReceiveWithErrorUnionReturnTest() {
        CompileResult result =
                BCompileUtil.compile("test-src/workers/invalid_receive_with_union_error_return_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        validateError(result, 0, "incompatible types: expected 'TrxError?', found 'FooError?'", 46, 29);
        validateError(result, 1, "incompatible types: expected 'TrxError?', " +
                                          "found '(FooError|TrxError)?'", 47, 19);
        validateError(result, 2, "incompatible types: expected '()', found '(FooError|TrxError)?'", 48, 16);
    }

    @Test
    public void testSendReceiveMismatch() {
        CompileResult result = BCompileUtil.compile("test-src/workers/send_receive_mismatch_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "incompatible types: expected 'string', found 'int'", 25, 21);
    }

    @Test
    public void testSyncSendReceiveMismatch() {
        CompileResult result = BCompileUtil.compile("test-src/workers/sync_send_receive_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        validateError(result, 0, "variable assignment is required", 33, 9);
        validateError(result, 1, "incompatible types: expected 'int', found '(E1|int)'", 37, 18);
        validateError(result, 2, "incompatible types: expected 'string', found '(E1|E2|string)'", 42, 20);
    }

    @Test
    public void invalidSendWithErrorCheckTest() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-with-error-check.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        String message = result.getDiagnostics()[0].message();
        Assert.assertTrue(message.contains("can not be used after a non-error return"), message);
    }

    @Test
    public void invalidSendInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position, must be a top level statement in " +
                                                   "a worker"), message);
    }

    @Test
    public void invalidSyncSendInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position, must be a top level statement in " +
                                                   "a worker"), message);
    }

    @Test
    public void invalidReceiveInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker receive statement position, must be a top level statement " +
                                                   "in a worker"), message);
    }

    @Test
    public void invalidReceiveWithTrapWithNonError() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-with-trap.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "incompatible types: expected 'int', found '(int|error)'", 10, 15);
        validateError(result, 1, "incompatible types: expected 'string', found '(string|error)'", 20, 18);
    }

    @Test
    public void invalidReceiveWithCheckWithNonError() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-with-check.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "invalid usage of the 'check' expression operator: no expression type is " +
                "equivalent to error type", 10, 21);
        validateError(result, 1, "invalid usage of the 'check' expression operator: no expression type is " +
                "equivalent to error type", 23, 21);
    }

    @Test
    public void invalidReceiveWithCheckpanic() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid_receive_with_checkpanic_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "invalid usage of the 'checkpanic' expression operator: no expression type is " +
                "equivalent to error type", 23, 31);
        validateError(result, 1, "invalid usage of the 'checkpanic' expression operator: no expression type is " +
                "equivalent to error type", 33, 31);
    }

    @Test
    public void invalidActionsInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-actions-in-fork.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "undefined worker 'w3'", 5, 13);
        validateError(result, 1, "undefined worker 'w1'", 8, 29);
    }

    @Test
    public void invalidReceiveInForEach() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-in-foreach.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker receive statement position, must be a top level statement " +
                                                   "in a worker"), message);
    }

    @Test
    public void invalidAsycnSendInForEach() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-async-send-in-foreach.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position, must be a top level statement in " +
                                                   "a worker"), message);
    }

    @Test
    public void invalidSycnSendInForEach() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-foreach.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position, must be a top level statement in " +
                                                   "a worker"), message);
    }

    @Test
    public void invalidAsyncSendInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-async-send-in-fork.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position, must be a top level statement " +
                                                   "in a worker"), message);
    }

    @Test
    public void invalidSyncSendInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-fork.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker send statement position, must be a top level statement " +
                                                   "in a worker"), message);
    }

    @Test
    public void invalidReceiveInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-in-fork.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker receive statement position, must be a top level statement " +
                                                   "in a worker"), message);
    }

    @Test(groups = { "brokenOnNewParser" })
    public void invalidUsagesOfDefault() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-usage-of-default.bal");
        Assert.assertEquals(result.getErrorCount(), 6);
        validateError(result, 0, "mismatched input 'default'. expecting Identifier", 18, 12);
        validateError(result, 1, "mismatched input 'default'. expecting Identifier", 29, 16);
        validateError(result, 2, "invalid token 'default'", 36, 12);
        validateError(result, 3, "invalid token 'default'", 41, 9);
        validateError(result, 4, "invalid token 'default'", 43, 13);
        validateError(result, 5, "extraneous input 'default'", 44, 16);
    }
}
