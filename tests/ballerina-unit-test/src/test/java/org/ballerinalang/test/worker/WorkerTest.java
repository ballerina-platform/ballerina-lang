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
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        Assert.assertEquals(ret.reason, "err");
    }


}
