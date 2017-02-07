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
import org.wso2.carbon.messaging.ClientConnector;
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
 * {@code ServerConnectorManager} is responsible for managing all the server connectors with an application runtime.
 * For an application that uses the transport framework, this manager uses the same message processor instance
 * used with initializing.
 */
public class ConnectorManager {

    private Map<String, ServerConnector> serverConnectors = new HashMap<>();

    private Map<String, ClientConnector> clientConnectors = new HashMap<>();

    private Map<String, ServerConnectorProvider> serverConnectorProviders = new HashMap<>();

    private Map<String, ServerConnectorErrorHandler> serverConnectorErrorHandlers = new HashMap<>();

    private CarbonMessageProcessor messageProcessor;

    private void registerServerConnector(ServerConnector serverConnector) {
        serverConnectors.put(serverConnector.getId(), serverConnector);
    }

    private void registerClientConnector(ClientConnector clientConnector) {
        clientConnectors.put(clientConnector.getProtocol(), clientConnector);
    }


    private void initializeServerConnectors() throws ServerConnectorException {
        for (ServerConnector connector : serverConnectors.values()) {
            connector.initConnector();
        }
    }

    /**
     * Returns the server connector instance associated with the given protocol.
     * @param id the identifier of the server connector.
     * @return server connector instance.
     */
    public ServerConnector getServerConnector(String id) {
        return serverConnectors.get(id);
    }

    /**
     * Creates and return a server connector using the given protocol and id. The protocol is used with acquiring the
     * correct server connector provider. An error will be thrown, if there are no server connector provider registered
     * for the given protocol.
     *
     * @param protocol transport protocol used with finding the correct server connector provider.
     * @param id unique id to use when creating the server connector instance.
     * @return returns the newly created instance.
     * @throws ServerConnectorException error if there are no server connector provider found.
     */
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

    /**
     * Returns the client connector instance associated with the given protocol.
     * @param protocol of the client connector.
     * @return client connector instance.
     */
    public ClientConnector getClientConnector(String protocol) {
        return clientConnectors.get(protocol);
    }

    private void registerServerConnectorProvider(ServerConnectorProvider serverConnectorProvider) {
        serverConnectorProviders.put(serverConnectorProvider.getProtocol(), serverConnectorProvider);
    }

    private Optional<ServerConnectorProvider> getServerConnectorProvider(String protocol) {
        return Optional.ofNullable(serverConnectorProviders.get(protocol));
    }

    /**
     * Register the given server connector error handler instance with the manager. Protocol of the handler will be
     * used with registering the handler.
     * @param serverConnectorErrorHandler handler instance to register.
     */
    public void registerServerConnectorErrorHandler(ServerConnectorErrorHandler serverConnectorErrorHandler) {
        serverConnectorErrorHandlers.put(serverConnectorErrorHandler.getProtocol(), serverConnectorErrorHandler);
    }

    /**
     * Returns an {@code Optional} value of the server connector error handler registered against the given transport
     * protocol.
     * @param protocol the transport protocol associated with the error handler.
     * @return error handler instance.
     */
    public Optional<ServerConnectorErrorHandler> getServerConnectorErrorHandler(String protocol) {
        return Optional.ofNullable(serverConnectorErrorHandlers.get(protocol));
    }

    /**
     * Initialize and load all the server connector providers, default connectors from those providers, error handlers
     * using respective SPI interfaces. The given instance of the message processor will be used to initialize all the
     * default server connectors and it will be used with subsequent new connector creation as-well.
     *
     * @param messageProcessor message processor instance used with initializing the server connectors.
     * @throws ServerConnectorException error if the initialization of the connectors failed.
     */
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

        //2. Loading server connector error handlers
        ServiceLoader<ServerConnectorErrorHandler> errorHandlerLoader =
                ServiceLoader.load(ServerConnectorErrorHandler.class);
        errorHandlerLoader.forEach(this::registerServerConnectorErrorHandler);

        //3. Initialize all server connectors
        initializeServerConnectors();
    }

    public void initializeClientConnectors() {
        // Loading client connectors
        ServiceLoader<ClientConnector> clientConnectorLoader =
                ServiceLoader.load(ClientConnector.class);
        clientConnectorLoader.forEach(this::registerClientConnector);
    }
}
