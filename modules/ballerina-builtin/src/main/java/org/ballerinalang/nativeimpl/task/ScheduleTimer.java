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

/**
 * Native function ballerina.model.task:scheduleTimer.
 */
@BallerinaFunction(
        packageName = "ballerina.task",
        functionName = "scheduleTimer",
        args = {@Argument(name = "onTrigger", type = TypeKind.ANY),
                @Argument(name = "onError", type = TypeKind.ANY),
                @Argument(name = "timerScheduler", type = TypeKind.STRUCT, structType = "TimerScheduler")},
        returnType = {@ReturnType(type = TypeKind.INT), @ReturnType(type = TypeKind.ANY)},
        isPublic = true
)
public class ScheduleTimer extends AbstractNativeFunction {
    private static final Log log = LogFactory.getLog(ScheduleTimer.class.getName());

    public BValue[] execute(Context ctx) {
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
        BStruct scheduler = (BStruct) getRefArgument(ctx, 2);
        long delay = scheduler.getIntField(0);
        long interval = scheduler.getIntField(1);
        log.info("Request has come to schedule the timer with the INITIAL DELAY: " + delay + " and INTERVAL: "
                + interval);
        BString error = new BString("Unable to schedule the timer");
        int taskId = TaskUtil.generateTaskId(ctx);
        if (taskId != -1) {
            TaskScheduler
                    .triggerTimer(ctx, taskId, delay, interval, onTriggerFunctionRefCPEntry, onErrorFunctionRefCPEntry);
            String errorFromContext = (String) ctx.getProperty(Constant.ERROR + "_" + taskId);
            String schedulerError = errorFromContext != null && !errorFromContext.isEmpty() ?
                    ctx.getProperty(Constant.ERROR + "_" + taskId).toString() :
                    "";
            taskId = errorFromContext == null || errorFromContext.isEmpty() ? taskId : -1;
            error = new BString(schedulerError);
        }
        return getBValues(new BInteger(taskId), error);
    }
}
