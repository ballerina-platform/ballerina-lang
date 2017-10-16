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
 * Methods used for scheduling ballerina task.
 */
public class TaskScheduler {

    private static final Log log = LogFactory.getLog(TaskScheduler.class.getName());

    protected static void triggerTimer(Context ctx, int taskId, long delay, long interval,
                                       FunctionRefCPEntry onTriggerFunction, FunctionRefCPEntry onErrorFunction) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        HashMap<Integer, ScheduledExecutorService> executorServiceMap = ctx.getProperty(Constant.SERVICEMAP) != null ?
                (HashMap<Integer, ScheduledExecutorService>) ctx.getProperty(Constant.SERVICEMAP) :
                new HashMap<>();
        try {
            final Runnable schedulerFunc = new Runnable() {
                public void run() {
                    log.info(Constant.PREFIX_TIMER + taskId + " starts the execution");
                    triggerTimer(ctx, taskId, delay, interval, onTriggerFunction, onErrorFunction);
                    //Call the onTrigger function
                    callFunction(ctx, taskId, onTriggerFunction, onErrorFunction);
                }
            };
            if (executorServiceMap.get(taskId) == null && delay != 0) {
                //Schedule the service with initial delay if the initial delay is set
                executorService.schedule(schedulerFunc, delay, TimeUnit.MILLISECONDS);
                log.info(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT + delay + "] MILLISECONDS");
                executorServiceMap.put(taskId, executorService);
                ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
            } else {
                if (interval > 0) {
                    //Schedule the service with the provided delay
                    executorService.schedule(schedulerFunc, interval, TimeUnit.MILLISECONDS);
                    log.info(Constant.PREFIX_TIMER + taskId + Constant.DELAY_HINT + interval + "] MILLISECONDS");
                    //Add the executor service into the context
                    executorServiceMap.put(taskId, executorService);
                    ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
                } else {
                    log.error("The vale of interval is invalid");
                    String error = !((String) ctx.getProperty(Constant.ERROR + "_" + taskId)).isEmpty() ?
                            ctx.getProperty(Constant.ERROR + "_" + taskId)
                                    + ",The vale of interval has to be greater than 0" :
                            "The vale of interval has to be greater than 0";
                    ctx.setProperty(Constant.ERROR + "_" + taskId, error);
                }
            }
        } catch (RejectedExecutionException | IllegalArgumentException e) {
            log.error("Error occurred while scheduling the task: " + e.getMessage());
            String errorFromContext = (String) ctx.getProperty(Constant.ERROR + "_" + taskId);
            String error = errorFromContext != null && !errorFromContext.isEmpty() ?
                    ctx.getProperty(Constant.ERROR + "_" + taskId) + "," + e.getMessage() :
                    e.getMessage();
            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
        }
    }

    protected static void triggerAppointment(Context ctx, int taskId, long minute, long hour, long dayOfWeek,
                                             long dayOfMonth, long month, FunctionRefCPEntry onTriggerFunction,
                                             FunctionRefCPEntry onErrorFunction) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        HashMap<Integer, ScheduledExecutorService> executorServiceMap = ctx.getProperty(Constant.SERVICEMAP) != null ?
                (HashMap<Integer, ScheduledExecutorService>) ctx.getProperty(Constant.SERVICEMAP) :
                new HashMap<>();
        try {
            final Runnable schedulerFunc = new Runnable() {
                @Override public void run() {
                    log.info(Constant.PREFIX_APPOINTMENT + taskId + " starts the execution");
                    if (Long.parseLong(ctx.getProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId).toString()) > 0) {
                        //Set the life time to 0 and trigger every minute
                        ctx.setProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId, 0);
                        triggerAppointment(ctx, taskId, -1, -1, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                                onErrorFunction);
                    } else {
                        triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                                onErrorFunction);
                    }
                    //Call the onTrigger function
                    callFunction(ctx, taskId, onTriggerFunction, onErrorFunction);
                }
            };
            //Calculate the delay
            long delay = calculateDelay(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month);
            executorService.schedule(schedulerFunc, delay, TimeUnit.MILLISECONDS);
            //Get the execution life time
            long period = Long.parseLong(ctx.getProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId).toString());
            //Add the executor service into the context
            executorServiceMap.put(taskId, executorService);
            ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
            if (period > 0) {
                //Calculate the actual execution lifetime from the delay and calculated value
                period = delay + period;
                ctx.setProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId, period);
                //Trigger stop if the execution lifetime > 0
                stopExecution(ctx, taskId, period);
            }
            log.info(Constant.PREFIX_APPOINTMENT + taskId + Constant.DELAY_HINT + delay + "] MILLISECONDS "
                    + Constant.SCHEDULER_LIFETIME_HINT + period + "]");
        } catch (RejectedExecutionException | IllegalArgumentException e) {
            log.error("Error occurred while scheduling the appointment: " + e.getMessage());
            String errorFromContext = (String) ctx.getProperty(Constant.ERROR + "_" + taskId);
            String error = errorFromContext != null && !errorFromContext.isEmpty() ?
                    ctx.getProperty(Constant.ERROR + "_" + taskId) + "," + e.getMessage() :
                    e.getMessage();
            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
        }
    }

    private static void stopExecution(Context ctx, int taskId, long sPeriod) {
        ScheduledExecutorService executorServiceToStopTheTask = Executors.newScheduledThreadPool(1);
        HashMap<Integer, ScheduledExecutorService> executorServiceMap = (HashMap<Integer, ScheduledExecutorService>) ctx
                .getProperty(Constant.SERVICEMAP);
        if (executorServiceMap != null) {
            if (log.isDebugEnabled()) {
                log.info("Attempting to stop the task: " + taskId);
            }
            executorServiceToStopTheTask.schedule(new Runnable() {
                public void run() {
                    //Get the corresponding executor service from context
                    ScheduledExecutorService executorService = executorServiceMap.get(taskId);
                    if (executorService != null) {
                        try {
                            //Invoke shutdown of the executor service
                            executorService.shutdown();
                            if (executorService.isShutdown()) {
                                //Remove the executor service from the context
                                executorServiceMap.remove(taskId);
                                ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
                                ctx.setProperty(Constant.ERROR + "_" + taskId, "");
                            } else {
                                log.error("Unable to stop the task");
                                String errorFromContext = (String) ctx.getProperty(Constant.ERROR + "_" + taskId);
                                String error = errorFromContext != null && !errorFromContext.isEmpty() ?
                                        ctx.getProperty(Constant.ERROR + "_" + taskId)
                                                + ",Unable to stop the task which is associated to the ID " + taskId :
                                        "Unable to stop the task which is associated to the ID " + taskId;
                                ctx.setProperty(Constant.ERROR + "_" + taskId, error);
                            }
                        } catch (SecurityException e) {
                            log.error("Unable to stop the task: " + e.getMessage());
                            String errorFromContext = (String) ctx.getProperty(Constant.ERROR + "_" + taskId);
                            String error = errorFromContext != null && !errorFromContext.isEmpty() ?
                                    ctx.getProperty(Constant.ERROR + "_" + taskId) + "," + e.getMessage() :
                                    e.getMessage();
                            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
                        }
                    }
                }
            }, sPeriod, TimeUnit.MILLISECONDS);
        } else {
            log.error("Unable to find the corresponding task");
        }
    }

    private static void callFunction(Context ctx, int taskId, FunctionRefCPEntry onTriggerFunction,
                                     FunctionRefCPEntry onErrorFunction) {
        AbstractNativeFunction abstractNativeFunction = new AbstractNativeFunction() {
            @Override public BValue[] execute(Context context) {
                return new BValue[0];
            }
        };
        ProgramFile programFile = ctx.getProgramFile();
        //Create new instance of the context and set required properties
        Context newContext = new Context(programFile);
        newContext.setProperty(Constant.SERVICEMAP, ctx.getProperty(Constant.SERVICEMAP));
        newContext.setProperty(Constant.ERROR + "_" + taskId, ctx.getProperty(Constant.ERROR + "_" + taskId));
        newContext.setProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId,
                ctx.getProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId));
        try {
            //Invoke the onTrigger function
            BValue[] response = BLangFunctions
                    .invokeFunction(programFile, onTriggerFunction.getFunctionInfo(), null, newContext);
            if (response.length == 1 && (response[0].stringValue() == null || response[0].stringValue().isEmpty())) {
                if (log.isDebugEnabled()) {
                    log.info("Invoking the onError function due to no response from the triggered function");
                }
                BValue[] error = abstractNativeFunction
                        .getBValues(new BString("Unable to get the response from the triggered function"));
                if (onErrorFunction != null) {
                    BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), error, newContext);
                } else {
                    log.error("The onError function is not provided");
                }
            }
        } catch (BLangRuntimeException e) {
            if (log.isDebugEnabled()) {
                log.info("Invoking the onError function");
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

    private static long calculateDelay(Context ctx, int taskId, long minute, long hour, long dayOfWeek, long dayOfMonth,
                                       long month) {
        if (isValidInput(minute, hour, dayOfWeek, dayOfMonth, month)) {
            //Validate the fields
            log.error("Invalid input");
            String errorFromContext = (String) ctx.getProperty(Constant.ERROR + "_" + taskId);
            String error = errorFromContext != null && !errorFromContext.isEmpty() ?
                    ctx.getProperty(Constant.ERROR + "_" + taskId) + ",Wrong input" :
                    "Wrong input";
            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
        } else {
            //Get the Calendar instance and clone
            Calendar currentTime = Calendar.getInstance();
            Calendar executionStartTime = (Calendar) currentTime.clone();
            //Tune the execution start time by the value of minute
            executionStartTime = tuneTheTimestampByMinute(executionStartTime, minute, hour);
            //Tune the execution start time by the value of hour
            executionStartTime = tuneTheTimestampByHour(currentTime, executionStartTime, minute, hour);
            if (dayOfWeek != -1 && dayOfMonth != -1) {
                //Clone the modified Calendar instances into two instances of either the day of week or
                Calendar newTimeAccordingToDOW = cloneCalendarAndSetTime(executionStartTime, dayOfWeek, dayOfMonth);
                Calendar newTimeAccordingToDOM = cloneCalendarAndSetTime(executionStartTime, dayOfWeek, dayOfMonth);
                //Tune the specific Calendar by the value of day of week
                newTimeAccordingToDOW = tuneTheTimestampByDOW(currentTime, newTimeAccordingToDOW, dayOfWeek, month);
                //Tune the specific Calendar by the value of day of month
                newTimeAccordingToDOM = tuneTheTimestampByDOM(currentTime, newTimeAccordingToDOM, dayOfMonth);
                //Tune both cloned Calendar instances by the value of month
                newTimeAccordingToDOW = tuneTheTimestampByMonth(ctx, taskId, currentTime, newTimeAccordingToDOW, minute,
                        hour, dayOfWeek, dayOfMonth, month);
                newTimeAccordingToDOM = tuneTheTimestampByMonth(ctx, taskId, currentTime, newTimeAccordingToDOM, minute,
                        hour, dayOfWeek, dayOfMonth, month);
                //Find the nearest value from both and set the final execution time
                executionStartTime = newTimeAccordingToDOW.before(newTimeAccordingToDOM) ?
                        newTimeAccordingToDOW :
                        newTimeAccordingToDOM;
            } else {
                //Tune the execution start time by the value of day of week, day of month and month respectively
                executionStartTime = tuneTheTimestampByDOW(currentTime, executionStartTime, dayOfWeek, month);
                executionStartTime = tuneTheTimestampByDOM(currentTime, executionStartTime, dayOfMonth);
                executionStartTime = tuneTheTimestampByMonth(ctx, taskId, currentTime, executionStartTime, minute, hour,
                        dayOfWeek, dayOfMonth, month);
            }
            //Calculate the time difference in MILLI SECONDS
            return calculateDifference(taskId, executionStartTime);
        }
        return 0;
    }

    private static boolean isValidInput(long minute, long hour, long dayOfWeek, long dayOfMonth, long month) {
        return minute > 59 || minute < -1 || hour > 23 || hour < -1 || dayOfWeek > 7 || dayOfWeek < -1 || dayOfWeek == 0
                || dayOfMonth > 31 || dayOfMonth < -1 || dayOfMonth == 0 || month > 11 || month < -1;
    }

    private static Calendar tuneTheTimestampByMinute(Calendar executionStartTime, long minute, long hour) {
        if (minute == -1) {
            if (hour != -1) {
                //If the minute = -1 and hour > -1,  Calendar.MINUTE to 0 if the
                executionStartTime.set(Calendar.MINUTE, 0);
            } else {
                executionStartTime.add(Calendar.MINUTE, 1);
            }
        } else {
            if (hour == -1 && (minute > 0 || minute == 0)) {
                //If the hour = -1 and minute > 0, set interval as the value of the minute
                //e.g; if minute = 5, execute every 5 minutes

                //If the hour = -1 and minute == 0, don't increase the value of the minute since it should be
                //scheduled every hour
                executionStartTime.add(Calendar.MINUTE, (int) minute);
            } else {
                //Set the value of the Calendar.MINUTE to provided value
                executionStartTime.set(Calendar.MINUTE, (int) minute);
            }
        }
        return executionStartTime;
    }

    private static Calendar tuneTheTimestampByHour(Calendar currentTime, Calendar executionStartTime, long minute,
                                                   long hour) {
        if (hour == -1 && minute == 0) {
            //If the hour = -1 and minute == 0, execute every hour
            executionStartTime.add(Calendar.HOUR, 1);
        } else if (hour != -1) {
            //If the hour >= 12, it's in the 24 hours system.
            //Therefore, find the duration to be added to the 12 hours system
            executionStartTime.set(Calendar.HOUR, (int) hour >= 12 ? (int) hour - 12 : (int) hour);
            if (hour <= 11) {
                //If the hour <= 11, it's first half of the day
                executionStartTime.set(Calendar.AM_PM, 0);
            } else {
                //It's second half of the day
                executionStartTime.set(Calendar.AM_PM, 1);
            }
            if (executionStartTime.before(currentTime)) {
                //If the modified time is behind the current time, add a day
                executionStartTime.add(Calendar.DATE, 1);
            }
        }
        return executionStartTime;
    }

    private static Calendar tuneTheTimestampByDOW(Calendar currentTime, Calendar executionStartTime, long dayOfWeek,
                                                  long month) {
        int numberOfDaysToBeAdded = 0;
        if (dayOfWeek >= 1) {
            if (currentTime.get(Calendar.MONTH) == (int) month || (int) month == -1) {
                //If the provided valu of the month is current month or no value to be considered,
                //calculate the number of days to be added
                if ((int) dayOfWeek < executionStartTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = 7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - (int) dayOfWeek);
                } else if ((int) dayOfWeek > executionStartTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = (int) dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK);
                } else if (executionStartTime.get(Calendar.DAY_OF_WEEK) == (int) dayOfWeek && executionStartTime
                        .before(currentTime)) {
                    //If the day of week of the tuned execution time is same as the provided value
                    //and the calculated time is behind, add 7 days
                    numberOfDaysToBeAdded = 7;
                }
            } else if (currentTime.get(Calendar.MONTH) < (int) month) {
                //If the provided value of the month is future, find the first possible date
                //which is the same day of week
                while (executionStartTime.get(Calendar.MONTH) < (int) month) {
                    if (executionStartTime.get(Calendar.DAY_OF_WEEK) == (int) dayOfWeek) {
                        //Increase 7 days if the current day of week is same as the provided value
                        executionStartTime.add(Calendar.DATE, 7);
                    } else if (executionStartTime.get(Calendar.DAY_OF_WEEK) > (int) dayOfWeek) {
                        //Find the number of days to reach the provided value and add that number
                        executionStartTime.add(Calendar.DATE,
                                7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - (int) dayOfWeek));
                    } else {
                        //Find the number of days to reach the provided value and add that number
                        executionStartTime
                                .add(Calendar.DATE, (int) dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK));
                    }
                }
            }
            executionStartTime.add(Calendar.DATE, numberOfDaysToBeAdded);
        }
        return executionStartTime;
    }

    private static Calendar tuneTheTimestampByDOM(Calendar currentTime, Calendar executionStartTime, long dayOfMonth) {
        if (dayOfMonth >= 1) {
            if (dayOfMonth > executionStartTime.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                //If the last day of the calculated execution start time is less than the provided day of month,
                //tune it to next month
                executionStartTime.add(Calendar.MONTH, 1);
                executionStartTime.set(Calendar.DATE, (int) dayOfMonth);
            } else {
                //Set the day of month of execution time
                executionStartTime.set(Calendar.DATE, (int) dayOfMonth);
                if (executionStartTime.before(currentTime)) {
                    //If the calculated execution time is behind the current time, tune it to next month
                    executionStartTime.add(Calendar.MONTH, 1);
                }
            }
        }
        return executionStartTime;
    }

    private static Calendar tuneTheTimestampByMonth(Context ctx, int taskId, Calendar currentTime,
                                                    Calendar executionStartTime, long minute, long hour,
                                                    long dayOfWeek, long dayOfMonth, long month) {
        if ((minute == 0 || minute == -1) && hour > -1) {
            //Set the minute and second to 0 if the task is scheduled to a particular hour
            executionStartTime.set(Calendar.MINUTE, 0);
            executionStartTime.set(Calendar.SECOND, 0);
            if (minute == -1) {
                //If the hour has considerable value and minute is -1, set the execution lifetime to 59 minutes
                ctx.setProperty(Constant.SCHEDULER_LIFETIME + "_" + taskId, 59 * 60000);
            }
        } else if (minute > 0 && hour != -1) {
            //Set the second to 0 if the task is scheduled to the particular time with hour and minute
            executionStartTime.set(Calendar.SECOND, 0);
        }
        if (month > -1) {
            if (executionStartTime.get(Calendar.MONTH) < (int) month) {
                //Add the number of months to be added when considerable value is passed to the month
                executionStartTime.add(Calendar.MONTH, (int) month - executionStartTime.get(Calendar.MONTH));
                if (dayOfWeek == -1 && dayOfMonth == -1) {
                    //Set the date as the first day of the month if there is no considerable value for both day of week
                    //and day of month
                    executionStartTime.set(Calendar.DATE, 1);
                }
            } else if (executionStartTime.get(Calendar.MONTH) > (int) month) {
                //If the month of the calculated execution start time is future, schedule it to next year
                int months = (int) month - executionStartTime.get(Calendar.MONTH);
                executionStartTime.add(Calendar.YEAR, 1);
                executionStartTime.add(Calendar.MONTH, months);
                if (dayOfWeek == -1 && dayOfMonth == -1) {
                    //Set the date as the first day of the month if there is no considerable value for both day of week
                    //and day of month
                    executionStartTime.set(Calendar.DATE, 1);
                }
            } else if (executionStartTime.get(Calendar.MONTH) == (int) month && executionStartTime
                    .before(currentTime)) {
                //If the calculated execution time is behind the current time, tune it to next year
                executionStartTime.add(Calendar.YEAR, 1);
            }
        }
        //Check the year of the calculated execution time and tune it properly
        executionStartTime = tuneTheTimestampByCheckingTheYear(currentTime, executionStartTime, minute, hour, dayOfWeek,
                dayOfMonth, month);
        return executionStartTime;
    }

    private static Calendar tuneTheTimestampByCheckingTheYear(Calendar currentTime, Calendar executionStartTime,
                                                              long minute, long hour, long dayOfWeek, long dayOfMonth,
                                                              long month) {
        if (((currentTime.get(Calendar.YEAR) < executionStartTime.get(Calendar.YEAR)) || (
                currentTime.get(Calendar.MONTH) < executionStartTime.get(Calendar.MONTH) && (month != -1
                        || dayOfMonth != -1)) || (
                currentTime.get(Calendar.DAY_OF_WEEK) != executionStartTime.get(Calendar.DAY_OF_WEEK)
                        && dayOfWeek != -1) || (
                currentTime.get(Calendar.DAY_OF_MONTH) != executionStartTime.get(Calendar.DAY_OF_MONTH)
                        && dayOfMonth != -1)) && (hour == -1 && minute >= -1)) {
            //If the the execution start time is future, set the time to midnight (00.00.00)
            executionStartTime.set(Calendar.AM_PM, 0);
            executionStartTime.set(Calendar.HOUR, 0);
            executionStartTime.set(Calendar.MINUTE, 0);
            executionStartTime.set(Calendar.SECOND, 0);
        }
        if (currentTime.get(Calendar.YEAR) < executionStartTime.get(Calendar.YEAR) && (
                ((dayOfWeek <= 7 && dayOfWeek >= 1) || dayOfWeek == -1)
                        && executionStartTime.get(Calendar.DAY_OF_WEEK) != (int) dayOfWeek)) {
            //If the execution start time is future, set the date to first day
            executionStartTime.set(Calendar.DATE, 1);
            if (dayOfWeek != -1) {
                while (executionStartTime.get(Calendar.DAY_OF_WEEK) != (int) dayOfWeek) {
                    //If the execution start time is future
                    //and there is a considerable value is passed to the dayOfWeek,
                    //find the first possible day which is the same day of week
                    if (executionStartTime.get(Calendar.DAY_OF_WEEK) > (int) dayOfWeek) {
                        executionStartTime.add(Calendar.DATE,
                                7 - (executionStartTime.get(Calendar.DAY_OF_WEEK) - (int) dayOfWeek));
                    } else {
                        executionStartTime
                                .add(Calendar.DATE, (int) dayOfWeek - executionStartTime.get(Calendar.DAY_OF_WEEK));
                    }
                }
            }
        }
        return executionStartTime;
    }

    private static Calendar cloneCalendarAndSetTime(Calendar executionStartTime, long dayOfWeek, long dayOfMonth) {
        //Clone the Calendar to another instance
        Calendar clonedCalendar = (Calendar) executionStartTime.clone();
        if (dayOfWeek != -1 && dayOfMonth != -1) {
            clonedCalendar.set(Calendar.HOUR, 0);
            clonedCalendar.set(Calendar.MINUTE, 0);
            clonedCalendar.set(Calendar.SECOND, 0);
        }
        return clonedCalendar;
    }

    private static long calculateDifference(int taskId, Calendar executionStartTime) {
        //Calculate the time difference between current time and the calculated execution time in milli seconds
        LocalDateTime localCurrentTime = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedCurrentTime = ZonedDateTime.of(localCurrentTime, currentZone);
        LocalDateTime localExecutionStartTime = LocalDateTime.ofInstant(executionStartTime.toInstant(), currentZone);
        ZonedDateTime zonedExecutionStartTime = ZonedDateTime.of(localExecutionStartTime, currentZone);
        Duration duration = Duration.between(zonedCurrentTime, zonedExecutionStartTime);
        if (log.isDebugEnabled()) {
            log.info(taskId + " is SCHEDULED to: [" + executionStartTime.getTime() + "]");
        }
        return duration.toMillis();
    }

    protected static void stopTask(Context ctx, int taskId) {
        //Stop the corresponding task
        stopExecution(ctx, taskId, 0);
    }
}
