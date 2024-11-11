/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.worker;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Tests the worker on fail clause.
 *
 * @since 2021.9.0
 */
public class WorkerOnFailTest {

    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/workers/worker-on-fail.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
        resultNegative = BCompileUtil.compile("test-src/workers/worker-on-fail-negative.bal");
    }

    @Test
    public void simpleOnFailTest() {
        Object returns = BRunUtil.invoke(result, "testOnFailInWorker");
        long ret = (long) returns;
        Assert.assertEquals(ret, -1);
    }

    @Test
    public void doOnFailInsideWorker() {
        Object returns = BRunUtil.invoke(result, "testDoOnFailInsideWorker");
        long ret = (long) returns;
        Assert.assertEquals(ret, 3);
    }

    @Test
    public void returnWithinOnFail() {
        Object returns = BRunUtil.invoke(result, "testReturnWithinOnFail");
        long ret = (long) returns;
        Assert.assertEquals(ret, -1);
    }

    @Test
    public void onFailWorkerWithVariable() {
        Object returns = BRunUtil.invoke(result, "testOnFailWorkerWithVariable");
        long ret = (long) returns;
        Assert.assertEquals(ret, 0);
    }

    @Test
    public void workerOnFailWithSend() {
        Object returns = BRunUtil.invoke(result, "testWorkerOnFailWithSend");
        long ret = (long) returns;
        Assert.assertEquals(ret, 1);
    }

    @Test
    public void asyncSendInsideWorkerOnFail() {
        Object returns = BRunUtil.invoke(result, "testAsyncSendInsideWorkerOnFail");
        long ret = (long) returns;
        Assert.assertEquals(ret, 17);
    }

    @Test
    public void syncSendInsideWorkerOnFail() {
        Object returns = BRunUtil.invoke(result, "testSyncSendInsideWorkerOnFail");
        long ret = (long) returns;
        Assert.assertEquals(ret, -8);
    }

    @Test
    public void onFailNegative() {
        String errMsg = "incompatible types: expected 'int', found '(int|ballerina/lang.error:0.0.0:NoMessage)'";
        int index = 0;
        validateError(resultNegative, index++, errMsg, 26, 17);
        validateError(resultNegative, index++, errMsg, 41, 17);
        validateError(resultNegative, index++, errMsg, 66, 17);
        validateError(resultNegative, index++, errMsg, 67, 17);
        validateError(resultNegative, index++, errMsg, 68, 17);
        validateError(resultNegative, index++, errMsg, 69, 17);
        validateError(resultNegative, index++, errMsg, 70, 17);
        Assert.assertEquals(resultNegative.getErrorCount(), index);
    }

    @AfterClass
    public void afterClass() {
        result = null;
        resultNegative = null;
    }
}
