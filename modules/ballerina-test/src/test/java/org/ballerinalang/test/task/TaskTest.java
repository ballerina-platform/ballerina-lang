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

package org.ballerinalang.test.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.io.File;
import java.io.IOException;

/**
 * Tests for Task related functions
 */
public class TaskTest {
    private CompileResult timerProgramFile;
    private CompileResult appointmentProgramFile;
    private static final Log log = LogFactory.getLog(TaskTest.class);

    @BeforeClass
    public void setup() {
        timerProgramFile = BTestUtils.compile("test-src/task/task-timer.bal");
        appointmentProgramFile = BTestUtils.compile("test-src/task/task-appointment.bal");
    }

    @Test(description = "Test for 'scheduleAppointment' function which is implemented in ballerina.task package")
    public void testScheduleAppointment() {
        int taskId;
        try {
            File dir = new File("/tmp/tmpDir");
            dir.mkdir();
            File.createTempFile("test", "txt", new File(dir.getAbsolutePath()));
        } catch (IOException e) {
            log.error("Unable to create the test file: " + e.getMessage());
        }
        //Schedules the appointment to execute every second
        BValue[] args = {new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(-1), new BInteger(-1)};
        BValue[] returns = BTestUtils.invoke(appointmentProgramFile, "scheduleAppointment", args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimer() {
        int taskId;
        //Schedules the timer with the interval 10seconds
        BValue[] args = {new BInteger(0), new BInteger(10000)};
        BValue[] returns = BTestUtils.invoke(timerProgramFile, "scheduleTimer", args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }
}
