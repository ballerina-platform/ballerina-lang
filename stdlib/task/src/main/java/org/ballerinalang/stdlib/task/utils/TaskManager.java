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
package org.ballerinalang.stdlib.task.utils;

import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.objects.Appointment;
import org.ballerinalang.stdlib.task.objects.Timer;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.OperableTrigger;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Manages appointments.
 */
public class TaskManager {

    private static final TaskManager instance = new TaskManager();

    // Ballerina task ID to Quart JobKey map
    private Map<String, JobKey> quartzJobs = new HashMap<>();

    private Scheduler scheduler;

    private TaskManager() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("Appointment manager creation failed", e);
        }
    }

    /**
     * Get singleton Appointment Manager instance.
     *
     * @return Appointment Manager Instance.
     */
    public static TaskManager getInstance() {
        return instance;
    }

    /**
     * Schedule an Appointment.
     *
     * @param appointment Appointment which should be scheduled.
     * @param jobData     Map containing the details of the job.
     * @throws SchedulerException if scheduling is failed.
     */
    public void scheduleAppointment(Appointment appointment, JobDataMap jobData) throws SchedulerException {
        String appointmentId = appointment.getId();
        JobDetail job = newJob(TaskJob.class).usingJobData(jobData).withIdentity(appointmentId).build();
        CronTrigger trigger = newTrigger()
                .withIdentity(appointmentId)
                .withSchedule(cronSchedule(appointment.getCronExpression()))
                .build();

        if (appointment.getMaxRuns() > 0) {
            int noOfRuns = (int) appointment.getMaxRuns();
            Date endDate = TriggerUtils.computeEndTimeToAllowParticularNumberOfFirings((OperableTrigger) trigger,
                    new BaseCalendar(Calendar.getInstance().getTimeZone()), noOfRuns);

            trigger = trigger.getTriggerBuilder().endAt(endDate).build();

        }
        scheduler.scheduleJob(job, trigger);
        quartzJobs.put(appointmentId, job.getKey());
    }

    /**
     * Schedule a Timer.
     *
     * @param timer   Timer which should be scheduled.
     * @param jobData Map containing the details of the job.
     * @throws SchedulerException if scheduling is failed.
     */
    public void scheduleTimer(Timer timer, JobDataMap jobData) throws SchedulerException {
        SimpleScheduleBuilder schedule = createSchedulerBuilder(timer.getInterval(), timer.getMaxRuns());
        String taskId = timer.getId();
        JobDetail job = newJob(TaskJob.class).usingJobData(jobData).withIdentity(taskId).build();
        Trigger trigger;

        if (timer.getDelay() > 0) {
            Date startTime = new Date(System.currentTimeMillis() + timer.getDelay());
            trigger = newTrigger()
                    .withIdentity(taskId)
                    .startAt(startTime)
                    .forJob(job)
                    .withSchedule(schedule)
                    .build();
        } else {
            trigger = newTrigger()
                    .withIdentity(taskId)
                    .startNow()
                    .forJob(job)
                    .withSchedule(schedule)
                    .build();
        }

        scheduler.scheduleJob(job, trigger);
        quartzJobs.put(taskId, job.getKey());
    }

    /*public void addJob() {

    }*/

    private SimpleScheduleBuilder createSchedulerBuilder(long interval, long maxRuns) {
        SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule().withIntervalInMilliseconds(interval);
        if (maxRuns > 0) {
            // Quartz uses number of repeats, but we count total number of runs.
            // Hence we subtract 1 from the maxRuns to get the repeat count.
            simpleScheduleBuilder.withRepeatCount((int) maxRuns - 1);
        } else {
            simpleScheduleBuilder.repeatForever();
        }
        return simpleScheduleBuilder;
    }


    /**
     * Stops the scheduled Appointment.
     *
     * @param taskId ID of the task which should be stopped.
     * @throws SchedulingException if failed to stop the task.
     */
    public void stop(String taskId) throws SchedulingException {
        if (quartzJobs.containsKey(taskId)) {
            try {
                scheduler.deleteJob(quartzJobs.get(taskId));
            } catch (SchedulerException e) {
                throw new SchedulingException("Stopping appointment with ID " + taskId + " failed", e);
            }
        }
    }

    /**
     * Pauses the scheduled Appointment.
     *
     * @param taskId ID of the task to be paused.
     * @throws SchedulingException if failed to pause the task.
     */
    // TODO: Check the task is present at the registry
    public void pause(String taskId) throws SchedulingException {
        if (quartzJobs.containsKey(taskId)) {
            try {
                scheduler.pauseJob(quartzJobs.get(taskId));
            } catch (SchedulerException e) {
                throw new SchedulingException("Cannot pause the task. " + e.getMessage());
            }
        }
    }

    /**
     * Resumes a paused Task.
     *
     * @param taskId ID of the task to be resumed.
     * @throws SchedulingException if failed to resume the task.
     */
    // TODO: Check the task is present at the registry
    public void resume(String taskId) throws SchedulingException {
        if (quartzJobs.containsKey(taskId)) {
            try {
                scheduler.resumeJob(quartzJobs.get(taskId));
            } catch (SchedulerException e) {
                throw new SchedulingException("Cannot resume the task. " + e.getMessage());
            }
        }
    }
}
