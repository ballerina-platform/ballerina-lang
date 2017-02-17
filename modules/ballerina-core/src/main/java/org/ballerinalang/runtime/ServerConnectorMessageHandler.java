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

package org.ballerinalang.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.DefaultServerConnectorErrorHandler;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

import java.io.PrintStream;
import java.util.Optional;

/**
 * {@code ServerConnectorMessageHandler} is responsible for bridging Ballerina Program and External Server Connector.
 *
 * @since 0.8.0
 */
public class ServerConnectorMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectorMessageHandler.class);

    private static PrintStream outStream = System.err;

    public static void handleInbound(CarbonMessage cMsg, CarbonCallback callback) {
        // Create the Ballerina Context
        Context balContext = new Context(cMsg);
        balContext.setServerConnectorProtocol(cMsg.getProperty("PROTOCOL"));
        try {
            String protocol = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
            if (protocol == null) {
                throw new BallerinaException("protocol not defined in the incoming request", balContext);
            }

            // Find the Service Dispatcher
            ServiceDispatcher dispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(protocol);
            if (dispatcher == null) {
                throw new BallerinaException("no service dispatcher available to handle protocol : " + protocol,
                        balContext);
            }

            // Find the Service
            Service service = dispatcher.findService(cMsg, callback, balContext);
            if (service == null) {
                throw new BallerinaException("no Service found to handle the service request", balContext);
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource Dispatcher
            ResourceDispatcher resourceDispatcher = DispatcherRegistry.getInstance().getResourceDispatcher(protocol);
            if (resourceDispatcher == null) {
                throw new BallerinaException("no resource dispatcher available to handle protocol : " + protocol,
                        balContext);
            }

            // Find the Resource
            Resource resource = null;
            try {
                resource = resourceDispatcher.findResource(service, cMsg, callback, balContext);
            } catch (BallerinaException ex) {
                throw new BallerinaException("no resource found to handle the request to Service : " +
                        service.getSymbolName().getName() + " : " + ex.getMessage());
            }
            if (resource == null) {
                throw new BallerinaException("no resource found to handle the request to Service : " +
                        service.getSymbolName().getName());
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Delegate the execution to the BalProgram Executor
            BalProgramExecutor.execute(cMsg, callback, resource, service, balContext);

        } catch (Throwable throwable) {
            handleErrorInboundPath(cMsg, callback, balContext, throwable);
        }
    }

    public static void handleOutbound(CarbonMessage cMsg, CarbonCallback callback) {
//        BalConnectorCallback connectorCallback = (BalConnectorCallback) callback;
//        try {
            callback.done(cMsg);
//            if (connectorCallback.isNonBlockingExecutor()) {
//                // Continue Non-Blocking
//                BLangExecutionVisitor executor = connectorCallback.getContext().getExecutor();
//                executor.continueExecution(connectorCallback.getCurrentNode().next());
//            }
//        } catch (Throwable throwable) {
//            handleErrorFromOutbound(cMsg, connectorCallback.getContext(), throwable);
//        }
    }

    public static void handleErrorInboundPath(CarbonMessage cMsg, CarbonCallback callback, Context balContext,
                                              Throwable throwable) {
        String errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
        String stacktrace = ErrorHandlerUtils.getServiceStackTrace(balContext, throwable);
        String errorWithTrace = errorMsg + "\n" + stacktrace;
        log.error(errorWithTrace);
        outStream.println(errorWithTrace);

        Object protocol = cMsg.getProperty("PROTOCOL");
        Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);

        try {
            optionalErrorHandler
                    .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                    .handleError(new BallerinaException(errorMsg, throwable.getCause(), balContext), cMsg, callback);
        } catch (Exception e) {
            throw new BallerinaException("Cannot handle error using the error handler for : " + protocol, e);
        }

    }

    public static void handleErrorFromOutbound(Context balContext, Throwable throwable) {
        String errorMsg = ErrorHandlerUtils.getErrorMessage(throwable);
        String stacktrace = ErrorHandlerUtils.getServiceStackTrace(balContext, throwable);
        String errorWithTrace = errorMsg + "\n" + stacktrace;
        log.error(errorWithTrace);
        outStream.println(errorWithTrace);

        Object protocol = balContext.getServerConnectorProtocol();
        Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);
        try {
            optionalErrorHandler
                    .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                    .handleError(new BallerinaException(errorMsg, throwable.getCause(), balContext), null,
                            balContext.getBalCallback());
        } catch (Exception e) {
            throw new BallerinaException("Cannot handle error using the error handler for : " + protocol, e);
        }
    }

}
