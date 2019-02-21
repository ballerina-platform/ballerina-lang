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
package org.ballerinalang.stdlib.task.listener.utils;

import org.ballerinalang.stdlib.task.SchedulingException;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
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
     * @param taskId         Task appointment ID which is going to be scheduled.
     * @param jobClass       Schedule Job class.
     * @param jobData        Map containing the details of the job.
     * @param cronExpression Cron expression in which the Appointment is scheduled.
     * @throws SchedulerException if scheduling is failed.
     */
    public void schedule(String taskId, Class<? extends Job> jobClass, JobDataMap jobData, String cronExpression)
            throws SchedulerException {
        JobDetail job = newJob(jobClass).usingJobData(jobData).withIdentity(taskId).build();
        CronTrigger trigger = newTrigger().withIdentity(taskId).withSchedule(cronSchedule(cronExpression)).build();

        scheduler.scheduleJob(job, trigger);
        quartzJobs.put(taskId, job.getKey());
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
     * @param taskID ID of the task to be paused.
     * @throws SchedulingException if failed to pause the task.
     */
    // TODO: Check the task is present at the registry
    public void pause(String taskID) throws SchedulingException {
        try {
            scheduler.pauseJob(quartzJobs.get(taskID));
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot pause the task. " + e.getMessage());
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
        try {
            scheduler.resumeJob(quartzJobs.get(taskId));
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot resume the task. " + e.getMessage());
        }
    }
}
