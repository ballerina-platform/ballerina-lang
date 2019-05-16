/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.jvm;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Basic worker related tests.
 *
 * @since 0.995.0
 */
public class WorkerTest {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/jvm/jBal-workers.bal");
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
    public void receiveWithCheck() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithCheck", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        BError ret = (BError) returns[0];
        Assert.assertEquals(ret.getReason(), "err");
    }

    @Test
    public void receiveWithCheckForDefault() {
        BValue[] returns = BRunUtil.invoke(result, "receiveWithCheckForDefault");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("err from panic", ((BError) returns[0]).getReason());
    }


    @Test
    public void workerTestWithLambda() {
        BValue[] returns = BRunUtil.invoke(result, "workerTestWithLambda");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 88);
    }

    @Test
    public void simpleSyncSendTest() {
        BValue[] returns = BRunUtil.invoke(result, "simpleSyncSend");
        Assert.assertEquals(returns[0].stringValue(), "10",
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
    public void simpleFlushTest() {

        BValue[] returns = BRunUtil.invoke(result, "singleFlush");
        Assert.assertEquals(returns[0].stringValue(), "w2w2w2w2w2w1w1w1w1w1");
    }

    @Test
    public void flushReturnNilTest() {

        BValue[] returns = BRunUtil.invoke(result, "flushReturn");
        Assert.assertNull(returns[0]);
    }

    @Test
    public void flushAll() {

        BValue[] returns = BRunUtil.invoke(result, "flushAll");
        Assert.assertFalse(returns[0].stringValue().startsWith("w1"),
                "Returned wrong value:" + returns[0].stringValue());
    }

    @Test
    public void errorBeforeFlush() {

        BValue[] returns = BRunUtil.invoke(result, "errorTest");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), "error3");
    }

    @Test
    public void flushInDefaultError() {

        BValue[] returns = BRunUtil.invoke(result, "flushInDefaultError");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), "err");
    }

    @Test
    public void flushInDefault() {

        BValue[] returns = BRunUtil.invoke(result, "flushInDefault");
        Assert.assertEquals(returns[0].stringValue(), "25");
    }

}
