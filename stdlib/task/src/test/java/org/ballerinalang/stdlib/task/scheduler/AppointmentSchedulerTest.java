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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Tests for the Scheduler appointments.
 */
@Test
public class AppointmentSchedulerTest {
    @Test(description = "Tests the functionality of initiating a Task Timer Listener.")
    public void testDynamicService() {
        CompileResult compileResult = BCompileUtil.compile("scheduler/appointment/simple_appointment.bal");
        BValue[] inputs = {new BString("0/2 * * * * ?")};
        BRunUtil.invoke(compileResult, "runService", inputs);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] configs = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(configs.length, 1);
            return (((BInteger) configs[0]).intValue() > 3);
        });
    }

    @Test(description = "Tests the functionality of initiating a Task Timer Listener.")
    public void testLimitedNoOfTimes() {
        CompileResult compileResult = BCompileUtil.compile("scheduler/appointment/limited_number_of_times.bal");
        BValue[] inputs = {new BString("* * * * * ? *")};
        BRunUtil.invoke(compileResult, "triggerAppointment", inputs);
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invoke(compileResult, "getCount");
            Assert.assertEquals(count.length, 1);
            return (((BInteger) count[0]).intValue() == 3);
        });
    }

    @Test(description = "Tests an appointment scheduler with multiple services attached")
    public void testMultipleServices() {
        CompileResult compileResult = BCompileUtil.compile("scheduler/appointment/multiple_services.bal");
        BRunUtil.invoke(compileResult, "triggerAppointment");
        await().atMost(10000, TimeUnit.MILLISECONDS).until(() -> {
            BValue[] count = BRunUtil.invoke(compileResult, "getResult");
            Assert.assertEquals(count.length, 1);
            Assert.assertTrue(count[0] instanceof BBoolean);
            return ((BBoolean) count[0]).booleanValue();
        });
    }
}
