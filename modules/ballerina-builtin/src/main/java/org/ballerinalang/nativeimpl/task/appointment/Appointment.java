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
import org.ballerinalang.bre.bvm.WorkerContext;
import org.ballerinalang.nativeimpl.task.SchedulingException;
import org.ballerinalang.nativeimpl.task.TaskException;
import org.ballerinalang.nativeimpl.task.TaskExecutor;
import org.ballerinalang.nativeimpl.task.TaskIdGenerator;
import org.ballerinalang.nativeimpl.task.TaskRegistry;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * Represents an appointment.
 */
public class Appointment implements Job {
    private String id = TaskIdGenerator.generate();
    private Context balParentContext;
    private FunctionRefCPEntry onTriggerFunction;
    private FunctionRefCPEntry onErrorFunction;

    public Appointment(Context balParentContext,
                       String cronExpression, FunctionRefCPEntry onTriggerFunction,
                       FunctionRefCPEntry onErrorFunction) throws SchedulingException {
        this.balParentContext = balParentContext;
        this.onTriggerFunction = onTriggerFunction;
        this.onErrorFunction = onErrorFunction;
        TaskRegistry.getInstance().addAppointment(this);

        try {
            AppointmentManager.getInstance().schedule(id, cronExpression);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    public String getId() {
        return id;
    }

    public void stop() throws TaskException {
//        executorService.shutdown();
        // TODO: remove from Quartz schedule
        TaskRegistry.getInstance().remove(id);
        balParentContext.endTrackWorker();
    }

    @Override
    public void execute(JobExecutionContext quartContext) throws JobExecutionException {
//        this.quartzJobKey = quartContext.getJobDetail().getKey();

        ProgramFile programFile = balParentContext.getProgramFile();
        //Create new instance of the context and set required properties.
        Context newContext = new WorkerContext(programFile, balParentContext);
        TaskExecutor.execute(balParentContext, onTriggerFunction, onErrorFunction, programFile, newContext);
    }
}
