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
 */
package org.ballerinalang.stdlib.task.listener.objects;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.utils.ResourceFunctionHolder;
import org.ballerinalang.stdlib.task.listener.utils.TaskExecutor;
import org.ballerinalang.stdlib.task.listener.utils.TaskRegistry;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a Timer object used to create and run Timers.
 */
@SuppressWarnings("Duplicates")
public class Timer extends AbstractTask {
    private long interval, delay;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private boolean isPaused = false;

    /**
     * Creates a Timer object.
     *
     * @param context  The ballerina context.
     * @param delay    The initial delay.
     * @param interval The interval between two task executions.
     * @param service  Service attached to the listener.
     * @throws SchedulingException if cannot create the scheduler.
     */
    public Timer(Context context, long delay, long interval, Service service) throws SchedulingException {
        super(service);
        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }
        this.interval = interval;
        this.delay = delay;

        TaskRegistry.getInstance().addTask(this);
    }

    /**
     * Creates a Timer object with limited number of running times.
     *
     * @param context   The ballerina context.
     * @param delay     The initial delay.
     * @param interval  The interval between two task executions.
     * @param service   Service attached to the listener.
     * @param maxRuns   Number of times after which the timer will turn off.
     * @throws SchedulingException if cannot create the scheduler.
     */
    public Timer(Context context, long delay, long interval, Service service, long maxRuns) throws SchedulingException {
        super(service, maxRuns);
        if (delay < 0 || interval < 0) {
            throw new SchedulingException("Timer scheduling delay and interval should be non-negative values");
        }
        this.interval = interval;
        this.delay = delay;

        TaskRegistry.getInstance().addTask(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws SchedulingException {
        this.executorService.shutdown();
        super.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws SchedulingException {
        if (this.isPaused) {
            throw new SchedulingException("Timer is already paused");
        }
        this.isPaused = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() throws SchedulingException {
        if (!this.isPaused) {
            throw new SchedulingException("Timer is already running.");
        }
        this.isPaused = false;
    }

    private static void callTriggerFunction(Context context, ResourceFunctionHolder functionHolder, Service service) {
        TaskExecutor.execute(context,
                functionHolder.getOnTriggerFunction(), functionHolder.getOnErrorFunction(), service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runServices(Context context) {
        final Runnable schedulerFunc = () -> {
            if (this.isPaused) {
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
            this.noOfRuns++;
            for (Service service : getServices()) {
                ResourceFunctionHolder resourceFunctionHolder = new ResourceFunctionHolder(service);
                callTriggerFunction(context, resourceFunctionHolder, service);
            }
        };
        executorService.scheduleWithFixedDelay(schedulerFunc, delay, interval, TimeUnit.MILLISECONDS);
    }
}
