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
 */
package org.ballerinalang.stdlib.task.service;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.ballerinalang.stdlib.task.utils.TaskTestUtils.getFilePath;

/**
 * Tests for Ballerina Task Timer Listener..
 */
@Test
public class TimerServiceTest {

    @Test(description = "Tests running an timer as a service")
    public void testListenerTimer() {
        CompileResult compileResult = BCompileUtil.compileOffline(true,
                getFilePath(getTestPath("service_simple.bal")));
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() > 3);
        });
    }

    @Test(description = "Tests running an timer as a service")
    public void testListenerTimerLimitedNoOfRuns() {
        CompileResult compileResult = BCompileUtil.compileOffline(true,
                getFilePath(getTestPath("service_limited_number_of_runs.bal")));
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == 3);
        });
    }

    @Test(description = "Tests a timer listener with inline configurations")
    public void testListenerTimerInlineConfigs() {
        CompileResult compileResult = BCompileUtil.compileOffline(true,
                getFilePath(getTestPath("service_inline_configs.bal")));
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() > 3);
        });
    }

    @Test(
            description = "Tests a timer listener with negative interval value",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Timer scheduling interval should be a positive integer.*"
    )
    public void testListenerTimerNegativeInterval() {
        BCompileUtil.compileOffline(getFilePath(getTestPath("service_negative_interval.bal")));
    }

    @Test(
            description = "Tests a timer listener with negative delay value",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Timer scheduling delay should be a non-negative value.*"
    )
    public void testListenerTimerNegativeDelay() {
        BCompileUtil.compileOffline(getFilePath(Paths.get("listener", "timer", "service_negative_delay.bal")));
    }

    @Test(description = "Tests a timer listener without delay field")
    public void testListenerTimerWithoutDelay() {
        BCompileUtil.compileOffline(getFilePath(getTestPath("service_without_delay.bal")));
    }

    @Test(
            description = "Tests a timer scheduler with zero interval",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*Timer scheduling interval should be a positive integer.*"
    )
    public void testZeroInterval() {
        BCompileUtil.compileOffline(getFilePath(getTestPath("zero_interval.bal")));
    }

    private static Path getTestPath(String fileName) {
        return Paths.get("listener", "timer", fileName);
    }
}
