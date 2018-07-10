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
import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.states.RuntimeStates;
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.program.BLangFunctions;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;

import java.util.List;
import java.util.Map;

import static org.ballerinalang.runtime.Constants.IS_METHOD_ACCESSED;
import static org.ballerinalang.runtime.Constants.MESSAGE_CORRELATED;
import static org.ballerinalang.runtime.Constants.SERVICE_ENDPOINT;
import static org.ballerinalang.runtime.Constants.TRANSPORT_MESSAGE;

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
     * @param observerContext  observer context.
     * @param context          worker execution context.
     * @param bValues          for parameters.
     */
    public static void execute(Resource resource, CallableUnitCallback responseCallback,
                               Map<String, Object> properties, ObserverContext observerContext,
                               WorkerExecutionContext context, BValue... bValues) throws BallerinaConnectorException {
        if (resource == null || responseCallback == null) {
            throw new BallerinaConnectorException("invalid arguments provided");
        }
        ResourceInfo resourceInfo = resource.getResourceInfo();
        if (properties != null) {
            Object o = properties.get(Constants.INSTANCE_ID);
            if (o != null) {
                String instanceId = o.toString();
                if (correlate(instanceId, bValues)) {
                    return;
                }
                RuntimeStates.add(new State(context, instanceId));
                context.interruptible = true;
            }
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

    private static boolean correlate(String instanceId , BValue... bValues) {
        List<State> stateList = RuntimeStates.get(instanceId);
        if (stateList != null && !stateList.isEmpty()) {
            State state = stateList.get(0);
            boolean isConnected = injectConnection(state, bValues);
            if (isConnected && state.getStatus().equals(State.Status.FAILED)) {
                // start the context
                WorkerExecutionContext failedContext = state.getContext();
                failedContext.ip = state.getIp() - 1;
                failedContext.runInCaller = false;
                WorkerResponseContext respCtx = failedContext.respCtx;
                if (respCtx instanceof CallableWorkerResponseContext) {
                    ((CallableWorkerResponseContext) respCtx).setNotFulfilled();
                }
                state.setStatus(State.Status.ACTIVE);
                BLangScheduler.schedule(failedContext);
            }
            return isConnected;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean injectConnection(State state, BValue... bValues) {
        boolean correlated = false;
        Object transportMessage = null;
        for (BValue bValue : bValues) {
            if (bValue instanceof BMap) {
                BMap bArg = (BMap) bValue;
                if (bArg.getNativeData(TRANSPORT_MESSAGE) != null) {
                    transportMessage = bArg.getNativeData(TRANSPORT_MESSAGE);
                    break;
                }
            }
        }
        if (transportMessage != null) {
            WorkerExecutionContext savedContext = state.getContext();
            WorkerExecutionContext rootContext = getRootContext(savedContext);
            BRefType<?>[] refRegs = rootContext.workerLocal.refRegs;
            for (BRefType refType : refRegs) {
                if (refType instanceof BMap) {
                    BMap bMap = (BMap) refType;
                    if (bMap.getNativeData().containsKey(TRANSPORT_MESSAGE)) {
                        bMap.addNativeData(TRANSPORT_MESSAGE, transportMessage);
                        bMap.addNativeData(MESSAGE_CORRELATED, true);
                        correlated = true;
                    }
                    if (SERVICE_ENDPOINT.equals(bMap.getType().getName())) {
                        BMap connection = (BMap) bMap.get(Constants.CONNECTION);
                        if (connection.getNativeData().containsKey(TRANSPORT_MESSAGE)) {
                            connection.addNativeData(TRANSPORT_MESSAGE, transportMessage);
                            connection.addNativeData(MESSAGE_CORRELATED, true);
                            if (connection.getNativeData(IS_METHOD_ACCESSED) != null) {
                                connection.addNativeData(IS_METHOD_ACCESSED, null);
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
