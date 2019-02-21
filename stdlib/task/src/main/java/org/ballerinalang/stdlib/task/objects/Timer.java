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
 *
 *
 */
package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.bre.Context;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.utils.TaskExecutor;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Timer object used to create and run Timers.
 */
public class Timer extends AbstractTask {
    private long interval, delay;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /**
     * Creates a Timer object.
     *
     * @param context  The ballerina context.
     * @param delay    The initial delay.
     * @param interval The interval between two task executions.
     * @throws SchedulingException When provided configuration values are invalid.
     */
    public Timer(Context context, long delay, long interval) throws SchedulingException {
        super();
        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }
        this.interval = interval;
        this.delay = delay;
    }

    /**
     * Creates a Timer object with limited number of running times.
     *
     * @param context   The ballerina context.
     * @param delay     The initial delay.
     * @param interval  The interval between two task executions.
     * @param maxRuns   Number of times after which the timer will turn off.
     * @throws SchedulingException When provided configuration values are invalid.
     */
    public Timer(Context context, long delay, long interval, long maxRuns) throws SchedulingException {
        super(maxRuns);
        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }
        this.interval = interval;
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws SchedulingException {
        this.executorService.shutdown();
    }

    private static void callTriggerFunction(Context context, ServiceWithParameters serviceWithParameters) {
        TaskExecutor.execute(context, serviceWithParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws SchedulingException {
        if (TaskState.PAUSED == this.getState()) {
            // TODO: Get the task name and print it on the error message.
            throw new SchedulingException("Cannot pause the task: " + " Task is already at paused state.");
        } else if (TaskState.STOPPED == this.getState()) {
            // TODO: Get the task name and print it on the error message.
            throw new SchedulingException("Cannot pause the task: " + " Task is not started yet.");
        }
        this.setState(TaskState.PAUSED);
    }

    /**
     * {@inheritDoc}
     */
    public void resume() throws SchedulingException {
        if (TaskState.STARTED == this.getState()) {
            // TODO: Get the task name and print it on the error message.
            throw new SchedulingException("Cannot resume the task: " + " Task is already at running state.");
        } else if (TaskState.STOPPED == this.getState()) {
            // TODO: Get the task name and print it on the error message.
            throw new SchedulingException("Cannot resume the task: " + " Task is not started yet.");
        }
        this.setState(TaskState.STARTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runServices(Context context) {
        final Runnable schedulerFunc = () -> {
            if (TaskState.PAUSED == this.getState()) {
                return;
            }
            if (this.maxRuns > 0 && this.maxRuns == noOfRuns) {
                try {
                    this.stop();
                } catch (SchedulingException e) {
                    throw new BLangRuntimeException("Failed to stop the task: " + e.getMessage());
                }
                return;
            }
            for (ServiceWithParameters serviceWithParameters : getServicesMap().values()) {
                callTriggerFunction(context, serviceWithParameters);
                this.noOfRuns++;
            }
        };
        executorService.scheduleWithFixedDelay(schedulerFunc, delay, interval, TimeUnit.MILLISECONDS);
    }
}
