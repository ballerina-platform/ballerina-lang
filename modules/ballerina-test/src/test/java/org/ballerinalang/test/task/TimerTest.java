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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.util.Calendar;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

/**
 * Tests for Ballerina timer tasks.
 */
public class TimerTest {
    private CompileResult timerCompileResult;
    private CompileResult stopTaskCompileResult;
    private CompileResult timerMWCompileResult;
    private CompileResult timerWithEmptyResponseCompileResult;
    private CompileResult timerWithoutOnErrorFunctionCompileResult;
    private static final Log log = LogFactory.getLog(TimerTest.class);

    //    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
    private int taskIdEM;

    @BeforeClass
    public void setup() {
        timerCompileResult = BCompileUtil.compile("test-src/task/simple-timer.bal");
//        stopTaskCompileResult = BCompileUtil.compile("test-src/task/task-stop.bal");
//        timerMWCompileResult = BCompileUtil.compile("test-src/task/task-timer-multiple-workers.bal");
//        timerWithEmptyResponseCompileResult =
//                BCompileUtil.compile("test-src/task/task-timer-with-empty-response.bal");
//        timerWithoutOnErrorFunctionCompileResult =
//                BCompileUtil.compile("test-src/task/task-timer-without-onErrorFunction.bal");
//
        System.setProperty("java.util.logging.config.file",
                ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
    }

    @Test(description = "Tests running a timer and stopping it")
    public void testExection() {
        CompileResult timerCompileResult = BCompileUtil.compile("test-src/task/simple-timer.bal");

        int initialDelay = 500;
        int interval = 1000;
        BValue[] returns =
                BRunUtil.invoke(timerCompileResult, "scheduleTimer",
                        new BValue[]{new BInteger(initialDelay), new BInteger(interval)});
        String taskId = returns[0].stringValue();
        assertNotEquals(taskId, "", "Invalid task ID");  // A non-null task ID should be returned
        assertEquals(returns.length, 2); // There should be no errors
        assertNull(returns[1], "Ballerina scheduler returned an error");
        await().atMost(10, SECONDS).until(() -> {
            BValue[] counts = BRunUtil.invoke(timerCompileResult, "getCount");
            return ((BInteger) counts[0]).intValue() == 5;
        });

        // Now let's try stopping the task
        BValue[] stopResult = BRunUtil.invoke(timerCompileResult, "stopTask", new BValue[]{new BString(taskId)});
        assertNull(stopResult[0], "Task stopping resulted in an error");

        // One more check to see whether the task really stopped
        BValue[] counts = BRunUtil.invoke(timerCompileResult, "getCount");
        assertEquals(((BInteger) counts[0]).intValue(), -1, "Count hasn't been reset");
    }

    @Test(description = "Tests running a timer where the onTrigger function generates an error")
    public void testWithErrorFn() {
        CompileResult timerCompileResult = BCompileUtil.compile("test-src/task/timer-error.bal");

        int initialDelay = 500;
        int interval = 1000;
        String errMsg = "Timer error";
        BValue[] returns =
                BRunUtil.invoke(timerCompileResult, "scheduleTimerWithError",
                        new BValue[]{new BInteger(initialDelay), new BInteger(interval), new BString(errMsg)});
        String taskId = returns[0].stringValue();
        assertNotEquals(taskId, "", "Invalid task ID");  // A non-null task ID should be returned
        assertEquals(returns.length, 2); // There should be no errors
        assertNull(returns[1], "Ballerina scheduler returned an error");
        await().atMost(5, SECONDS).until(() -> {
            BValue[] error = BRunUtil.invoke(timerCompileResult, "getError");
            return error != null && error[0] != null && !error[0].stringValue().equals("");
        });

        // Now test whether the onError Ballerina function got called
        BValue[] error = BRunUtil.invoke(timerCompileResult, "getError");
        assertNotNull(error[0], "Expected error not returned.");
        assertEquals(error[0].stringValue(), errMsg, "Expected error message not returned.");
    }

    /*

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithoutDelay() {
        consoleOutput.reset();
        int taskId;
        int interval = 10000;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.add(Calendar.MILLISECOND, interval);
        BValue[] args = {new BInteger(0), new BInteger(interval), new BInteger(25000)};
        BValue[] returns = BRunUtil.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = getTimerLog(taskId);
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_TIMER, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.TIMER_SUCCESS_MESSAGE));
        Assert.assertTrue(isAcceptable(interval, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithEmptyResponse() {
        consoleOutput.reset();
        int taskId;
        int interval = 10000;
        BValue[] args = {new BInteger(0), new BInteger(interval), new BInteger(15000)};
        BValue[] returns = BRunUtil.invoke(timerWithEmptyResponseCompileResult,
                TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithoutOnErrorFunction() {
        consoleOutput.reset();
        int taskId;
        int interval = 10000;
        BValue[] args = {new BInteger(0), new BInteger(interval), new BInteger(15000)};
        BValue[] returns = BRunUtil.invoke(timerWithoutOnErrorFunctionCompileResult,
                TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithInvalidInput() {
        int taskId;
        int interval = 0;
        BValue[] args = {new BInteger(0), new BInteger(interval), new BInteger(5000)};
        BValue[] returns = BRunUtil.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function with invalid input")
    public void testTaskWithInputOutOfRange() {
        int taskId;
        BValue[] args = {new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(32), new BInteger(-1),
                new BInteger(0)};
        BValue[] returns = BRunUtil
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function with invalid input")
    public void testTaskWithInvalidInput() {
        int taskId;
        BValue[] args = {new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(30), new BInteger(1),
                new BInteger(0)};
        BValue[] returns = BRunUtil
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertEquals(taskId, -1);
    }

    @Test(dependsOnMethods = "testScheduleAppointmentEveryMinute",
            description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTimer() {
        BValue[] args = {new BInteger(taskIdEM), new BInteger(10000)};
        BValue[] returns = BRunUtil.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("true", returns[0].stringValue());
        Assert.assertTrue(!TaskScheduler.isTheTaskRunning(taskIdEM));
    }

    @Test(description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTimerWithInvalidId() {
        int taskId = -1;
        BValue[] args = {new BInteger(taskId), new BInteger(1000)};
        BValue[] returns = BRunUtil.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("false", returns[0].stringValue());
    }

    @Test(description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTimerWithNonexistentId() {
        int taskId = 1000000000;
        BValue[] args = {new BInteger(taskId), new BInteger(1000)};
        BValue[] returns = BRunUtil.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("false", returns[0].stringValue());
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testTimerWithMultipleWorkers() {
        consoleOutput.reset();
        int interval = 1000;
        int sleepTime = 9000;
        BValue[] args = {new BInteger(0), new BInteger(interval), new BInteger(sleepTime)};
        BValue[] returns = BRunUtil.invoke(timerMWCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        int taskId = Integer.parseInt(returns[0].stringValue());
        int i = 0;
        Pattern p = Pattern.compile(TestConstant.TIMER_SUCCESS_MESSAGE);
        Matcher m = p.matcher(consoleOutput.toString());
        while (m.find()) {
            i++;
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue((sleepTime / interval * 2 - i) <= 1);
    }

    private long getCalculatedDuration(String scheduler, int taskId, String log) {
        String logEntry = log.substring(log.lastIndexOf(scheduler + taskId + Constant.DELAY_HINT));
        return Long.parseLong(
                logEntry.substring((scheduler + taskId + Constant.DELAY_HINT).length(), logEntry.indexOf("]")));
    }

    private boolean isAcceptable(long expectedDuration, long calculatedDuration) {
        return Math.abs(expectedDuration - calculatedDuration) <= 1000;
    }*/
}
