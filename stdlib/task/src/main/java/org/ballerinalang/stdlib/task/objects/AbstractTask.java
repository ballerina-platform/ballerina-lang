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

package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.utils.TaskIdGenerator;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_OBJECT;

/**
 * Abstract class which represents a ballerina task.
 *
 * @since 0.995.0
 */
public abstract class AbstractTask implements Task {

    protected String id = TaskIdGenerator.generate();
    HashMap<String, ServiceWithParameters> serviceMap;
    protected Map<String, JobKey> quartzJobs = new HashMap<>();
    long maxRuns;
    Scheduler scheduler;

    /**
     * Constructor to create a task without a limited (maximum) number of runs.
     */
    AbstractTask() throws SchedulingException {
        this.serviceMap = new HashMap<>();
        this.maxRuns = -1;
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot initialize the Task Listener/Scheduler. " + e.getMessage());
        }
    }

    /**
     * Constructor to create a task with limited (maximum) number of runs.
     *
     * @param maxRuns Maximum number of runs allowed.
     */
    AbstractTask(long maxRuns) throws SchedulingException {
        validateMaxRuns(maxRuns);
        this.serviceMap = new HashMap<>();
        this.maxRuns = maxRuns;
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot initialize the Task Listener/Scheduler. " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addService(ServiceWithParameters service) {
        this.serviceMap.put(service.getName(), service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeService(String serviceName) {
        this.serviceMap.remove(serviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, ServiceWithParameters> getServicesMap() {
        return this.serviceMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceWithParameters getService(String serviceName) {
        return this.serviceMap.get(serviceName);
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Create a job data map using the context and the service.
     *
     * @return JobDataMap consists of context and the <code>ServiceWithParameter</code> object.
     */
    JobDataMap getJobDataMapFromTask() {
        JobDataMap jobData = new JobDataMap();
        jobData.put(TASK_OBJECT, this);
        return jobData;
    }

    private void validateMaxRuns(long maxRuns) throws SchedulingException {
        if (maxRuns < 1) {
            throw new SchedulingException("Task noOfOccurrences should be a positive integer.");
        }
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
                throw new SchedulingException("Cannot cancel the task. ", e);
            }
        } else {
            throwTaskNotFoundException();
        }
    }

    /**
     * Pauses the scheduled Appointment.
     *
     * @param taskId ID of the task to be paused.
     * @throws SchedulingException if failed to pause the task.
     */
    public void pause(String taskId) throws SchedulingException {
        if (quartzJobs.containsKey(taskId)) {
            try {
                scheduler.pauseJob(quartzJobs.get(taskId));
            } catch (SchedulerException e) {
                throw new SchedulingException("Cannot pause the task. " + e.getMessage());
            }
        } else {
            throwTaskNotFoundException();
        }
    }

    /**
     * Resumes a paused Task.
     *
     * @param taskId ID of the task to be resumed.
     * @throws SchedulingException if failed to resume the task.
     */
    public void resume(String taskId) throws SchedulingException {
        if (quartzJobs.containsKey(taskId)) {
            try {
                scheduler.resumeJob(quartzJobs.get(taskId));
            } catch (SchedulerException e) {
                throw new SchedulingException("Cannot resume the task. " + e.getMessage());
            }
        } else {
            throwTaskNotFoundException();
        }
    }

    private void throwTaskNotFoundException() throws SchedulingException {
        throw new SchedulingException("Task not found");
    }
}
