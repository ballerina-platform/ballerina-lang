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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Basic worker related tests.
 */
public class WorkerTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/workers.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @Test
    public void workerReturnTest() {
        Object returns = BRunUtil.invoke(result, "workerReturnTest", new Object[0]);

        long ret = (long) returns;
        Assert.assertEquals(ret, 52);
    }

    @Test
    public void workerSendToWorkerTest() {
        Object returns = BRunUtil.invoke(result, "workerSendToWorker", new Object[0]);

        long ret = (long) returns;
        Assert.assertEquals(ret, 41);
    }

    @Test
    public void workerSendToDefault() {
        Object returns = BRunUtil.invoke(result, "workerSendToDefault", new Object[0]);

        long ret = (long) returns;
        Assert.assertEquals(ret, 51);
    }

    @Test
    public void workerSendFromDefault() {
        Object returns = BRunUtil.invoke(result, "workerSendFromDefault", new Object[0]);

        long ret = (long) returns;
        Assert.assertEquals(ret, 51);
    }

    @Test
    public void receiveWithTrap() {
        BRunUtil.invoke(result, "receiveWithTrap", new Object[0]);
    }

    @Test()
    public void syncSendReceiveWithTrap() {
        BRunUtil.invoke(result, "syncSendReceiveWithTrap");
    }

    @Test
    public void receiveWithCheck() {
        BRunUtil.invoke(result, "receiveWithCheck", new Object[0]);
    }

    @Test
    public void syncSendReceiveWithCheck() {
        BRunUtil.invoke(result, "syncSendReceiveWithCheck", new Object[0]);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: err \\{\"message\":\"err msg.*")
    public void receiveWithCheckpanic() {
        BRunUtil.invoke(result, "receiveWithCheckpanic");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: err \\{\"message\":\"sync send err msg.*")
    public void syncSendReceiveWithCheckpanic() {
        BRunUtil.invoke(result, "syncSendReceiveWithCheckpanic");
    }

    @Test
    public void sendToDefaultWithPanicBeforeSendInWorker() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "sendToDefaultWithPanicBeforeSendInWorker");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expected = "error: error: err from panic\n\tat workers:$lambda$";
        Assert.assertTrue(actualException.getMessage().contains(expected), actualException.getMessage());
    }

    @Test
    public void sendToDefaultWithPanicBeforeSendInDefault() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "sendToDefaultWithPanicBeforeSendInDefault");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expected = "error: error: err from panic\n" + "\tat " +
                "workers:sendToDefaultWithPanicBeforeSendInDefault(workers.bal:";
        Assert.assertTrue(actualException.getMessage().contains(expected), actualException.getMessage());
    }

    @Test
    public void sendToDefaultWithPanicAfterSendInWorker() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "sendToDefaultWithPanicAfterSendInWorker");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expected = "error: error: err from panic\n" + "\tat workers:$lambda$";
        Assert.assertTrue(actualException.getMessage().contains(expected), actualException.getMessage());
    }

    @Test
    public void sendToDefaultWithPanicAfterSendInDefault() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "sendToDefaultWithPanicAfterSendInDefault");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expected = "error: error: err from panic\n" +
                "\tat workers:sendToDefaultWithPanicAfterSendInDefault(workers.bal:";
        Assert.assertTrue(actualException.getMessage().contains(expected), actualException.getMessage());
    }

    @Test
    public void receiveFromDefaultWithPanicAfterSendInDefault() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "receiveFromDefaultWithPanicAfterSendInDefault");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expectedResult = "error: error: err from panic\n" +
                "\tat workers:receiveFromDefaultWithPanicAfterSendInDefault(workers.bal:";
        Assert.assertTrue(actualException.getMessage().contains(expectedResult),
                actualException.getMessage());
    }

    @Test
    public void receiveFromDefaultWithPanicBeforeSendInDefault() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "receiveFromDefaultWithPanicBeforeSendInDefault");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expected = "error: error: err from panic\n" +
                "\tat workers:receiveFromDefaultWithPanicBeforeSendInDefault(workers.bal:";
        Assert.assertTrue(actualException.getMessage().contains(expected), actualException.getMessage());
    }

    @Test
    public void receiveFromDefaultWithPanicBeforeReceiveInWorker() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "receiveFromDefaultWithPanicBeforeReceiveInWorker");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expected = "error: error: err from panic\n" + "\tat workers:$lambda$";
        Assert.assertTrue(actualException.getMessage().contains(expected), actualException.getMessage());
    }

    @Test
    public void receiveFromDefaultWithPanicAfterReceiveInWorker() {
        Exception actualException = null;
        try {
            BRunUtil.invoke(result, "receiveFromDefaultWithPanicAfterReceiveInWorker");
        } catch (Exception e) {
            actualException = e;
        }
        Assert.assertNotNull(actualException);
        String expectedMessage = "error: error: err from panic\n\tat";
        String actualMessage = actualException.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage), actualMessage);
    }

    @Test
    public void receiveWithCheckAndTrap() {
        BRunUtil.invoke(result, "receiveWithCheckAndTrap");
    }

    @Test()
    public void receiveWithTrapForDefault() {
        BRunUtil.invoke(result, "receiveWithTrapForDefault");
    }

    @Test
    public void receiveWithCheckForDefault() {
        BRunUtil.invoke(result, "receiveWithCheckForDefault");
    }

    @Test
    public void receiveDefaultWithCheckAndTrap() {
        BRunUtil.invoke(result, "receiveDefaultWithCheckAndTrap");
    }

    @Test
    public void workerTestWithLambda() {
        Object returns = BRunUtil.invoke(result, "workerTestWithLambda");

        Assert.assertEquals(returns, 88L);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.future\\}FutureAlreadyCancelled.*")
    public void workerWithFutureTest1() {
        Object returns = BRunUtil.invoke(result, "workerWithFutureTest1");

        Assert.assertEquals(returns, 10L);
    }

    @Test
    public void workerWithFutureTest2() {
        Object returns = BRunUtil.invoke(result, "workerWithFutureTest2");

        Assert.assertEquals(returns, 12L);
    }

    @Test
    public void workerWithFutureTest3() {
        try {
            Object returns = BRunUtil.invoke(result, "workerWithFutureTest3");

            Assert.assertEquals(returns, 18L);
        } catch (BLangRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("error: {ballerina/lang.future}FutureAlreadyCancelled"));
        }
    }

    @Test
    public void testComplexTypeSend() {
        Object returns = BRunUtil.invoke(result, "testComplexType");

        Assert.assertEquals(getType(returns).getName(), "Rec");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("k")), 10L);
    }

    @Test
    public void innerWorkerPanicTest() {
        try {
            BRunUtil.invoke(result, "panicFunc");
            Assert.fail("Worker did not panic");
        } catch (BLangRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("worker w5 panic"));
        }
    }

    @Test
    public void waitInReturn() {
        Object returns = BRunUtil.invoke(result, "waitInReturn");

        Assert.assertTrue(returns instanceof BMap);
        BMap mapResult = (BMap) returns;
        Assert.assertEquals(mapResult.get(StringUtils.fromString("w1")).toString(), "w1");
        Assert.assertEquals(mapResult.get(StringUtils.fromString("w2")).toString(), "w2");
    }

    @Test
    public void testLambdaWithWorkerMessagePassing() {
        BRunUtil.invoke(result, "testLambdaWithWorkerMessagePassing");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testFunctionWithWorkerInsideLock() {
        Object returns = BRunUtil.invoke(result, "testPanicWorkerInsideLock");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testFunctionWithWorkerInsideLockWithDepth3() {
        Object returns = BRunUtil.invoke(result, "testPanicWorkerInsideLockWithDepth3");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testFunctionWithStartInsideLock() {
        Object returns = BRunUtil.invoke(result, "testPanicStartInsideLock");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testFunctionWithStartInsideLockWithDepth3() {
        Object returns = BRunUtil.invoke(result, "testPanicStartInsideLockWithDepth3");
    }

    @Test
    public void testWorkerInteractionsAfterCheck() {
        BRunUtil.invoke(result, "testWorkerInteractionsAfterCheck");
    }

    @Test
    public void testWorkerInsideLock() {
        CompileResult result = BCompileUtil.compile("test-src/workers/worker-in-lock.bal");
        int index = 0;
        BAssertUtil.validateError(result, index++, "cannot use a named worker inside a lock statement", 4, 20);
        BAssertUtil.validateError(result, index++, "cannot use an async call inside a lock statement", 13, 13);
        BAssertUtil.validateError(result, index++, "cannot use a named worker inside a lock statement", 25, 20);
        BAssertUtil.validateError(result, index++, "cannot use a named worker inside a lock statement", 27, 28);
        BAssertUtil.validateError(result, index++, "cannot use a named worker inside a lock statement", 42, 20);
    }

    @Test
    public void testMultipleReceiveAction() {
        // Multiple receive action is not yet supported. This is to test the error message.
        CompileResult result = BCompileUtil.compile("test-src/workers/multiple-receive-action.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "multiple receive action not yet supported", 23, 25);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
