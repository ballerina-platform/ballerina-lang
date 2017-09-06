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
package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.net.ws.BallerinaWebSocketConnectorListener;
import org.wso2.carbon.messaging.ServerConnector;
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
import java.util.Set;

/**
 * {@code BallerinaConnectorManager} is responsible for managing all the server connectors with ballerina runtime.
 *
 * @since 0.89
 */
public class HttpConnectionManager {

    private static HttpConnectionManager instance = new HttpConnectionManager();
    private Map<String, ServerConnector> startupDelayedServerConnectors = new HashMap<>();
    private Map<String, org.wso2.carbon.transport.http.netty.contract.ServerConnector>
            startupDelayedHTTPServerConnectors = new HashMap<>();

    private Map<String, org.wso2.carbon.transport.http.netty.contract.ServerConnector>
            startedHTTPServerConnectors = new HashMap<>();
    private Map<String, HttpServerConnectorContext>
            serverConnectorPool = new HashMap<>();
    private ServerBootstrapConfiguration serverBootstrapConfiguration;
    private TransportsConfiguration trpConfig;
    private HttpWsConnectorFactory httpConnectorFactory = new HttpWsConnectorFactoryImpl();
    private static final String HTTP_TRANSPORT_CONF = "transports.netty.conf";

    private HttpConnectionManager() {
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
                throw new BallerinaConnectorException("Error in configuring HTTP trace log");
            }
        }
    }

    public static HttpConnectionManager getInstance() {
        return instance;
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
                throw new BallerinaConnectorException("Conflicting configuration detected for listener " +
                        "configuration id " + listenerConfig.getId());
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
        startupDelayedHTTPServerConnectors.clear();
        return startedConnectors;
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

    public WebSocketClientConnector getWebSocketClientConnector(Map<String, Object> properties) {
        return  httpConnectorFactory.createWsClientConnector(properties);
    }

}
