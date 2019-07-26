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
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;
import org.ballerinalang.stdlib.task.objects.Task;
import org.ballerinalang.stdlib.task.utils.Utils;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.OBJECT_NAME_LISTENER;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.ORGANIZATION_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PARAMETER_ATTACHMENT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.TASK_PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.utils.Utils.validateService;

/**
 * Native function to attach a service to the listener.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = ORGANIZATION_NAME,
        packageName = PACKAGE_NAME,
        functionName = "register",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = OBJECT_NAME_LISTENER,
                structPackage = TASK_PACKAGE_NAME),
        isPublic = true
)
public class Register {

    public static Object register(Strand strand, ObjectValue taskListener, ObjectValue service,
                                  MapValue<String, Object> config) {
        Object attachments = config.get(PARAMETER_ATTACHMENT);
        ServiceInformation serviceInformation;
        if (Objects.nonNull(attachments)) {
            serviceInformation = new ServiceInformation(strand, service, attachments);
        } else {
            serviceInformation = new ServiceInformation(strand, service);
        }

        /* 
         * TODO: After #14148 fixed, use compiler plugin to validate the service
         */
        try {
            validateService(serviceInformation);
        } catch (BLangRuntimeException e) {
            return Utils.createTaskError(e.getMessage());
        }

        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        task.addService(serviceInformation);
        return null;
    }
}
