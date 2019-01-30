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
package org.ballerinalang.stdlib.task;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ListenerTest {
    ByteArrayOutputStream out;
    @BeforeClass
    public void setupStreams() {
        out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));
    }

    @Test(description = "Tests a dynamic timer")
    public void testListenerTimerDynamicService() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/dynamic_service.bal");
        BRunUtil.invoke(compileResult, "main");
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == 4);
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
            return (((BInteger) count[0]).intValue() == 4);
        });
    }

    @Test(description = "Tests a timer listener with inline configurations")
    public void testListenerTimerInlineConfigs() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/service_inline_configs.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == 4);
        });
    }

    @Test(
            description = "Tests a timer listener with negative delay and interval values",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Task scheduling configuration is invalid.*"
    )
    public void testListenerTimerNegativeValues() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/service_negative_values.bal");
        BServiceUtil.runService(compileResult);
    }

    @Test(description = "Tests a timer listener with inline configurations")
    public void testListenerTimerStop() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/service_stop.bal");
        BServiceUtil.runService(compileResult);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invokeStateful(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == 20);
        });
    }

    @Test(description = "Tests a timer listener without delay field")
    public void testListenerTimerWithoutDelay() {
        CompileResult compileResult = BCompileUtil.compile(
                "listener-test-src/timer/service_without_delay.bal");
        BServiceUtil.runService(compileResult);
    }
}
