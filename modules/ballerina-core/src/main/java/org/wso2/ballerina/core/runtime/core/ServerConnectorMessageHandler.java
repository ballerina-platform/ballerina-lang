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

package org.wso2.ballerina.core.runtime.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.ResourceInvoker;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.core.dispatching.ResourceDispatcher;
import org.wso2.ballerina.core.runtime.core.dispatching.ServiceDispatcher;
import org.wso2.ballerina.core.runtime.errors.handler.DefaultErrorHandler;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandler;
import org.wso2.ballerina.core.runtime.internal.ServiceContextHolder;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;

/**
 * {@code ServerConnectorMessageHandler} is responsible for bridging Ballerina Program and External Server Connector
 */
public class ServerConnectorMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerConnectorMessageHandler.class);


    public static void handleInbound(Context context, BalCallback callback) {
        try {
            String protocol = (String) context.getProperty(Constants.PROTOCOL);

            // Find the Service Dispatcher
            ServiceDispatcher dispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(protocol);
            if (dispatcher == null) {
                throw new BallerinaException("No service dispatcher available to handle protocol : " + protocol);
            }

            // Find the Service
            Service service = dispatcher.findService(context, callback);
            if (service == null) {
                throw new BallerinaException("No Service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource Dispatcher
            ResourceDispatcher resourceDispatcher = DispatcherRegistry.getInstance().getResourceDispatcher(protocol);
            if (resourceDispatcher == null) {
                throw new BallerinaException("No resource dispatcher available to handle protocol : " + protocol);
            }

            // Find the Resource
            Resource resource = resourceDispatcher.findResource(service, context, callback);
            if (resource == null) {
                throw new BallerinaException("No Resource found to handle the request to Service : " +
                                             service.getSymbolName().getName());
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Create the interpreter and Execute
            BLangInterpreter interpreter = new BLangInterpreter(context);
            context.setBalCallback(callback);
            new ResourceInvoker(resource).accept(interpreter);

        } catch (Throwable throwable) {
            handleError(context, callback, throwable);
        }
    }

    public static void handleOutbound(Context context, BalCallback callback) {
        try {
            callback.done(context.getCarbonMessage());
        } catch (Throwable throwable) {
            handleError(context, callback, throwable);
        }
    }

    private static void handleError(Context context, BalCallback callback, Throwable throwable) {
        log.error("Error while executing ballerina program. " + throwable.getMessage());

        ErrorHandler errorHandler;
        Object protocol = context.getProperty(Constants.PROTOCOL);
        if (protocol != null) {
            errorHandler = ServiceContextHolder.getInstance().getErrorHandler((String) protocol);
        } else {
            errorHandler = DefaultErrorHandler.getInstance();
        }
        errorHandler.handleError(new Exception(throwable.getMessage(), throwable.getCause()), context, callback);
    }

}
