/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.worker;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the sync send worker action.
 */
public class WorkerSyncSendTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {

        this.result = BCompileUtil.compile("test-src/workers/sync-send.bal");
        Assert.assertEquals(result.getErrorCount(), 0, result.toString());
    }

    @Test
    public void simpleSyncSendTest() {

        BValue[] returns = BRunUtil.invoke(result, "simpleSyncSend");
        Assert.assertTrue(returns[0].stringValue().startsWith("w2w2w2w2w2"),
                "Returned wrong value:" + returns[0].stringValue());
    }

    @Test
    public void multipleSyncSendTest() {

        BValue[] returns = BRunUtil.invoke(result, "multipleSyncSend");
        Assert.assertTrue(returns[0].stringValue().startsWith("w2w2w2w2w2"),
                "Returned wrong value:" + returns[0].stringValue());
        Assert.assertFalse(returns[0].stringValue().startsWith("w11"),
                "Returned wrong value:" + returns[0].stringValue());
    }

    @Test
    public void nilReturnTest() {

        BValue[] returns = BRunUtil.invoke(result, "process2");
        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void multiWorkerTest() {

        BValue[] returns = BRunUtil.invoke(result, "multiWorkerSend");
        Assert.assertFalse(returns[0].stringValue().startsWith("w1"),
                "Returned wrong value:" + returns[0].stringValue());
        Assert.assertFalse(returns[0].stringValue().startsWith("w11"),
                "Returned wrong value:" + returns[0].stringValue());
    }

    @Test
    public void errorAfterSendTest() {

        BValue[] returns = BRunUtil.invoke(result, "errorResult");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), "error3");
    }

    @Test
    public void panicAfterSendTest() {

        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "panicTest");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: error3 message=msg3\n" + "\tat sync-send:$lambda$14(sync-send.bal:271)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void basicSyncSendTest() {
        BValue[] returns = BRunUtil.invoke(result, "basicSyncSendTest");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 60);
    }

    @Test
    public void multipleSyncSendWithPanic() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "multipleSyncSendWithPanic");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: err from panic from w2 \n\tat sync-send:$lambda$18(sync-send.bal:320)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void multipleSyncSendWithPanicInSend() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "multipleSyncSendWithPanicInSend");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: err from panic from w1 w1 \n\tat sync-send:$lambda$19(sync-send.bal:335)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void syncSendWithPanicInReceive() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "syncSendWithPanicInReceive");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: err from panic from w2 \n\tat sync-send:$lambda$22(sync-send.bal:364)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void panicWithMultipleSendStmtsTest() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "panicWithMultipleSendStmtsTest");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result = "error: err from panic from w3w3 \n\tat sync-send:$lambda$25(sync-send.bal:401)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test()
    public void errorResultWithMultipleWorkers() {
        BValue[] returns = BRunUtil.invoke(result, "errorResultWithMultipleWorkers");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("err returned from w2", ((BError) returns[0]).getReason());
    }

    @Test
    public void testComplexTypeSend() {
        BValue[] returns = BRunUtil.invoke(result, "testComplexType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "Rec");
        Assert.assertEquals(((BMap) returns[0]).get("k"), new BInteger(10));
    }

    @Test
    public void multipleSendsToErroredWorker() {
        BValue[] returns = BRunUtil.invoke(result, "multipleSendsToErroredChannel");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(returns[0].stringValue(), "error one {}");
    }

    @Test
    public void testSyncSendAfterSend() {
        BValue[] returns = BRunUtil.invoke(result, "testSyncSendAfterSend");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getType().getName(), "error");
        Assert.assertEquals(returns[0].stringValue(), "w2 error {}");
    }
}
