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
package org.ballerinalang.stdlib.task.impl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.stdlib.task.api.TaskServerConnector;
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.objects.Task;

/**
 * ballerina/task server connector implementation.
 *
 * @since 0.995.0
 */
public class TaskServerConnectorImpl implements TaskServerConnector {

    /**
     * Native Task object mapped to listener.
     */
    private Task task;

    /**
     * Context of the calling service.
     */
    private Context context = null;

    /**
     * Constructor of the server connector.
     *
     * @param context Context of which the server connector is called.
     * @param task    Native Task object which is mapped to the Listener.
     */
    //TODO Remove after migration : implemented using bvm values/types
    public TaskServerConnectorImpl(Context context, Task task) {
        this.task = task;
        this.context = context;
    }

    /**
     * Constructor of the server connector.
     *
     * @param task Native Task object which is mapped to the Listener.
     */
    public TaskServerConnectorImpl(Task task) {
        this.task = task;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws SchedulingException {
        //TODO Remove this condition after migration : Added just to distinguish both bvm and jvm exec
        if (context == null) {
            this.task.start();
        } else {
            this.task.start(context);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws SchedulingException {
        this.task.stop();
    }
}
