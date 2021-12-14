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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

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
        validateError(result, 0, "undefined worker 'worker1'", 3, 11);
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
        validateError(result, 0, "undefined worker 'w1'", 2, 11);
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

    @Test
    public void invalidReceiveWithErrorReturnTest() {
        CompileResult result =
                BCompileUtil.compile("test-src/workers/invalid-receive-with-error-return.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("incompatible types"), message);
    }

    @Test
    public void invalidReceiveWithErrorUnionReturnTest() {
        CompileResult result =
                BCompileUtil.compile("test-src/workers/invalid_receive_with_union_error_return_negative.bal");
        int index = 0;
        validateWarning(result, index++, "unused variable 'j'", 29, 9);
        validateError(result, index++, "unreachable code", 37, 13);
        validateWarning(result, index++, "unused variable 'success'", 47, 9);
        validateError(result, index++, "incompatible types: expected 'TrxError?', found 'FooError?'", 47, 29);
        validateError(result, index++, "incompatible types: expected 'TrxError?', " +
                                          "found '(FooError|TrxError)?'", 48, 19);
        validateError(result, index++, "incompatible types: expected '()', found '(FooError|TrxError)?'", 49, 16);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @Test
    public void testSendReceiveMismatch() {
        CompileResult result = BCompileUtil.compile("test-src/workers/send_receive_mismatch_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        validateError(result, 0, "incompatible types: expected 'string', found 'int'", 25, 20);
    }

    @Test
    public void testSyncSendReceiveMismatch() {
        CompileResult result = BCompileUtil.compile("test-src/workers/sync_send_receive_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        Assert.assertEquals(result.getWarnCount(), 1);
        validateError(result, 0, "variable assignment is required", 33, 9);
        validateError(result, 1, "incompatible types: expected 'int', found '(E1|int)'", 38, 17);
        validateError(result, 2, "incompatible types: expected 'string', found '(E1|E2|string)'", 43, 20);
        validateWarning(result, 3, "unused variable 'err'", 47, 5);
    }

    @Test
    public void invalidSendInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-send-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("worker send statement position not supported yet, " +
                "must be a top level statement in a worker"), message);
    }

    @Test
    public void invalidSyncSendInIf() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-if.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("worker send statement position not supported yet, " +
                "must be a top level statement in a worker"), message);
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
        Assert.assertEquals(result.getWarnCount(), 3);
        validateWarning(result, 0, "invalid usage of the 'check' expression operator: no expression type is " +
                "equivalent to error type", 10, 21);
        validateWarning(result, 1, "unused variable 'j'", 23, 7);
        validateWarning(result, 2, "invalid usage of the 'check' expression operator: no expression type is " +
                "equivalent to error type", 23, 21);
    }

    @Test
    public void invalidReceiveWithCheckpanic() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid_receive_with_checkpanic_negative.bal");
        Assert.assertEquals(result.getWarnCount(), 2);
        validateWarning(result, 0, "invalid usage of the 'checkpanic' expression operator: no expression type is " +
                "equivalent to error type", 23, 31);
        validateWarning(result, 1, "invalid usage of the 'checkpanic' expression operator: no expression type is " +
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
        Assert.assertTrue(message.contains("worker send statement position not supported yet, " +
                "must be a top level statement in a worker"), message);
    }

    @Test
    public void invalidSycnSendInForEach() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-foreach.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("worker send statement position not supported yet, " +
                "must be a top level statement in a worker"), message);
    }

    @Test
    public void invalidAsyncSendInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-async-send-in-fork.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("worker send statement position not supported yet, " +
                "must be a top level statement in a worker"), message);
    }

    @Test
    public void invalidSyncSendInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-sync-send-in-fork.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("worker send statement position not supported yet, " +
                "must be a top level statement in a worker"), message);
    }

    @Test
    public void invalidReceiveInFork() {
        CompileResult result = BCompileUtil.compile("test-src/workers/invalid-receive-in-fork.bal");
        String message = Arrays.toString(result.getDiagnostics());
        Assert.assertEquals(result.getErrorCount(), 1, message);
        Assert.assertTrue(message.contains("invalid worker receive statement position, must be a top level statement " +
                                                   "in a worker"), message);
    }
}
