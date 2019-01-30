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
package org.ballerinalang.stdlib.task.listener.impl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.api.TaskServerConnector;
import org.ballerinalang.stdlib.task.listener.objects.Timer;
import org.ballerinalang.stdlib.task.listener.utils.TaskRegistry;

/**
 * ballerina/task server connector implementation.
 */
public class TaskServerConnectorImpl implements TaskServerConnector {

    /**
     * ID of the task which is attached to the listener.
     */
    private String taskId;

    /**
     * Context of the calling service.
     */
    private Context context;

    /**
     * Constructor of the server connector
     *
     * @param context - context of which the server connector is called.
     * @param taskId - task ID of the task which is bound to the service.
     */
    public TaskServerConnectorImpl(Context context, String taskId) {
        this.taskId = taskId;
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws SchedulingException {
        Timer timer = TaskRegistry.getInstance().getTimer(taskId);
        timer.runServices(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stop() throws SchedulingException {
        Timer timer = TaskRegistry.getInstance().getTimer(taskId);
        timer.stop();
        return true;
    }
}
