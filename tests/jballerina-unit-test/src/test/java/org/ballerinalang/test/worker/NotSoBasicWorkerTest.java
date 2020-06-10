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
 * Advanced worker related tests.
 */
public class NotSoBasicWorkerTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/not-so-basic-worker-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void forkWithTimeoutTest1() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithTimeoutTest1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 15);
    }

    @Test
    public void forkWithTimeoutTest2() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithTimeoutTest2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 25);
    }

    @Test
    public void forkWithMessagePassing() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithMessagePassing", new BValue[0]);
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
    public void forkWithWaitOnSomeSelectedWorkers1() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithWaitOnSomeSelectedWorkers1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger xy = (BInteger) vals[0];
        Assert.assertEquals(xy.intValue(), 75);
    }

    @Test
    public void forkWithWaitOnSomeSelectedWorkers2() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithWaitOnSomeSelectedWorkers2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 320);
    }

    @Test
    public void forkWithWaitOnSomeSelectedWorkers3() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithWaitOnSomeSelectedWorkers3", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 160);
    }

    @Test
    public void forkWithWaitOnAllSelectedWorkers1() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithWaitOnAllSelectedWorkers1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        @SuppressWarnings("unchecked")
        BMap<String, BInteger> map = (BMap<String, BInteger>) vals[0];
        Assert.assertEquals(map.get("x").intValue(), 33);
    }

    @Test
    public void forkWithWaitOnAllSelectedWorkers2() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithWaitOnAllSelectedWorkers2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger result = (BInteger) vals[0];
        Assert.assertEquals(result.intValue(), 777);
    }

    @Test
    public void forkWithinWorkers() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithinWorkers", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger ret = (BInteger) vals[0];
        Assert.assertEquals(ret.intValue(), 30);
    }

    @Test(enabled = false)
    public void largeForkCreationTest() {
        BValue[] vals = BRunUtil.invoke(result, "largeForkCreationTest", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        BInteger ret = (BInteger) vals[0];
        Assert.assertEquals(ret.intValue(), 65000);
    }

    @Test
    public void forkWithStructTest() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithStruct", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "[block] sW1: w1[block] fW2: 10.344");
    }

    @Test
    public void forkWithSameWorkerContent() {
        BValue[] vals = BRunUtil.invoke(result, "forkWithSameWorkerContent", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "W3: data1, W4: data2");
    }

    @Test(groups = "brokenOnErrorChange")
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
        Assert.assertTrue((((BInteger) vals[0]).intValue() == 10) || ((BInteger) vals[0]).intValue() == 5);
        // Not applicable for jBallerina
//        int pkgIndex = result.getProgFile().getEntryPackage().pkgIndex;
//        Assert.assertTrue((result.getProgFile().globalMemArea.getIntField(pkgIndex, 0) == 5) ||
//                (result.getProgFile().globalMemArea.getIntField(pkgIndex, 0) == 10));
    }
}
