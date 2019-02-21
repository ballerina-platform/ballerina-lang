/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.task.listener.service;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.api.TaskServerConnector;
import org.ballerinalang.stdlib.task.listener.impl.TaskServerConnectorImpl;
import org.ballerinalang.stdlib.task.listener.objects.Task;
import org.ballerinalang.stdlib.task.listener.objects.TaskState;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.ORGANIZATION_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.REF_ARG_INDEX_TASK_STRUCT;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.STRUCT_NAME_LISTENER;
import static org.ballerinalang.stdlib.task.listener.utils.Utils.createError;

/**
 * Native function to start the service attached to the listener.
 */
@BallerinaFunction(
        orgName = ORGANIZATION_NAME,
        packageName = PACKAGE_NAME,
        functionName = "start",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = STRUCT_NAME_LISTENER,
                structPackage = PACKAGE_STRUCK_NAME),
        isPublic = true
)
public class Start extends BlockingNativeCallableUnit {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context) {
        BMap<String, BValue> taskStruct = (BMap<String, BValue>) context.getRefArgument(REF_ARG_INDEX_TASK_STRUCT);
        Task task = (Task) taskStruct.getNativeData(NATIVE_DATA_TASK_OBJECT);
        if (TaskState.STOPPED != task.getState()) {
            String errorMessage = "Cannot start the task: " + taskStruct.get("name") + " Task is already running.";
            context.setReturnValues(createError(context, errorMessage));
            return;
        }
        TaskServerConnector serverConnector = new TaskServerConnectorImpl(context, task);
        try {
            serverConnector.start();
        } catch (SchedulingException e) {
            context.setReturnValues(createError(context, e.getMessage()));
        }
    }
}
