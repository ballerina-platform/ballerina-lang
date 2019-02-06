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
import org.ballerinalang.stdlib.task.listener.utils.AppointmentConstants;
import org.ballerinalang.stdlib.task.listener.utils.AppointmentManager;
import org.ballerinalang.stdlib.task.listener.utils.ResourceFunctionHolder;
import org.ballerinalang.stdlib.task.listener.utils.TaskIdGenerator;
import org.ballerinalang.stdlib.task.listener.utils.TaskRegistry;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import java.util.ArrayList;

/**
 * Represents an appointment.
 */
public class Appointment extends AbstractTask {

    private String id = TaskIdGenerator.generate();
    private String cronExpression;

    public Appointment(Context context, String cronExpression, Service service) {
        super(service);
        TaskRegistry.getInstance().addTask(this);

        this.cronExpression = cronExpression;
        this.maxRuns = -1;
    }

    public Appointment(Context context, String cronExpression, Service service, long maxRuns) {
        super(service, maxRuns);
        TaskRegistry.getInstance().addTask(this);
        this.serviceList = new ArrayList<>();

        this.cronExpression = cronExpression;
        this.maxRuns = maxRuns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws SchedulingException {
        AppointmentManager.getInstance().stop(id);
        super.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws SchedulingException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() throws SchedulingException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runServices(Context context) throws SchedulingException {
        for (Service service : this.serviceList) {
            JobDataMap jobDataMap = getJobDataMapFromService(context, service);
            try {
                AppointmentManager.getInstance().schedule(id, AppointmentJob.class, jobDataMap, cronExpression);
            } catch (SchedulerException e) {
                throw new SchedulingException("Failed to schedule Task: " + this.id + ". " + e.getMessage());
            }
        }
    }

    private JobDataMap getJobDataMapFromService(Context context, Service service) {
        JobDataMap jobData = new JobDataMap();

        ResourceFunctionHolder resourceFunctionHolder = new ResourceFunctionHolder(service);
        FunctionInfo onErrorFunction = resourceFunctionHolder.getOnErrorFunction();
        FunctionInfo onTriggerFunction = resourceFunctionHolder.getOnTriggerFunction();

        jobData.put(AppointmentConstants.BALLERINA_PARENT_CONTEXT, context);
        jobData.put(AppointmentConstants.BALLERINA_ON_TRIGGER_FUNCTION, onTriggerFunction);
        jobData.put(AppointmentConstants.BALLERINA_ON_ERROR_FUNCTION, onErrorFunction);
        jobData.put(AppointmentConstants.BALLERINA_SERVICE_OBJECT, service);
        return jobData;
    }
}
