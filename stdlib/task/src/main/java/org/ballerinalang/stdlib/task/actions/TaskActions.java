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
package org.ballerinalang.stdlib.task.actions;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.stdlib.task.api.TaskServerConnector;
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.impl.TaskServerConnectorImpl;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;
import org.ballerinalang.stdlib.task.objects.Task;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.MEMBER_LISTENER_CONFIGURATION;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RECORD_TIMER_CONFIGURATION;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.SCHEDULER_ERROR;
import static org.ballerinalang.stdlib.task.utils.Utils.createTaskError;
import static org.ballerinalang.stdlib.task.utils.Utils.processAppointment;
import static org.ballerinalang.stdlib.task.utils.Utils.processTimer;
import static org.ballerinalang.stdlib.task.utils.Utils.validateService;

/**
 * Class to handle ballerina external functions in Task library.
 *
 * @since 0.1.4
 */
public class TaskActions {

    public static Object pause(BObject taskListener) {
        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        try {
            task.pause();
        } catch (SchedulingException e) {
            return createTaskError(SCHEDULER_ERROR, e.getMessage());
        }
        return null;
    }

    public static Object resume(BObject taskListener) {
        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        try {
            task.resume();
        } catch (SchedulingException e) {
            return createTaskError(SCHEDULER_ERROR, e.getMessage());
        }
        return null;
    }

    public static Object detach(BObject taskListener, BObject service) {
        try {
            Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
            String serviceName = service.getType().getName();
            task.removeService(serviceName);
        } catch (Exception e) {
            return createTaskError(SCHEDULER_ERROR, e.getMessage());
        }
        return null;
    }

    public static Object start(BObject taskListener) {
        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        TaskServerConnector serverConnector = new TaskServerConnectorImpl(task);
        try {
            serverConnector.start();
        } catch (SchedulingException e) {
            return createTaskError(e.getMessage());
        }
        return null;
    }

    public static Object stop(BObject taskListener) {
        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        TaskServerConnector serverConnector = new TaskServerConnectorImpl(task);
        try {
            serverConnector.stop();
        } catch (SchedulingException e) {
            return createTaskError(e.getMessage());
        }
        return null;
    }

    public static Object attach(Environment env, BObject taskListener, BObject service, Object... attachments) {
        ServiceInformation serviceInformation;
        Runtime runtime = env.getRuntime();
        if (attachments == null) {
            serviceInformation = new ServiceInformation(runtime, service);
        } else {
            serviceInformation = new ServiceInformation(runtime, service, attachments);
        }

        /*
         * TODO: After #14148 fixed, use compiler plugin to validate the service
         */
        try {
            validateService(serviceInformation);
        } catch (SchedulingException e) {
            return createTaskError(e.getMessage());
        }

        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        task.addService(serviceInformation);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object init(BObject taskListener) {
        BMap<BString, Object> configurations = taskListener.getMapValue(MEMBER_LISTENER_CONFIGURATION);
        String configurationTypeName = configurations.getType().getName();
        Task task;
        try {
            if (RECORD_TIMER_CONFIGURATION.equals(configurationTypeName)) {
                task = processTimer(configurations);
            } else { // Record type validates at the compile time; Hence we do not need exhaustive validation.
                task = processAppointment(configurations);
            }
            taskListener.addNativeData(NATIVE_DATA_TASK_OBJECT, task);
        } catch (SchedulingException e) {
            return createTaskError(e.getMessage());
        }
        return null;
    }
}
