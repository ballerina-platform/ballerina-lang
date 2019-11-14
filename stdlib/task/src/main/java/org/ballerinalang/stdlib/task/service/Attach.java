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

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.util.exceptions.BLangRuntimeException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.stdlib.task.exceptions.SchedulingException;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;
import org.ballerinalang.stdlib.task.objects.Task;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.NATIVE_DATA_TASK_OBJECT;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PARAMETER_ATTACHMENT;
import static org.ballerinalang.stdlib.task.utils.Utils.validateService;

/**
 * Native function to attach a service to the listener.
 *
 * @since 0.995.0
 */
public class Attach {

    public static Object attach(ObjectValue taskListener, ObjectValue service, MapValue<String, Object> config) {
        Object attachments = config.get(PARAMETER_ATTACHMENT);
        ServiceInformation serviceInformation;
        if (Objects.nonNull(attachments)) {
            serviceInformation = new ServiceInformation(BRuntime.getCurrentRuntime(), service, attachments);
        } else {
            serviceInformation = new ServiceInformation(BRuntime.getCurrentRuntime(), service);
        }

        /*
         * TODO: After #14148 fixed, use compiler plugin to validate the service
         */
        try {
            validateService(serviceInformation);
        } catch (SchedulingException e) {
            //TODO: Ideally this should return an Error using createError() method.
            // Fix this once we can return errors from interop functions (Check with master)
            throw new BLangRuntimeException(e.getMessage());
        }

        Task task = (Task) taskListener.getNativeData(NATIVE_DATA_TASK_OBJECT);
        task.addService(serviceInformation);
        return null;
    }
}
