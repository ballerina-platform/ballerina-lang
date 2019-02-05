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

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.utils.TaskRegistry;
import org.ballerinalang.stdlib.task.utils.TaskIdGenerator;

import java.util.ArrayList;

/**
 * Abstract class which represents a ballerina task.
 */
public abstract class AbstractTask implements Task {

    protected String id = TaskIdGenerator.generate();
    protected ArrayList<Service> serviceList;
    protected long noOfRuns, maxRuns;

    /**
     * Constructor to create a task without a limited (maximum) number of runs.
     *
     * @param service   service attached to the task.
     */
    protected AbstractTask(Service service) {
        this.serviceList = new ArrayList<>();
        this.addService(service);
        this.maxRuns = -1;
    }

    /**
     * Constructor to create a task with limited (maximum) number of runs.
     *
     * @param service   Service attached to the task.
     * @param maxRuns   Maximum number of runs allowed.
     */
    protected AbstractTask(Service service, long maxRuns) {
        this.serviceList = new ArrayList<>();
        this.addService(service);
        this.maxRuns = maxRuns;
        this.noOfRuns = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addService(Service service) {
        this.serviceList.add(service);
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws SchedulingException {
        TaskRegistry.getInstance().remove(this.id);
    }
}
