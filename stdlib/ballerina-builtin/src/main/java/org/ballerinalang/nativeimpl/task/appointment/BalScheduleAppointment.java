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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.nativeimpl.task.SchedulingException;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

/**
 * Native function ballerina.task:scheduleAppointment.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "task",
        functionName = "scheduleAppointment",
        args = {@Argument(name = "onTrigger", type = TypeKind.FUNCTION),
                @Argument(name = "onError", type = TypeKind.FUNCTION),
                @Argument(name = "scheduleCronExpression", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class BalScheduleAppointment extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        FunctionRefCPEntry onTriggerFunctionRefCPEntry;
        FunctionRefCPEntry onErrorFunctionRefCPEntry = null;
        if (ctx.getLocalWorkerData().refRegs[0] != null &&
                ctx.getLocalWorkerData().refRegs[0] instanceof BFunctionPointer) {
            onTriggerFunctionRefCPEntry = ((BFunctionPointer) ctx.getRefArgument(0)).value();
        } else {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_TASK_CONFIG);
        }
        if (ctx.getLocalWorkerData().refRegs[1] != null &&
                ctx.getLocalWorkerData().refRegs[1] instanceof BFunctionPointer) {
            onErrorFunctionRefCPEntry = ((BFunctionPointer) ctx.getRefArgument(1)).value();
        }
        String schedule = ctx.getStringArgument(0);
        boolean isDaemon = ctx.getBooleanArgument(0);

        try {
            Appointment appointment = new Appointment(this, ctx, schedule, isDaemon, onTriggerFunctionRefCPEntry,
                    onErrorFunctionRefCPEntry);
            ctx.setReturnValues(new BString(appointment.getId()));
        } catch (SchedulingException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_TASK_CONFIG);
        }
    }
}
