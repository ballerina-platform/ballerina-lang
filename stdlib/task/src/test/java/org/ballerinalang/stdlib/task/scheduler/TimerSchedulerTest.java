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

package org.ballerinalang.stdlib.task.scheduler;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Tests for Ballerina Task Timer Scheduler.
 */
@Test
public class TimerSchedulerTest {

    @Test(description = "Tests a timer scheduler cancel functionality", enabled = false)
    public void testListenerTimerStop() {
        CompileResult compileResult = BCompileUtil.compile(true, "scheduler/timer/timer_stop.bal");
        await().atLeast(4000, TimeUnit.MILLISECONDS).atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BRunUtil.invoke(compileResult, "triggerTimer");
            BValue[] count = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BInteger);
            return (((BInteger) count[0]).intValue() == -2000);
        });
    }

    @Test(description = "Tests a timer scheduler with multiple services attached", enabled = false)
    public void testMultipleServices() {
        CompileResult compileResult = BCompileUtil.compile("scheduler/timer/multiple_services.bal");
        BRunUtil.invoke(compileResult, "triggerTimer");
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invoke(compileResult, "getResult");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BBoolean);
            return ((BBoolean) count[0]).booleanValue();
        });
    }
}
