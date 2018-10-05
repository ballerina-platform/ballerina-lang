/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.task.appointment;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.util.codegen.FunctionInfo;
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
public class AppointmentManager {
    private static final AppointmentManager instance = new AppointmentManager();

    // Ballerina task ID to Quart JobKey map
    private Map<String, JobKey> quartzJobs = new HashMap<>();

    private Scheduler scheduler;

    private AppointmentManager() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("Appointment manager creation failed", e);
        }
    }

    public static AppointmentManager getInstance() {
        return instance;
    }

    void schedule(String taskId, NativeCallableUnit fn, Class<? extends Job> jobClass,
                  Context balParentContext, FunctionInfo onTriggerFunction,
                  FunctionInfo onErrorFunction, String cronExpression) throws SchedulerException {
        JobDataMap jobData = new JobDataMap();
        jobData.put(AppointmentConstants.BALLERINA_FUNCTION, fn);
        jobData.put(AppointmentConstants.BALLERINA_PARENT_CONTEXT, balParentContext);
        jobData.put(AppointmentConstants.BALLERINA_ON_TRIGGER_FUNCTION, onTriggerFunction);
        jobData.put(AppointmentConstants.BALLERINA_ON_ERROR_FUNCTION, onErrorFunction);
        JobDetail job = newJob(jobClass).usingJobData(jobData).withIdentity(taskId).build();

        CronTrigger trigger =
                newTrigger().withIdentity(taskId).withSchedule(cronSchedule(cronExpression))
                        .build();

        scheduler.scheduleJob(job, trigger);
        quartzJobs.put(taskId, job.getKey());
    }

    public void stop(String taskId) {
        if (quartzJobs.containsKey(taskId)) {
            try {
                scheduler.deleteJob(quartzJobs.get(taskId));
            } catch (SchedulerException e) {
                throw new RuntimeException("Stopping appointment with ID " + taskId + " failed", e);
            }
        }
    }
}
