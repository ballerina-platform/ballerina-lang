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
package org.ballerinalang.nativeimpl.task.appointment;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.task.TaskRegistry;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * Native function ballerina.task:scheduleAppointment.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "task",
        functionName = "Appointment.cancel",
        args = {@Argument(name = "app", type = TypeKind.STRUCT, structType = "Appointment",
                structPackage = "ballerina.task")},
        isPublic = true
)
public class Cancel extends BlockingNativeCallableUnit {

    public void execute(Context ctx) {
        BStruct task = (BStruct) ctx.getRefArgument(0);
        String taskId = task.getStringField(0);
        TaskRegistry.getInstance().stopTask(taskId);
        task.setBooleanField(0, 0);
    }
}
