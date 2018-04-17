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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Advanced worker related tests.
 */
@Test
public class NotSoBasicWorkerTest {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/not-so-basic-worker-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }
    
    @Test
    public void forkJoinWithTimeoutTest1() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithTimeoutTest1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 15);
    }
    
    @Test
    public void forkJoinWithTimeoutTest2() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithTimeoutTest2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 25);
    }
    
    @Test
    public void forkJoinWithMessagePassingTimeoutNotTriggered() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithMessagePassingTimeoutNotTriggered", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 90);
    }
    
    @Test
    public void chainedWorkerSendReceive() {
        BValue[] vals = BRunUtil.invoke(result, "chainedWorkerSendReceive", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 12);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin1() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithSomeSelectedJoin1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger xy = (BInteger) vals[0];
        Assert.assertEquals(xy.intValue(), 75);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin2() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithSomeSelectedJoin2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 320);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin3() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithSomeSelectedJoin3", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 160);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin4() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithSomeSelectedJoin4", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger x = (BInteger) vals[0];
        Assert.assertEquals(x.intValue(), 10);
    }
    
    @Test
    public void forkJoinWithSomeSelectedJoin5() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithSomeSelectedJoin5", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger x = (BInteger) vals[0];
        Assert.assertEquals(x.intValue(), 555);
    }
    
    @Test
    public void forkJoinWithAllSelectedJoin1() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithAllSelectedJoin1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 33);
    }
    
    @Test
    public void forkJoinWithAllSelectedJoin2() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithAllSelectedJoin2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger result = (BInteger) vals[0];
        Assert.assertEquals(result.intValue(), 777);
    }
    
    @Test
    public void forkJoinInWorkers() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinInWorkers", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger ret = (BInteger) vals[0];
        Assert.assertEquals(ret.intValue(), 30);
    }
    
    @Test
    public void largeForkJoinCreationTest() {
        BValue[] vals = BRunUtil.invoke(result, "largeForkJoinCreationTest", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger ret = (BInteger) vals[0];
        Assert.assertEquals(ret.intValue(), 65000);
    }

    @Test
    public void forkJoinWithStructTest() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithStruct", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "[join-block] sW1: w1[join-block] fW2: 10.344");
    }

    @Test
    public void forkJoinWithSameWorkerContent() {
        BValue[] vals = BRunUtil.invoke(result, "forkJoinWithSameWorkerContent", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "W1: data1, W2: data2");
    }

    @Test
    public void testForkJoinWorkersWithNonBlockingConnector() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-blocking.bal");
        BValue[] vals = BRunUtil.invoke(result, "testForkJoin", new BValue[0]);
        Assert.assertEquals(vals.length, 2);
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 200);
        Assert.assertEquals(((BInteger) vals[1]).intValue(), 100);
    }

    @Test
    public void testVoidFunctionWorkers() {
        CompileResult result = BCompileUtil.compile("test-src/workers/void-function-workers.bal");
        BValue[] vals = BRunUtil.invoke(result, "testVoidFunction", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 10);
        Assert.assertEquals(result.getProgFile().getGlobalMemoryBlock().getIntField(0), 10);
    }
}
