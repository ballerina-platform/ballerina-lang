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
import org.ballerinalang.stdlib.task.utils.TaskRegistry;

import java.util.ArrayList;

import static org.ballerinalang.stdlib.task.utils.TaskIdGenerator.generate;

/**
 * Abstract class which represents a ballerina task.
 */
public abstract class AbstractTask {

    protected String id = generate();
    protected ArrayList<Service> serviceList;
    protected long noOfRuns, maxRuns;

    public String getId() {
        return id;
    }

    /**
     * Stop the task.
     */
    public void stop() {
        TaskRegistry.getInstance().remove(id);
    }

    /**
     * Get list of attached services of the task.
     * @return  Services List
     */
    public abstract ArrayList<Service> getServices();

    /**
     * Add particular service to the registry of the Task.
     *
     * @param service   Service which needs to be attached to the task.
     */
    public abstract void addService(Service service);

    /**
     * Pause the task, if running.
     */
    public abstract void pause();

    /**
     * Resume the task if paused.
     */
    public abstract void resume();

    /**
     * Run all the services attached to the task.
     *
     * @param context - Ballerina context which runs the services.
     */
    public abstract void runServices(Context context);
}
