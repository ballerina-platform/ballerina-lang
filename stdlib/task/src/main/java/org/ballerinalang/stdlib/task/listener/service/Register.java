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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.objects.Appointment;
import org.ballerinalang.stdlib.task.listener.objects.Task;
import org.ballerinalang.stdlib.task.listener.objects.Timer;
import org.ballerinalang.stdlib.task.listener.utils.TaskRegistry;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_DELAY;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_INTERVAL;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.FIELD_NAME_NO_OF_RUNS;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.LISTENER_CONFIGURATION_MEMBER_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.LISTENER_STRUCT_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.ORGANIZATION_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TASK_ID_FIELD;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TASK_IS_RUNNING_FIELD;
import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TIMER_CONFIGURATION_STRUCT_NAME;
import static org.ballerinalang.stdlib.task.listener.utils.Utils.createError;
import static org.ballerinalang.stdlib.task.listener.utils.Utils.getCronExpressionFromAppointmentRecord;
import static org.ballerinalang.stdlib.task.listener.utils.Utils.validateService;

/**
 * Native function to attach a service to the listener.
 */
@BallerinaFunction(
        orgName = ORGANIZATION_NAME,
        packageName = PACKAGE_NAME,
        functionName = "register",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = LISTENER_STRUCT_NAME,
                structPackage = PACKAGE_STRUCK_NAME),
        isPublic = true
)
public class Register extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        // Validate service at runtime, as compiler plugin not available.
        validateService(service);
        BMap<String, BValue> taskStruct = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> configurations = (BMap<String, BValue>) taskStruct.get(LISTENER_CONFIGURATION_MEMBER_NAME);

        String configurationTypeName = configurations.getType().getName();
        Task task;

        if (TIMER_CONFIGURATION_STRUCT_NAME.equals(configurationTypeName)) {
            long interval = ((BInteger) configurations.get(FIELD_NAME_INTERVAL)).intValue();
            long delay = ((BInteger) configurations.get(FIELD_NAME_DELAY)).intValue();

            try {
                if (Objects.nonNull(taskStruct.get(TASK_ID_FIELD))) {
                    task = TaskRegistry.getInstance().getTask(taskStruct.get(TASK_ID_FIELD).stringValue());
                    task.addService(service);
                } else {
                    if (Objects.nonNull(configurations.get(FIELD_NAME_NO_OF_RUNS))) {
                        long noOfRuns = ((BInteger) configurations.get(FIELD_NAME_NO_OF_RUNS)).intValue();
                        task = new Timer(context, delay, interval, service, noOfRuns);
                    } else {
                        task = new Timer(context, delay, interval, service);
                    }
                    taskStruct.put(TASK_ID_FIELD, new BString(task.getId()));
                    taskStruct.put(TASK_IS_RUNNING_FIELD, new BBoolean(false));
                }
            } catch (SchedulingException e) {
                context.setReturnValues(createError(context, e.getMessage()));
            }

        } else { // Record type validates at the compile time; Hence we do not need exhaustive validation.
            try {
                String cronExpression = getCronExpressionFromAppointmentRecord(configurations);
                if (Objects.nonNull(taskStruct.get(TASK_ID_FIELD))) {
                    task = TaskRegistry.getInstance().getTask(taskStruct.get(TASK_ID_FIELD).stringValue());
                    task.addService(service);
                } else {
                    if (Objects.nonNull(configurations.get(FIELD_NAME_NO_OF_RUNS))) {
                        long noOfRuns = ((BInteger) configurations.get(FIELD_NAME_NO_OF_RUNS)).intValue();
                        task = new Appointment(context, cronExpression, service, noOfRuns);
                    } else {
                        task = new Appointment(context, cronExpression, service);
                    }
                    taskStruct.put(TASK_ID_FIELD, new BString(task.getId()));
                    taskStruct.put(TASK_IS_RUNNING_FIELD, new BBoolean(false));
                }
            } catch (SchedulingException e) {
                context.setReturnValues(createError(context, e.getMessage()));
            }
        }
    }
}
