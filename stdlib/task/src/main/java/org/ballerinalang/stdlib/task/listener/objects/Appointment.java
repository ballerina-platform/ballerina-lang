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
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.utils.AppointmentJob;
import org.ballerinalang.stdlib.task.listener.utils.AppointmentManager;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.APPOINTMENT_PARENT_CONTEXT;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.APPOINTMENT_SERVICE_OBJECT;
import static org.quartz.CronExpression.isValidExpression;

/**
 * Represents an appointment.
 */
public class Appointment extends AbstractTask {

    private String cronExpression;

    /**
     * Creates an Appointment object with provided cron expression.
     *
     * @param context        Ballerina context which creating the Appointment.
     * @param cronExpression Cron expression for which the Appointment triggers.
     * @throws SchedulingException When provided cron expression is invalid.
     */
    public Appointment(Context context, String cronExpression) throws SchedulingException {
        super();
        if (!validateCronExpression(cronExpression)) {
            throw new SchedulingException("Invalid cron expression provided.");
        }
        this.cronExpression = cronExpression;
        this.maxRuns = -1;
    }

    /**
     * Creates an Appointment object with provided cron expression,
     * which will stop after running provided number of times.
     *
     * @param context        Ballerina context which creating the Appointment.
     * @param cronExpression Cron expression for which the Appointment triggers.
     * @param maxRuns        Number of times after which the Appointment will cancel.
     * @throws SchedulingException When provided cron expression is invalid.
     */
    public Appointment(Context context, String cronExpression, long maxRuns) throws SchedulingException {
        super();
        if (!validateCronExpression(cronExpression)) {
            throw new SchedulingException("Invalid cron expression provided.");
        }
        this.cronExpression = cronExpression;
        this.maxRuns = maxRuns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws SchedulingException {
        AppointmentManager.getInstance().stop(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws SchedulingException {
        AppointmentManager.getInstance().pause(this.getId());
    }

    /**
     * {@inheritDoc}
     */
    public void resume() throws SchedulingException {
        AppointmentManager.getInstance().resume(this.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runServices(Context context) throws SchedulingException {
        for (ServiceWithParameters serviceWithParameters : this.getServicesMap().values()) {
            JobDataMap jobDataMap = getJobDataMapFromService(context, serviceWithParameters);
            try {
                AppointmentManager.getInstance().schedule(id, AppointmentJob.class, jobDataMap, cronExpression);
            } catch (SchedulerException e) {
                throw new SchedulingException("Failed to schedule Task: " + this.id + ". " + e.getMessage());
            }
        }
    }

    private JobDataMap getJobDataMapFromService(Context context, ServiceWithParameters serviceWithParameters) {
        JobDataMap jobData = new JobDataMap();
        jobData.put(APPOINTMENT_PARENT_CONTEXT, context);
        jobData.put(APPOINTMENT_SERVICE_OBJECT, serviceWithParameters);
        return jobData;
    }

    private boolean validateCronExpression(String cronExpression) {
        return isValidExpression(cronExpression);
    }
}
