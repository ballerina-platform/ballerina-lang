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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.task.SchedulingException;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;

/**
 * Native function ballerina.task:scheduleAppointment.
 */
@BallerinaFunction(
        packageName = "ballerina.task",
        functionName = "scheduleAppointment",
        args = {@Argument(name = "onTrigger", type = TypeKind.ANY),
                @Argument(name = "onError", type = TypeKind.ANY),
                @Argument(name = "scheduleCronExpression", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRING), @ReturnType(type = TypeKind.STRUCT)},
        isPublic = true
)
public class BalScheduleAppointment extends AbstractNativeFunction {

    public BValue[] execute(Context ctx) {
        FunctionRefCPEntry onTriggerFunctionRefCPEntry;
        FunctionRefCPEntry onErrorFunctionRefCPEntry = null;
        if (ctx.getControlStack().getCurrentFrame().getRefRegs()[0] != null &&
                ctx.getControlStack().getCurrentFrame().getRefRegs()[0] instanceof BFunctionPointer) {
            onTriggerFunctionRefCPEntry = ((BFunctionPointer) getRefArgument(ctx, 0)).value();
        } else {
            return getBValues(new BString(""),
                    BLangVMErrors.createError(ctx, 0, "The onTrigger function is not provided"));
        }
        if (ctx.getControlStack().getCurrentFrame().getRefRegs()[1] != null && ctx.getControlStack()
                .getCurrentFrame().getRefRegs()[1] instanceof BFunctionPointer) {
            onErrorFunctionRefCPEntry = ((BFunctionPointer) getRefArgument(ctx, 1)).value();
        }
        String schedule = getStringArgument(ctx, 0);

        try {
            Appointment appointment =
                    new Appointment(this, ctx, schedule, onTriggerFunctionRefCPEntry, onErrorFunctionRefCPEntry);
            return getBValues(new BString(appointment.getId()), null);
        } catch (SchedulingException e) {
            return getBValues(new BString(""), BLangVMErrors.createError(ctx, 0, e.getMessage()));
        }
    }
}
