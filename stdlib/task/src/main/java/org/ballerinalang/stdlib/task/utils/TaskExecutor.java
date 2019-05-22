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

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.task.objects.ServiceWithParameters;
import org.ballerinalang.util.codegen.FunctionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class invokes the Ballerina onTrigger function, and if an error occurs while invoking that function, it invokes
 * the onError function.
 */
public class TaskExecutor {

    public static void execute(ServiceWithParameters serviceWithParameters) {
        // Get resource functions from service
        ResourceFunctionHolder resourceFunctionHolder = new ResourceFunctionHolder(serviceWithParameters.getService());
        FunctionInfo onTriggerFunction = resourceFunctionHolder.getOnTriggerFunction();

        List<BValue> onTriggerFunctionArgs = getParameterList(onTriggerFunction, serviceWithParameters);
        BVMExecutor.executeFunction(
                onTriggerFunction.getPackageInfo().getProgramFile(),
                onTriggerFunction,
                onTriggerFunctionArgs.toArray(new BValue[0]));
    }

    private static List<BValue> getParameterList(FunctionInfo function, ServiceWithParameters serviceWithParameters) {
        List<BValue> functionParameters = new ArrayList<>();
        functionParameters.add(serviceWithParameters.getService().getBValue());
        if (function.getParamTypes().length > 1 && Objects.nonNull(serviceWithParameters.getAttachment())) {
            functionParameters.add(serviceWithParameters.getAttachment());
        }
        return functionParameters;
    }

    //TODO test the logic here
    public static void executeFunction(ServiceWithParameters serviceWithParameters) {
        // Get resource functions from service
        ResourceFunctionHolder resourceFunctionHolder = new ResourceFunctionHolder(
                serviceWithParameters.getServiceObj());
        AttachedFunction onTriggerFunction = resourceFunctionHolder.getOnTriggerResource();

        List<Object> onTriggerFunctionArgs = getParameterList(onTriggerFunction, serviceWithParameters);
        Executor.executeFunction(serviceWithParameters.getStrand(), serviceWithParameters.getServiceObj(),
                                 onTriggerFunction, onTriggerFunctionArgs);
    }

    private static List<Object> getParameterList(AttachedFunction function,
                                                 ServiceWithParameters serviceWithParameters) {
        List<Object> functionParameters = new ArrayList<>();
        //TODO why we need service value here?
        functionParameters.add(serviceWithParameters.getServiceObj());
        if (function.getParameterType().length > 1 && Objects.nonNull(serviceWithParameters.getAttachmentObj())) {
            functionParameters.add(serviceWithParameters.getAttachmentObj());
        }
        return functionParameters;
    }
}
