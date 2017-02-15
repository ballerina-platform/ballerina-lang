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

package org.wso2.ballerina.core.runtime.internal;

import org.wso2.carbon.messaging.ServerConnectorErrorHandler;
import org.wso2.carbon.messaging.TransportSender;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code ServiceContextHolder} is the place where Ballerina Service Component related information is kept.
 */
public class ServiceContextHolder {

    private static ServiceContextHolder instance = new ServiceContextHolder();
    private Map<String, TransportSender> transportSenders = new HashMap<>();
    /* Protocol specific error handlers */
    private Map<String, ServerConnectorErrorHandler> errorHandlers = new HashMap<>();

    private ServiceContextHolder() {
    }

    public static ServiceContextHolder getInstance() {
        return instance;
    }

    public void addTransportSender(TransportSender transportSender) {
        transportSenders.put(transportSender.getId(), transportSender);
    }

    public void removeTransportSender(TransportSender transportSender) {
        transportSenders.remove(transportSender.getId());
    }

    public TransportSender getSender() {
        //TODO: need to write a logic to identify the correct sender
        Map.Entry<String, TransportSender> senderEntry = transportSenders.entrySet().iterator().next();
        return senderEntry.getValue();
    }

    /**
     * Register a transport specific ErrorHandler.
     *
     * @param errorHandler Error Handler
     */
    public void registerErrorHandler(ServerConnectorErrorHandler errorHandler) {
        errorHandlers.put(errorHandler.getProtocol(), errorHandler);
    }

    /**
     * Unregister an ErrorHandler.
     *
     * @param protocol protocol of the Error Handler
     */
    public void unregisterErrorHandler(String protocol) {
        errorHandlers.remove(protocol);
    }

    /**
     * Get the error handler associated with a particular protocol.
     *
     * @param protocol transport protocol
     * @return Error Handler for a given protocol
     */
    public ServerConnectorErrorHandler getErrorHandler(String protocol) {
        return errorHandlers.get(protocol);
    }

}
