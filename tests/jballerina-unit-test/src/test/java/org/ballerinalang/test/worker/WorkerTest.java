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

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

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
        BValue[] returns = BRunUtil.invoke(result, "workerReturnTest", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BInteger ret = (BInteger) returns[0];
        Assert.assertEquals(ret.intValue(), 52);
    }

    @Test
    public void workerSendToWorkerTest() {
        BValue[] returns = BRunUtil.invoke(result, "workerSendToWorker", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BInteger ret = (BInteger) returns[0];
        Assert.assertEquals(ret.intValue(), 41);
    }

    @Test
    public void workerSendToDefault() {
        BValue[] returns = BRunUtil.invoke(result, "workerSendToDefault", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BInteger ret = (BInteger) returns[0];
        Assert.assertEquals(ret.intValue(), 51);
    }

    @Test
    public void workerSendFromDefault() {
        BValue[] returns = BRunUtil.invoke(result, "workerSendFromDefault", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BInteger ret = (BInteger) returns[0];
        Assert.assertEquals(ret.intValue(), 51);
    }

   @Test
    public void receiveWithTrap() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithTrap", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BError ret = (BError) returns[0];
        Assert.assertEquals(ret.getReason(), "err");
    }

    @Test
    public void receiveWithCheck() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithCheck", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BError ret = (BError) returns[0];
        Assert.assertEquals(ret.getReason(), "err");
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
        String expected = "error: error: err from panic \n\tat $lambda$";
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
        String expected = "error: error: err from panic \n" + "\tat " +
                                    "sendToDefaultWithPanicBeforeSendInDefault(workers.bal:";
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
        String expected = "error: error: err from panic \n" + "\tat $lambda$";
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
        String expected = "error: error: err from panic \n" +
                                "\tat sendToDefaultWithPanicAfterSendInDefault(workers.bal:";
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
        String expectedResult = "error: error: err from panic \n" +
                "\tat receiveFromDefaultWithPanicAfterSendInDefault(workers.bal:";
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
        String expected = "error: error: err from panic \n" +
                "\tat receiveFromDefaultWithPanicBeforeSendInDefault(workers.bal:";
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
        String expected = "error: error: err from panic \n" + "\tat $lambda$";
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
        String expectedMessage = "error: error: err from panic \n\tat";
        String actualMessage = actualException.getMessage();
        Assert.assertTrue(actualMessage.contains(expectedMessage), actualMessage);
    }

    @Test
    public void receiveWithCheckAndTrap() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithCheckAndTrap");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("error: err from panic", ((BError) returns[0]).getReason());
    }

    @Test(enabled = false) // Issue with trap
    public void receiveWithTrapForDefault() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithTrapForDefault");
        Assert.assertEquals(returns.length, 1);
    }

    @Test
    public void receiveWithCheckForDefault() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithCheckForDefault");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("err from panic", ((BError) returns[0]).getReason());
    }

    @Test
    public void receiveDefaultWithCheckAndTrap() {
        BValue[] returns = BRunUtil.invoke(result, "receiveDefaultWithCheckAndTrap");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("error: err from panic", ((BError) returns[0]).getReason());
    }

    @Test
    public void sameStrandMultipleInvocation() {
        for (int i = 0; i < 20; i++) {
            sameStrandMultipleInvocationTest();
        }
    }

    @Test
    public void workerTestWithLambda() {
        BValue[] returns = BRunUtil.invoke(result, "workerTestWithLambda");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 88);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: future is already cancelled.*")
    public void workerWithFutureTest1() {
        BValue[] returns = BRunUtil.invoke(result, "workerWithFutureTest1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void workerWithFutureTest2() {
        BValue[] returns = BRunUtil.invoke(result, "workerWithFutureTest2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

    @Test
    public void workerWithFutureTest3() {
        try {
            BValue[] returns = BRunUtil.invoke(result, "workerWithFutureTest3");
            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
        } catch (BLangRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("error: future is already cancelled"));
        }
    }

    private void sameStrandMultipleInvocationTest() {
        PrintStream defaultOut = System.out;
        try {
            ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(tempOutStream));
            BRunUtil.invoke(result, "sameStrandMultipleInvocation");
            String result = new String(tempOutStream.toByteArray());
            // we cannot guarantee an ordering between message sends
            Assert.assertTrue((result.contains("11 - 11") && result.contains("12 - 12")) ||
                            (result.contains("11 - 12") && result.contains("12 - 11")), result);
        } finally {
            System.setOut(defaultOut);
        }
    }

    @Test
    public void waitOnSameFutureByMultiple() {
        BValue[] returns = BRunUtil.invoke(result, "waitOnSameFutureByMultiple");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
    }

    @Test
    public void testComplexTypeSend() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "Rec");
        Assert.assertEquals(((BMap) returns[0]).get("k"), new BInteger(10));
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
}
