/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.runtime.threadpool;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.nonblocking.BLangExecutionVisitor;
import org.ballerinalang.model.Node;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Worker Thread which is responsible for response processing.
 */
public class ResponseWorkerThread extends WorkerThread {

    public ResponseWorkerThread(CarbonMessage cMsg, CarbonCallback callback) {
        super(cMsg, callback);
    }

    public void run() {
        // TODO : Fix this properly.
        // Connector callback's done method is called from different locations, i.e: MessageProcessor, from Netty etc.
        // Because of this we have to start new thread from the callback, if non-blocking is enabled.
        BalConnectorCallback connectorCallback = (BalConnectorCallback) this.callback;
        BLangExecutionVisitor executor = connectorCallback.getContext().getExecutor();
        try {
            try {
                connectorCallback.getActionNode().getCallableUnit().validate(connectorCallback);
                executor.startExecution(connectorCallback.getCurrentNode().next());
            } catch (RuntimeException e) {
                BException bException;
                if (e instanceof BallerinaException && ((BallerinaException) e).getBException() != null) {
                    bException = ((BallerinaException) e).getBException();
                } else {
                    bException = new BException(e.getMessage());
                }
                executor.handleBException(bException);
                executor.continueExecution();
            }
        } catch (Throwable unhandled) {
            // Root level Error handler. we have to notify server connector.
            Context bContext = connectorCallback.getContext();
            Node lastActive = executor.getLastActiveNode();
            if (executor.isResourceInvocation()) {
                ErrorHandlerUtils.handleResourceInvocationError(bContext, lastActive, null, unhandled);
            } else if (executor.isTestFunctionInvocation()) {
                ErrorHandlerUtils.handleTestFuncInvocationError(bContext, lastActive, null, unhandled);
                throw unhandled;
            } else {
                ErrorHandlerUtils.handleMainFuncInvocationError(bContext, lastActive, null, unhandled);
            }
        }
    }
}
