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
package org.wso2.ballerina.core.nativeimpl.connectors;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.dispatching.ResourceDispatcher;
import org.wso2.ballerina.core.runtime.dispatching.ServiceDispatcher;
import org.wso2.ballerina.core.runtime.registry.DispatcherRegistry;
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
 * {@code BallerinaConnectorManager} is responsible for managing all the server connectors with ballerina runtime.
 */
public class BallerinaConnectorManager {

    private Map<String, ServerConnector> serverConnectors = new HashMap<>();

    private Map<String, ServerConnectorProvider> serverConnectorProviders = new HashMap<>();

    private Map<String, ServerConnectorErrorHandler> serverConnectorErrorHandlers = new HashMap<>();

    private static BallerinaConnectorManager instance = new BallerinaConnectorManager();

    private MessageProcessor messageProcessor;

    private boolean connectorsInitialized = false;

    private BallerinaConnectorManager() {
    }

    public static BallerinaConnectorManager getInstance() {
        return instance;
    }

    private void registerServerConnector(ServerConnector serverConnector) {
        if (serverConnectors.get(serverConnector.getId()) != null) {
            throw new BallerinaException("A server connector with id : '" + serverConnector.getId() + "' " +
                    "is already registered");
        }
        serverConnectors.put(serverConnector.getId(), serverConnector);
    }

    private void initializeConnectors() {
        for (ServerConnector connector : serverConnectors.values()) {
            try {
                connector.initConnector();
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Error while starting the connector with id : " + connector.getId(), e);
            }
        }
    }

    public ServerConnector getServerConnector(String id) {
        return serverConnectors.get(id);
    }

    public ServerConnector createServerConnector(String protocol, String id) {
        return getServerConnectorProvider(protocol)
                .map(serverConnectorProvider -> {
                    ServerConnector serverConnector = serverConnectorProvider.createConnector(id);
                    serverConnector.setMessageProcessor(messageProcessor);
                    registerServerConnector(serverConnector);
                    return serverConnector;
                })
                .orElseThrow(() -> new BallerinaException("Cannot create a new server connector instance with " +
                        "the given id '" + id + "'"));
    }

    private void registerServerConnectorProvider(ServerConnectorProvider serverConnectorProvider) {
        if (serverConnectorProviders.get(serverConnectorProvider.getProtocol()) != null) {
            throw new BallerinaException("A server connector provider for : '" + serverConnectorProvider.getProtocol() +
                    "' is already registered");
        }
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

    public void initializeServerConnectors(MessageProcessor messageProcessor) {

        if (connectorsInitialized) {
            return;
        }

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

        //3. Loading service and resource dispatchers related to transports
        loadDispatchers();

        //4. Initialize all the connectors
        initializeConnectors();

        connectorsInitialized = true;
    }

    private void loadDispatchers() {
        ServiceLoader<ResourceDispatcher> resourceDispatcherServiceLoader =
                ServiceLoader.load(ResourceDispatcher.class);
        resourceDispatcherServiceLoader.forEach(resourceDispatcher ->
                DispatcherRegistry.getInstance().registerResourceDispatcher(resourceDispatcher));

        ServiceLoader<ServiceDispatcher> serviceDispatcherServiceLoader = ServiceLoader.load(ServiceDispatcher.class);
        serviceDispatcherServiceLoader.forEach(serviceDispatcher ->
                DispatcherRegistry.getInstance().registerServiceDispatcher(serviceDispatcher));
    }

}
