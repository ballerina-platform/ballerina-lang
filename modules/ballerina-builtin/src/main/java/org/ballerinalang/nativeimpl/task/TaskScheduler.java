/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Methods which are used for scheduling ballerina task.
 */
public class TaskScheduler {

    private static final Log log = LogFactory.getLog(TaskScheduler.class.getName());
    private static HashMap<Integer, Appointment> executorServiceMap = new HashMap<>();

    /**
     * Triggers the timer.
     *
     * @param ctx               The ballerina context.
     * @param taskId            The identifier of the task.
     * @param delay             The initial delay.
     * @param interval          The interval between two task executions.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     * @throws SchedulingFailedException
     */
    static void triggerTimer(Context ctx, int taskId, long delay, long interval, FunctionRefCPEntry onTriggerFunction,
                             FunctionRefCPEntry onErrorFunction) throws SchedulingFailedException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Constant.POOL_SIZE);
        try {
            final Runnable schedulerFunc = () -> {
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_TIMER + taskId + " starts the execution");
                }
                //Call the onTrigger function.
                callFunction(ctx, onTriggerFunction, onErrorFunction);
            };
            if (interval > 0) {
                //Schedule the task
                executorService.scheduleAtFixedRate(schedulerFunc, delay, interval, TimeUnit.MILLISECONDS);
                ctx.startTrackWorker();
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT + delay + "] and interval ["
                            + interval + "] MILLISECONDS");
                }
                //Add the executor service into the map.
                Appointment appointment = new Appointment();
                appointment.setExecutorService(executorService);
                appointment.setLifeTime(0L);
                executorServiceMap.put(taskId, appointment);
            } else {
                throw new SchedulingFailedException("The vale of interval is invalid");
            }
        } catch (RejectedExecutionException | IllegalArgumentException e) {
            throw new SchedulingFailedException("Error occurred while scheduling the timer: " + e.getMessage());
        }
    }

    /**
     * Triggers the appointment.
     *
     * @param ctx               The ballerina context.
     * @param taskId            The identifier of the task.
     * @param minute            The value of the minute in the appointment expression.
     * @param hour              The value of the hour in the appointment expression.
     * @param dayOfWeek         The value of the day of week in the appointment expression.
     * @param dayOfMonth        The value of the day of month in the appointment expression.
     * @param month             The value of the month in the appointment expression.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     * @throws SchedulingFailedException
     */
    static void triggerAppointment(Context ctx, int taskId, int minute, int hour, int dayOfWeek, int dayOfMonth,
                                   int month, FunctionRefCPEntry onTriggerFunction, FunctionRefCPEntry onErrorFunction)
            throws SchedulingFailedException {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Constant.POOL_SIZE);
        try {
            final Runnable schedulerFunc = () -> {
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_APPOINTMENT + taskId + " starts the execution");
                }
                try {
                    if (executorServiceMap.get(taskId) != null && executorServiceMap.get(taskId).getLifeTime() > 0) {
                        //Set the life time to 0 and trigger every minute.
                        Appointment appointment = executorServiceMap.get(taskId);
                        appointment.setLifeTime(0L);
                        executorServiceMap.put(taskId, appointment);
                        triggerAppointment(ctx, taskId, Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE, dayOfWeek,
                                dayOfMonth, month, onTriggerFunction, onErrorFunction);
                    } else {
                        triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                                onErrorFunction);
                    }
                } catch (SchedulingFailedException e) {
                    log.error(e.getMessage());
                }
                //Call the onTrigger function.
                callFunction(ctx, onTriggerFunction, onErrorFunction);
            };
            //Calculate the delay.
            long delay = calculateDelay(taskId, minute, hour, dayOfWeek, dayOfMonth, month);
            if (delay != -1) {
                //Schedule the task
                executorService.schedule(schedulerFunc, delay, TimeUnit.MILLISECONDS);
                ctx.startTrackWorker();
                //Get the execution life time.
                long period = executorServiceMap.get(taskId) != null ? executorServiceMap.get(taskId).getLifeTime()
                        : 0L;
                //Add the executor service into the map.
                Appointment appointment = new Appointment();
                appointment.setExecutorService(executorService);
                if (period > 0) {
                    //Calculate the actual execution lifetime from the delay and calculated value.
                    period = delay + period;
                    appointment.setLifeTime(period);
                    executorServiceMap.put(taskId, appointment);
                    //Trigger stop if the execution lifetime > 0.
                    stopExecution(taskId, period, ctx, minute, hour, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                            onErrorFunction);
                } else {
                    appointment.setLifeTime(0L);
                    executorServiceMap.put(taskId, appointment);
                }
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_APPOINTMENT + taskId + Constant.DELAY_HINT + delay + "] MILLISECONDS "
                            + Constant.SCHEDULER_LIFETIME_HINT + period + "]");
                }
            }
        } catch (RuntimeException | SchedulingFailedException e) {
            throw new SchedulingFailedException("Error occurred while scheduling the appointment: " + e.getMessage());
        }
    }

    /**
     * Stops the execution.
     *
     * @param taskId            The identifier of the task.
     * @param sPeriod           The delay to start the task shutdown function.
     * @param ctx               The ballerina context.
     * @param minute            The value of the minute in the appointment expression.
     * @param hour              The value of the hour in the appointment expression.
     * @param dayOfWeek         The value of the day of week in the appointment expression.
     * @param dayOfMonth        The value of the day of month in the appointment expression.
     * @param month             The value of the month in the appointment expression.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     * @throws SchedulingFailedException
     */
    private static void stopExecution(int taskId, long sPeriod, Context ctx, int minute, int hour, int dayOfWeek,
                                      int dayOfMonth, int month, FunctionRefCPEntry onTriggerFunction,
                                      FunctionRefCPEntry onErrorFunction)
            throws SchedulingFailedException {
        ScheduledExecutorService executorServiceToStopTheTask = Executors.newScheduledThreadPool(Constant.POOL_SIZE);
        ScheduledExecutorService task;
        if (executorServiceMap.get(taskId) == null) {
            throw new SchedulingFailedException("Unable to find the corresponding task");
        }
        task = executorServiceMap.get(taskId).getExecutorService();
        if (log.isDebugEnabled()) {
            log.debug("Attempting to stop the task: " + taskId);
        }
        final Runnable schedulerFunc = () -> {
            //Get the corresponding executor service from map.
            try {
                //Invoke shutdown of the executor service.
                task.shutdown();
                if (task.isShutdown()) {
                    //Remove the executor service from the map.
                    executorServiceMap.remove(taskId);
                    if (onTriggerFunction != null && sPeriod > 0) {
                        triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                                onErrorFunction);
                    } else {
                        ctx.endTrackWorker();
                    }
                } else {
                    throw new SchedulingFailedException("Unable to stop the task");
                }
            } catch (SecurityException e) {
                log.error("Unable to stop the task: " + e.getMessage());
            } catch (SchedulingFailedException e) {
                log.error(e.getMessage());
            }
        };
        executorServiceToStopTheTask.schedule(schedulerFunc, sPeriod, TimeUnit.MILLISECONDS);
    }

    /**
     * Calls the onTrigger and onError functions.
     *
     * @param ctx               The ballerina context.
     * @param onTriggerFunction The main function which will be triggered by the task.
     * @param onErrorFunction   The function which will be triggered in the error situation.
     */
    private static void callFunction(Context ctx, FunctionRefCPEntry onTriggerFunction,
                                     FunctionRefCPEntry onErrorFunction) {
        AbstractNativeFunction abstractNativeFunction = new AbstractNativeFunction() {
            @Override public BValue[] execute(Context context) {
                return new BValue[0];
            }
        };
        ProgramFile programFile = ctx.getProgramFile();
        //Create new instance of the context and set required properties.
        Context newContext = new Context(programFile);
        try {
            //Invoke the onTrigger function.
            BLangFunctions.invokeFunction(programFile, onTriggerFunction.getFunctionInfo(), null, newContext);
        } catch (BLangRuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Invoking the onError function");
            }
            BValue[] error = abstractNativeFunction.getBValues(new BString(e.getMessage()));
            //Call the onError function in case of error.
            if (onErrorFunction != null) {
                BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), error, newContext);
            } else {
                log.error("The onError function is not provided");
            }
        }
    }

    /**
     * Calculates the delay to schedule the appointment.
     *
     * @param taskId     The identifier of the task.
     * @param minute     The value of the minute in the appointment expression.
     * @param hour       The value of the hour in the appointment expression.
     * @param dayOfWeek  The value of the day of week in the appointment expression.
     * @param dayOfMonth The value of the day of month in the appointment expression.
     * @param month      The value of the month in the appointment expression.
     * @return delay which is used to schedule the appointment.
     */
    public static long calculateDelay(int taskId, int minute, int hour, int dayOfWeek, int dayOfMonth, int month)
            throws SchedulingFailedException {
        //Get the Calendar instance.
        Calendar currentTime = Calendar.getInstance();
        if (isInvalidInput(currentTime, minute, hour, dayOfWeek, dayOfMonth, month)) {
            //Validate the fields.
            throw new SchedulingFailedException("Wrong input");
        } else {
            //Clone the current time to another instance.
            Calendar executionStartTime = (Calendar) currentTime.clone();
            //Tune the execution start time by the value of minute.
            executionStartTime = modifyCalendarByCheckingMinute(currentTime, executionStartTime, minute, hour);
            //Tune the execution start time by the value of hour.
            executionStartTime = modifyCalendarByCheckingHour(currentTime, executionStartTime, minute, hour, dayOfWeek,
                    dayOfMonth);
            if (dayOfWeek != Constant.NOT_CONSIDERABLE && dayOfMonth != Constant.NOT_CONSIDERABLE) {
                //Clone the modified Calendar instances into two instances.
                Calendar newTimeAccordingToDOW = cloneCalendarAndSetTime(executionStartTime, dayOfWeek, dayOfMonth);
                Calendar newTimeAccordingToDOM = cloneCalendarAndSetTime(executionStartTime, dayOfWeek, dayOfMonth);
                //Modify the specific Calendar by the value of day of week.
                newTimeAccordingToDOW = modifyCalendarByCheckingDayOfWeek(currentTime, newTimeAccordingToDOW, dayOfWeek,
                        month);
                //Modify the specific Calendar by the value of day of month.
                newTimeAccordingToDOM = modifyCalendarByCheckingDayOfMonth(currentTime, newTimeAccordingToDOM,
                        dayOfMonth, month);
                //Modify both cloned Calendar instances by the value of month.
                newTimeAccordingToDOW = modifyCalendarByCheckingMonth(taskId, currentTime, newTimeAccordingToDOW,
                        minute, hour, dayOfWeek, dayOfMonth, month);
                newTimeAccordingToDOM = modifyCalendarByCheckingMonth(taskId, currentTime, newTimeAccordingToDOM,
                        minute, hour, dayOfWeek, dayOfMonth, month);
                //Find the nearest value from both and set the final execution time.
                executionStartTime = newTimeAccordingToDOW.before(newTimeAccordingToDOM) ?
                        newTimeAccordingToDOW :
                        newTimeAccordingToDOM;
            } else {
                //Tune the execution start time by the value of day of week, day of month and month respectively.
                executionStartTime = modifyCalendarByCheckingDayOfWeek(currentTime, executionStartTime, dayOfWeek,
                        month);
                executionStartTime = modifyCalendarByCheckingDayOfMonth(currentTime, executionStartTime, dayOfMonth,
                        month);
                executionStartTime = modifyCalendarByCheckingMonth(taskId, currentTime, executionStartTime, minute,
                        hour, dayOfWeek, dayOfMonth, month);
            }
            //Calculate the time difference in MILLI SECONDS.
            return calculateDifference(taskId, executionStartTime);
        }
    }

    /**
     * Checks the validity of the input.
     *
     * @param currentTime The Calendar instance with current time.
     * @param minute      The value of the minute in the appointment expression.
     * @param hour        The value of the hour in the appointment expression.
     * @param dayOfWeek   The value of the day of week in the appointment expression.
     * @param dayOfMonth  The value of the day of month in the appointment expression.
     * @param month       The value of the month in the appointment expression.
     * @return boolean value. 'true' if the input is valid else 'false'.
     */
    private static boolean isInvalidInput(Calendar currentTime, int minute, int hour, int dayOfWeek, int dayOfMonth,
                                          int month) {
        //Valid ranges: (minute :- 0 - 59, hour :- 0 - 23, dayOfWeek :- 1 - 7, dayOfMonth :- 1 - 31, month :- 0 - 11).
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.set(Calendar.MONTH, month);
        return minute > 59 || minute < Constant.NOT_CONSIDERABLE || hour > 23 || hour < Constant.NOT_CONSIDERABLE
                || dayOfWeek > 7 || dayOfWeek < Constant.NOT_CONSIDERABLE || dayOfWeek == 0 || dayOfMonth > 31
                || dayOfMonth < Constant.NOT_CONSIDERABLE || dayOfMonth == 0 || month > 11
                || month < Constant.NOT_CONSIDERABLE || dayOfMonth > clonedCalendar
                .getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Modifies the Calendar by checking the minute.
     *
     * @param currentTime        The Calendar instance with current time.
     * @param executionStartTime The modified Calendar instance.
     * @param minute             The value of the minute in the appointment expression.
     * @param hour               The value of the hour in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar modifyCalendarByCheckingMinute(Calendar currentTime, Calendar executionStartTime,
                                                           int minute, int hour) {
        if (minute == Constant.NOT_CONSIDERABLE && hour == Constant.NOT_CONSIDERABLE) {
            //Run every minute.
            executionStartTime.add(Calendar.MINUTE, 1);
        } else if (minute == Constant.NOT_CONSIDERABLE) {
            //Run at clock time at 0th minute with 59 minutes execution lifetime e.g start at 2AM and end at 2.59AM.
            executionStartTime.set(Calendar.MINUTE, 0);
        } else {
            //Run every hour at 0th minute or at 5th minute or at a clock time e.g: 2.30AM.
            executionStartTime = setCalendarFields(executionStartTime, Constant.NOT_CONSIDERABLE, minute,
                    Constant.NOT_CONSIDERABLE);
            if (minute != 0 && hour == Constant.NOT_CONSIDERABLE && executionStartTime.before(currentTime)) {
                //If the modified time is behind the current time and it is every hour at 0th minute.
                //or at 5th minute case, add an hour
                executionStartTime.add(Calendar.HOUR, 1);
            }
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the hour.
     *
     * @param currentTime        The Calendar instance with current time.
     * @param executionStartTime The modified Calendar instance.
     * @param minute             The value of the minute in the appointment expression.
     * @param hour               The value of the hour in the appointment expression.
     * @param dayOfWeek          The value of the day of week in the appointment expression.
     * @param dayOfMonth         The value of the day of month in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar modifyCalendarByCheckingHour(Calendar currentTime, Calendar executionStartTime, int minute,
                                                         int hour, int dayOfWeek, int dayOfMonth) {
        if (minute == 0 && hour == Constant.NOT_CONSIDERABLE) {
            //If the minute == 0 and hour = -1, execute every hour.
            executionStartTime.add(Calendar.HOUR, 1);
            executionStartTime = setCalendarFields(executionStartTime, Constant.NOT_CONSIDERABLE, 0,
                    Constant.NOT_CONSIDERABLE);
        } else if (hour != Constant.NOT_CONSIDERABLE) {
            /*If the hour >= 12, it's in the 24 hours system.
            Therefore, find the duration to be added to the 12 hours system.*/
            executionStartTime.set(Calendar.HOUR, hour >= 12 ? hour - 12 : hour);
            executionStartTime = setCalendarFields(executionStartTime, Constant.NOT_CONSIDERABLE,
                    Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE);
            if (hour <= 11) {
                //If the hour <= 11, it's first half of the day.
                executionStartTime.set(Calendar.AM_PM, Calendar.AM);
            } else {
                //It's second half of the day
                executionStartTime.set(Calendar.AM_PM, Calendar.PM);
            }
        }
        if (executionStartTime.before(currentTime) && dayOfWeek == Constant.NOT_CONSIDERABLE
                && dayOfMonth == Constant.NOT_CONSIDERABLE) {
            //If the modified time is behind the current time, add a day.
            executionStartTime.add(Calendar.DATE, 1);
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the day of week.
     *
     * @param currentTime        The Calendar instance with current time.
     * @param executionStartTime The modified Calendar instance.
     * @param dayOfWeek          The value of the day of week in the appointment expression.
     * @param month              The value of the month in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar modifyCalendarByCheckingDayOfWeek(Calendar currentTime, Calendar executionStartTime,
                                                              int dayOfWeek, int month) {
        int numberOfDaysToBeAdded = 0;
        if (dayOfWeek >= 1) {
            if (month == currentTime.get(Calendar.MONTH) || month == Constant.NOT_CONSIDERABLE) {
                /*If the provided value of the month is current month or no value considerable value is provided
                for month, calculate the number of days to be added.*/
                if (dayOfWeek < executionStartTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = 7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek);
                } else if (dayOfWeek > executionStartTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK);
                } else if (executionStartTime.get(Calendar.DAY_OF_WEEK) == dayOfWeek && executionStartTime
                        .before(currentTime)) {
                    /*If the day of week of the execution time is same as the provided value
                    and the calculated time is behind, add 7 days.*/
                    numberOfDaysToBeAdded = 7;
                }
            } else if (currentTime.get(Calendar.MONTH) < month) {
                /*If the provided value of the month is future, find the first possible date
                which is the same day of week.*/
                executionStartTime.set(Calendar.MONTH, month);
                executionStartTime.set(Calendar.DATE, 1);
                executionStartTime = setFirstPossibleDate(executionStartTime, dayOfWeek);
            }
            executionStartTime.add(Calendar.DATE, numberOfDaysToBeAdded);
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the day of month.
     *
     * @param currentTime        The Calendar instance with current time.
     * @param executionStartTime The modified Calendar instance.
     * @param dayOfMonth         The value of the day of month in the appointment expression.
     * @param month              The value of the month in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar modifyCalendarByCheckingDayOfMonth(Calendar currentTime, Calendar executionStartTime,
                                                               int dayOfMonth, int month) {
        if (dayOfMonth >= 1) {
            if (dayOfMonth > executionStartTime.getActualMaximum(Calendar.DAY_OF_MONTH)
                    && month == Constant.NOT_CONSIDERABLE) {
                /*If the last day of the calculated execution start time is less than the provided day of month,
                set it to next month.*/
                executionStartTime.add(Calendar.MONTH, 1);
                executionStartTime.set(Calendar.DATE, dayOfMonth);
            } else {
                //Set the day of month of execution time.
                executionStartTime.set(Calendar.DATE, dayOfMonth);
                if (executionStartTime.before(currentTime)) {
                    //If the calculated execution time is behind the current time, set it to next month.
                    executionStartTime.add(Calendar.MONTH, 1);
                }
            }
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the month.
     *
     * @param taskId             The identifier of the task.
     * @param currentTime        The Calendar instance with current time.
     * @param executionStartTime The modified Calendar instance.
     * @param minute             The value of the minute in the appointment expression.
     * @param hour               The value of the hour in the appointment expression.
     * @param dayOfWeek          The value of the day of week in the appointment expression.
     * @param dayOfMonth         The value of the day of month in the appointment expression.
     * @param month              The value of the month in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar modifyCalendarByCheckingMonth(int taskId, Calendar currentTime, Calendar executionStartTime,
                                                          int minute, int hour, int dayOfWeek, int dayOfMonth,
                                                          int month) {
        if (minute == Constant.NOT_CONSIDERABLE && hour > Constant.NOT_CONSIDERABLE) {
            //If the hour has considerable value and minute is -1, set the execution lifetime to 59 minutes.
            Appointment appointment = new Appointment();
            appointment.setLifeTime(Constant.LIFETIME);
            executorServiceMap.put(taskId, appointment);
        }
        if (month > Constant.NOT_CONSIDERABLE) {
            if (executionStartTime.get(Calendar.MONTH) < month) {
                //Add the number of months to be added when considerable value is passed to the month.
                executionStartTime.add(Calendar.MONTH, month - executionStartTime.get(Calendar.MONTH));
                if (dayOfWeek == Constant.NOT_CONSIDERABLE && dayOfMonth == Constant.NOT_CONSIDERABLE) {
                    /*Set the date as the first day of the month if there is no considerable value for both day of week
                    and day of month.*/
                    executionStartTime.set(Calendar.DATE, 1);
                }
            } else if (executionStartTime.get(Calendar.MONTH) > month) {
                //If the month of the calculated execution start time is future, schedule it to next year.
                int months = month - executionStartTime.get(Calendar.MONTH);
                executionStartTime.add(Calendar.YEAR, 1);
                executionStartTime.add(Calendar.MONTH, months);
                if (dayOfWeek == Constant.NOT_CONSIDERABLE && dayOfMonth == Constant.NOT_CONSIDERABLE) {
                    /*Set the date as the first day of the month if there is no considerable value for both day of week
                    and day of month.*/
                    executionStartTime.set(Calendar.DATE, 1);
                }
            }
        }
        //Check the year of the calculated execution time and set it properly.
        executionStartTime = modifyCalendarByCheckingTheYear(currentTime, executionStartTime, minute, hour, dayOfWeek,
                dayOfMonth, month);
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the year.
     *
     * @param currentTime        The Calendar instance with current time.
     * @param executionStartTime The modified Calendar instance.
     * @param minute             The value of the minute in the appointment expression.
     * @param hour               The value of the hour in the appointment expression.
     * @param dayOfWeek          The value of the day of week in the appointment expression.
     * @param dayOfMonth         The value of the day of month in the appointment expression.
     * @param month              The value of the month in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar modifyCalendarByCheckingTheYear(Calendar currentTime, Calendar executionStartTime,
                                                            int minute, int hour, int dayOfWeek, int dayOfMonth,
                                                            int month) {
        if ((minute == Constant.NOT_CONSIDERABLE && hour == Constant.NOT_CONSIDERABLE) && (
                currentTime.get(Calendar.YEAR) < executionStartTime.get(Calendar.YEAR) || (
                        currentTime.get(Calendar.MONTH) < executionStartTime.get(Calendar.MONTH) && (
                                month != Constant.NOT_CONSIDERABLE || dayOfMonth != Constant.NOT_CONSIDERABLE)) || (
                        currentTime.get(Calendar.DAY_OF_WEEK) != executionStartTime.get(Calendar.DAY_OF_WEEK)
                                && dayOfWeek != Constant.NOT_CONSIDERABLE) || (
                        currentTime.get(Calendar.DAY_OF_MONTH) != executionStartTime.get(Calendar.DAY_OF_MONTH)
                                && dayOfMonth != Constant.NOT_CONSIDERABLE))) {
            //If the the execution start time is future, set the time to midnight (00.00.00).
            executionStartTime = setCalendarFields(executionStartTime, 0, 0, 0);
        }
        if (currentTime.get(Calendar.YEAR) < executionStartTime.get(Calendar.YEAR) && (
                (dayOfWeek != Constant.NOT_CONSIDERABLE && dayOfWeek != executionStartTime.get(Calendar.DAY_OF_WEEK))
                        || (dayOfMonth == Constant.NOT_CONSIDERABLE && currentTime.get(Calendar.DATE) != 1))) {
            /*1. If the year of the execution start time is greater than current and
            day of week is set and execution start time's day of week is not same, find the first possible day.
            2. If the year of the execution start time is greater than current and day of month is not set
            and date of the execution start time is not first day, set the date to first day.
            */
            //Set the date to first day.
            executionStartTime.set(Calendar.DATE, 1);
            if (dayOfWeek != Constant.NOT_CONSIDERABLE) {
                executionStartTime = setFirstPossibleDate(executionStartTime, dayOfWeek);
            }
        }
        return executionStartTime;
    }

    private static Calendar setFirstPossibleDate(Calendar executionStartTime, int dayOfWeek) {
        while (executionStartTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                    /*If the execution start time is future and there is a considerable value is passed to the dayOfWeek
                    find the first possible day which is the same day of week.*/
            if (executionStartTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek) {
                executionStartTime.add(Calendar.DATE, 7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek));
            } else {
                executionStartTime.add(Calendar.DATE, dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK));
            }
        }
        return executionStartTime;
    }

    /**
     * Clone a Calendar into new instance.
     *
     * @param executionStartTime The modified Calendar instance.
     * @param dayOfWeek          The value of the day of week in the appointment expression.
     * @param dayOfMonth         The value of the day of month in the appointment expression.
     * @return updated Calendar.
     */
    private static Calendar cloneCalendarAndSetTime(Calendar executionStartTime, long dayOfWeek, long dayOfMonth) {
        //Clone the Calendar to another instance
        Calendar clonedCalendar = (Calendar) executionStartTime.clone();
        if (dayOfWeek != Constant.NOT_CONSIDERABLE && dayOfMonth != Constant.NOT_CONSIDERABLE) {
            clonedCalendar.set(Calendar.HOUR, 0);
            clonedCalendar.set(Calendar.MINUTE, 0);
            clonedCalendar.set(Calendar.SECOND, 0);
        }
        return clonedCalendar;
    }

    /**
     * Calculates the time difference in milliseconds.
     *
     * @param taskId             The identifier of the task.
     * @param executionStartTime The modified Calendar instance.
     * @return duration in milliseconds.
     */
    private static long calculateDifference(int taskId, Calendar executionStartTime) {
        //Calculate the time difference between current time and the calculated execution time in milli seconds.
        LocalDateTime localCurrentTime = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedCurrentTime = ZonedDateTime.of(localCurrentTime, currentZone);
        LocalDateTime localExecutionStartTime = LocalDateTime.ofInstant(executionStartTime.toInstant(), currentZone);
        ZonedDateTime zonedExecutionStartTime = ZonedDateTime.of(localExecutionStartTime, currentZone);
        Duration duration = Duration.between(zonedCurrentTime, zonedExecutionStartTime);
        if (log.isDebugEnabled()) {
            log.debug(taskId + " is SCHEDULED to: [" + executionStartTime.getTime() + "]");
        }
        return duration.toMillis();
    }

    /**
     * Stops the running task.
     *
     * @param taskId The identifier of the task.
     */
    static void stopTask(int taskId) throws SchedulingFailedException {
        //Stop the corresponding task.
        stopExecution(taskId, 0, null, Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE,
                Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE, null, null);
    }

    /**
     * Sets the calendar fields.
     *
     * @param calendar The Calendar instance to be modified.
     * @param ampm     The value of ampm field to set to the Calendar.
     * @param minutes  The value of the minutes to set to the Calendar.
     * @param hours    The value of the hours to set to the Calendar.
     * @return updated Calendar.
     */
    private static Calendar setCalendarFields(Calendar calendar, int ampm, int minutes, int hours) {
        if (hours != Constant.NOT_CONSIDERABLE) {
            calendar.set(Calendar.HOUR, hours);
        }
        if (minutes != Constant.NOT_CONSIDERABLE) {
            calendar.set(Calendar.MINUTE, minutes);
        }
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (ampm != Constant.NOT_CONSIDERABLE) {
            calendar.set(Calendar.AM_PM, ampm);
        }
        return calendar;
    }

    /**
     * Returns the status of the task.
     *
     * @param taskId The identifier of the task.
     * @return boolean value. 'true' if the task is running, else 'false'.
     */
    public static boolean isTheTaskRunning(int taskId) {
        return executorServiceMap.get(taskId) != null;
    }

    /**
     * Returns the life time of the task if it is defined.
     *
     * @param taskId The identifier of the task.
     * @return The numeric value.
     */
    public static long getExecutionLifeTime(int taskId) {
        return executorServiceMap.get(taskId).getLifeTime();
    }
}
