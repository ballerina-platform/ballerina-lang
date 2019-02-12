/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ballerinalang.stdlib.task.listener;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ListenerTest {

    @Test(description = "Tests a dynamic timer")
    public void testListenerTimerDynamicService() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/dynamic_service.bal");
        BRunUtil.invoke(compileResult, "main");
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() > 3);
        });
    }

    @Test(description = "Tests running an timer as a service")
    public void testListenerTimer() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/service_simple.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() > 3);
        });
    }

    /* TODO:
     * There should be way to know exactly a service is stopped or not.
     * This method is not the optimal way to check the service has stopped.
     */
    @Test(description = "Tests running an timer as a service")
    public void testListenerTimerLimitedNoOfRuns() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/service_limited_number_of_runs.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == 3);
        });
    }

    @Test(description = "Tests a timer listener with inline configurations")
    public void testListenerTimerInlineConfigs() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/service_inline_configs.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() > 3);
        });
    }

    @Test(
            description = "Tests a timer listener with negative delay and interval values",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Timer scheduling delay and interval should be non-negative values.*"
    )
    public void testListenerTimerNegativeValues() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/service_negative_values.bal");
        BServiceUtil.runService(compileResult);
    }

    @Test(description = "Tests a timer listener with inline configurations")
    public void testListenerTimerStop() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/service_cancel.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == 1000);
        });
    }

    @Test(description = "Tests a timer listener without delay field")
    public void testListenerTimerWithoutDelay() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/service_without_delay.bal");
        BServiceUtil.runService(compileResult);
    }

    @Test(description = "Tests for pause and resume functions of the timer")
    public void testPauseResume() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/pause_resume.bal");
        BServiceUtil.runService(compileResult);
        BRunUtil.invokeStateful(compileResult, "testAttach");
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] isPaused = BRunUtil.invokeStateful(compileResult, "getIsPaused");
            BValue[] isResumed = BRunUtil.invokeStateful(compileResult, "getIsResumed");
            Assert.assertEquals(isPaused.length, 1);
            Assert.assertTrue(isPaused[0] instanceof BBoolean);
            Assert.assertEquals(isResumed.length, 1);
            Assert.assertTrue(isResumed[0] instanceof BBoolean);
            return (((BBoolean) isPaused[0]).booleanValue() && ((BBoolean) isResumed[0]).booleanValue());
        });
    }

    @Test(description = "Tests for onError function parameter")
    public void testOnErrorParameter() {
        CompileResult compileResult = BCompileUtil.compile("listener-test-src/timer/service_error_return.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] isPaused = BRunUtil.invokeStateful(compileResult, "getResult");
            Assert.assertEquals(isPaused.length, 1);
            Assert.assertTrue(isPaused[0] instanceof BString);
            return ("test".equals(isPaused[0].stringValue()));
        });
    }
}
