/*
 *  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.task.listener.utils;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.task.listener.objects.ServiceWithParameters;
import org.ballerinalang.util.codegen.FunctionInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.stdlib.task.listener.utils.TaskConstants.TASK_IS_PAUSED_FIELD;

/**
 * This class invokes the Ballerina onTrigger function, and if an error occurs while invoking that function, it invokes
 * the onError function.
 */
@SuppressWarnings("Duplicates")
public class TaskExecutor {

    public static void execute(Context context, ServiceWithParameters serviceWithParameters) {
        boolean isErrorFnCalled = false;
        BMap<String, BValue> task = (BMap<String, BValue>) context.getRefArgument(0);
        boolean isPaused = ((BBoolean) task.get(TASK_IS_PAUSED_FIELD)).booleanValue();

        if (isPaused) {
            return;
        }

        // Get resource functions from service
        ResourceFunctionHolder resourceFunctionHolder = new ResourceFunctionHolder(serviceWithParameters.getService());
        FunctionInfo onTriggerFunction = resourceFunctionHolder.getOnTriggerFunction();
        FunctionInfo onErrorFunction = resourceFunctionHolder.getOnErrorFunction();

        try {
            List<BValue> onTriggerFunctionArgs = new ArrayList<>();
            onTriggerFunctionArgs.add(serviceWithParameters.getService().getBValue());
            if (onTriggerFunction.getParamTypes().length > 1) {
                onTriggerFunctionArgs.add(serviceWithParameters.getServiceParameter());
            }

            // Invoke the onTrigger function.
            BValue[] results = BVMExecutor.executeFunction(onTriggerFunction.getPackageInfo().getProgramFile(),
                    onTriggerFunction, onTriggerFunctionArgs.toArray(new BValue[0]));

            // If there are results, that mean an error has been returned
            if (onErrorFunction != null && results.length > 0 && results[0] != null) {
                isErrorFnCalled = true;
                List<BValue> onErrorFunctionArgs = new ArrayList<>();
                // We have to pass the service BValue as a function parameter, as it is required.
                onErrorFunctionArgs.add(serviceWithParameters.getService().getBValue());
                onErrorFunctionArgs.addAll(Arrays.asList(results));
                if (onErrorFunction.getParamTypes().length > 2) {
                    onErrorFunctionArgs.add(serviceWithParameters.getServiceParameter());
                }
                BVMExecutor.executeFunction(onErrorFunction.getPackageInfo().getProgramFile(), onErrorFunction,
                        onErrorFunctionArgs.toArray(new BValue[0]));
            }
        } catch (RuntimeException e) {
            //Call the onError function in case of error.
            if (onErrorFunction != null && !isErrorFnCalled) {
                BVMExecutor.executeFunction(onErrorFunction.getPackageInfo().getProgramFile(), onErrorFunction,
                        BLangVMErrors.createError(context, e.getMessage()));
            }
        }
    }
}
