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
package org.ballerinalang.natives.connectors;

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.services.dispatchers.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.services.dispatchers.ws.BallerinaWebSocketConnectorListener;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.connector.framework.ConnectorManager;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * {@code BallerinaConnectorManager} is responsible for managing all the server connectors with ballerina runtime.
 *
 * @since 0.89
 */
public class BallerinaConnectorManager {

    private ConnectorManager connectorManager = new ConnectorManager();

    private static BallerinaConnectorManager instance = new BallerinaConnectorManager();
    private boolean connectorsInitialized = false;
    private Map<String, ServerConnector> startupDelayedServerConnectors = new HashMap<>();
    private Map<String, org.wso2.carbon.transport.http.netty.contract.ServerConnector>
            startupDelayedHTTPServerConnectors = new HashMap<>();
    private CarbonMessageProcessor messageProcessor;

    private Map<String, org.wso2.carbon.transport.http.netty.contract.ServerConnector>
            startedHTTPServerConnectors = new HashMap<>();
    private Map<String, HttpServerConnectorContext>
            serverConnectorPool = new HashMap<>();
    private ServerBootstrapConfiguration serverBootstrapConfiguration;
    private TransportsConfiguration trpConfig;
    private HttpWsConnectorFactory httpConnectorFactory = new HttpWsConnectorFactoryImpl();
    private static final String HTTP_TRANSPORT_CONF = "transports.netty.conf";

    private BallerinaConnectorManager() {
        String nettyConfigFile = System.getProperty(HTTP_TRANSPORT_CONF,
                "conf" + File.separator + "transports" +
                        File.separator + "netty-transports.yml");
        trpConfig = ConfigurationBuilder.getInstance().getConfiguration(nettyConfigFile);
        serverBootstrapConfiguration = HTTPMessageUtil
                .getServerBootstrapConfiguration(trpConfig.getTransportProperties());

        if (System.getProperty(BLogManager.HTTP_TRACE_LOGGER) != null) {
            try {
                ((BLogManager) BLogManager.getLogManager()).setHttpTraceLogHandler();
            } catch (IOException e) {
                throw new BallerinaException("Error in configuring HTTP trace log");
            }
        }
    }

    public static BallerinaConnectorManager getInstance() {
        return instance;
    }

    /**
     * Returns the server connector instance associated with the given protocol.
     * 
     * @param id the identifier of the server connector.
     * @return server connector instance.
     */
    public ServerConnector getServerConnector(String id) {
        return connectorManager.getServerConnector(id);
    }

    /**
     * Returns the client connector instance associated with the given protocol.
     * 
     * @param protocol of the client connector.
     * @return client connector instance.
     */
    public ClientConnector getClientConnector(String protocol) {
        return connectorManager.getClientConnector(protocol);
    }

    /**
     * Creates and return a server connector using the given protocol and id. The protocol is used with acquiring the
     * correct server connector provider. An error will be thrown, if there are no server connector provider registered
     * for the given protocol. Throws {@code BallerinaException} error if there were any issues trying to create a new
     * connector instance.
     *
     * @param protocol transport protocol used with finding the correct server connector provider.
     * @param id unique id to use when creating the server connector instance.
     * @param parameters Required parameters to create a server connector
     * @return returns the newly created instance.
     */
    public ServerConnector createServerConnector(String protocol, String id, Map<String, String> parameters) {
        ServerConnector serverConnector;
        try {
            serverConnector = connectorManager.createServerConnector(protocol, id, parameters);
            //TODO: Look at the possibility of moving the error handler assignment code from dispatcher to here
        } catch (ServerConnectorException e) {
            throw new BallerinaException("Error occurred while creating a server connector for protocol : '" +
                    protocol + "' with the given id : '" + id + "'", e);
        }
        return serverConnector;
    }

    public Set<ListenerConfiguration> getDefaultListenerConfiugrationSet() {
        Set<ListenerConfiguration> listenerConfigurationSet = new HashSet<>();
        for (ListenerConfiguration listenerConfiguration : trpConfig.getListenerConfigurations()) {
            listenerConfiguration.setId(listenerConfiguration.getHost() == null ?
                    "0.0.0.0" : listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort());
            listenerConfigurationSet.add(listenerConfiguration);
        }
        return listenerConfigurationSet;
    }

    public org.wso2.carbon.transport.http.netty.contract.ServerConnector createHttpServerConnector(
            ListenerConfiguration listenerConfig) {
        String listenerInterface = listenerConfig.getHost() + ":" + listenerConfig.getPort();
        HttpServerConnectorContext httpServerConnectorContext =
                serverConnectorPool.get(listenerInterface);
        if (httpServerConnectorContext != null) {
            if (checkForConflicts(listenerConfig, httpServerConnectorContext)) {
                throw new BallerinaException("Conflicting configuration detected for listener configuration id "
                        + listenerConfig.getId());
            } else {
                httpServerConnectorContext.incrementReferenceCount();
                return httpServerConnectorContext.getServerConnector();
            }
        }

        if (System.getProperty(BLogManager.HTTP_TRACE_LOGGER) != null) {
            listenerConfig.setHttpTraceLogEnabled(true);
        }

        serverBootstrapConfiguration = HTTPMessageUtil
                .getServerBootstrapConfiguration(trpConfig.getTransportProperties());
        org.wso2.carbon.transport.http.netty.contract.ServerConnector serverConnector =
                httpConnectorFactory.createServerConnector(serverBootstrapConfiguration, listenerConfig);

        httpServerConnectorContext = new HttpServerConnectorContext(serverConnector, listenerConfig);
        serverConnectorPool.put(serverConnector.getConnectorID(), httpServerConnectorContext);
        httpServerConnectorContext.incrementReferenceCount();
        addStartupDelayedHTTPServerConnector(listenerInterface, serverConnector);
        return serverConnector;
    }

    /**
     * Register the given server connector error handler instance with the manager. Protocol of the handler will be
     * used with registering the handler.
     * 
     * @param serverConnectorErrorHandler handler instance to register.
     */
    public void registerServerConnectorErrorHandler(ServerConnectorErrorHandler serverConnectorErrorHandler) {
        connectorManager.registerServerConnectorErrorHandler(serverConnectorErrorHandler);
    }

    /**
     * Returns an {@code Optional} value of the server connector error handler registered against the given transport
     * protocol.
     * 
     * @param protocol the transport protocol associated with the error handler.
     * @return error handler instance.
     */
    public Optional<ServerConnectorErrorHandler> getServerConnectorErrorHandler(String protocol) {
        return connectorManager.getServerConnectorErrorHandler(protocol);
    }

    /**
     * Initialize and load all the server and client connectors. The given instance of the message processor will
     * be used to initialize all the default server connectors and it will be used with subsequent new connector
     * creation as-well.
     *
     * @param messageProcessor message processor instance used with initializing the server connectors.
     */
    public void initialize(MessageProcessor messageProcessor) {
        if (connectorsInitialized) {
            return;
        }
        //1. Loading service and resource dispatchers related to transports
        loadDispatchers();

        //2. Initialize server connectors
        try {
            connectorManager.initializeServerConnectors(messageProcessor);
        } catch (ServerConnectorException e) {
            throw new BallerinaException("Error occurred while initializing all server connectors", e);
        }

        //3. Initialize client connectors
        connectorManager.initializeClientConnectors(messageProcessor);

        connectorsInitialized = true;
    }

    /**
     * Initialize and load client connectors. The given instance of the message processor will
     * be used to initialize all the client connectors and it will be used with subsequent new connector
     * creation as-well.
     *
     * @param messageProcessor message processor instance used with initializing the server connectors.
     */
    public void initializeClientConnectors(MessageProcessor messageProcessor) {
        connectorManager.initializeClientConnectors(messageProcessor);
    }

    /**
     * Add a ServerConnector which startup is delayed at the service deployment time.
     *
     * @param serverConnector ServerConnector
     */
    public void addStartupDelayedServerConnector(ServerConnector serverConnector) {
        startupDelayedServerConnectors.put(serverConnector.getId(), serverConnector);
    }

    /**
     * Add a HTTP ServerConnector which startup is delayed at the service deployment time.
     *
     * @param serverConnector ServerConnector
     */
    public void addStartupDelayedHTTPServerConnector(String id,
            org.wso2.carbon.transport.http.netty.contract.ServerConnector serverConnector) {
        startupDelayedHTTPServerConnectors.put(id, serverConnector);
    }

    /**
     * Start all the ServerConnectors which startup is delayed at the service deployment time.
     *
     * @return the list of started server connectors.
     * @throws ServerConnectorException if exception occurs while starting at least one connector.
     */
    public List<ServerConnector> startPendingConnectors() throws ServerConnectorException {
        List<ServerConnector> startedConnectors = new ArrayList<>();
        for (Map.Entry<String, ServerConnector> serverConnectorEntry: startupDelayedServerConnectors.entrySet()) {
            ServerConnector serverConnector = serverConnectorEntry.getValue();
            serverConnector.start();
            startedConnectors.add(serverConnector);
        }
        startupDelayedServerConnectors.clear();
        return startedConnectors;
    }

    /**
     * Start all the ServerConnectors which startup is delayed at the service deployment time.
     *
     * @return the list of started server connectors.
     * @throws ServerConnectorException if exception occurs while starting at least one connector.
     */
    public List<org.wso2.carbon.transport.http.netty.contract.ServerConnector> startPendingHTTPConnectors()
            throws ServerConnectorException {
        List<org.wso2.carbon.transport.http.netty.contract.ServerConnector> startedConnectors = new ArrayList<>();
        for (Map.Entry<String, org.wso2.carbon.transport.http.netty.contract.ServerConnector>
                serverConnectorEntry: startupDelayedHTTPServerConnectors.entrySet()) {
            org.wso2.carbon.transport.http.netty.contract.ServerConnector serverConnector =
                    serverConnectorEntry.getValue();
            ServerConnectorFuture connectorFuture = serverConnector.start();
            connectorFuture.setHttpConnectorListener(new BallerinaHTTPConnectorListener());
            connectorFuture.setWSConnectorListener(new BallerinaWebSocketConnectorListener());
            startedConnectors.add(serverConnector);
            startedHTTPServerConnectors.put(serverConnector.getConnectorID(), serverConnector);
        }
        startupDelayedServerConnectors.clear();
        return startedConnectors;
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

    public void setMessageProcessor(CarbonMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    public CarbonMessageProcessor getMessageProcessor() {
        return this.messageProcessor;
    }

    public void registerClientConnector(ClientConnector clientConnector) {
        this.connectorManager.registerClientConnector(clientConnector);
    }

    public HttpClientConnector getHTTPHttpClientConnector() {
        Map<String, Object> properties = HTTPMessageUtil.getTransportProperties(trpConfig);
        SenderConfiguration senderConfiguration =
                HTTPMessageUtil.getSenderConfiguration(trpConfig);

        if (System.getProperty(BLogManager.HTTP_TRACE_LOGGER) != null) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }

        return httpConnectorFactory.createHttpClientConnector(properties, senderConfiguration);
    }

    public WebSocketClientConnector getWebSocketClientConnector(Map<String, Object> properties) {
        return  httpConnectorFactory.createWsClientConnector(properties);
    }

    private static class HttpServerConnectorContext {
        private org.wso2.carbon.transport.http.netty.contract.ServerConnector serverConnector;
        private ListenerConfiguration listenerConfiguration;
        private int referenceCount = 0;

        public HttpServerConnectorContext(org.wso2.carbon.transport.http.netty.contract.ServerConnector
                serverConnector, ListenerConfiguration listenerConfiguration) {
            this.serverConnector = serverConnector;
            this.listenerConfiguration = listenerConfiguration;
        }

        public void incrementReferenceCount() {
            this.referenceCount++;
        }

        public void decrementReferenceCount() {
            this.referenceCount--;
        }

        public org.wso2.carbon.transport.http.netty.contract.ServerConnector getServerConnector() {
            return this.serverConnector;
        }

        public ListenerConfiguration getListenerConfiguration() {
            return this.listenerConfiguration;
        }

        public int getReferenceCount() {
            return this.referenceCount;
        }
    }

    private boolean checkForConflicts(ListenerConfiguration listenerConfiguration,
            HttpServerConnectorContext context) {
        if (context != null) {
            if (listenerConfiguration.getScheme().equalsIgnoreCase("https")) {
                ListenerConfiguration config = context.getListenerConfiguration();
                if (!listenerConfiguration.getKeyStoreFile().equals(config.getKeyStoreFile())
                        || !listenerConfiguration.getKeyStorePass().equals(config.getKeyStorePass())
                        || !listenerConfiguration.getCertPass().equals(config.getCertPass())) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean closeIfLast(String connectorId) {
        HttpServerConnectorContext context = serverConnectorPool.get(connectorId);
        if (context.getReferenceCount() == 1) {
            return context.getServerConnector().stop();
        } else {
            context.decrementReferenceCount();
            return false;
        }
    }
}
