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
package org.ballerinalang.stdlib.task.listener;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.timer.Timer;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.RuntimeErrors;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_DELAY;
import static org.ballerinalang.stdlib.task.TaskConstants.FIELD_NAME_INTERVAL;
import static org.ballerinalang.stdlib.task.TaskConstants.LISTENER_CONFIGURATION_FIELD_NAME;
import static org.ballerinalang.stdlib.task.TaskConstants.LISTENER_STRUCT_NAME;
import static org.ballerinalang.stdlib.task.TaskConstants.ORGANIZATION_NAME;
import static org.ballerinalang.stdlib.task.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.TaskConstants.PACKAGE_STRUCK_NAME;
import static org.ballerinalang.stdlib.task.TaskConstants.TIMER_CONFIGURATION_STRUCT_NAME;
import static org.ballerinalang.stdlib.task.Utils.getCronExpressionFromAppointmentRecord;

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
        BMap<String, BValue> task = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> configurations = (BMap<String, BValue>) task.get(LISTENER_CONFIGURATION_FIELD_NAME);

        String configurationTypeName = configurations.getType().getName();

        if (TIMER_CONFIGURATION_STRUCT_NAME.equals(configurationTypeName)) {
            long interval = ((BInteger) configurations.get(FIELD_NAME_INTERVAL)).intValue();
            long delay = interval;

            if (Objects.nonNull(configurations.get(FIELD_NAME_DELAY))) {
                delay = ((BInteger) configurations.get(FIELD_NAME_DELAY)).intValue();
            }
            try {
                Timer timer = new Timer(context, delay, interval, service.getName());
            } catch (SchedulingException e) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_TASK_CONFIG);
            }

        } else { // Record type validates at the compile time; Hence we do not need exhaustive validation.
            String cronExpression = getCronExpressionFromAppointmentRecord(configurations);
        }
    }
}
