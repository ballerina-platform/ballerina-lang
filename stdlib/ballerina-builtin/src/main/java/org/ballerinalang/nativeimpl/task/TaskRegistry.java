/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.nativeimpl.task;

import org.ballerinalang.nativeimpl.task.appointment.Appointment;
import org.ballerinalang.nativeimpl.task.timer.Timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains timers and appointments (Appointment support will be available in the future).
 */
public class TaskRegistry {

    private static TaskRegistry instance = new TaskRegistry();
    private Map<String, Timer> timers = new HashMap<>();
    private Map<String, Appointment> appointments = new HashMap<>();

    private TaskRegistry() {
    }

    public static TaskRegistry getInstance() {
        return instance;
    }

    public void stopTask(String taskId) {
        if (timers.containsKey(taskId)) {
            timers.get(taskId).stop();
        } else if (appointments.containsKey(taskId)) {
            appointments.get(taskId).stop();
        }
    }

    public void addTimer(Timer timer) {
        String taskId = timer.getId();
        checkDuplicateTask(taskId);
        timers.put(taskId, timer);
    }

    public void addAppointment(Appointment appointment) {
        String taskId = appointment.getId();
        checkDuplicateTask(taskId);
        appointments.put(taskId, appointment);
    }

    private void checkDuplicateTask(String taskId) {
        if (timers.containsKey(taskId) || appointments.containsKey(taskId)) {
            throw new IllegalArgumentException("Task with ID " + taskId + " already exists");
        }
    }

    public void remove(String taskId) {
        if (timers.containsKey(taskId)) {
            timers.remove(taskId);
        } else if (appointments.containsKey(taskId)) {
            appointments.remove(taskId);
        }
    }
}
