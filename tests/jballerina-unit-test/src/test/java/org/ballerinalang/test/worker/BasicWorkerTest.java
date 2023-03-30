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
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Basic worker related tests.
 */
public class BasicWorkerTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/basic-worker-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @Test
    public void workerDeclTest() {
        BRunUtil.invoke(result, "workerDeclTest", new Object[0]);
    }

    @Test
    public void simpleWorkerMessagePassingTest() {
        BRunUtil.invoke(result, "simpleWorkerMessagePassingTest", new Object[0]);
    }

    @Test
    public void basicForkTest() {
        Object vals = BRunUtil.invoke(result, "basicForkTest", new Object[0]);
        Assert.assertEquals(vals, 10L);
    }

    @Test
    public void forkWithMessageParsingTest() {
        Object vals = BRunUtil.invoke(result, "forkWithMessageParsingTest", new Object[0]);
        Assert.assertEquals(vals, 5L);
    }

    @Test
    public void forkWithWaitForAny() {
        Object vals = BRunUtil.invoke(result, "forkWithWaitForAny", new Object[0]);
        long val = (long) vals;
        Assert.assertTrue(val == 15 || val == 5);
    }

    @Test
    public void workerReturnTest() {
        Object vals = BRunUtil.invoke(result, "workerReturnTest", new Object[0]);
        long ret = (long) vals;
        Assert.assertEquals(ret, 51);
    }

    @Test
    public void workerSameThreadSchedulingTest() {
        Object vals = BRunUtil.invoke(result, "workerSameThreadTest", new Object[0]);
        BMap result = (BMap) vals;
        Assert.assertEquals(result.get(StringUtils.fromString("w")), result.get(StringUtils.fromString("w1")));
        Assert.assertEquals(result.get(StringUtils.fromString("w")), result.get(StringUtils.fromString("w2")));
    }

    @Test(dataProvider = "workerSendTests")
    public void testSimpleSyncSendFunctions(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "workerSendTests")
    public Object[] testFunctions() {
        return new Object[]{
                "testSimpleSendActionWithCloneableType",
                "testSimpleSendActionErrorType",
                "testSimpleSendActionXMLType",
                "testSimpleSendActionReadonlyRecord",
                "testSimpleSendActionWithMapType",
                "testSimpleSendActionWithListType",
        };
    }

    @Test(dataProvider = "basicWorkerFunctions")
    public void testBasicWorkerTests(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "basicWorkerFunctions")
    public Object[] basicWorkerFunctions() {
        return new Object[]{
                "basicWorkerTest1",
                "basicWorkerTest2"
        };
    }

    @Test(dataProvider = "workerMessagePassingFunctions")
    public void testWorkerMessagePassing(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "workerMessagePassingFunctions")
    public Object[] workerMessagePassingFunctions() {
        return new Object[]{
                "testWorkerMessagePassingRepeatedly",
                "testPanicWithMessagePassing"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
