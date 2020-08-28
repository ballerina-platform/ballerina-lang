/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_MISFIRE_THRESHOLD;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_MISFIRE_THRESHOLD_VALUE;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_THREAD_COUNT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.QUARTZ_THREAD_COUNT_VALUE;


/**
 * Task manager to handle schedulers in ballerina tasks.
 */
public class TaskManager {
    private Scheduler scheduler;

    private static class TaskManagerHelper {
        private static final TaskManager INSTANCE = new TaskManager();
    }

    private TaskManager() {}

    public static TaskManager getInstance() {
        return TaskManagerHelper.INSTANCE;
    }

    public Scheduler getScheduler() throws SchedulingException {
        try {
            if (this.scheduler != null && this.scheduler.isStarted()) {
                return this.scheduler;
            }
            StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory(createSchedulerProperties());
            this.scheduler = stdSchedulerFactory.getScheduler();
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new SchedulingException("Cannot start the Task Listener/Scheduler.", e);
        }
        return this.scheduler;
    }

    private Properties createSchedulerProperties() {
        Properties properties = new Properties();
        properties.setProperty(QUARTZ_MISFIRE_THRESHOLD, QUARTZ_MISFIRE_THRESHOLD_VALUE);
        properties.setProperty(QUARTZ_THREAD_COUNT, QUARTZ_THREAD_COUNT_VALUE);
        return properties;
    }
}
