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

import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.AttachedFunctionType;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;

import static io.ballerina.runtime.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_NAME;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.PACKAGE_VERSION;
import static org.ballerinalang.stdlib.task.utils.TaskConstants.RESOURCE_ON_TRIGGER;

/**
 * This class invokes the Ballerina onTrigger function, and if an error occurs while invoking that function, it invokes
 * the onError function.
 */
public class TaskExecutor {

    private static final StrandMetadata TASK_METADATA =
            new StrandMetadata(BALLERINA_BUILTIN_PKG_PREFIX, PACKAGE_NAME, PACKAGE_VERSION, RESOURCE_ON_TRIGGER);

    public static void executeFunction(ServiceInformation serviceInformation) {
        AttachedFunctionType onTriggerFunction = serviceInformation.getOnTriggerFunction();
        Object[] onTriggerFunctionArgs = getParameterList(onTriggerFunction, serviceInformation);

        Runtime runtime = serviceInformation.getRuntime();
        runtime.invokeMethodAsync(serviceInformation.getService(), RESOURCE_ON_TRIGGER, null, TASK_METADATA, null,
                                  onTriggerFunctionArgs);
    }

    private static Object[] getParameterList(AttachedFunctionType function, ServiceInformation serviceInformation) {
        Object[] attachments = serviceInformation.getAttachment();
        int numberOfParameters = function.getType().getParameterTypes().length;
        Object[] parameters = null;
        if (numberOfParameters == attachments.length) {
            int i = 0;
            parameters = new Object[attachments.length * 2];
            for (Object attachment : attachments) {
                parameters[i++] = attachment;
                parameters[i++] = Boolean.TRUE;
            }
        }
        return parameters;
    }
}
