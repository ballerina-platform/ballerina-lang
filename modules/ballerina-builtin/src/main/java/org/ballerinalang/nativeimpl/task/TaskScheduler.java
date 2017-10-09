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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;

import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Methods used for scheduling ballerina task.
 */
public class TaskScheduler {

    private static long period = 0;
    static PrintStream out = System.out;

    protected static void triggerTimer(Context ctx, int taskId, long delay, long interval,
                                       FunctionRefCPEntry onTriggerFunction, FunctionRefCPEntry onErrorFunction) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        HashMap<Integer, ScheduledExecutorService> executorServiceMap = ctx.getProperty(Constant.SERVICEMAP) != null ?
                (HashMap<Integer, ScheduledExecutorService>) ctx.getProperty(Constant.SERVICEMAP) : new HashMap<>();
        try {
            final Runnable schedulerFunc = new Runnable() {
                public void run() {
                    Calendar now = Calendar.getInstance();
                    out.println(taskId + ": triggerTimer: RUNS @ " + now.getTime());
                    triggerTimer(ctx, taskId, delay, interval, onTriggerFunction, onErrorFunction);
                    callFunction(ctx, onTriggerFunction, onErrorFunction);
                }
            };
            if (interval > 0) {
                Calendar now = Calendar.getInstance();
                out.print("triggerTimer: Scheduling the timer @ " + now.getTime() + " with delay: " + delay
                        + " interval: " + interval);
                executorService.schedule(schedulerFunc, interval, TimeUnit.MILLISECONDS);
                executorServiceMap.put(taskId, executorService);
                ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
            } else {
                String error = StringUtils.isNotEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId)) ?
                        ctx.getProperty(Constant.ERROR + "_" + taskId)
                                + ",The vale of interval has to be greater than 0" :
                        "The vale of interval has to be greater than 0";
                ctx.setProperty(Constant.ERROR + "_" + taskId, error);
            }
        } catch (RejectedExecutionException | NullPointerException | IllegalArgumentException e) {
            String error = StringUtils.isNotEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId)) ?
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
                (HashMap<Integer, ScheduledExecutorService>) ctx.getProperty(Constant.SERVICEMAP) : new HashMap<>();
        try {
            final Runnable schedulerFunc = new Runnable() {
                @Override
                public void run() {
                    Date now = new Date();
                    out.println(taskId + ": triggerAppointment: RUNS @ " + now);
                    if (period > 0) {
                        period = 0;
                        triggerAppointment(ctx, taskId, -1, -1, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                                onErrorFunction);
                    } else {
                        triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month, onTriggerFunction,
                                onErrorFunction);
                    }
                    callFunction(ctx, onTriggerFunction, onErrorFunction);
                }
            };
            long delay = computeNextDelay(taskId, ctx, minute, hour, dayOfWeek, dayOfMonth, month);
            Calendar now = Calendar.getInstance();
            out.println("triggerAppointment: Scheduling the appointment @ " + now.getTime() + " with delay: " + delay);
            now.add(Calendar.MILLISECOND, (int) delay);
            out.println(" and Scheduled to: " + now.getTime());
            executorService.schedule(schedulerFunc, delay, TimeUnit.MILLISECONDS);
            executorServiceMap.put(taskId, executorService);
            ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
            if (period > 0) {
                period = delay + period;
                stopExecution(ctx, taskId, period);
            }
        } catch (RejectedExecutionException | NullPointerException | IllegalArgumentException e) {
            String error = StringUtils.isNotEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId)) ?
                    ctx.getProperty(Constant.ERROR + "_" + taskId) + "," + e.getMessage() :
                    e.getMessage();
            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
        }
    }

    private static void stopExecution(Context ctx, int taskId, long sPeriod) {
        ScheduledExecutorService executorServiceToStopTheTask = Executors.newScheduledThreadPool(1);
        HashMap<Integer, ScheduledExecutorService> executorServiceMap =
                (HashMap<Integer, ScheduledExecutorService>) ctx.getProperty(Constant.SERVICEMAP);
        if (executorServiceMap != null) {
            out.println("TaskScheduler : stopExecution: executorServiceMap SIZE: " + executorServiceMap.size());
            executorServiceToStopTheTask.schedule(new Runnable() {
                public void run() {
                    ScheduledExecutorService executorService = executorServiceMap.get(taskId);
                    if (executorService != null) {
                        try {
                            executorService.shutdown();
                            if (executorService.isShutdown()) {
                                executorServiceMap.remove(taskId);
                                ctx.setProperty(Constant.SERVICEMAP, executorServiceMap);
                                ctx.setProperty(Constant.ERROR + "_" + taskId, "");
                            } else {
                                String error =
                                        StringUtils.isNotEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId))
                                                ? ctx.getProperty(Constant.ERROR + "_" + taskId)
                                                + ",Unable to stop the task which is associated to the ID " + taskId :
                                                "Unable to stop the task which is associated to the ID " + taskId;
                                ctx.setProperty(Constant.ERROR + "_" + taskId, error);
                            }
                        } catch (SecurityException e) {
                            String error = StringUtils.isNotEmpty(ctx.getProperty(Constant.ERROR + "_"
                                    + taskId).toString()) ? ctx.getProperty(Constant.ERROR + "_" + taskId) + ","
                                    + e.getMessage() : e.getMessage();
                            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
                        }
                    }
                }
            }, sPeriod, TimeUnit.SECONDS);
        }
    }

    private static BValue[] callFunction(Context ctx, FunctionRefCPEntry onTriggerFunction,
                                         FunctionRefCPEntry onErrorFunction) {
        AbstractNativeFunction abstractNativeFunction = new AbstractNativeFunction() {
            @Override
            public BValue[] execute(Context context) {
                return new BValue[0];
            }
        };
        ProgramFile programFile = ctx.getProgramFile();
        Context newContext = new Context(programFile);
        newContext.setProperty(Constant.SERVICEMAP, ctx.getProperty(Constant.SERVICEMAP));
        newContext.setProperty(Constant.ERROR, ctx.getProperty(Constant.ERROR));
        BValue[] response = abstractNativeFunction.getBValues(new BString(""));
        try {
            response = BLangFunctions
                    .invokeFunction(programFile, onTriggerFunction.getFunctionInfo(), null, newContext);
            if (response.length == 1 && StringUtils.isEmpty(response[0].stringValue())) {
                BValue[] error = abstractNativeFunction.getBValues(
                        new BString("Unable to get the response from the triggered function"));
                if (onErrorFunction != null) {
                    BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), error,
                            newContext);
                }
            }
        } catch (BLangRuntimeException e) {
            BValue[] error = abstractNativeFunction.getBValues(new BString(e.getMessage()));
            if (onErrorFunction != null) {
                BLangFunctions.invokeFunction(programFile, onErrorFunction.getFunctionInfo(), error,
                        newContext);
            }
        }
        return response;
    }

    private static long computeNextDelay(int taskId, Context ctx, long minute, long hour, long dayOfWeek,
                                         long dayOfMonth, long month) {
        if (minute > 59 || minute < -1 || hour > 23 || hour < -1 || dayOfWeek > 7 || dayOfWeek < -1 || dayOfWeek == 0
                || dayOfMonth > 31 || dayOfMonth < -1 || dayOfMonth == 0 || month > 11 || month < -1) {
            String error = StringUtils.isNotEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId)) ?
                    ctx.getProperty(Constant.ERROR + "_" + taskId) + ",Wrong input" : "Wrong input";
            ctx.setProperty(Constant.ERROR + "_" + taskId, error);
        } else {
            Calendar startTime = Calendar.getInstance();
            Calendar now = Calendar.getInstance();
            if (minute == -1) {
                startTime.add(Calendar.MINUTE, 1);
            } else {
                if (hour == -1 && minute > 0 || hour == -1 && minute == 0) {
                    startTime.add(Calendar.MINUTE, (int) minute);
                } else {
                    startTime.set(Calendar.MINUTE, (int) minute);
                }
            }
            if (hour == -1 && minute == 0) {
                startTime.add(Calendar.HOUR, 1);
            } else if (hour != -1) {
                startTime.set(Calendar.HOUR, (int) hour >= 12 ? (int) hour - 12 : (int) hour);
                if (hour <= 11) {
                    startTime.set(Calendar.AM_PM, 0);
                } else {
                    startTime.set(Calendar.AM_PM, 1);
                }
                if (startTime.before(now)) {
                    startTime.add(Calendar.DATE, 1);
                }
            }

            Calendar newTimeAccordingToDOW = (Calendar) startTime.clone();
            Calendar newTimeAccordingToDOM = (Calendar) startTime.clone();
            if (dayOfWeek != -1 && dayOfMonth != -1) {
                newTimeAccordingToDOW.set(Calendar.HOUR, 0);
                newTimeAccordingToDOW.set(Calendar.MINUTE, 0);
                newTimeAccordingToDOW.set(Calendar.SECOND, 0);
                newTimeAccordingToDOM.set(Calendar.HOUR, 0);
                newTimeAccordingToDOM.set(Calendar.MINUTE, 0);
                newTimeAccordingToDOM.set(Calendar.SECOND, 0);
            }
            if (dayOfWeek != -1 && dayOfMonth != -1) {
                newTimeAccordingToDOW = fixTheDateByDOW(now, newTimeAccordingToDOW, dayOfWeek, month);
                newTimeAccordingToDOM = fixTheDateByDOM(now, newTimeAccordingToDOM, dayOfMonth);
                newTimeAccordingToDOW = calculateRemainingTime(now, newTimeAccordingToDOW, minute, hour, dayOfWeek,
                        dayOfMonth, month);
                newTimeAccordingToDOM = calculateRemainingTime(now, newTimeAccordingToDOM, minute, hour, dayOfWeek,
                        dayOfMonth, month);
                out.println("newTimeAccordingToDOW: " + newTimeAccordingToDOW.getTime());
                out.println("newTimeAccordingToDOM: " + newTimeAccordingToDOM.getTime());
                startTime = newTimeAccordingToDOW.before(newTimeAccordingToDOM) ?
                        newTimeAccordingToDOW :
                        newTimeAccordingToDOM;
            } else {
                startTime = fixTheDateByDOW(now, startTime, dayOfWeek, month);
                startTime = fixTheDateByDOM(now, startTime, dayOfMonth);
                startTime = calculateRemainingTime(now, startTime, minute, hour, dayOfWeek, dayOfMonth, month);
            }
            LocalDateTime localNow = LocalDateTime.now();
            ZoneId currentZone = ZoneId.systemDefault();
            ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
            LocalDateTime localNext = LocalDateTime.ofInstant(startTime.toInstant(), currentZone);
            ZonedDateTime zonedNextTarget = ZonedDateTime.of(localNext, currentZone);
            out.println("Scheduled from : " + now.getTime() + " ---- to: " + startTime.getTime());
            Duration duration = Duration.between(zonedNow, zonedNextTarget);
            return duration.toMillis();
        }
        return 0;
    }

    private static Calendar fixTheDateByDOW(Calendar now, Calendar startTime, long dayOfWeek, long month) {
        int numberOfDaysToBeAdded = 0;
        if (dayOfWeek >= 1) {
            if (now.get(Calendar.MONTH) == (int) month || (int) month == -1) {
                if ((int) dayOfWeek < startTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = 7 - (startTime.get(Calendar.DAY_OF_WEEK) - (int) dayOfWeek);
                } else if ((int) dayOfWeek > startTime.get(Calendar.DAY_OF_WEEK)) {
                    numberOfDaysToBeAdded = (int) dayOfWeek - startTime.get(Calendar.DAY_OF_WEEK);
                } else if (startTime.get(Calendar.DAY_OF_WEEK) == (int) dayOfWeek && startTime.before(now)) {
                    numberOfDaysToBeAdded = 7;
                }
            } else if (now.get(Calendar.MONTH) < (int) month) {
                while (startTime.get(Calendar.MONTH) < (int) month) {
                    if (startTime.get(Calendar.DAY_OF_WEEK) == (int) dayOfWeek) {
                        startTime.add(Calendar.DATE, 7);
                    } else if (startTime.get(Calendar.DAY_OF_WEEK) > (int) dayOfWeek) {
                        startTime.add(Calendar.DATE, 7 - (startTime.get(Calendar.DAY_OF_WEEK) - (int) dayOfWeek));
                    } else {
                        startTime.add(Calendar.DATE, (int) dayOfWeek - startTime.get(Calendar.DAY_OF_WEEK));
                    }
                }
            }
            startTime.add(Calendar.DATE, numberOfDaysToBeAdded);
        }
        return startTime;
    }

    private static Calendar fixTheDateByDOM(Calendar now, Calendar startTime, long dayOfMonth) {
        if (dayOfMonth >= 1) {
            startTime.set(Calendar.DATE, (int) dayOfMonth);
            if (startTime.before(now)) {
                startTime.add(Calendar.MONTH, 1);
            }
        }
        return startTime;
    }

    private static Calendar calculateRemainingTime(Calendar now, Calendar startTime, long minute, long hour,
                                                   long dayOfWeek, long dayOfMonth, long month) {
        if ((minute == 0 && hour > -1) || (minute == -1 && hour > -1)) {
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 0);
            if (minute == -1) {
                period = 59 * 60;
            }
        } else if (minute > 0 && hour != -1) {
            startTime.set(Calendar.SECOND, 0);
        }
        if (month == -1 && ((startTime.before(now) || startTime.equals(now))) && !(dayOfWeek != -1
                && dayOfMonth != -1)) {
            startTime.add(Calendar.MONTH, 1);
        } else if (month > -1) {
            if (startTime.get(Calendar.MONTH) < (int) month) {
                startTime.add(Calendar.MONTH, (int) month - startTime.get(Calendar.MONTH));
            } else if (startTime.get(Calendar.MONTH) > (int) month) {
                int months = (int) month - startTime.get(Calendar.MONTH);
                startTime.add(Calendar.YEAR, 1);
                startTime.add(Calendar.MONTH, months);
            } else if (startTime.get(Calendar.MONTH) == (int) month && startTime.before(now)) {
                startTime.add(Calendar.YEAR, 1);
            }
        }

        if (((now.get(Calendar.YEAR) < startTime.get(Calendar.YEAR)) || (
                now.get(Calendar.MONTH) < startTime.get(Calendar.MONTH) && (month != -1 || dayOfMonth != -1)) || (
                now.get(Calendar.DAY_OF_WEEK) != startTime.get(Calendar.DAY_OF_WEEK) && dayOfWeek != -1) || (
                now.get(Calendar.DAY_OF_MONTH) != startTime.get(Calendar.DAY_OF_MONTH) && dayOfMonth != -1)) && (
                hour == -1 && minute >= -1)) {
            startTime.set(Calendar.AM_PM, 0);
            startTime.set(Calendar.HOUR, 0);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 0);
        }
        if (now.get(Calendar.YEAR) < startTime.get(Calendar.YEAR) && (
                ((dayOfWeek <= 7 && dayOfWeek >= 1) || dayOfWeek == -1)
                        && startTime.get(Calendar.DAY_OF_WEEK) != (int) dayOfWeek)) {
            startTime.set(Calendar.DATE, 1);
            if (dayOfWeek != -1) {
                while (startTime.get(Calendar.DAY_OF_WEEK) != (int) dayOfWeek) {
                    if (startTime.get(Calendar.DAY_OF_WEEK) > (int) dayOfWeek) {
                        startTime.add(Calendar.DATE, 7 - (startTime.get(Calendar.DAY_OF_WEEK) - (int) dayOfWeek));
                    } else {
                        startTime.add(Calendar.DATE, (int) dayOfWeek - startTime.get(Calendar.DAY_OF_WEEK));
                    }
                }
            }
        }
        return startTime;
    }

    protected static void stopTask(Context ctx, int taskId) {
        stopExecution(ctx, taskId, 0);
    }
}
