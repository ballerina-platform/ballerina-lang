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
package org.ballerinalang.connector.api;

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.observability.ObserverContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * {@code Executor} Is the entry point from server connector side to ballerina side. After doing the dispatching and
 * finding the resource, server connector implementations can use this API to invoke Ballerina engine.
 *
 * @since 0.94
 */
public class Executor {

    /**
     * This method will execute Ballerina resource in non-blocking manner. It will use Ballerina worker-pool for the
     * execution and will return the connector thread immediately.
     *
     * @param resource         to be executed.
     * @param responseCallback to be executed when execution completes.
     * @param properties       to be passed to context.
     * @param observerContext  for the resource invocation.
     * @param values           required for the resource.
     */
    public static void submit(Resource resource, CallableUnitCallback responseCallback, Map<String, Object> properties,
                              ObserverContext observerContext, BValue... values)
            throws BallerinaConnectorException {
        if (resource == null || responseCallback == null) {
            throw new BallerinaConnectorException("invalid arguments provided");
        }
        List<BValue> args = new ArrayList<>();
        args.add(resource.getService().getBValue());
        args.addAll(Arrays.asList(values));
        FunctionInfo resourceInfo = resource.getResourceInfo();
        BVMExecutor.executeResource(resourceInfo.getPackageInfo().getProgramFile(),
                                    resourceInfo, responseCallback, properties, observerContext,
                                    resource.getService().getServiceInfo(), args.toArray(new BValue[0]));
    }

    /**
     * This method will execute Ballerina resource in non-blocking manner. It will use Ballerina worker-pool for the
     * execution and will return the connector thread immediately.
     *
     * @param resource         to be executed.
     * @param responseCallback to be executed when execution completes.
     * @param properties       to be passed to context.
     * @param observerContext  for the resource invocation.
     * @param values           required for the resource.
     */
    public static void execute(Resource resource, CallableUnitCallback responseCallback, Map<String, Object> properties,
                               ObserverContext observerContext, BValue... values)
            throws BallerinaConnectorException {
        if (resource == null || responseCallback == null) {
            throw new BallerinaConnectorException("invalid arguments provided");
        }
        List<BValue> args = new ArrayList<>();
        args.add(resource.getService().getBValue());
        args.addAll(Arrays.asList(values));
        FunctionInfo resourceInfo = resource.getResourceInfo();
        BVMExecutor.execute(resourceInfo.getPackageInfo().getProgramFile(),
                            resourceInfo, responseCallback, properties, observerContext,
                            resource.getService().getServiceInfo(), args.toArray(new BValue[0]));
    }

    private Executor() {
    }
}
