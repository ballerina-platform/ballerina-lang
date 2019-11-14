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

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.objects.Task;
import org.ballerinalang.stdlib.task.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.SCHEDULER_ERROR_REASON;

/**
 * Class to handle ballerina external functions in Task library.
 *
 * @since 0.1.4
 */
public class TaskActions {

    private static final Logger LOG = LoggerFactory.getLogger(TaskActions.class);

    public static Object pause(ObjectValue taskListener) {
        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        try {
            task.pause();
        } catch (SchedulingException e) {
            LOG.error(e.getMessage(), e);
            return Utils.createTaskError(SCHEDULER_ERROR_REASON, e.getMessage());
        }
        return null;
    }

    public static Object resume(ObjectValue taskListener) {
        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        try {
            task.resume();
        } catch (SchedulingException e) {
            LOG.error(e.getMessage(), e);
            return Utils.createTaskError(SCHEDULER_ERROR_REASON, e.getMessage());
        }
        return null;
    }

    public static Object detach(ObjectValue taskListener, ObjectValue service) {
        try {
            Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
            String serviceName = service.getType().getName();
            task.removeService(serviceName);
        } catch (Exception e) {
            return Utils.createTaskError(SCHEDULER_ERROR_REASON, e.getMessage());
        }
        return null;
    }
}
