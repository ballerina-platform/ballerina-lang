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
    private static HashMap<Integer, ScheduledExecutorService> executorServiceMap = new HashMap<>();
    private static HashMap<Integer, Long> taskLifeTimeMap = new HashMap<>();
    protected static HashMap<Integer, String> scheduleTaskErrorsMap = new HashMap<>();
    protected static HashMap<Integer, String> stopTaskErrorsMaps = new HashMap<>();

    /**
     * Triggers the timer
     *
     * @param ctx               The ballerina context
     * @param taskId            The identifier of the task
     * @param delay             The initial delay
     * @param interval          The interval between two task executions
     * @param onTriggerFunction The main function which will be triggered by the task
     * @param onErrorFunction   The function which will be triggered in the error situation
     */
    protected static void triggerTimer(Context ctx, int taskId, long delay, long interval,
                                       FunctionRefCPEntry onTriggerFunction, FunctionRefCPEntry onErrorFunction) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Constant.POOL_SIZE);
        try {
            final Runnable schedulerFunc = () -> {
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_TIMER + taskId + " starts the execution");
                }
                triggerTimer(ctx, taskId, delay, interval, onTriggerFunction, onErrorFunction);
                //Call the onTrigger function
                callFunction(ctx, onTriggerFunction, onErrorFunction);
            };
            if (executorServiceMap.get(taskId) == null && delay != 0) {
                //Schedule the service with initial delay if the initial delay is set
                executorService.schedule(schedulerFunc, delay, TimeUnit.MILLISECONDS);
                log.info(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT + delay + "] MILLISECONDS");
                executorServiceMap.put(taskId, executorService);
            } else {
                if (interval > 0) {
                    //Schedule the service with the provided delay
                    executorService.schedule(schedulerFunc, interval, TimeUnit.MILLISECONDS);
                    log.info(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT + interval + "] MILLISECONDS");
                    //Add the executor service into the context
                    executorServiceMap.put(taskId, executorService);
                } else {
                    log.error("The vale of interval is invalid");
                    String errorFromMap = scheduleTaskErrorsMap.get(taskId);
                    String error = errorFromMap != null && !errorFromMap.isEmpty() ?
                            errorFromMap + ",The vale of interval must be greater than 0" :
                            "The vale of interval must be greater than 0";
                    scheduleTaskErrorsMap.put(taskId, error);
                }
            }
        } catch (RejectedExecutionException | IllegalArgumentException e) {
            log.error("Error occurred while scheduling the task: " + e.getMessage());
            String errorFromMap = scheduleTaskErrorsMap.get(taskId);
            String error = errorFromMap != null && !errorFromMap.isEmpty() ?
                    errorFromMap + "," + e.getMessage() :
                    e.getMessage();
            scheduleTaskErrorsMap.put(taskId, error);
        }
    }

    /**
     * Triggers the appointment
     *
     * @param ctx               The ballerina context
     * @param taskId            The identifier of the task
     * @param minute            The value of the minute in the appointment expression
     * @param hour              The value of the hour in the appointment expression
     * @param dayOfWeek         The value of the day of week in the appointment expression
     * @param dayOfMonth        The value of the day of month in the appointment expression
     * @param month             The value of the month in the appointment expression
     * @param onTriggerFunction The main function which will be triggered by the task
     * @param onErrorFunction   The function which will be triggered in the error situation
     */
    protected static void triggerAppointment(Context ctx, int taskId, int minute, int hour, int dayOfWeek,
                                             int dayOfMonth, int month, FunctionRefCPEntry onTriggerFunction,
                                             FunctionRefCPEntry onErrorFunction) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        try {
            final Runnable schedulerFunc = () -> {
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_APPOINTMENT + taskId + " starts the execution");
                }
                if (taskLifeTimeMap.get(taskId) != null && taskLifeTimeMap.get(taskId) > 0) {
                    //Set the life time to 0 and trigger every minute
                    taskLifeTimeMap.put(taskId, 0L);
                    triggerAppointment(ctx, taskId, Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE, dayOfWeek,
                            dayOfMonth, month, onTriggerFunction, onErrorFunction);
                } else {
                    triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                            onErrorFunction);
                }
                //Call the onTrigger function
                callFunction(ctx, onTriggerFunction, onErrorFunction);
            };
            //Calculate the delay
            long delay = calculateDelay(taskId, minute, hour, dayOfWeek, dayOfMonth, month);
            if (delay != -1) {
                executorService.schedule(schedulerFunc, delay, TimeUnit.MILLISECONDS);
                //Get the execution life time
                long period = taskLifeTimeMap.get(taskId) != null ? taskLifeTimeMap.get(taskId) : 0L;
                //Add the executor service into the context
                executorServiceMap.put(taskId, executorService);
                if (period > 0) {
                    //Calculate the actual execution lifetime from the delay and calculated value
                    period = delay + period;
                    taskLifeTimeMap.put(taskId, period);
                    //Trigger stop if the execution lifetime > 0
                    stopExecution(taskId, period);
                }
                if (log.isDebugEnabled()) {
                    log.debug(Constant.PREFIX_APPOINTMENT + taskId + Constant.DELAY_HINT + delay + "] MILLISECONDS "
                            + Constant.SCHEDULER_LIFETIME_HINT + period + "]");
                }
            }
        } catch (RuntimeException e) {
            log.error("Error occurred while scheduling the appointment: " + e.getMessage());
            String errorFromMap = scheduleTaskErrorsMap.get(taskId);
            String error = errorFromMap != null && !errorFromMap.isEmpty() ?
                    errorFromMap + "," + e.getMessage() :
                    e.getMessage();
            scheduleTaskErrorsMap.put(taskId, error);
        }
    }

    /**
     * Stops the execution
     *
     * @param taskId  The identifier of the task
     * @param sPeriod The delay to start the task shutdown function
     */
    private static void stopExecution(int taskId, long sPeriod) {
        ScheduledExecutorService executorServiceToStopTheTask = Executors.newScheduledThreadPool(1);
        if (taskId <= 0) {
            log.error("Invalid task ID " + taskId);
            stopTaskErrorsMaps.put(taskId, "Invalid task ID " + taskId);
        } else if (executorServiceMap.get(taskId) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to stop the task: " + taskId);
            }
            final Runnable schedulerFunc = () -> {
                //Get the corresponding executor service from context
                ScheduledExecutorService executorService = executorServiceMap.get(taskId);
                try {
                    //Invoke shutdown of the executor service
                    executorService.shutdown();
                    if (executorService.isShutdown()) {
                        //Remove the executor service from the context
                        executorServiceMap.remove(taskId);
                        scheduleTaskErrorsMap.put(taskId, "");
                        taskLifeTimeMap.put(taskId, 0L);
                    } else {
                        log.error("Unable to stop the task");
                        stopTaskErrorsMaps.put(taskId, "Unable to stop the task ");
                    }
                } catch (SecurityException e) {
                    log.error("Unable to stop the task: " + e.getMessage());
                    stopTaskErrorsMaps.put(taskId, e.getMessage());
                }
            };
            executorServiceToStopTheTask.schedule(schedulerFunc, sPeriod, TimeUnit.MILLISECONDS);
        } else {
            log.error("Unable to find the corresponding task");
            stopTaskErrorsMaps.put(taskId, "Unable to find the corresponding task");
        }
    }

    /**
     * Calls the onTrigger and onError functions
     *
     * @param ctx               The ballerina context
     * @param onTriggerFunction The main function which will be triggered by the task
     * @param onErrorFunction   The function which will be triggered in the error situation
     */
    private static void callFunction(Context ctx, FunctionRefCPEntry onTriggerFunction,
                                     FunctionRefCPEntry onErrorFunction) {
        AbstractNativeFunction abstractNativeFunction = new AbstractNativeFunction() {
            @Override public BValue[] execute(Context context) {
                return new BValue[0];
            }
        };
        ProgramFile programFile = ctx.getProgramFile();
        //Create new instance of the context and set required properties
        Context newContext = new Context(programFile);
        try {
            //Invoke the onTrigger function
            BValue[] response = BLangFunctions
                    .invokeFunction(programFile, onTriggerFunction.getFunctionInfo(), null, newContext);
            if (response[0].stringValue() == null || response[0].stringValue().isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Invoking the onError function due to no response from the triggered function");
                }
                BValue[] error = abstractNativeFunction.getBValues(new BString(Constant.TIMER_ERROR));
                if (onErrorFunction != null) {
                    BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), error, newContext);
                } else {
                    log.error("The onError function is not provided");
                }
            }
        } catch (BLangRuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Invoking the onError function");
            }
            BValue[] error = abstractNativeFunction.getBValues(new BString(e.getMessage()));
            //Call the onError function in case of error
            if (onErrorFunction != null) {
                BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), error, newContext);
            } else {
                log.error("The onError function is not provided");
            }
        }
    }

    /**
     * Calculates the delay to schedule the appointment
     *
     * @param taskId     The identifier of the task
     * @param minute     The value of the minute in the appointment expression
     * @param hour       The value of the hour in the appointment expression
     * @param dayOfWeek  The value of the day of week in the appointment expression
     * @param dayOfMonth The value of the day of month in the appointment expression
     * @param month      The value of the month in the appointment expression
     * @return delay which is used to schedule the appointment
     */
    public static long calculateDelay(int taskId, int minute, int hour, int dayOfWeek, int dayOfMonth, int month) {
        //Get the Calendar instance
        Calendar currentTime = Calendar.getInstance();
        if (isInvalidInput(currentTime, minute, hour, dayOfWeek, dayOfMonth, month)) {
            //Validate the fields
            log.error("Invalid input");
            String errorFromMap = scheduleTaskErrorsMap.get(taskId);
            String error =
                    errorFromMap != null && !errorFromMap.isEmpty() ? errorFromMap + ",Wrong input" : "Wrong input";
            scheduleTaskErrorsMap.put(taskId, error);
        } else {
            //Clone the current time to another instance
            Calendar executionStartTime = (Calendar) currentTime.clone();
            //Tune the execution start time by the value of minute
            executionStartTime = modifyCalendarByCheckingMinute(currentTime, executionStartTime, minute, hour);
            //Tune the execution start time by the value of hour
            executionStartTime = modifyCalendarByCheckingHour(currentTime, executionStartTime, minute, hour, dayOfWeek,
                    dayOfMonth);
            if (dayOfWeek != Constant.NOT_CONSIDERABLE && dayOfMonth != Constant.NOT_CONSIDERABLE) {
                //Clone the modified Calendar instances into two instances of either the day of week or
                Calendar newTimeAccordingToDOW = cloneCalendarAndSetTime(executionStartTime, dayOfWeek, dayOfMonth);
                Calendar newTimeAccordingToDOM = cloneCalendarAndSetTime(executionStartTime, dayOfWeek, dayOfMonth);
                //Tune the specific Calendar by the value of day of week
                newTimeAccordingToDOW = modifyCalendarByCheckingDayOfWeek(currentTime, newTimeAccordingToDOW, dayOfWeek,
                        month);
                //Tune the specific Calendar by the value of day of month
                newTimeAccordingToDOM = modifyCalendarByCheckingDayOfMonth(currentTime, newTimeAccordingToDOM,
                        dayOfMonth, month);
                //Tune both cloned Calendar instances by the value of month
                newTimeAccordingToDOW = modifyCalendarByCheckingMonth(taskId, currentTime, newTimeAccordingToDOW,
                        minute, hour, dayOfWeek, dayOfMonth, month);
                newTimeAccordingToDOM = modifyCalendarByCheckingMonth(taskId, currentTime, newTimeAccordingToDOM,
                        minute, hour, dayOfWeek, dayOfMonth, month);
                //Find the nearest value from both and set the final execution time
                executionStartTime = newTimeAccordingToDOW.before(newTimeAccordingToDOM) ?
                        newTimeAccordingToDOW :
                        newTimeAccordingToDOM;
            } else {
                //Tune the execution start time by the value of day of week, day of month and month respectively
                executionStartTime = modifyCalendarByCheckingDayOfWeek(currentTime, executionStartTime, dayOfWeek,
                        month);
                executionStartTime = modifyCalendarByCheckingDayOfMonth(currentTime, executionStartTime, dayOfMonth,
                        month);
                executionStartTime = modifyCalendarByCheckingMonth(taskId, currentTime, executionStartTime, minute,
                        hour, dayOfWeek, dayOfMonth, month);
            }
            //Calculate the time difference in MILLI SECONDS
            return calculateDifference(taskId, executionStartTime);
        }
        return -1;
    }

    /**
     * Checks the validity of the input
     *
     * @param currentTime The Calendar instance with current time
     * @param minute      The value of the minute in the appointment expression
     * @param hour        The value of the hour in the appointment expression
     * @param dayOfWeek   The value of the day of week in the appointment expression
     * @param dayOfMonth  The value of the day of month in the appointment expression
     * @param month       The value of the month in the appointment expression
     * @return boolean value. 'true' if the input is valid else 'false'
     */
    private static boolean isInvalidInput(Calendar currentTime, int minute, int hour, int dayOfWeek, int dayOfMonth,
                                          int month) {
        //Valid ranges: (minute :- 0 - 59, hour :- 0 - 23, dayOfWeek :- 1 - 7, dayOfMonth :- 1 - 31, month :- 0 - 11)
        Calendar clonedCalendar = (Calendar) currentTime.clone();
        clonedCalendar.set(Calendar.MONTH, month);
        return minute > 59 || minute < Constant.NOT_CONSIDERABLE || hour > 23 || hour < Constant.NOT_CONSIDERABLE
                || dayOfWeek > 7 || dayOfWeek < Constant.NOT_CONSIDERABLE || dayOfWeek == 0 || dayOfMonth > 31
                || dayOfMonth < Constant.NOT_CONSIDERABLE || dayOfMonth == 0 || month > 11
                || month < Constant.NOT_CONSIDERABLE || dayOfMonth > clonedCalendar
                .getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Modifies the Calendar by checking the minute
     *
     * @param currentTime        The Calendar instance with current time
     * @param executionStartTime The modified Calendar instance
     * @param minute             The value of the minute in the appointment expression
     * @param hour               The value of the hour in the appointment expression
     * @return updated Calendar
     */
    private static Calendar modifyCalendarByCheckingMinute(Calendar currentTime, Calendar executionStartTime,
                                                           int minute, int hour) {
        if (minute == Constant.NOT_CONSIDERABLE && hour == Constant.NOT_CONSIDERABLE) {
            //Run every minute
            executionStartTime.add(Calendar.MINUTE, 1);
        } else if (minute == Constant.NOT_CONSIDERABLE) {
            //Run at clock time at 0th minute with 59 minutes execution lifetime e.g start at 2AM and end at 2.59AM
            executionStartTime.set(Calendar.MINUTE, 0);
        } else {
            //Run every hour at 0th minute or at 5th minute or at a clock time e.g: 2.30AM
            executionStartTime = setCalendarFields(executionStartTime, Constant.NOT_CONSIDERABLE, 0, 0, minute,
                    Constant.NOT_CONSIDERABLE);
            if (minute != 0 && hour == Constant.NOT_CONSIDERABLE && executionStartTime.before(currentTime)) {
                //If the modified time is behind the current time and it is every hour at 0th minute
                //or at 5th minute case, add an hour
                executionStartTime.add(Calendar.HOUR, 1);
            }
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the hour
     *
     * @param currentTime        The Calendar instance with current time
     * @param executionStartTime The modified Calendar instance
     * @param minute             The value of the minute in the appointment expression
     * @param hour               The value of the hour in the appointment expression
     * @param dayOfWeek          The value of the day of week in the appointment expression
     * @param dayOfMonth         The value of the day of month in the appointment expression
     * @return updated Calendar
     */
    private static Calendar modifyCalendarByCheckingHour(Calendar currentTime, Calendar executionStartTime, int minute,
                                                         int hour, int dayOfWeek, int dayOfMonth) {
        if (minute == 0 && hour == Constant.NOT_CONSIDERABLE) {
            //If the minute == 0 and hour = -1, execute every hour
            executionStartTime.add(Calendar.HOUR, 1);
            executionStartTime = setCalendarFields(executionStartTime, Constant.NOT_CONSIDERABLE, 0, 0, 0,
                    Constant.NOT_CONSIDERABLE);
        } else if (hour != Constant.NOT_CONSIDERABLE) {
            //If the hour >= 12, it's in the 24 hours system.
            //Therefore, find the duration to be added to the 12 hours system
            executionStartTime.set(Calendar.HOUR, hour >= 12 ? hour - 12 : hour);
            executionStartTime = setCalendarFields(executionStartTime, Constant.NOT_CONSIDERABLE, 0, 0,
                    Constant.NOT_CONSIDERABLE, Constant.NOT_CONSIDERABLE);
            if (hour <= 11) {
                //If the hour <= 11, it's first half of the day
                executionStartTime.set(Calendar.AM_PM, 0);
            } else {
                //It's second half of the day
                executionStartTime.set(Calendar.AM_PM, 1);
            }
        }
        if (executionStartTime.before(currentTime) && dayOfWeek == Constant.NOT_CONSIDERABLE
                && dayOfMonth == Constant.NOT_CONSIDERABLE) {
            //If the modified time is behind the current time, add a day
            executionStartTime.add(Calendar.DATE, 1);
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the day of week
     *
     * @param currentTime        The Calendar instance with current time
     * @param executionStartTime The modified Calendar instance
     * @param dayOfWeek          The value of the day of week in the appointment expression
     * @param month              The value of the month in the appointment expression
     * @return updated Calendar
     */
    private static Calendar modifyCalendarByCheckingDayOfWeek(Calendar currentTime, Calendar executionStartTime,
                                                              int dayOfWeek, int month) {
        int numberOfDaysToBeAdded = 0;
        if (dayOfWeek >= 1) {
            if (month == currentTime.get(Calendar.MONTH) || month == Constant.NOT_CONSIDERABLE) {
                //If the provided value of the month is current month or no value considerable value is provided
                // for month, calculate the number of days to be added
                if (dayOfWeek < executionStartTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = 7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek);
                } else if (dayOfWeek > executionStartTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK);
                } else if (executionStartTime.get(Calendar.DAY_OF_WEEK) == dayOfWeek && executionStartTime
                        .before(currentTime)) {
                    //If the day of week of the execution time is same as the provided value
                    //and the calculated time is behind, add 7 days
                    numberOfDaysToBeAdded = 7;
                }
            } else if (currentTime.get(Calendar.MONTH) < month) {
                //If the provided value of the month is future, find the first possible date
                //which is the same day of week
                while (executionStartTime.get(Calendar.MONTH) < month) {
                    if (executionStartTime.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                        //Increase 7 days if the current day of week is same as the provided value
                        executionStartTime.add(Calendar.DATE, 7);
                    } else if (executionStartTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek) {
                        //Find the number of days to reach the provided value and add that number
                        executionStartTime
                                .add(Calendar.DATE, 7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek));
                    } else {
                        //Find the number of days to reach the provided value and add that number
                        executionStartTime.add(Calendar.DATE,
                                dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK));
                    }
                }
            }
            executionStartTime.add(Calendar.DATE, numberOfDaysToBeAdded);
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the day of month
     *
     * @param currentTime        The Calendar instance with current time
     * @param executionStartTime The modified Calendar instance
     * @param dayOfMonth         The value of the day of month in the appointment expression
     * @param month              The value of the month in the appointment expression
     * @return updated Calendar
     */
    private static Calendar modifyCalendarByCheckingDayOfMonth(Calendar currentTime, Calendar executionStartTime,
                                                               int dayOfMonth, int month) {
        if (dayOfMonth >= 1) {
            if (dayOfMonth > executionStartTime.getActualMaximum(Calendar.DAY_OF_MONTH)
                    && month == Constant.NOT_CONSIDERABLE) {
                //If the last day of the calculated execution start time is less than the provided day of month,
                //set it to next month
                executionStartTime.add(Calendar.MONTH, 1);
                executionStartTime.set(Calendar.DATE, dayOfMonth);
            } else {
                //Set the day of month of execution time
                executionStartTime.set(Calendar.DATE, dayOfMonth);
                if (executionStartTime.before(currentTime)) {
                    //If the calculated execution time is behind the current time, set it to next month
                    executionStartTime.add(Calendar.MONTH, 1);
                }
            }
        }
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the month
     *
     * @param taskId             The identifier of the task
     * @param currentTime        The Calendar instance with current time
     * @param executionStartTime The modified Calendar instance
     * @param minute             The value of the minute in the appointment expression
     * @param hour               The value of the hour in the appointment expression
     * @param dayOfWeek          The value of the day of week in the appointment expression
     * @param dayOfMonth         The value of the day of month in the appointment expression
     * @param month              The value of the month in the appointment expression
     * @return updated Calendar
     */
    private static Calendar modifyCalendarByCheckingMonth(int taskId, Calendar currentTime, Calendar executionStartTime,
                                                          int minute, int hour, int dayOfWeek, int dayOfMonth,
                                                          int month) {
        if (minute == Constant.NOT_CONSIDERABLE && hour > Constant.NOT_CONSIDERABLE) {
            //If the hour has considerable value and minute is -1, set the execution lifetime to 59 minutes
            taskLifeTimeMap.put(taskId, Constant.LIFETIME);
        }
        if (month > Constant.NOT_CONSIDERABLE) {
            if (executionStartTime.get(Calendar.MONTH) < month) {
                //Add the number of months to be added when considerable value is passed to the month
                executionStartTime.add(Calendar.MONTH, month - executionStartTime.get(Calendar.MONTH));
                if (dayOfWeek == Constant.NOT_CONSIDERABLE && dayOfMonth == Constant.NOT_CONSIDERABLE) {
                    //Set the date as the first day of the month if there is no considerable value for both day of week
                    //and day of month
                    executionStartTime.set(Calendar.DATE, 1);
                }
            } else if (executionStartTime.get(Calendar.MONTH) > month) {
                //If the month of the calculated execution start time is future, schedule it to next year
                int months = month - executionStartTime.get(Calendar.MONTH);
                executionStartTime.add(Calendar.YEAR, 1);
                executionStartTime.add(Calendar.MONTH, months);
                if (dayOfWeek == Constant.NOT_CONSIDERABLE && dayOfMonth == Constant.NOT_CONSIDERABLE) {
                    //Set the date as the first day of the month if there is no considerable value for both day of week
                    //and day of month
                    executionStartTime.set(Calendar.DATE, 1);
                }
            }
        }
        //Check the year of the calculated execution time and set it properly
        executionStartTime = modifyCalendarByCheckingTheYear(currentTime, executionStartTime, minute, hour, dayOfWeek,
                dayOfMonth, month);
        return executionStartTime;
    }

    /**
     * Modifies the Calendar by checking the year
     *
     * @param currentTime        The Calendar instance with current time
     * @param executionStartTime The modified Calendar instance
     * @param minute             The value of the minute in the appointment expression
     * @param hour               The value of the hour in the appointment expression
     * @param dayOfWeek          The value of the day of week in the appointment expression
     * @param dayOfMonth         The value of the day of month in the appointment expression
     * @param month              The value of the month in the appointment expression
     * @return updated Calendar
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
            //If the the execution start time is future, set the time to midnight (00.00.00)
            executionStartTime = setCalendarFields(executionStartTime, 0, 0, 0, 0, 0);
        }
        if (currentTime.get(Calendar.YEAR) < executionStartTime.get(Calendar.YEAR) && (
                (dayOfWeek != Constant.NOT_CONSIDERABLE && dayOfWeek != executionStartTime.get(Calendar.DAY_OF_WEEK))
                        || (dayOfMonth == Constant.NOT_CONSIDERABLE && currentTime.get(Calendar.DATE) != 1))) {
            //If the execution start time is future, set the date to first day
            executionStartTime.set(Calendar.DATE, 1);
            if (dayOfWeek != Constant.NOT_CONSIDERABLE) {
                while (executionStartTime.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                    //If the execution start time is future and there is a considerable value is passed to the dayOfWeek
                    //find the first possible day which is the same day of week
                    if (executionStartTime.get(Calendar.DAY_OF_WEEK) > dayOfWeek) {
                        executionStartTime
                                .add(Calendar.DATE, 7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - dayOfWeek));
                    } else {
                        executionStartTime.add(Calendar.DATE,
                                dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK));
                    }
                }
            }
        }
        return executionStartTime;
    }

    /**
     * Clone a Calendar into new instance
     *
     * @param executionStartTime The modified Calendar instance
     * @param dayOfWeek          The value of the day of week in the appointment expression
     * @param dayOfMonth         The value of the day of month in the appointment expression
     * @return updated Calendar
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
     * Calculates the time difference in milliseconds
     *
     * @param taskId             The identifier of the task
     * @param executionStartTime The modified Calendar instance
     * @return duration in milliseconds
     */
    private static long calculateDifference(int taskId, Calendar executionStartTime) {
        //Calculate the time difference between current time and the calculated execution time in milli seconds
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
     * Stops the running task
     *
     * @param taskId The identifier of the task
     */
    protected static void stopTask(int taskId) {
        //Stop the corresponding task
        stopExecution(taskId, 0);
    }

    /**
     * Sets the calendar fields
     *
     * @param calendar     The Calendar instance to be modified
     * @param ampm         The value of ampm field to set to the Calendar
     * @param milliseconds The value of the milli seconds to set to the Calendar
     * @param seconds      The value of the seconds to set to the Calendar
     * @param minutes      The value of the minutes to set to the Calendar
     * @param hours        The value of the hours to set to the Calendar
     * @return updated Calendar
     */
    private static Calendar setCalendarFields(Calendar calendar, int ampm, int milliseconds, int seconds, int minutes,
                                              int hours) {
        if (hours != Constant.NOT_CONSIDERABLE) {
            calendar.set(Calendar.HOUR, hours);
        }
        if (minutes != Constant.NOT_CONSIDERABLE) {
            calendar.set(Calendar.MINUTE, minutes);
        }
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        if (ampm != Constant.NOT_CONSIDERABLE) {
            calendar.set(Calendar.AM_PM, ampm);
        }
        return calendar;
    }

    /**
     * Returns the status of the task
     *
     * @param taskId The identifier of the task
     * @return boolean value. 'true' if the task is running, else 'false'
     */
    public static boolean isTheTaskRunning(int taskId) {
        return executorServiceMap.get(taskId) != null;
    }

    /**
     * Returns the life time of the task if it is defined
     *
     * @param taskId The identifier of the task
     * @return The numeric value
     */
    public static long getExecutionLifeTime(int taskId) {
        return taskLifeTimeMap.get(taskId);
    }
}
