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

import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.stdlib.task.objects.ServiceInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class invokes the Ballerina onTrigger function, and if an error occurs while invoking that function, it invokes
 * the onError function.
 */
public class TaskExecutor {

    public static void executeFunction(ServiceInformation serviceInformation) {
        AttachedFunction onTriggerFunction = serviceInformation.getOnTriggerFunction();
        List<Object> onTriggerFunctionArgs = getParameterList(onTriggerFunction, serviceInformation);

        Executor.executeFunction(serviceInformation.getStrand(), serviceInformation.getService(), onTriggerFunction,
                onTriggerFunctionArgs.toArray());
    }

    private static List<Object> getParameterList(AttachedFunction function, ServiceInformation serviceInformation) {
        List<Object> functionParameters = new ArrayList<>();
        if (function.type.paramTypes.length > 0 && Objects.nonNull(serviceInformation.getAttachment())) {
            functionParameters.add(serviceInformation.getAttachment());
            functionParameters.add(Boolean.TRUE);
        }
        return functionParameters;
    }
}
