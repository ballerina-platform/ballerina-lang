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
package org.ballerinalang.nativeimpl.task.appointment;

import org.ballerinalang.bre.Context;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Manages appointments.
 */
public class AppointmentManager {
    private static final AppointmentManager instance = new AppointmentManager();

    private Scheduler scheduler;

    private AppointmentManager() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
        } catch (SchedulerException e) {
            throw new RuntimeException("Appointment manager creation failed", e);
        }
    }

    public static AppointmentManager getInstance() {
        return instance;
    }

    void schedule(String taskId, AbstractNativeFunction fn, Class<? extends Job> jobClass,
                  Context balParentContext, FunctionRefCPEntry onTriggerFunction,
                  FunctionRefCPEntry onErrorFunction, String cronExpression) throws SchedulerException {
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

        //TODO: [Major issue] need to check whether adding jobs after the scheduler is started works
        scheduler.start();
    }

    public void stop(String taskId) {
//        scheduler.deleteJob(taskId);
//        scheduler.
    }
}
