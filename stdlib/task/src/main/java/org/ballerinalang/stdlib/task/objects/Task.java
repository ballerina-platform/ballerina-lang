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
package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.stdlib.task.exceptions.SchedulingException;

import java.util.HashMap;

/**
 * Ballerina task interface.
 *
 * @since 0.995.0
 */
public interface Task {

    /**
     * Get the registered ID of the task.
     *
     * @return taskId
     */
    String getId();

    /**
     * Run all the services attached to the task.
     *
     * @throws SchedulingException When there is a failure to stop the task after maximum number of runs specified.
     */
    void start() throws SchedulingException;

    /**
     * Stop the task.
     *
     * @throws SchedulingException If the task cannot be stopped.
     */
    void stop() throws SchedulingException;

    /**
     * Pause the task.
     *
     * @throws SchedulingException If the task cannot be paused.
     */
    void pause() throws SchedulingException;

    /**
     * Resumes the task.
     *
     * @throws SchedulingException If the task cannot be resumed.
     */
    void resume() throws SchedulingException;

    /**
     * Get map of attached services of the task.
     *
     * @return Services Map
     */
    HashMap<String, ServiceInformation> getServicesMap();

    /**
     * Get map of attached services of the task.
     *
     * @param serviceName Service name of which the service should be retrieved.
     * @return Service object with the provided name.
     */
    ServiceInformation getService(String serviceName);

    /**
     * Add particular service to the registry of the Task.
     *
     * @param service Service which needs to be attached to the task.
     */
    void addService(ServiceInformation service);

    /**
     * Remove particular service from the registry of the Task.
     *
     * @param serviceName Name of the service which needs to be detached from the task.
     */
    void removeService(String serviceName);
}
