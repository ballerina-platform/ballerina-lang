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

import org.apache.commons.lang3.StringUtils;
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
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;

import java.io.PrintStream;

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
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Schedules the task service with cron expression") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "onTrigger",
        value = "The schedule function as any type") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "onError",
        value = "The error function as any type") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "appointmentScheduler",
        value = "The struct with required attributes") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int)",
        value = "The identifier of the task") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "any)",
        value = "The error which is occurred while scheduling the task") })
public class ScheduleAppointment extends AbstractNativeFunction {
    private static final Log log = LogFactory.getLog(ScheduleAppointment.class.getName());

    public BValue[] execute(Context ctx) {
        log.info("Request has come to schedule the appointment");
        FunctionRefCPEntry onTriggerFunctionRefCPEntry = null;
        FunctionRefCPEntry onErrorFunctionRefCPEntry = null;
        if (ctx.getControlStackNew().getCurrentFrame().getRefLocalVars()[0] != null && ctx.getControlStackNew()
                .getCurrentFrame().getRefLocalVars()[0] instanceof BFunctionPointer) {
            onTriggerFunctionRefCPEntry = ((BFunctionPointer) getRefArgument(ctx, 0)).value();
        }
        if (ctx.getControlStackNew().getCurrentFrame().getRefLocalVars()[1] != null && ctx.getControlStackNew()
                .getCurrentFrame().getRefLocalVars()[1] instanceof BFunctionPointer) {
            onErrorFunctionRefCPEntry = ((BFunctionPointer) getRefArgument(ctx, 1)).value();
        }
        BValue scheduler = getRefArgument(ctx, 2);
        long minute = ((BStruct) scheduler).getIntField(0);
        long hour = ((BStruct) scheduler).getIntField(1);
        long dayOfWeek = ((BStruct) scheduler).getIntField(2);
        long dayOfMonth = ((BStruct) scheduler).getIntField(3);
        long month = ((BStruct) scheduler).getIntField(4);
        int taskId = -1;
        BString error = new BString("Unable to schedule the appointment");
        taskId = TaskUtil.generateTaskId(ctx);
        if (taskId != -1) {
            TaskScheduler.triggerAppointment(ctx, taskId, minute, hour, dayOfWeek, dayOfMonth, month,
                    onTriggerFunctionRefCPEntry, onErrorFunctionRefCPEntry);
            String schedulerError = StringUtils.isNotEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId))
                    ? ctx.getProperty(Constant.ERROR + "_" + taskId).toString() : "";
            taskId = StringUtils.isEmpty((String) ctx.getProperty(Constant.ERROR + "_" + taskId)) ? taskId : -1;
            error = new BString(schedulerError);
        }
        return getBValues(new BInteger(taskId), error);
    }
}
