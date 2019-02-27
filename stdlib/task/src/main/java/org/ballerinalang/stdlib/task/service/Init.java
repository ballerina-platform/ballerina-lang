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
 *
 */

package org.ballerinalang.stdlib.task.service;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.objects.Appointment;
import org.ballerinalang.stdlib.task.objects.Task;
import org.ballerinalang.stdlib.task.objects.Timer;
import org.ballerinalang.stdlib.task.utils.Utils;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_DELAY;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_INTERVAL;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_NO_OF_RUNS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.MEMBER_APPOINTMENT_DETAILS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.MEMBER_LISTENER_CONFIGURATION;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.ORGANIZATION_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.REF_ARG_INDEX_TASK_STRUCT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.STRUCT_NAME_LISTENER;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.STRUCT_TIMER_CONFIGURATION;
import static org.ballerinalang.stdlib.task.utils.Utils.getCronExpressionFromAppointmentRecord;

/**
 * Native function to attach a service to the listener.
 */
@BallerinaFunction(
        orgName = ORGANIZATION_NAME,
        packageName = PACKAGE_NAME,
        functionName = "init",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = STRUCT_NAME_LISTENER,
                structPackage = PACKAGE_STRUCK_NAME),
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Context context) {
        BMap<String, BValue> taskStruct = (BMap<String, BValue>) context.getRefArgument(REF_ARG_INDEX_TASK_STRUCT);
        BMap<String, BValue> configurations = (BMap<String, BValue>) taskStruct.get(MEMBER_LISTENER_CONFIGURATION);
        String configurationTypeName = configurations.getType().getName();
        Task task;

        if (STRUCT_TIMER_CONFIGURATION.equals(configurationTypeName)) {
            task = processTimer(context, configurations);
        } else { // Record type validates at the compile time; Hence we do not need exhaustive validation.
            task = processAppointment(context, configurations);
        }
        taskStruct.addNativeData(NATIVE_DATA_TASK_OBJECT, task);
    }

    private static Timer processTimer(Context context, BMap<String, BValue> configurations) {
        Timer task;
        long interval = ((BInteger) configurations.get(FIELD_INTERVAL)).intValue();
        long delay = ((BInteger) configurations.get(FIELD_DELAY)).intValue();

        try {
            if (Objects.nonNull(configurations.get(FIELD_NO_OF_RUNS))) {
                long noOfRuns = ((BInteger) configurations.get(FIELD_NO_OF_RUNS)).intValue();
                task = new Timer(context, delay, interval, noOfRuns);
            } else {
                task = new Timer(context, delay, interval);
            }
            return task;
        } catch (SchedulingException e) {
            context.setReturnValues(Utils.createError(context, e.getMessage()));
            return null;
        }
    }

    private static Appointment processAppointment(Context context, BMap<String, BValue> configurations) {
        Appointment appointment;
        try {
            BValue appointmentDetails = configurations.get(MEMBER_APPOINTMENT_DETAILS);
            String cronExpression = getCronExpressionFromAppointmentRecord(appointmentDetails);

            if (Objects.nonNull(configurations.get(FIELD_NO_OF_RUNS))) {
                long noOfRuns = ((BInteger) configurations.get(FIELD_NO_OF_RUNS)).intValue();
                appointment = new Appointment(context, cronExpression, noOfRuns);
            } else {
                appointment = new Appointment(context, cronExpression);
            }
            return appointment;
        } catch (SchedulingException e) {
            context.setReturnValues(Utils.createError(context, e.getMessage()));
            return null;
        }
    }
}
