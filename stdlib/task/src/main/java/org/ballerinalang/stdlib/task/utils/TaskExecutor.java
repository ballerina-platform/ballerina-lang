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
package org.ballerinalang.stdlib.task.utils;

import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;

import java.util.Objects;

import static org.ballerinalang.stdlib.task.utils.TaskConstants.RESOURCE_ON_TRIGGER;

/**
 * This class invokes the Ballerina onTrigger function, and if an error occurs while invoking that function, it invokes
 * the onError function.
 */
public class TaskExecutor {

    public static void executeFunction(ServiceInformation serviceInformation) {
        AttachedFunction onTriggerFunction = serviceInformation.getOnTriggerFunction();
        Object[] onTriggerFunctionArgs = getParameterList(onTriggerFunction, serviceInformation);

        BRuntime runtime = serviceInformation.getRuntime();
        runtime.invokeMethodAsync(serviceInformation.getService(), RESOURCE_ON_TRIGGER, onTriggerFunctionArgs);
    }

    private static Object[] getParameterList(AttachedFunction function, ServiceInformation serviceInformation) {
        if (function.type.paramTypes.length > 0 && Objects.nonNull(serviceInformation.getAttachment())) {
            return new Object[]{serviceInformation.getAttachment(), Boolean.TRUE};
        }
        return new Object[]{};
    }
}
