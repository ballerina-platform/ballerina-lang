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
import org.ballerinalang.nativeimpl.task.Constant;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

/**
 * Tests for Task related functions
 */
public class TaskTest {
    private CompileResult timerCompileResult;
    private CompileResult appointmentCompileResult;
    private CompileResult stopTaskCompileResult;
    private static final Log log = LogFactory.getLog(TaskTest.class);

    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();

    @BeforeClass public void setup() {
        timerCompileResult = BTestUtils.compile("test-src/task/task-timer.bal");
        appointmentCompileResult = BTestUtils.compile("test-src/task/task-appointment.bal");
        stopTaskCompileResult = BTestUtils.compile("test-src/task/task-stop.bal");

        System.setErr(new PrintStream(consoleOutput));
        System.setProperty("java.util.logging.config.file",
                ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
    }

    @Test(priority = 1, description = "Test for 'scheduleAppointment' function to trigger every minute")
    public void testScheduleAppointmentEveryMinute() {
        consoleOutput.reset();
        int taskId;
        long expectedDuration = 60000;
        createDirectoryWitFile();
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(130000) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.APPOINTMENT_SUCCESS_MESSAGE));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every hour")
    public void testScheduleAppointmentEveryHour() {
        consoleOutput.reset();
        int taskId;
        long expectedDuration = 3600000;
        createDirectoryWitFile();
        BValue[] args = { new BInteger(0), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every 5 minutes")
    public void testScheduleAppointmentEvery5Minutes() {
        consoleOutput.reset();
        int taskId;
        long expectedDuration = 300000;
        createDirectoryWitFile();
        BValue[] args = { new BInteger(5), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 10AM everyday")
    public void testScheduleAppointmentEveryday10AM() {
        consoleOutput.reset();
        int taskId;
        int hour = 10;
        createDirectoryWitFile();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.set(Calendar.HOUR, hour);
        modifiedTime.set(Calendar.MINUTE, 0);
        modifiedTime.set(Calendar.SECOND, 0);
        modifiedTime.set(Calendar.MILLISECOND, 0);
        modifiedTime.set(Calendar.AM_PM, 0);
        if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.DATE, 1);
        }
        long expectedDuration = calculateDifference(currentTime, modifiedTime);
        BValue[] args = { new BInteger(0), new BInteger(hour), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every minute on Mondays")
    public void testScheduleAppointmentEveryMinuteOnMondays() {
        consoleOutput.reset();
        int taskId;
        int dayOfWeek = 2;
        long expectedDuration = 60000;
        createDirectoryWitFile();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            int days = currentTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek ?
                    7 - (currentTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek) :
                    dayOfWeek - currentTime.get(Calendar.DAY_OF_WEEK);
            modifiedTime.add(Calendar.DATE, days);
            modifiedTime.set(Calendar.HOUR, 0);
            modifiedTime.set(Calendar.MINUTE, 0);
            modifiedTime.set(Calendar.SECOND, 0);
            modifiedTime.set(Calendar.MILLISECOND, 0);
            modifiedTime.set(Calendar.AM_PM, 0);
            if (modifiedTime.before(currentTime)) {
                modifiedTime.add(Calendar.DATE, 7);
            }
            expectedDuration = calculateDifference(currentTime, modifiedTime);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 2PM on Mondays")
    public void testScheduleAppointment2PMOnMondays() {
        consoleOutput.reset();
        int taskId;
        createDirectoryWitFile();
        BValue[] args = { new BInteger(0), new BInteger(14), new BInteger(2), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every minute in January")
    public void testScheduleAppointmentEveryMinuteInJanuary() {
        consoleOutput.reset();
        int taskId;
        int month = 0;
        long expectedDuration = 60000;
        createDirectoryWitFile();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.MONTH) != month) {
            if (modifiedTime.get(Calendar.MONTH) > month) {
                modifiedTime.add(Calendar.YEAR, 1);
            }
            modifiedTime.set(Calendar.MONTH, month);
            modifiedTime.set(Calendar.DATE, 1);
            modifiedTime.set(Calendar.HOUR, 0);
            modifiedTime.set(Calendar.MINUTE, 0);
            modifiedTime.set(Calendar.SECOND, 0);
            modifiedTime.set(Calendar.MILLISECOND, 0);
            modifiedTime.set(Calendar.AM_PM, 0);
            expectedDuration = calculateDifference(currentTime, modifiedTime);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(month),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 2PM on Mondays in January")
    public void testScheduleAppointment2PMOnTuesdaysInJanuary() {
        consoleOutput.reset();
        int taskId;
        createDirectoryWitFile();
        BValue[] args = { new BInteger(0), new BInteger(14), new BInteger(3), new BInteger(-1), new BInteger(0),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger at 10AM and stop at 10.59AM")
    public void testScheduleAppointmentStartAt10AMEndAt1059AM() {
        consoleOutput.reset();
        int taskId;
        int hour = 10;
        createDirectoryWitFile();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.set(Calendar.HOUR, hour);
        modifiedTime.set(Calendar.MINUTE, 0);
        modifiedTime.set(Calendar.SECOND, 0);
        modifiedTime.set(Calendar.MILLISECOND, 0);
        modifiedTime.set(Calendar.AM_PM, 0);
        if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.DATE, 1);
        }
        long expectedDuration = calculateDifference(currentTime, modifiedTime);
        BValue[] args = { new BInteger(-1), new BInteger(10), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        String logEntry = log.substring(log.lastIndexOf(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT));
        String logEntryWithRuntime = logEntry.substring(logEntry.lastIndexOf(Constant.SCHEDULER_LIFETIME_HINT));
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_TIMER, taskId, log);
        int period = Integer.parseInt(logEntryWithRuntime
                .substring(Constant.SCHEDULER_LIFETIME_HINT.length(), logEntryWithRuntime.indexOf("]")));
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
        Assert.assertTrue(period == calculatedDuration + 59 * 60000);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 25th of every month")
    public void testScheduleAppointment25thOfEachMonth() {
        consoleOutput.reset();
        int taskId;
        int dayOfMonth = 25;
        long expectedDuration = 60000;
        createDirectoryWitFile();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
            modifiedTime.set(Calendar.HOUR, 0);
            modifiedTime.set(Calendar.MINUTE, 0);
            modifiedTime.set(Calendar.SECOND, 0);
            modifiedTime.set(Calendar.MILLISECOND, 0);
            modifiedTime.set(Calendar.AM_PM, 0);
            if (dayOfMonth > modifiedTime.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                modifiedTime.add(Calendar.MONTH, 1);
                modifiedTime.set(Calendar.DATE, dayOfMonth);
            } else {
                modifiedTime.set(Calendar.DATE, dayOfMonth);
                if (modifiedTime.before(currentTime)) {
                    modifiedTime.add(Calendar.MONTH, 1);
                }
            }
            expectedDuration = calculateDifference(currentTime, modifiedTime);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(dayOfMonth),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger at midnight on Mondays and 15th of every "
            + "month")
    public void testScheduleAppointmentMidnightOnMondayAnd15thOfEachMonth() {
        consoleOutput.reset();
        int taskId;
        int dayOfWeek = 2;
        int dayOfMonth = 15;
        createDirectoryWitFile();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTimeByDOW = (Calendar) currentTime.clone();
        Calendar modifiedTimeByDOM = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            int days = currentTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek ?
                    7 - (currentTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek) :
                    dayOfWeek - currentTime.get(Calendar.DAY_OF_WEEK);
            modifiedTimeByDOW.add(Calendar.DATE, days);
        }
        modifiedTimeByDOW.set(Calendar.HOUR, 0);
        modifiedTimeByDOW.set(Calendar.MINUTE, 0);
        modifiedTimeByDOW.set(Calendar.SECOND, 0);
        modifiedTimeByDOW.set(Calendar.MILLISECOND, 0);
        modifiedTimeByDOW.set(Calendar.AM_PM, 0);
        if (modifiedTimeByDOW.before(currentTime)) {
            modifiedTimeByDOW.add(Calendar.DATE, 7);
        }
        long expectedDurationByDOW = calculateDifference(currentTime, modifiedTimeByDOW);
        modifiedTimeByDOM.set(Calendar.HOUR, 0);
        modifiedTimeByDOM.set(Calendar.MINUTE, 0);
        modifiedTimeByDOM.set(Calendar.SECOND, 0);
        modifiedTimeByDOM.set(Calendar.MILLISECOND, 0);
        modifiedTimeByDOM.set(Calendar.AM_PM, 0);
        if (dayOfMonth > modifiedTimeByDOM.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            modifiedTimeByDOM.add(Calendar.MONTH, 1);
            modifiedTimeByDOM.set(Calendar.DATE, dayOfMonth);
        } else {
            modifiedTimeByDOM.set(Calendar.DATE, dayOfMonth);
            if (modifiedTimeByDOM.before(currentTime)) {
                modifiedTimeByDOM.add(Calendar.MONTH, 1);
            }
        }
        long expectedDurationByDOM = calculateDifference(currentTime, modifiedTimeByDOM);
        long expectedDuration =
                expectedDurationByDOW < expectedDurationByDOM ? expectedDurationByDOW : expectedDurationByDOM;

        BValue[] args = { new BInteger(0), new BInteger(0), new BInteger(dayOfWeek), new BInteger(dayOfMonth),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BTestUtils
                .invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_APPOINTMENT, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(isAcceptable(expectedDuration, calculatedDuration));
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithDelay() {
        consoleOutput.reset();
        int taskId;
        int initialDelay = 1000;
        int interval = 10000;
        BValue[] args = { new BInteger(initialDelay), new BInteger(interval), new BInteger(25000) };
        BValue[] returns = BTestUtils.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        String firstLogEntry = log.substring(log.indexOf(taskId + Constant.DELAY_HINT));
        long firstCalculatedDuration = Long.parseLong(firstLogEntry
                .substring((Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT).length(),
                        firstLogEntry.indexOf("]")));
        String lastLogEntry = log.substring(log.lastIndexOf(taskId + Constant.DELAY_HINT));
        if (lastLogEntry.contains(Constant.SCHEDULER_LIFETIME_HINT)) {
            log.substring(log.lastIndexOf(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT));
        }
        long lastCalculatedDuration = Long.parseLong(lastLogEntry
                .substring((Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT).length(), lastLogEntry.indexOf("]")));
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.TIMER_SUCCESS_MESSAGE));
        Assert.assertTrue(isAcceptable(initialDelay, firstCalculatedDuration));
        Assert.assertTrue(isAcceptable(interval, lastCalculatedDuration));
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithoutDelay() {
        consoleOutput.reset();
        int taskId;
        int interval = 10000;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.add(Calendar.MILLISECOND, interval);
        BValue[] args = { new BInteger(0), new BInteger(interval), new BInteger(25000) };
        BValue[] returns = BTestUtils.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        String log = consoleOutput.toString();
        long calculatedDuration = getCalculatedDuration(Constant.PREFIX_TIMER, taskId, log);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.TIMER_SUCCESS_MESSAGE));
        Assert.assertTrue(isAcceptable(interval, calculatedDuration));
    }

    @Test(description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTask() {
        //Schedules the timer with the interval 10seconds and stops it after some time
        BValue[] args = { new BInteger(0), new BInteger(10000) };
        BValue[] returns = BTestUtils.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("true", returns[0].stringValue());
    }

    private void createDirectoryWitFile() {
        try {
            File dir = new File("/tmp/tmpDir");
            dir.mkdir();
            File.createTempFile("test", "txt", new File(dir.getAbsolutePath()));
        } catch (IOException e) {
            log.error("Unable to create the test file: " + e.getMessage());
        }
    }

    private long getCalculatedDuration(String scheduler, int taskId, String log) {
        String logEntry = log.substring(log.lastIndexOf(scheduler + taskId + Constant.DELAY_HINT));
        return Long.parseLong(
                logEntry.substring((scheduler + taskId + Constant.DELAY_HINT).length(), logEntry.indexOf("]")));
    }

    private boolean isAcceptable(long expectedDuration, long calculatedDuration) {
        return Math.abs(expectedDuration - calculatedDuration) <= 1000;
    }

    private long calculateDifference(Calendar calendar1, Calendar calendar2) {
        ZoneId currentZone = ZoneId.systemDefault();
        LocalDateTime localTime1 = LocalDateTime.ofInstant(calendar1.toInstant(), currentZone);
        ZonedDateTime zonedTime1 = ZonedDateTime.of(localTime1, currentZone);
        LocalDateTime localTime2 = LocalDateTime.ofInstant(calendar2.toInstant(), currentZone);
        ZonedDateTime zonedTime2 = ZonedDateTime.of(localTime2, currentZone);
        Duration duration = Duration.between(zonedTime1, zonedTime2);
        return duration.toMillis();
    }
}
