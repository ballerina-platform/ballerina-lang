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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.logging.util.BLogLevel;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.http.util.ConnectorStartupSynchronizer;
import org.ballerinalang.net.ws.BallerinaWsServerConnectorListener;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.ProxyServerConfiguration;
import org.wso2.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WsClientConnectorConfig;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.LogManager;

/**
 * {@code HttpConnectionManager} is responsible for managing all the server connectors with ballerina runtime.
 *
 * @since 0.94
 */
public class HttpConnectionManager {

    private static HttpConnectionManager instance = new HttpConnectionManager();
    private Map<String, org.wso2.transport.http.netty.contract.ServerConnector>
            startupDelayedHTTPServerConnectors = new HashMap<>();

    private Map<String, org.wso2.transport.http.netty.contract.ServerConnector>
            startedHTTPServerConnectors = new HashMap<>();
    private Map<String, HttpServerConnectorContext>
            serverConnectorPool = new HashMap<>();
    private ServerBootstrapConfiguration serverBootstrapConfiguration;
    private TransportsConfiguration trpConfig;
    private HttpWsConnectorFactory httpConnectorFactory = new HttpWsConnectorFactoryImpl();

    private HttpConnectionManager() {
        String nettyConfigFile = System.getProperty(Constants.HTTP_TRANSPORT_CONF,
                "conf" + File.separator + "transports" +
                        File.separator + "netty-transports.yml");
        trpConfig = ConfigurationBuilder.getInstance().getConfiguration(nettyConfigFile);
        serverBootstrapConfiguration = HTTPConnectorUtil
                .getServerBootstrapConfiguration(trpConfig.getTransportProperties());

        if (isHTTPTraceLoggerEnabled()) {
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

    public org.wso2.transport.http.netty.contract.ServerConnector createHttpServerConnector(
            ListenerConfiguration listenerConfig) {
        String listenerInterface = listenerConfig.getHost() + ":" + listenerConfig.getPort();
        HttpServerConnectorContext httpServerConnectorContext =
                serverConnectorPool.get(listenerInterface);
        if (httpServerConnectorContext != null) {
            if (checkForConflicts(listenerConfig, httpServerConnectorContext)) {
                throw new BallerinaConnectorException("Conflicting configuration detected for listener " +
                        "configuration id " + listenerConfig.getId());
            }
            httpServerConnectorContext.incrementReferenceCount();
            return httpServerConnectorContext.getServerConnector();
        }

        if (isHTTPTraceLoggerEnabled()) {
            listenerConfig.setHttpTraceLogEnabled(true);
        }

        serverBootstrapConfiguration = HTTPConnectorUtil
                .getServerBootstrapConfiguration(trpConfig.getTransportProperties());
        org.wso2.transport.http.netty.contract.ServerConnector serverConnector =
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
            org.wso2.transport.http.netty.contract.ServerConnector serverConnector) {
        startupDelayedHTTPServerConnectors.put(id, serverConnector);
    }

    /**
     * Start all the ServerConnectors which startup is delayed at the service deployment time.
     *
     * @param httpServerConnector {@link BallerinaHttpServerConnector} of the pending transport server connectors.
     * @throws ServerConnectorException if exception occurs while starting at least one connector.
     */
    public void startPendingHTTPConnectors(BallerinaHttpServerConnector httpServerConnector)
            throws ServerConnectorException {
        ConnectorStartupSynchronizer startupSyncer =
                new ConnectorStartupSynchronizer(new CountDownLatch(startupDelayedHTTPServerConnectors.size()));

        for (Map.Entry<String, ServerConnector> serverConnectorEntry : startupDelayedHTTPServerConnectors.entrySet()) {
            ServerConnector serverConnector = serverConnectorEntry.getValue();
            ServerConnectorFuture connectorFuture = serverConnector.start();
            setConnectorListeners(connectorFuture, serverConnector.getConnectorID(), startupSyncer,
                                  httpServerConnector);
            startedHTTPServerConnectors.put(serverConnector.getConnectorID(), serverConnector);
        }
        try {
            // Wait for all the connectors to start
            startupSyncer.getCountDownLatch().await();
        } catch (InterruptedException e) {
            throw new BallerinaConnectorException("Error in starting HTTP server connector(s)");
        }
        validateConnectorStartup(startupSyncer);
        startupDelayedHTTPServerConnectors.clear();
    }

    public HttpClientConnector getHTTPHttpClientConnector(String scheme, BConnector bConnector) {
        Map<String, Object> properties = HTTPConnectorUtil.getTransportProperties(trpConfig);
        SenderConfiguration senderConfiguration =
                HTTPConnectorUtil.getSenderConfiguration(trpConfig, scheme);

        if (isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTlsStoreType(Constants.PKCS_STORE_TYPE);

        BStruct options = (BStruct) bConnector.getRefField(Constants.OPTIONS_STRUCT_INDEX);
        if (options != null) {
            populateSenderConfigurationOptions(senderConfiguration, options);
        }
        return httpConnectorFactory.createHttpClientConnector(properties, senderConfiguration);
    }

    private static class HttpServerConnectorContext {
        private org.wso2.transport.http.netty.contract.ServerConnector serverConnector;
        private ListenerConfiguration listenerConfiguration;
        private int referenceCount = 0;

        public HttpServerConnectorContext(org.wso2.transport.http.netty.contract.ServerConnector
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

        public org.wso2.transport.http.netty.contract.ServerConnector getServerConnector() {
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
        if (context == null) {
            return false;
        }
        if (listenerConfiguration.getScheme().equalsIgnoreCase("https")) {
            ListenerConfiguration config = context.getListenerConfiguration();
            if (!listenerConfiguration.getKeyStoreFile().equals(config.getKeyStoreFile())
                    || !listenerConfiguration.getKeyStorePass().equals(config.getKeyStorePass())
                    || !listenerConfiguration.getCertPass().equals(config.getCertPass())) {
                return true;
            }
        }
        return false;
    }

    public boolean closeIfLast(String connectorId) {
        HttpServerConnectorContext context = serverConnectorPool.get(connectorId);
        if (context.getReferenceCount() == 1) {
            return context.getServerConnector().stop();
        }
        context.decrementReferenceCount();
        return false;
    }

    public WebSocketClientConnector getWebSocketClientConnector(WsClientConnectorConfig configuration) {
        return  httpConnectorFactory.createWsClientConnector(configuration);
    }

    private void setConnectorListeners(ServerConnectorFuture connectorFuture, String serverConnectorId,
                                       ConnectorStartupSynchronizer startupSyncer,
                                       BallerinaHttpServerConnector httpServerConnector) {
        HTTPServicesRegistry httpServicesRegistry = httpServerConnector.getHttpServicesRegistry();
        WebSocketServicesRegistry webSocketServicesRegistry = httpServerConnector.getWebSocketServicesRegistry();
        connectorFuture.setHttpConnectorListener(new BallerinaHTTPConnectorListener(httpServicesRegistry));
        connectorFuture.setWSConnectorListener(new BallerinaWsServerConnectorListener(webSocketServicesRegistry));
        connectorFuture.setPortBindingEventListener(
                new HttpConnectorPortBindingListener(startupSyncer, serverConnectorId));
    }

    private void validateConnectorStartup(ConnectorStartupSynchronizer startupSyncer) {
        int noOfExceptions = startupSyncer.getExceptions().size();
        if (noOfExceptions <= 0) {
            return;
        }
        PrintStream console = System.err;

        startupSyncer.getExceptions().forEach((connectorId, e) -> {
            console.println("ballerina: " + makeFirstLetterLowerCase(e.getMessage()) + ": [" + connectorId + "]");
        });

        if (noOfExceptions == startupDelayedHTTPServerConnectors.size()) {
            // If the no. of exceptions is equal to the no. of connectors to be started, then none of the
            // connectors have started properly and we can terminate the runtime
            throw new BallerinaConnectorException("failed to start the server connectors");
        }
    }

    private boolean isHTTPTraceLoggerEnabled() {
        // TODO: Take a closer look at this since looking up from the Config Registry here caused test failures
        return ((BLogManager) LogManager.getLogManager()).getPackageLogLevel(
                org.ballerinalang.logging.util.Constants.HTTP_TRACE_LOG) == BLogLevel.TRACE;
    }

    private void populateSenderConfigurationOptions(SenderConfiguration senderConfiguration, BStruct options) {
        //TODO Define default values until we get Anonymous struct (issues #3635)
        ProxyServerConfiguration proxyServerConfiguration = null;
        int followRedirect = 0;
        int maxRedirectCount = 5;
        if (options.getRefField(Constants.FOLLOW_REDIRECT_STRUCT_INDEX) != null) {
            BStruct followRedirects = (BStruct) options.getRefField(Constants.FOLLOW_REDIRECT_STRUCT_INDEX);
            followRedirect = followRedirects.getBooleanField(Constants.FOLLOW_REDIRECT_INDEX);
            maxRedirectCount = (int) followRedirects.getIntField(Constants.MAX_REDIRECT_COUNT);
        }
        if (options.getRefField(Constants.SSL_STRUCT_INDEX) != null) {
            BStruct ssl = (BStruct) options.getRefField(Constants.SSL_STRUCT_INDEX);
            String trustStoreFile = ssl.getStringField(Constants.TRUST_STORE_FILE_INDEX);
            String trustStorePassword = ssl.getStringField(Constants.TRUST_STORE_PASSWORD_INDEX);
            String keyStoreFile = ssl.getStringField(Constants.KEY_STORE_FILE_INDEX);
            String keyStorePassword = ssl.getStringField(Constants.KEY_STORE_PASSWORD_INDEX);
            String sslEnabledProtocols = ssl.getStringField(Constants.SSL_ENABLED_PROTOCOLS_INDEX);
            String ciphers = ssl.getStringField(Constants.CIPHERS_INDEX);
            String sslProtocol = ssl.getStringField(Constants.SSL_PROTOCOL_INDEX);

            if (StringUtils.isNotBlank(trustStoreFile)) {
                senderConfiguration.setTrustStoreFile(trustStoreFile);
            }
            if (StringUtils.isNotBlank(trustStorePassword)) {
                senderConfiguration.setTrustStorePass(trustStorePassword);
            }
            if (StringUtils.isNotBlank(keyStoreFile)) {
                senderConfiguration.setKeyStoreFile(keyStoreFile);
            }
            if (StringUtils.isNotBlank(keyStorePassword)) {
                senderConfiguration.setKeyStorePassword(keyStorePassword);
            }

            List<Parameter> clientParams = new ArrayList<>();
            if (StringUtils.isNotBlank(sslEnabledProtocols)) {
                Parameter clientProtocols = new Parameter(Constants.SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                clientParams.add(clientProtocols);
            }
            if (StringUtils.isNotBlank(ciphers)) {
                Parameter clientCiphers = new Parameter(Constants.CIPHERS, ciphers);
                clientParams.add(clientCiphers);
            }
            if (StringUtils.isNotBlank(sslProtocol)) {
                senderConfiguration.setSslProtocol(sslProtocol);
            }
            if (!clientParams.isEmpty()) {
                senderConfiguration.setParameters(clientParams);
            }
        }
        if (options.getRefField(Constants.PROXY_STRUCT_INDEX) != null) {
            BStruct proxy = (BStruct) options.getRefField(Constants.PROXY_STRUCT_INDEX);
            String proxyHost = proxy.getStringField(Constants.PROXY_HOST_INDEX);
            int proxyPort = (int) proxy.getIntField(Constants.PROXY_PORT_INDEX);
            String proxyUserName = proxy.getStringField(Constants.PROXY_USER_NAME_INDEX);
            String proxyPassword = proxy.getStringField(Constants.PROXY_PASSWORD_INDEX);
            try {
                proxyServerConfiguration = new ProxyServerConfiguration(proxyHost, proxyPort);
            } catch (UnknownHostException e) {
                throw new BallerinaConnectorException("Failed to resolve host" + proxyHost, e);
            }
            if (!proxyUserName.isEmpty()) {
                proxyServerConfiguration.setProxyUsername(proxyUserName);
            }
            if (!proxyPassword.isEmpty()) {
                proxyServerConfiguration.setProxyPassword(proxyPassword);
            }
            senderConfiguration.setProxyServerConfiguration(proxyServerConfiguration);
        }

        senderConfiguration.setFollowRedirect(followRedirect == 1);
        senderConfiguration.setMaxRedirectCount(maxRedirectCount);
        int enableChunking = options.getBooleanField(Constants.ENABLE_CHUNKING_INDEX);
        senderConfiguration.setChunkDisabled(enableChunking == 0);

        long endpointTimeout = options.getIntField(Constants.ENDPOINT_TIMEOUT_STRUCT_INDEX);
        if (endpointTimeout < 0 || (int) endpointTimeout != endpointTimeout) {
            throw new BallerinaConnectorException("Invalid idle timeout: " + endpointTimeout);
        }
        senderConfiguration.setSocketIdleTimeout((int) endpointTimeout);

        boolean isKeepAlive = options.getBooleanField(Constants.IS_KEEP_ALIVE_INDEX) == 1;
        senderConfiguration.setKeepAlive(isKeepAlive);
    }

    private String makeFirstLetterLowerCase(String str) {
        if (str == null) {
            return null;
        }
        char ch[] = str.toCharArray();
        ch[0] = Character.toLowerCase(ch[0]);
        return new String(ch);
    }
}
