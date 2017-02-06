/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.serverconnector.framework;

import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;
import org.wso2.carbon.messaging.ServerConnectorProvider;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * {@code ServerConnectorManager} is responsible for managing all the server connectors with ballerina runtime.
 * For an application that uses the transport framework, this manager uses the same message processor instance
 * used with initializing.
 */
public class ServerConnectorManager {

    private Map<String, ServerConnector> serverConnectors = new HashMap<>();

    private Map<String, ServerConnectorProvider> serverConnectorProviders = new HashMap<>();

    private Map<String, ServerConnectorErrorHandler> serverConnectorErrorHandlers = new HashMap<>();

    private CarbonMessageProcessor messageProcessor;

    private void registerServerConnector(ServerConnector serverConnector) {
        serverConnectors.put(serverConnector.getId(), serverConnector);
    }

    private void initializeConnectors() throws ServerConnectorException {
        for (ServerConnector connector : serverConnectors.values()) {
            connector.initConnector();
        }
    }

    public ServerConnector getServerConnector(String id) {
        return serverConnectors.get(id);
    }

    public ServerConnector createServerConnector(String protocol, String id) throws ServerConnectorException {
        Optional<ServerConnectorProvider> serverConnectorProviderOptional = getServerConnectorProvider(protocol);

        if (!serverConnectorProviderOptional.isPresent()) {
            throw new ServerConnectorException("Cannot create a new server connector as there are no connector " +
                    "provider available for protocol : " + protocol);
        }

        ServerConnector serverConnector = serverConnectorProviderOptional.get().createConnector(id);
        serverConnector.setMessageProcessor(messageProcessor);
        registerServerConnector(serverConnector);
        return serverConnector;
    }

    private void registerServerConnectorProvider(ServerConnectorProvider serverConnectorProvider) {
        serverConnectorProviders.put(serverConnectorProvider.getProtocol(), serverConnectorProvider);
    }

    private Optional<ServerConnectorProvider> getServerConnectorProvider(String protocol) {
        return Optional.ofNullable(serverConnectorProviders.get(protocol));
    }

    public void registerServerConnectorErrorHandler(ServerConnectorErrorHandler serverConnectorErrorHandler) {
        serverConnectorErrorHandlers.put(serverConnectorErrorHandler.getProtocol(), serverConnectorErrorHandler);
    }

    public Optional<ServerConnectorErrorHandler> getServerConnectorErrorHandler(String protocol) {
        return Optional.ofNullable(serverConnectorErrorHandlers.get(protocol));
    }

    public void initializeServerConnectors(CarbonMessageProcessor messageProcessor) throws ServerConnectorException {
        this.messageProcessor = messageProcessor;

        //1. Loading server connector providers
        ServiceLoader<ServerConnectorProvider> serverConnectorProviderLoader =
                ServiceLoader.load(ServerConnectorProvider.class);
        serverConnectorProviderLoader.
                forEach(serverConnectorProvider -> {
                    this.registerServerConnectorProvider(serverConnectorProvider);
                    List<ServerConnector> serverConnectors = serverConnectorProvider.initializeConnectors();
                    if (serverConnectors == null || serverConnectors.isEmpty()) {
                        return;
                    }
                    serverConnectors.forEach(serverConnector -> {
                        serverConnector.setMessageProcessor(messageProcessor);
                        this.registerServerConnector(serverConnector);
                    });
                });

        //2. Loading transport listener error handlers
        ServiceLoader<ServerConnectorErrorHandler> errorHandlerLoader =
                ServiceLoader.load(ServerConnectorErrorHandler.class);
        errorHandlerLoader.forEach(this::registerServerConnectorErrorHandler);

        //3. Initialize all the connectors
        initializeConnectors();
    }
}
