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
*/
package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.utils.TaskJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Represents a Timer object used to create and run Timers.
 *
 * @since 0.995.0
 */
public class Timer extends AbstractTask {

    private long interval, delay;

    /**
     * Creates a Timer object.
     *
     * @param delay    The initial delay.
     * @param interval The interval between two task executions.
     * @throws SchedulingException When provided configuration values are invalid.
     */
    public Timer(long delay, long interval) throws SchedulingException {
        super();
        validateTimerConfigurations(delay, interval);
        this.interval = interval;
        this.delay = delay;
    }

    /**
     * Creates a Timer object with limited number of running times.
     *
     * @param delay    The initial delay.
     * @param interval The interval between two task executions.
     * @param maxRuns  Number of times after which the timer will turn off.
     * @throws SchedulingException When provided configuration values are invalid.
     */
    public Timer(long delay, long interval, long maxRuns) throws SchedulingException {
        super(maxRuns);
        validateTimerConfigurations(delay, interval);
        this.interval = interval;
        this.delay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws SchedulingException {
        JobDataMap jobDataMap = getJobDataMapFromTask();
        try {
            scheduleTimer(jobDataMap);
        } catch (SchedulerException e) {
            throw new SchedulingException("Failed to schedule task.", e);
        }
    }

    /**
     * Gets the interval of this Timer.
     *
     * @return the interval of this timer.
     */
    public long getInterval() {
        return this.interval;
    }

    /**
     * Gets the delay of this Timer.
     *
     * @return the delay of this timer.
     */
    public long getDelay() {
        return this.delay;
    }

    /**
     * Gets the number of maximum runs of this Timer.
     *
     * @return the number of times the timer runs before shutdown.
     */
    private long getMaxRuns() {
        return this.maxRuns;
    }

    private void validateTimerConfigurations(long delay, long interval) throws SchedulingException {
        if (delay < 0) {
            throw new SchedulingException("Timer scheduling delay should be a non-negative value.");
        }
        if (interval < 1) {
            throw new SchedulingException("Timer scheduling interval should be a positive integer.");
        }
    }

    /**
     * Schedule a Timer.
     *
     * @param jobData Map containing the details of the job.
     * @throws SchedulerException if scheduling is failed.
     */
    private void scheduleTimer(JobDataMap jobData) throws SchedulerException {
        SimpleScheduleBuilder schedule = createSchedulerBuilder(this.getInterval(), this.getMaxRuns());
        String triggerId = this.getId();
        JobDetail job = newJob(TaskJob.class).usingJobData(jobData).withIdentity(triggerId).build();
        Trigger trigger;

        if (this.getDelay() > 0) {
            Date startTime = new Date(System.currentTimeMillis() + this.getDelay());
            trigger = newTrigger()
                    .withIdentity(triggerId)
                    .startAt(startTime)
                    .forJob(job)
                    .withSchedule(schedule)
                    .build();
        } else {
            trigger = newTrigger()
                    .withIdentity(triggerId)
                    .startNow()
                    .forJob(job)
                    .withSchedule(schedule)
                    .build();
        }

        scheduler.scheduleJob(job, trigger);
        quartzJobs.put(triggerId, job.getKey());
    }

    private static SimpleScheduleBuilder createSchedulerBuilder(long interval, long maxRuns) {
        SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule()
                .withMisfireHandlingInstructionNextWithExistingCount()
                .withIntervalInMilliseconds(interval);
        if (maxRuns > 0) {
            // Quartz uses number of repeats, but we count total number of runs.
            // Hence we subtract 1 from the maxRuns to get the repeat count.
            simpleScheduleBuilder.withRepeatCount((int) (maxRuns - 1));
        } else {
            simpleScheduleBuilder.repeatForever();
        }
        return simpleScheduleBuilder;
    }
}
