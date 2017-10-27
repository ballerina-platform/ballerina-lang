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
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.task.Constant;
import org.ballerinalang.nativeimpl.task.SchedulingFailedException;
import org.ballerinalang.nativeimpl.task.TaskScheduler;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.nativeimpl.task.TaskScheduler.findExecutionTime;
import static org.ballerinalang.nativeimpl.task.TaskScheduler.getExecutionLifeTime;

/**
 * Tests for Task related functions
 */
public class TaskTest {
    private CompileResult timerCompileResult;
    private CompileResult appointmentCompileResult;
    private CompileResult stopTaskCompileResult;
    private CompileResult timerMWCompileResult;
    private CompileResult timerWithErrorCompileResult;
    private CompileResult timerWithoutOnErrorFunctionCompileResult;
    private static final Log log = LogFactory.getLog(TaskTest.class);

    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
    private int taskIdEM;

    @BeforeClass
    public void setup() {
        timerCompileResult = BCompileUtil.compile("test-src/task/task-timer.bal");
        appointmentCompileResult = BCompileUtil.compile("test-src/task/task-appointment.bal");
        stopTaskCompileResult = BCompileUtil.compile("test-src/task/task-stop.bal");
        timerMWCompileResult = BCompileUtil.compile("test-src/task/task-timer-multiple-workers.bal");
        timerWithErrorCompileResult = BCompileUtil.compile("test-src/task/task-timer-with-error.bal");
        timerWithoutOnErrorFunctionCompileResult = BCompileUtil
                .compile("test-src/task/task-timer-without-onErrorFunction.bal");

        System.setErr(new PrintStream(consoleOutput));
        System.setProperty("java.util.logging.config.file",
                ClassLoader.getSystemResource("logging.properties").getPath());
        System.setProperty("java.util.logging.manager", "org.ballerinalang.logging.BLogManager");
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger now and run every minute in an hour")
    public void testScheduleAppointmentToStartNowAndRunOneHour() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR);
        BValue[] args = { new BInteger(-1), new BInteger(hour), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(65000) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(priority = 1, description = "Test for 'scheduleAppointment' function to trigger every minute")
    public void testScheduleAppointmentEveryMinute() {
        consoleOutput.reset();
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.add(Calendar.MINUTE, 1);
        modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, -1, -1);
        createDirectoryWithFile();
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(65000) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskIdEM = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskIdEM, -1, -1, -1, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskIdEM, -1);
        Assert.assertEquals(calculateDifference(modifiedTime, executionTime) % 60000, 0);
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.APPOINTMENT_SUCCESS_MESSAGE));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every hour")
    public void testScheduleAppointmentEveryHour() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, 0, -1);
        modifiedTime.add(Calendar.HOUR, 1);
        BValue[] args = { new BInteger(0), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, 0, -1, -1, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every 5th minute")
    public void testScheduleAppointmentEvery5thMinute() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, 5, -1);
        if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.HOUR, 1);
        }
        BValue[] args = { new BInteger(5), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, 5, -1, -1, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 10AM everyday")
    public void testScheduleAppointmentEveryday10AM() {
        int taskId;
        int hour = 10;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, hour);
        if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.DATE, 1);
        }
        BValue[] args = { new BInteger(0), new BInteger(hour), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, 0, hour, -1, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every minute on Mondays")
    public void testScheduleAppointmentEveryMinuteOnMondays() {
        int taskId;
        int dayOfWeek = 2;
        long expectedDuration = 0;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
            modifiedTime.add(Calendar.MINUTE, 1);
            modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, -1, -1);
        } else {
            modifiedTime = modifyTheCalendarAndCalculateTimeByDayOfWeek(modifiedTime, currentTime, dayOfWeek);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, dayOfWeek, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 2PM on Mondays")
    public void testScheduleAppointment2PMOnMondays() {
        int taskId;
        int dayOfWeek = 2;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            int days = currentTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek ?
                    7 - (currentTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek) :
                    dayOfWeek - currentTime.get(Calendar.DAY_OF_WEEK);
            modifiedTime.add(Calendar.DATE, days);
        }
        modifiedTime = setCalendarFields(modifiedTime, 1, 0, 0, 0, 2);
        if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.DATE, 7);
        }
        BValue[] args = { new BInteger(0), new BInteger(14), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, 0, 14, dayOfWeek, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every minute on tomorrow")
    public void testScheduleAppointmentEveryMinuteTomorrow() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.add(Calendar.DATE, 1);
        int dayOfWeek = clonedCalendar.get(Calendar.DAY_OF_WEEK);
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
            modifiedTime.add(Calendar.MINUTE, 1);
            modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, -1, -1);
        } else {
            modifiedTime = modifyTheCalendarAndCalculateTimeByDayOfWeek(modifiedTime, currentTime, dayOfWeek);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, dayOfWeek, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every minute in January")
    public void testScheduleAppointmentEveryMinuteInJanuary() {
        int taskId;
        int month = 0;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.MONTH) != month) {
            modifiedTime = modifyTheCalendarAndCalculateTimeByMonth(modifiedTime, currentTime, month);
        } else {
            modifiedTime.add(Calendar.MINUTE, 1);
            modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, -1, -1);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(month),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, -1, -1, month);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger next week the hour is one hour back "
            + "from the current hour")
    public void testScheduleAppointmentBeforeAnHourInNextWeek() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.add(Calendar.HOUR, -1);
        int hour = clonedCalendar.get(Calendar.AM_PM) == 0 ?
                clonedCalendar.get(Calendar.HOUR) :
                clonedCalendar.get(Calendar.HOUR) + clonedCalendar.get(Calendar.HOUR);
        int dayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK);
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, hour);
        if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.DATE, 7);
        }
        BValue[] args = { new BInteger(0), new BInteger(hour), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, 0, hour, dayOfWeek, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger next week. The day of week is "
            + "one day back from the current day of week")
    public void testScheduleAppointmentBeforeADOWInNextWeek() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.add(Calendar.DATE, 6);
        int dayOfWeek = clonedCalendar.get(Calendar.DAY_OF_WEEK);
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.setTime(clonedCalendar.getTime());
        modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, 0);
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, dayOfWeek, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger every minute in next month")
    public void testScheduleAppointmentEveryMinuteInNextMonth() {
        int taskId;
        int month = (Calendar.getInstance()).get(Calendar.MONTH) + 1;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.MONTH) != month) {
            modifiedTime = modifyTheCalendarAndCalculateTimeByMonth(modifiedTime, currentTime, month);
        } else {
            modifiedTime.add(Calendar.MINUTE, 1);
            modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, -1, -1);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(month),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, -1, -1, month);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger next hour from current in next month")
    public void testScheduleAppointmentNextHourInNextMonth() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.add(Calendar.HOUR, 1);
        clonedCalendar.set(Calendar.DATE, 1);
        clonedCalendar.add(Calendar.MONTH, 1);
        int hour = clonedCalendar.get(Calendar.AM_PM) == 0 ?
                clonedCalendar.get(Calendar.HOUR) :
                clonedCalendar.get(Calendar.HOUR) + 12;
        int month = clonedCalendar.get(Calendar.MONTH);
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.setTime(clonedCalendar.getTime());
        modifiedTime = setCalendarFields(modifiedTime, clonedCalendar.get(Calendar.AM_PM), 0, 0, 0, -1);
        BValue[] args = { new BInteger(-1), new BInteger(hour), new BInteger(-1), new BInteger(-1),
                new BInteger(month), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, hour, -1, -1, month);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger next day of week in next month")
    public void testScheduleAppointmentOnNextDOWInNextMonth() {
        testAppointmentWithDifferentDOW('+', 1);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger before one day of week in next month")
    public void testScheduleAppointmentOnDifferentBeforeOneDOWInNextMonth() {
        testAppointmentWithDifferentDOW('-', -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger before an hour "
            + "and a month from current and the year is next year")
    public void testScheduleAppointmentBeforeAnHourAndOneMonthInNextYear() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.add(Calendar.HOUR, -1);
        clonedCalendar.add(Calendar.MONTH, -1);
        clonedCalendar.set(Calendar.YEAR, currentTime.get(Calendar.YEAR) + 1);
        int hour = clonedCalendar.get(Calendar.AM_PM) == 0 ?
                clonedCalendar.get(Calendar.HOUR) :
                clonedCalendar.get(Calendar.HOUR) + 12;
        int dayOfMonth = clonedCalendar.get(Calendar.DAY_OF_MONTH);
        int month = clonedCalendar.get(Calendar.MONTH);
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.setTime(clonedCalendar.getTime());
        modifiedTime = setCalendarFields(modifiedTime, clonedCalendar.get(Calendar.AM_PM), 0, 0, 0, -1);
        BValue[] args = { new BInteger(-1), new BInteger(hour), new BInteger(-1), new BInteger(dayOfMonth),
                new BInteger(month), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, hour, -1, dayOfMonth, month);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger on 31st day")
    public void testScheduleAppointmentOn31st() {
        int taskId;
        int dayOfMonth = 31;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
            modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, 0);
            modifiedTime = modifyTheCalendarByDayOfMonth(modifiedTime, currentTime, dayOfMonth);
        } else {
            modifiedTime.add(Calendar.MINUTE, 1);
            modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, -1, -1);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(dayOfMonth),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, -1, dayOfMonth, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 2PM on Tuesdays in January")
    public void testScheduleAppointment2PMOnTuesdaysInJanuary() {
        int taskId;
        BValue[] args = { new BInteger(0), new BInteger(14), new BInteger(3), new BInteger(-1), new BInteger(0),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 2PM on one day of week before in January")
    public void testScheduleAppointment2PMOnOneDayOfWeekBeforeInJanuary() {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        if (currentTime.get(Calendar.MONTH) != 0) {
            currentTime.set(Calendar.DATE, 1);
            currentTime.set(Calendar.MONTH, 0);
            currentTime.add(Calendar.YEAR, 1);
        }
        int dayOfWeek = currentTime.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : currentTime.get(Calendar.DAY_OF_WEEK) - 1;
        BValue[] args = { new BInteger(0), new BInteger(14), new BInteger(dayOfWeek), new BInteger(-1), new BInteger(0),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger at 10AM and stop at 10.59AM")
    public void testScheduleAppointmentStartAt10AMEndAt1059AM() {
        int taskId;
        int hour = 10;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, hour);
        long expectedPeriod;
        long delay = 0;
        if (currentTime.get(Calendar.HOUR) == hour) {
            long difference = currentTime.getTimeInMillis() - modifiedTime.getTimeInMillis();
            Calendar clonedTime = (Calendar) currentTime.clone();
            clonedTime.add(Calendar.MINUTE, 1);
            clonedTime = setCalendarFields(clonedTime, 0, 0, 0, -1, -1);
            long calculatedDelay = clonedTime.getTimeInMillis() - currentTime.getTimeInMillis();
            expectedPeriod = calculatedDelay + Constant.LIFETIME - difference;
        } else if (modifiedTime.before(currentTime)) {
            modifiedTime.add(Calendar.DATE, 1);
            expectedPeriod = Constant.LIFETIME + (modifiedTime.getTimeInMillis() - currentTime.getTimeInMillis());
        } else {
            expectedPeriod = Constant.LIFETIME + (modifiedTime.getTimeInMillis() - currentTime.getTimeInMillis());
        }
        BValue[] args = { new BInteger(-1), new BInteger(10), new BInteger(-1), new BInteger(-1), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, 10, -1, -1, -1);
        } catch (SchedulingFailedException e) {
        }
        if (currentTime.get(Calendar.HOUR) == hour) {
            Calendar clonedTime = (Calendar) currentTime.clone();
            clonedTime.add(Calendar.MINUTE, 1);
            clonedTime = setCalendarFields(clonedTime, 0, 0, 0, -1, -1);
            delay = clonedTime.getTimeInMillis() - currentTime.getTimeInMillis();
        } else {
            delay = executionTime.getTimeInMillis() - currentTime.getTimeInMillis();
        }
        long period = delay + getExecutionLifeTime(taskId);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
        Assert.assertTrue(isAcceptable(period, expectedPeriod));
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger 25th of every month")
    public void testScheduleAppointment25thOfEachMonth() {
        int taskId;
        int dayOfMonth = 25;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTime = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
            modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, 0);
            modifiedTime = modifyTheCalendarByDayOfMonth(modifiedTime, currentTime, dayOfMonth);
        } else {
            modifiedTime.add(Calendar.MINUTE, 1);
            modifiedTime = setCalendarFields(modifiedTime, -1, 0, 0, -1, -1);
        }
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(dayOfMonth),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, -1, dayOfMonth, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleAppointment' function to trigger at midnight on Mondays "
            + "and 15th of every month")
    public void testScheduleAppointmentMidnightOnMondayAnd15thOfEachMonth() {
        int taskId;
        int dayOfWeek = 2;
        int dayOfMonth = 15;
        Calendar currentTime = Calendar.getInstance();
        Calendar modifiedTimeByDOW = (Calendar) currentTime.clone();
        Calendar modifiedTimeByDOM = (Calendar) currentTime.clone();
        if (currentTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            int days = currentTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek ?
                    7 - (currentTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek) :
                    dayOfWeek - currentTime.get(Calendar.DAY_OF_WEEK);
            modifiedTimeByDOW.add(Calendar.DATE, days);
        }
        modifiedTimeByDOW = setCalendarFields(modifiedTimeByDOW, 0, 0, 0, 0, 0);
        if (modifiedTimeByDOW.before(currentTime)) {
            modifiedTimeByDOW.add(Calendar.DATE, 7);
        }
        modifiedTimeByDOM = setCalendarFields(modifiedTimeByDOM, 0, 0, 0, 0, 0);
        modifiedTimeByDOM = modifyTheCalendarByDayOfMonth(modifiedTimeByDOM, currentTime, dayOfMonth);
        Calendar expectedTime = modifiedTimeByDOW.getTimeInMillis() < modifiedTimeByDOM.getTimeInMillis() ?
                modifiedTimeByDOW :
                modifiedTimeByDOM;
        BValue[] args = { new BInteger(0), new BInteger(0), new BInteger(dayOfWeek), new BInteger(dayOfMonth),
                new BInteger(-1), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, 0, 0, dayOfWeek, dayOfMonth, -1);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), expectedTime.getTimeInMillis());
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithDelay() {
        consoleOutput.reset();
        int taskId;
        int initialDelay = 1000;
        int interval = 10000;
        BValue[] args = { new BInteger(initialDelay), new BInteger(interval), new BInteger(25000) };
        BValue[] returns = BRunUtil.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        int i = countOccurrences(consoleOutput.toString(), TestConstant.TIMER_SUCCESS_MESSAGE);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.TIMER_SUCCESS_MESSAGE));
        Assert.assertEquals(i, 3);
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
        BValue[] returns = BRunUtil.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue(consoleOutput.toString().contains(TestConstant.TIMER_SUCCESS_MESSAGE));
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithError() {
        int taskId;
        int interval = 10000;
        BValue[] args = { new BInteger(0), new BInteger(interval), new BInteger(15000) };
        BValue[] returns = BRunUtil.invoke(timerWithErrorCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithoutOnErrorFunction() {
        int taskId;
        int interval = 10000;
        BValue[] args = { new BInteger(0), new BInteger(interval), new BInteger(15000) };
        BValue[] returns = BRunUtil
                .invoke(timerWithoutOnErrorFunctionCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertNotEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testScheduleTimerWithInvalidInput() {
        int taskId;
        int interval = 0;
        BValue[] args = { new BInteger(0), new BInteger(interval), new BInteger(5000) };
        BValue[] returns = BRunUtil.invoke(timerCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function with invalid input")
    public void testTaskWithInputOutOfRange() {
        int taskId;
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(32), new BInteger(-1),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertEquals(taskId, -1);
    }

    @Test(description = "Test for 'scheduleAppointment' function with invalid input")
    public void testTaskWithInvalidInput() {
        int taskId;
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(-1), new BInteger(30), new BInteger(1),
                new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION, args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Assert.assertEquals(taskId, -1);
    }

    @Test(dependsOnMethods = "testScheduleAppointmentEveryMinute",
            description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTask() {
        BValue[] args = { new BInteger(taskIdEM), new BInteger(10000) };
        BValue[] returns = BRunUtil.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("true", returns[0].stringValue());
        Assert.assertTrue(!TaskScheduler.isTheTaskRunning(taskIdEM));
    }

    @Test(description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTaskWithInvalidId() {
        int taskId = -1;
        BValue[] args = { new BInteger(taskId), new BInteger(1000) };
        BValue[] returns = BRunUtil.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("false", returns[0].stringValue());
    }

    @Test(description = "Test for 'stopTask' function which is implemented in ballerina.task package")
    public void testStopTaskWithNonexistentId() {
        int taskId = 1000000000;
        BValue[] args = { new BInteger(taskId), new BInteger(1000) };
        BValue[] returns = BRunUtil.invoke(stopTaskCompileResult, "stopTask", args);
        Assert.assertEquals("false", returns[0].stringValue());
    }

    @Test(description = "Test for 'scheduleTimer' function which is implemented in ballerina.task package")
    public void testTimerWithMultipleWorkers() {
        consoleOutput.reset();
        int interval = 1000;
        int sleepTime = 9000;
        BValue[] args = { new BInteger(0), new BInteger(interval), new BInteger(sleepTime) };
        BValue[] returns = BRunUtil.invoke(timerMWCompileResult, TestConstant.TIMER_ONTRIGGER_FUNCTION, args);
        int taskId = Integer.parseInt(returns[0].stringValue());
        int i = countOccurrences(consoleOutput.toString(), TestConstant.TIMER_SUCCESS_MESSAGE);
        Assert.assertNotEquals(taskId, -1);
        Assert.assertTrue((sleepTime / interval * 2 - i) <= 1);
    }

    private int countOccurrences(String consoleOutput, String message) {
        int i = 0;
        Pattern p = Pattern.compile(message);
        Matcher m = p.matcher(consoleOutput);
        while (m.find()) {
            i++;
        }
        return i;
    }

    private void testAppointmentWithDifferentDOW(char c, int days) {
        int taskId;
        Calendar currentTime = Calendar.getInstance();
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.add(Calendar.MONTH, 1);
        clonedCalendar.set(Calendar.DATE, 1);
        if (clonedCalendar.get(Calendar.DAY_OF_WEEK) == currentTime.get(Calendar.DAY_OF_WEEK)) {
            clonedCalendar.add(Calendar.DATE, days);
        } else if (c == '+' && clonedCalendar.get(Calendar.DAY_OF_WEEK) < currentTime.get(Calendar.DAY_OF_WEEK)) {
            days = currentTime.get(Calendar.DAY_OF_WEEK) - clonedCalendar.get(Calendar.DAY_OF_WEEK) + 1;
            clonedCalendar.add(Calendar.DATE, days);
        } else if (c == '-' && clonedCalendar.get(Calendar.DAY_OF_WEEK) > currentTime.get(Calendar.DAY_OF_WEEK)) {
            days = 7 - (clonedCalendar.get(Calendar.DAY_OF_WEEK) - currentTime.get(Calendar.DAY_OF_WEEK)) - 1;
            clonedCalendar.add(Calendar.DATE, days);
        }
        int dayOfWeek = clonedCalendar.get(Calendar.DAY_OF_WEEK);
        int month = clonedCalendar.get(Calendar.MONTH);
        Calendar modifiedTime = (Calendar) currentTime.clone();
        modifiedTime.setTime(clonedCalendar.getTime());
        modifiedTime = setCalendarFields(modifiedTime, 0, 0, 0, 0, 0);
        BValue[] args = { new BInteger(-1), new BInteger(-1), new BInteger(dayOfWeek), new BInteger(-1),
                new BInteger(month), new BInteger(0) };
        BValue[] returns = BRunUtil.invoke(appointmentCompileResult, TestConstant.APPOINTMENT_ONTRIGGER_FUNCTION,
                args);
        taskId = Integer.parseInt(returns[0].stringValue());
        Calendar executionTime = Calendar.getInstance();
        try {
            executionTime = findExecutionTime(taskId, -1, -1, dayOfWeek, -1, month);
        } catch (SchedulingFailedException e) {
        }
        Assert.assertNotEquals(taskId, -1);
        Assert.assertEquals(executionTime.getTimeInMillis(), modifiedTime.getTimeInMillis());
    }

    private void createDirectoryWithFile() {
        try {
            File dir = new File(TestConstant.DIRECTORY_PATH);
            dir.mkdir();
            File.createTempFile("test", "txt", new File(dir.getAbsolutePath()));
        } catch (IOException e) {
            log.error("Unable to create the test file: " + e.getMessage());
        }
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

    private Calendar setCalendarFields(Calendar calendar, int ampm, int milliseconds, int seconds, int minutes,
                                       int hours) {
        if (hours != -1) {
            calendar.set(Calendar.HOUR, hours);
        }
        if (minutes != -1) {
            calendar.set(Calendar.MINUTE, minutes);
        }
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        if (ampm != -1) {
            calendar.set(Calendar.AM_PM, ampm);
        }
        return calendar;
    }

    private Calendar modifyTheCalendarByDayOfMonth(Calendar calendar, Calendar currentTime, int dayOfMonth) {
        if (dayOfMonth > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DATE, dayOfMonth);
        } else {
            calendar.set(Calendar.DATE, dayOfMonth);
            if (calendar.before(currentTime)) {
                calendar.add(Calendar.MONTH, 1);
            }
        }
        return calendar;
    }

    private Calendar modifyTheCalendarAndCalculateTimeByDayOfWeek(Calendar calendar, Calendar currentTime,
                                                                  int dayOfWeek) {
        if (currentTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            int days = currentTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek ?
                    7 - (currentTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek) :
                    dayOfWeek - currentTime.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.DATE, days);
            calendar = setCalendarFields(calendar, 0, 0, 0, 0, 0);
            if (calendar.before(currentTime)) {
                calendar.add(Calendar.DATE, 7);
            }
        }
        return calendar;
    }

    private Calendar modifyTheCalendarAndCalculateTimeByMonth(Calendar calendar, Calendar currentTime, int month) {
        if (calendar.get(Calendar.MONTH) > month) {
            calendar.add(Calendar.YEAR, 1);
        }
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, 1);
        calendar = setCalendarFields(calendar, 0, 0, 0, 0, 0);
        return calendar;
    }
}
