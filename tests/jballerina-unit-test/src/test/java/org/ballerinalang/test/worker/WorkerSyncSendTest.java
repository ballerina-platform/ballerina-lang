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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
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
 * Tests the sync send worker action.
 */
public class WorkerSyncSendTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {

        this.result = BCompileUtil.compile("test-src/workers/sync-send.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @Test
    public void simpleSyncSendTest() {

        Object returns = BRunUtil.invoke(result, "simpleSyncSend");
        Assert.assertTrue((Boolean) returns.toString().startsWith("w2w2w2w2w2"),
                "Returned wrong value:" + returns.toString());
    }

    @Test
    public void multipleSyncSendTest() {

        Object returns = BRunUtil.invoke(result, "multipleSyncSend");
        Assert.assertTrue((Boolean) returns.toString().startsWith("w2w2w2w2w2"),
                          "Returned wrong value:" + returns.toString());
        Assert.assertFalse(returns.toString().startsWith("w11"),
                           "Returned wrong value:" + returns.toString());
    }

    @Test
    public void nilReturnTest() {

        Object returns = BRunUtil.invoke(result, "process2");
        Assert.assertNull(returns);
    }

    @Test
    public void multiWorkerTest() {

        Object returns = BRunUtil.invoke(result, "multiWorkerSend");
        Assert.assertFalse(returns.toString().startsWith("w1"),
                "Returned wrong value:" + returns.toString());
        Assert.assertFalse(returns.toString().startsWith("w11"),
                "Returned wrong value:" + returns.toString());
    }

    @Test
    public void errorAfterSendTest() {
        BRunUtil.invoke(result, "errorResult");
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
        String result = "error: error3 {\"message\":\"msg3\"}\n" +
                "\tat sync-send:$lambda$_15(sync-send.bal:312)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void basicSyncSendTest() {
        Object returns = BRunUtil.invoke(result, "basicSyncSendTest");
        Assert.assertEquals(returns, 60L);
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
        String result = "error: err from panic from w2\n" +
                "\tat sync-send:$lambda$_19(sync-send.bal:365)";
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
        String result = "error: err from panic from w1 w1\n" +
                "\tat sync-send:$lambda$_20(sync-send.bal:381)";
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
        String result = "error: err from panic from w2\n" +
                "\tat sync-send:$lambda$_23(sync-send.bal:413)";
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
        String result = "error: err from panic from w3w3\n" +
                "\tat sync-send:$lambda$_26(sync-send.bal:453)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test()
    public void errorResultWithMultipleWorkers() {
        BRunUtil.invoke(result, "errorResultWithMultipleWorkers");
    }

    @Test
    public void testComplexTypeSend() {
        Object returns = BRunUtil.invoke(result, "testComplexType");
        Assert.assertEquals(getType(returns).getName(), "Rec");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("k")), 10L);
    }

    @Test
    public void multipleSendsToErroredWorker() {
        BRunUtil.invoke(result, "multipleSendsToErroredChannel");
    }

    @Test
    public void testSyncSendAfterSend() {
        BRunUtil.invoke(result, "testSyncSendAfterSend");
    }

    @Test
    public void testAsyncSend() {
        BRunUtil.invoke(result, "testAsyncSend");
    }

    @Test
    public void testNoFailureForReceiveWithError() {
        Object returns = BRunUtil.invoke(result, "testNoFailureForReceiveWithError");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFailureForReceiveWithError() {
        Object returns = BRunUtil.invoke(result, "testFailureForReceiveWithError");
        Assert.assertTrue((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
