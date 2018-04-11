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

import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.ActiveStates;
import org.ballerinalang.persistence.State;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;
import org.w3c.dom.html.HTMLParagraphElement;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.List;
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
        if (correlate(properties, bValues)) {
            return;
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

        Object o = properties.get("instance.id");
        if (o != null && o instanceof String) {
            String instanceId = (String) o;
            ActiveStates.add(instanceId, new State(context));
        }

        ObservabilityUtils.continueServerObservation(observerContext, resource.getServiceName(), resource.getName(),
                context);
        BLangVMUtils.setServiceInfo(context, resourceInfo.getServiceInfo());
        BLangFunctions.invokeServiceCallable(resourceInfo, context, bValues, responseCallback);
    }

    private static boolean correlate(Map<String, Object> properties, BValue... bValues) {
        Object oInstanceId = properties.get("instance.id");
        if (oInstanceId == null && !(oInstanceId instanceof String)) {
            return false;
        }

        String instanceId = (String) oInstanceId;
        List<State> stateList = ActiveStates.get(instanceId);
        if (stateList != null && !stateList.isEmpty()) {
            return injectConnection(stateList.get(0), bValues);
        }

        stateList = StateStore.getInstance().getFailedStates(instanceId);
        StateStore.getInstance().removeFailedStates(instanceId);
        if (stateList != null && !stateList.isEmpty()) {
            State state = stateList.get(0);
            if (injectConnection(state, bValues)) {
                // start the context
                WorkerExecutionContext failedContext = state.getContext();
                failedContext.ip = state.getIp() - 1;
                failedContext.runInCaller = false;
                BLangScheduler.schedule(failedContext);
                return true;
            }
        }
        return false;
    }

    private static boolean injectConnection(State state, BValue... bValues) {
        boolean correlated = false;
        Object transportMessage = null;
        for (BValue bValue : bValues) {
            if (bValue instanceof BStruct) {
                BStruct bArg = (BStruct) bValue;
                if (bArg.getNativeData("transport_message") != null) {
                    transportMessage = bArg.getNativeData("transport_message");
                    break;
                }
            }
        }

        if (transportMessage != null) {
            WorkerExecutionContext savedContext = state.getContext();
            WorkerExecutionContext rootContext = getRootContext(savedContext);
            BRefType<?>[] refRegs = rootContext.workerLocal.refRegs;
            for (BRefType refType : refRegs) {
                if (refType instanceof BStruct) {
                    BStruct bStruct = (BStruct) refType;
                    if (bStruct.getNativeData("transport_message") != null) {
                        bStruct.addNativeData("transport_message", transportMessage);
                        bStruct.addNativeData("message_correlated", "true");
                        correlated = true;
                    }
                    if ("ServiceEndpoint".equals(bStruct.getType().getName())) {
                        BStruct connStruct = (BStruct) bStruct.getRefField(0);
                        if (connStruct.getNativeData("transport_message") != null) {
                            connStruct.addNativeData("transport_message", transportMessage);
                            connStruct.addNativeData("message_correlated", "true");
                            if (connStruct.getNativeData("isMethodAccessed") != null) {
                                connStruct.addNativeData("isMethodAccessed", null);
                            }
                        }
                    }
                }
            }
        }
        return correlated;
    }

    private static WorkerExecutionContext getRootContext(WorkerExecutionContext context) {
        if (context == null) {
            return null;
        } else if (context.isRootContext()) {
            return context;
        } else {
            return getRootContext(context.parent);
        }
    }
}
