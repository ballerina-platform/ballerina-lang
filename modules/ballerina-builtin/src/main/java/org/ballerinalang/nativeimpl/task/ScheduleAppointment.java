/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;

import static org.ballerinalang.nativeimpl.task.TaskScheduler.getScheduleTaskErrorsMap;

/**
 * Native function ballerina.model.task:scheduleAppointment.
 */
@BallerinaFunction(
    packageName = "ballerina.task",
    functionName = "scheduleAppointment",
    args = {@Argument(name = "onTrigger", type = TypeKind.ANY),
            @Argument(name = "onError", type = TypeKind.ANY),
            @Argument(name = "appointmentScheduler", type = TypeKind.STRUCT, structType = "AppointmentScheduler")},
    returnType = {@ReturnType(type = TypeKind.INT), @ReturnType(type = TypeKind.ANY)},
    isPublic = true
)
public class ScheduleAppointment extends AbstractNativeFunction {
    private static final Log log = LogFactory.getLog(ScheduleAppointment.class.getName());

    public BValue[] execute(Context ctx) {
        FunctionRefCPEntry onTriggerFunctionRefCPEntry = null;
        FunctionRefCPEntry onErrorFunctionRefCPEntry = null;
        if (ctx.getControlStackNew().getCurrentFrame().getRefLocalVars()[0] != null && ctx.getControlStackNew()
                .getCurrentFrame().getRefLocalVars()[0] instanceof BFunctionPointer) {
            onTriggerFunctionRefCPEntry = ((BFunctionPointer) getRefArgument(ctx, 0)).value();
        } else {
            log.error("The onTrigger function is not provided");
            return getBValues(new BInteger(-1), new BString("The onTrigger function is not provided"));
        }
        if (ctx.getControlStackNew().getCurrentFrame().getRefLocalVars()[1] != null && ctx.getControlStackNew()
                .getCurrentFrame().getRefLocalVars()[1] instanceof BFunctionPointer) {
            onErrorFunctionRefCPEntry = ((BFunctionPointer) getRefArgument(ctx, 1)).value();
        }
        BStruct scheduler = (BStruct) getRefArgument(ctx, 2);
        int minute = (int) scheduler.getIntField(0);
        int hour = (int) scheduler.getIntField(1);
        int dayOfWeek = (int) scheduler.getIntField(2);
        int dayOfMonth = (int) scheduler.getIntField(3);
        int month = (int) scheduler.getIntField(4);
        if (log.isDebugEnabled()) {
            log.debug("Request has come to schedule the appointment with the expression: " + minute + " " + hour + " "
                    + dayOfWeek + " " + dayOfMonth + " " + month);
        }
        int taskId = TaskUtil.generateTaskId();
        TaskScheduler.triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month,
                onTriggerFunctionRefCPEntry, onErrorFunctionRefCPEntry);
        String errorFromScheduler = getScheduleTaskErrorsMap().get(taskId);
        String schedulerError = errorFromScheduler != null && !errorFromScheduler.isEmpty() ? errorFromScheduler : "";
        taskId = errorFromScheduler == null || errorFromScheduler.isEmpty() ? taskId : -1;
        BString error = new BString(schedulerError);
        return getBValues(new BInteger(taskId), error);
    }
}
