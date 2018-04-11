/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.connector.impl;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;

import java.util.Map;

/**
 * {@code ResourceExecutor} This provides the implementation to execute resources within Ballerina.
 *
 * @since 0.94
 */
public class ResourceExecutor {

    /**
     * This method will execute the resource, given required details.
     * And it will use the callback to notify interested parties about the
     * outcome of the execution.
     *
     * @param resource         to be executed.
     * @param responseCallback to notify.
     * @param properties       to be passed to context.
     * @param observerContext  to be passed to context.
     * @param bValues          for parameters.
     */
    public static void execute(Resource resource, CallableUnitCallback responseCallback,
                               Map<String, Object> properties, ObserverContext observerContext,
                               BValue... bValues) throws BallerinaConnectorException {
        if (resource == null || responseCallback == null) {
            throw new BallerinaConnectorException("invalid arguments provided");
        }
        ResourceInfo resourceInfo = resource.getResourceInfo();
        WorkerExecutionContext context = new WorkerExecutionContext(resourceInfo.getPackageInfo().getProgramFile());
        if (properties != null) {
            context.globalProps.putAll(properties);
            if (properties.get(Constants.GLOBAL_TRANSACTION_ID) != null) {
                context.setLocalTransactionInfo(new LocalTransactionInfo(
                        properties.get(Constants.GLOBAL_TRANSACTION_ID).toString(),
                        properties.get(Constants.TRANSACTION_URL).toString(), "2pc"));
            }
        }
        BLangVMUtils.setServiceInfo(context, resourceInfo.getServiceInfo());
        BLangFunctions.invokeServiceCallable(resourceInfo, context, observerContext, bValues, responseCallback);
    }
}
