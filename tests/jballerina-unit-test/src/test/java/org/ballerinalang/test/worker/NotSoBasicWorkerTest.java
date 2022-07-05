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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/30590
    public void forkWithTimeoutTest1() {
        Object vals = BRunUtil.invoke(result, "forkWithTimeoutTest1", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 15);
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/30590
    public void forkWithTimeoutTest2() {
        Object vals = BRunUtil.invoke(result, "forkWithTimeoutTest2", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 25);
    }

    @Test
    public void forkWithMessagePassing() {
        Object vals = BRunUtil.invoke(result, "forkWithMessagePassing", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 90);
    }

    @Test
    public void chainedWorkerSendReceive() {
        Object vals = BRunUtil.invoke(result, "chainedWorkerSendReceive", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 12);
    }

    @Test
    public void forkWithWaitOnSomeSelectedWorkers1() {
        Object vals = BRunUtil.invoke(result, "forkWithWaitOnSomeSelectedWorkers1", new Object[0]);

        long xy = (long) vals;
        Assert.assertEquals(xy, 75);
    }

    @Test
    public void forkWithWaitOnSomeSelectedWorkers2() {
        Object vals = BRunUtil.invoke(result, "forkWithWaitOnSomeSelectedWorkers2", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 320);
    }

    @Test
    public void forkWithWaitOnSomeSelectedWorkers3() {
        Object vals = BRunUtil.invoke(result, "forkWithWaitOnSomeSelectedWorkers3", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 160);
    }

    @Test
    public void forkWithWaitOnAllSelectedWorkers1() {
        Object vals = BRunUtil.invoke(result, "forkWithWaitOnAllSelectedWorkers1", new Object[0]);

        @SuppressWarnings("unchecked")
        BMap<String, Long> map = (BMap<String, Long>) vals;
        Assert.assertEquals((long) map.get(StringUtils.fromString("x")), 33);
    }

    @Test
    public void forkWithWaitOnAllSelectedWorkers2() {
        Object vals = BRunUtil.invoke(result, "forkWithWaitOnAllSelectedWorkers2", new Object[0]);

        long result = (long) vals;
        Assert.assertEquals(result, 777);
    }

    @Test
    public void forkWithinWorkers() {
        Object vals = BRunUtil.invoke(result, "forkWithinWorkers", new Object[0]);

        long ret = (long) vals;
        Assert.assertEquals(ret, 30);
    }

    @Test
    public void largeForkCreationTest() {
        Object vals = BRunUtil.invoke(result, "largeForkCreationTest", new Object[0]);

        long ret = (long) vals;
        Assert.assertEquals(ret, 65000);
    }

    @Test
    public void forkWithStructTest() {
        Object vals = BRunUtil.invoke(result, "forkWithStruct", new Object[0]);

        Assert.assertEquals(vals.toString(), "[block] sW1: w1[block] fW2: 10.344");
    }

    @Test
    public void forkWithSameWorkerContent() {
        Object vals = BRunUtil.invoke(result, "forkWithSameWorkerContent", new Object[0]);

        Assert.assertEquals(vals.toString(), "W3: data1, W4: data2");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/30590
    public void testForkJoinWorkersWithNonBlockingConnector() {
        CompileResult result = BCompileUtil.compile("test-src/workers/fork-join-blocking.bal");
        BArray vals = (BArray) BRunUtil.invoke(result, "testForkJoin", new Object[0]);
        Assert.assertEquals(vals.size(), 2);
        Assert.assertEquals(vals.get(0), 200);
        Assert.assertEquals(vals.get(1), 100);
    }

    @Test
    public void testVoidFunctionWorkers() {
        CompileResult result = BCompileUtil.compile("test-src/workers/void-function-workers.bal");
        Object vals = BRunUtil.invoke(result, "testVoidFunction", new Object[0]);

        Assert.assertTrue(((long) vals == 10L) || ((long) vals == 5L));
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
