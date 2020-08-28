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
package org.ballerinalang.stdlib.task.utils;

import org.ballerinalang.stdlib.task.objects.ServiceInformation;
import org.ballerinalang.stdlib.task.objects.Task;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_OBJECT;

/**
 * Represents a Quartz job related to an appointment.
 */
public class TaskJob implements Job {

    public TaskJob() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        Task task = (Task) jobDataMap.get(TASK_OBJECT);
        for (ServiceInformation serviceInformation : task.getServicesMap().values()) {
            TaskExecutor.executeFunction(serviceInformation);
        }
    }
}
