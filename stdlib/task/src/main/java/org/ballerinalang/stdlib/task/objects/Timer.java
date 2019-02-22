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
import org.ballerinalang.stdlib.task.utils.TaskManager;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

/**
 * Represents a Timer object used to create and run Timers.
 */
public class Timer extends AbstractTask {
    private long interval, delay;

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
        if (delay < 0 || interval < 1) {
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
        if (delay < 0 || interval < 1) {
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
        TaskManager.getInstance().stop(this.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws SchedulingException {
        TaskManager.getInstance().pause(this.getId());
    }

    /**
     * {@inheritDoc}
     */
    public void resume() throws SchedulingException {
        TaskManager.getInstance().resume(this.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runServices(Context context) throws SchedulingException {
        for (ServiceWithParameters serviceWithParameters : getServicesMap().values()) {
            JobDataMap jobDataMap = getJobDataMapFromService(context, serviceWithParameters);
            try {
                TaskManager.getInstance().scheduleTimer(this, jobDataMap);
            } catch (SchedulerException e) {
                throw new SchedulingException("Failed to schedule Task: " + this.id + ". " + e.getMessage());
            }
        }
    }

    public long getInterval() {
        return this.interval;
    }

    public long getDelay() {
        return this.delay;
    }

    public long getMaxRuns() {
        return this.maxRuns;
    }
}
