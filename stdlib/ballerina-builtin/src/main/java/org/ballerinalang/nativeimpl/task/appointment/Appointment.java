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
package org.ballerinalang.nativeimpl.task.appointment;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.nativeimpl.task.SchedulingException;
import org.ballerinalang.nativeimpl.task.TaskException;
import org.ballerinalang.nativeimpl.task.TaskIdGenerator;
import org.ballerinalang.nativeimpl.task.TaskRegistry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.quartz.SchedulerException;

/**
 * Represents an appointment.
 */
public class Appointment {

    private String id = TaskIdGenerator.generate();
    private boolean isDaemon;

    Appointment(NativeCallableUnit fn, Context balParentContext, String cronExpression, boolean isDaemon,
                FunctionRefCPEntry onTriggerFunction, FunctionRefCPEntry onErrorFunction) throws SchedulingException {
        TaskRegistry.getInstance().addAppointment(this);

        try {
            AppointmentManager.getInstance().
                    schedule(id, fn, AppointmentJob.class,
                            balParentContext, onTriggerFunction, onErrorFunction, cronExpression);
            this.isDaemon = isDaemon;
            if (isDaemon) {
                BLangScheduler.workerCountUp();
            }
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    public String getId() {
        return id;
    }

    public void stop() throws TaskException {
        if (isDaemon) {
            BLangScheduler.workerCountDown();
        }
        AppointmentManager.getInstance().stop(id);
        TaskRegistry.getInstance().remove(id);
    }
}
