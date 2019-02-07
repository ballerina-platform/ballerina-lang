/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.task.listener.utils;

import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.objects.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maintains taskMap and appointments (Appointment support will be available in the future).
 */
public class TaskRegistry {

    private static TaskRegistry instance = new TaskRegistry();
    private Map<String, Task> taskMap = new HashMap<>();

    private TaskRegistry() {
    }

    /**
     * Get singleton Task Registry instance.
     *
     * @return Task Registry instance.
     */
    public static TaskRegistry getInstance() {
        return instance;
    }

    /**
     * Add a task to the registery.
     *
     * @param task task to be added to the registry.
     */
    public void addTask(Task task) {
        String taskId = task.getId();
        checkDuplicateTask(taskId);
        taskMap.put(taskId, task);
    }

    /**
     * Checks fro a task is available in the registry, and return it.
     *
     * @param taskID Task ID to look for.
     * @return Task object with the provided ID.
     * @throws SchedulingException if the task with provided ID does not found.
     */
    public Task getTask(String taskID) throws SchedulingException {
        if (Objects.nonNull(taskMap.get(taskID))) {
            return taskMap.get(taskID);
        } else {
            throw new SchedulingException("Timer not available for the service");
        }
    }

    /**
     * Check for a task is already available with the same ID.
     *
     * @param taskId Task ID to look for.
     */
    private void checkDuplicateTask(String taskId) {
        if (taskMap.containsKey(taskId)) {
            throw new IllegalArgumentException("Task with ID " + taskId + " already exists");
        }
    }

    /**
     * Removes a Task from the registry.
     *
     * @param taskId ID of the task to be removed.
     * @throws SchedulingException if a task is not available with the provided ID.
     */
    public void remove(String taskId) throws SchedulingException {
        if (taskMap.containsKey(taskId)) {
            taskMap.remove(taskId);
        } else {
            throw new SchedulingException("Task cannot be found");
        }
    }
}
