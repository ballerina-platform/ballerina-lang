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
 */

package org.ballerinalang.stdlib.task.service;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.objects.Appointment;
import org.ballerinalang.stdlib.task.objects.Task;
import org.ballerinalang.stdlib.task.objects.Timer;
import org.ballerinalang.stdlib.task.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_DELAY;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_INTERVAL;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.FIELD_NO_OF_RUNS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.MEMBER_APPOINTMENT_DETAILS;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.MEMBER_LISTENER_CONFIGURATION;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.OBJECT_NAME_LISTENER;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.ORGANIZATION_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RECORD_TIMER_CONFIGURATION;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.utils.Utils.getCronExpressionFromAppointmentRecord;

/**
 * Native function to attach a service to the listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = ORGANIZATION_NAME,
        packageName = PACKAGE_NAME,
        functionName = "init",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = OBJECT_NAME_LISTENER,
                structPackage = TASK_PACKAGE_NAME),
        isPublic = true
)
public class Init {

    private static final Logger LOG = LoggerFactory.getLogger(Init.class);

    @SuppressWarnings("unchecked")
    public static Object init(Strand strand, ObjectValue taskListener) {
        MapValue<String, Object> configurations = (MapValue<String, Object>) taskListener.get(
                MEMBER_LISTENER_CONFIGURATION);
        String configurationTypeName = configurations.getType().getName();
        Task task;
        try {
            if (RECORD_TIMER_CONFIGURATION.equals(configurationTypeName)) {
                task = processTimer(configurations);
            } else { // Record type validates at the compile time; Hence we do not need exhaustive validation.
                task = processAppointment(configurations);
            }
            taskListener.addNativeData(NATIVE_DATA_TASK_OBJECT, task);
        } catch (SchedulingException e) {
            LOG.error(e.getMessage(), e);
            return Utils.createTaskError(e.getMessage());
        }
        return null;
    }

    private static Timer processTimer(MapValue<String, Object> configurations) throws SchedulingException {
        Timer task;
        long interval = ((Long) configurations.get(FIELD_INTERVAL)).intValue();
        long delay = ((Long) configurations.get(FIELD_DELAY)).intValue();

        if (Objects.nonNull(configurations.get(FIELD_NO_OF_RUNS))) {
            long noOfRuns = ((Long) configurations.get(FIELD_NO_OF_RUNS)).intValue();
            task = new Timer(delay, interval, noOfRuns);
        } else {
            task = new Timer(delay, interval);
        }
        return task;
    }

    private static Appointment processAppointment(MapValue<String, Object> configurations) throws SchedulingException {
        Appointment appointment;
        Object appointmentDetails = configurations.get(MEMBER_APPOINTMENT_DETAILS);
        String cronExpression = getCronExpressionFromAppointmentRecord(appointmentDetails);

        if (Objects.nonNull(configurations.get(FIELD_NO_OF_RUNS))) {
            long noOfRuns = ((Long) configurations.get(FIELD_NO_OF_RUNS)).intValue();
            appointment = new Appointment(cronExpression, noOfRuns);
        } else {
            appointment = new Appointment(cronExpression);
        }
        return appointment;
    }
}
