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
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.TriggerUtils;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.OperableTrigger;

import java.util.Calendar;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Represents an appointment.
 *
 * @since 0.995.0
 */
public class Appointment extends AbstractTask {

    private String cronExpression;

    /**
     * Creates an Appointment object with provided cron expression.
     *
     * @param cronExpression Cron expression for which the Appointment triggers.
     * @throws SchedulingException When initializing this Appointment is failed.
     */
    public Appointment(String cronExpression) throws SchedulingException {
        super();
        this.cronExpression = cronExpression;
    }

    /**
     * Creates an Appointment object with provided cron expression,
     * which will stop after running provided number of times.
     *
     * @param cronExpression Cron expression for which the Appointment triggers.
     * @param maxRuns        Number of times after which the Appointment will cancel.
     * @throws SchedulingException When initializing this Appointment is failed.
     */
    public Appointment(String cronExpression, long maxRuns) throws SchedulingException {
        super(maxRuns);
        this.cronExpression = cronExpression;
    }

    /**
     * Returns the cron expression for this Appointment.
     *
     * @return cron expression for this appointment to trigger.
     */
    private String getCronExpression() {
        return this.cronExpression;
    }

    /**
     * Gets the number of maximum runs of this Appointment.
     *
     * @return the number of times the appointment runs before shutdown.
     */
    private long getMaxRuns() {
        return this.maxRuns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws SchedulingException {
        JobDataMap jobDataMap = getJobDataMapFromTask();
        try {
            scheduleAppointment(jobDataMap);
        } catch (SchedulerException e) {
            throw new SchedulingException("Failed to schedule Task.", e);
        }
    }

    /**
     * Schedule an Appointment.
     *
     * @param jobData     Map containing the details of the job.
     * @throws SchedulerException if scheduling is failed.
     */
    private void scheduleAppointment(JobDataMap jobData) throws SchedulerException {
        String triggerId = this.getId();
        JobDetail job = newJob(TaskJob.class).usingJobData(jobData).withIdentity(triggerId).build();
        CronTrigger trigger = newTrigger()
                .withIdentity(triggerId)
                .withSchedule(buildCronScheduler(this.getCronExpression()))
                .build();

        if (this.getMaxRuns() > 0) {
            int repeatCount = (int) (this.getMaxRuns() - 1);
            Date endDate = TriggerUtils.computeEndTimeToAllowParticularNumberOfFirings((OperableTrigger) trigger,
                    new BaseCalendar(Calendar.getInstance().getTimeZone()), repeatCount);

            trigger = trigger.getTriggerBuilder().endAt(endDate).build();

        }
        scheduler.scheduleJob(job, trigger);
        quartzJobs.put(triggerId, job.getKey());
    }

    private static CronScheduleBuilder buildCronScheduler(String cronExpression) {
        return cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
    }
}
