/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.task.appointment;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import static org.ballerinalang.stdlib.task.TaskConstants.APPOINTMENT_CRON_EXPR_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.APPOINTMENT_IS_RUNNING_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.APPOINTMENT_ON_ERROR_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.APPOINTMENT_ON_TRIGGER_FIELD;
import static org.ballerinalang.stdlib.task.TaskConstants.APPOINTMENT_TASK_ID_FIELD;

/**
 * Extern function ballerina/task:Appointment.schedule.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "task",
        functionName = "schedule",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Appointment", structPackage = "ballerina/task"),
        isPublic = true
)
public class Schedule extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        BMap<String, BValue> task = (BMap<String, BValue>) ctx.getRefArgument(0);
        boolean isRunning = ((BBoolean) task.get(APPOINTMENT_IS_RUNNING_FIELD)).booleanValue();
        if (isRunning) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.TASK_ALREADY_RUNNING);
        }

        FunctionInfo onTriggerFunctionRef =
                ((BFunctionPointer) task.get(APPOINTMENT_ON_TRIGGER_FIELD)).value();
        BValue onErrorFunc = task.get(APPOINTMENT_ON_ERROR_FIELD);
        FunctionInfo onErrorFunctionRef =
                onErrorFunc != null ? ((BFunctionPointer) onErrorFunc).value() : null;
        String schedule = task.get(APPOINTMENT_CRON_EXPR_FIELD).stringValue();

        try {
            Appointment appointment =
                    new Appointment(this, ctx, schedule, onTriggerFunctionRef, onErrorFunctionRef);
            task.put(APPOINTMENT_TASK_ID_FIELD, new BString(appointment.getId()));
            task.put(APPOINTMENT_IS_RUNNING_FIELD, new BBoolean(true));
        } catch (SchedulingException e) {
            throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_TASK_CONFIG);
        }
    }
}
