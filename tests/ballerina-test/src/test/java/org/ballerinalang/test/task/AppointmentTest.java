/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Arrays;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Tests for Ballerina appointment tasks.
 */
public class AppointmentTest {
    private static final Log log = LogFactory.getLog(AppointmentTest.class);

    @BeforeClass
    public void setup() {
        System.setProperty("java.util.logging.config.file",
                ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
    }

    @Test(description = "Tests running an appointment and stopping it")
    public void testAppointment() {
        CompileResult compileResult = BCompileUtil.compileAndSetup("test-src/task/app-simple.bal");
        printDiagnostics(compileResult);

        String cronExpression = "0/2 * * * * ?";
        BValue[] returns =
                BRunUtil.invokeStateful(compileResult, "scheduleAppointment",
                        new BValue[]{new BString(cronExpression)});
        String taskId = returns[0].stringValue();
        assertNotEquals(taskId, "", "Invalid task ID");  // A non-null task ID should be returned
        assertEquals(returns.length, 2); // There should be no errors
        assertNull(returns[1], "Ballerina scheduler returned an error");
        await().atMost(30, SECONDS).until(() -> {
            BValue[] counts = BRunUtil.invokeStateful(compileResult, "getCount");
            return ((BInteger) counts[0]).intValue() >= 5;
        });

        // Now let's try stopping the task
        BValue[] stopResult = BRunUtil.invokeStateful(compileResult,
                "stopTask", new BValue[]{new BString(taskId)});
        assertNull(stopResult[0], "Task stopping resulted in an error");

        // One more check to see whether the task really stopped
        BValue[] counts = BRunUtil.invokeStateful(compileResult, "getCount");
        assertEquals(((BInteger) counts[0]).intValue(), -1, "Count hasn't been reset");

    }

    @Test(description = "Tests running an appointment started within workers  where the onTrigger function " +
            "generates an error")
    public void testAppointmentWithWorkersAndErrFn() {
        CompileResult compileResult = BCompileUtil.compileAndSetup("test-src/task/app-workers.bal");
        printDiagnostics(compileResult);

        String w1CronExpression = "0/2 * * * * ?";
        String w1ErrMsg = "w1: Appointment error";

        BValue[] returns =
                BRunUtil.invokeStateful(compileResult, "scheduleAppointment",
                        new BValue[]{
                                new BString(w1CronExpression), new BString(w1ErrMsg)});
        String w1TaskId = returns[0].stringValue();
        assertNotEquals(w1TaskId, "", "Invalid task ID from worker w1");  // A non-null task ID should be returned
        await().atMost(10, SECONDS).until(() -> {
            BValue[] errors = BRunUtil.invokeStateful(compileResult, "getError");
            return errors != null && errors[0] != null && errors[0].stringValue() != null && !errors[0].stringValue()
                    .equals("");
        });

        // Now test whether the onError Ballerina function got called
        BValue[] error = BRunUtil.invokeStateful(compileResult, "getError");
        assertNotNull(error[0], "Expected error not returned.");
        assertEquals(error[0].stringValue(), w1ErrMsg, "Expected error message not returned.");

        // Now let's try stopping the tasks
        BValue[] stopResult = BRunUtil.invokeStateful(compileResult, "stopTask", new BValue[]{new BString(w1TaskId)});
        assertNull(stopResult[0], "Task stopping on worker w1 resulted in an error");

        // One more check to see whether the task really stopped
        BValue[] counts = BRunUtil.invokeStateful(compileResult, "getCount");
        assertEquals(((BInteger) counts[0]).intValue(), -1, "Count hasn't been reset");
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
    }
}
