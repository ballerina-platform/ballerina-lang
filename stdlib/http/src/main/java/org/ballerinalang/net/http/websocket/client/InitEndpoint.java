/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketException;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Initialize the WebSocket Client.
 *
 * @since 0.966
 */
public class InitEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(InitEndpoint.class);
    private static final String INTERVAL_IN_MILLIS = "intervalInMillis";
    private static final String MAX_WAIT_INTERVAL = "maxWaitIntervalInMillis";
    private static final String MAX_COUNT = "maxCount";
    private static final String BACK_OF_FACTOR = "backOffFactor";

    public static void initEndpoint(ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        Strand strand = Scheduler.getStrand();
        String remoteUrl = webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG);
        WebSocketService wsService = validateAndCreateWebSocketService(clientEndpointConfig, strand);
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        String scheme = URI.create(remoteUrl).getScheme();
        populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, scheme);
        // Create the client connector.
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        WebSocketClientConnectorListener clientConnectorListener = new WebSocketClientConnectorListener();
        // Add the client connector as a native data when the client is not a failover client
        // because when using one URL, there is no need to create the client connector again.
        webSocketClient.addNativeData(WebSocketConstants.CLIENT_CONNECTOR, clientConnector);
        webSocketClient.addNativeData(WebSocketConstants.CLIENT_LISTENER, clientConnectorListener);
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            @SuppressWarnings(WebSocketConstants.UNCHECKED)
            MapValue<String, Object> retryConfig = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                    WebSocketConstants.RETRY_CONFIG);
            RetryContext retryConnectorConfig = new RetryContext();
            populateRetryConnectorConfig(retryConfig, retryConnectorConfig);
            webSocketClient.addNativeData(WebSocketConstants.RETRY_CONFIG, retryConnectorConfig);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            webSocketClient.addNativeData(WebSocketConstants.COUNT_DOWN_LATCH, countDownLatch);
            WebSocketUtil.establishWebSocketConnection(webSocketClient, wsService);
            // Set the count Down latch for initial connection
            waitForHandshake(countDownLatch);
        } else {
            WebSocketUtil.establishWebSocketConnection(webSocketClient, wsService);
        }

    }

    private static void waitForHandshake(CountDownLatch countDownLatch) {
        try {
            // Wait to call countDown()
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WebSocketException(WebSocketConstants.ERROR_MESSAGE + e.getMessage());
        }
    }

    private static void populateClientConnectorConfig(MapValue<String, Object> clientEndpointConfig,
                                                      WebSocketClientConnectorConfig clientConnectorConfig,
                                                      String scheme) {
        clientConnectorConfig.setAutoRead(false); // Frames are read sequentially in ballerina.
        clientConnectorConfig.setSubProtocols(WebSocketUtil.findNegotiableSubProtocols(clientEndpointConfig));
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> headerValues = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                WebSocketConstants.CLIENT_CUSTOM_HEADERS_CONFIG);
        if (headerValues != null) {
            clientConnectorConfig.addHeaders(getCustomHeaders(headerValues));
        }

        long idleTimeoutInSeconds = WebSocketUtil.findTimeoutInSeconds(clientEndpointConfig,
                WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT, 0);
        if (idleTimeoutInSeconds > 0) {
            clientConnectorConfig.setIdleTimeoutInMillis((int) (idleTimeoutInSeconds * 1000));
        }

        clientConnectorConfig.setMaxFrameSize(WebSocketUtil.findMaxFrameSize(clientEndpointConfig));

        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(clientConnectorConfig, secureSocket);
        } else if (scheme.equals(WebSocketConstants.WSS_SCHEME)) {
            clientConnectorConfig.useJavaDefaults();
        }
        clientConnectorConfig.setWebSocketCompressionEnabled(
                clientEndpointConfig.getBooleanValue(WebSocketConstants.COMPRESSION_ENABLED_CONFIG));
    }

    private static Map<String, String> getCustomHeaders(MapValue<String, Object> headers) {
        Map<String, String> customHeaders = new HashMap<>();
        headers.entrySet().forEach(
                entry -> customHeaders.put(entry.getKey(), headers.get(entry.getKey()).toString())
        );
        return customHeaders;
    }

    /**
     * Populate the retry config.
     *
     * @param retryConfig - the retry config.
     * @param retryConnectorConfig - the retry connector config.
     */
    private static void populateRetryConnectorConfig(MapValue<String, Object> retryConfig,
                                                     RetryContext retryConnectorConfig) {
        retryConnectorConfig.setInterval(getIntValue(retryConfig, INTERVAL_IN_MILLIS, 1000));
        retryConnectorConfig.setBackOfFactor(getDoubleValue(retryConfig));
        retryConnectorConfig.setMaxInterval(getIntValue(retryConfig, MAX_WAIT_INTERVAL, 30000));
        retryConnectorConfig.setMaxAttempts(getIntValue(retryConfig, MAX_COUNT, 0));
    }

    /**
     * Validate and create the webSocket service.
     *
     * @param clientEndpointConfig - a client endpoint config.
     * @param strand - which holds the observer context being started
     * @return webSocketService
     */
    private static WebSocketService validateAndCreateWebSocketService(MapValue<String, Object> clientEndpointConfig,
                                                                      Strand strand) {
        Object clientService = clientEndpointConfig.get(WebSocketConstants.CLIENT_SERVICE_CONFIG);
        if (clientService != null) {
            BType param = ((ObjectValue) clientService).getType().getAttachedFunctions()[0].getParameterType()[0];
            if (param == null || !WebSocketConstants.WEBSOCKET_CLIENT_NAME.equals(param.toString())) {
                throw new WebSocketException("The callback service should be a WebSocket Client Service");
            }
            return new WebSocketService((ObjectValue) clientService, strand.scheduler);
        } else {
            return new WebSocketService(strand.scheduler);
        }
    }

    private static int getIntValue(MapValue<String, Object> configs, String key, int defaultValue) {
        int value = Math.toIntExact(configs.getIntValue(key));
        if (value < 0) {
            logger.warn("The value set for `{}` needs to be great than than -1. The `{}` value is set to {}", key, key,
                    defaultValue);
            value = defaultValue;
        }
        return value;
    }

    private static Double getDoubleValue(MapValue<String, Object> configs) {
        double value = Math.toRadians(configs.getFloatValue(BACK_OF_FACTOR));
        if (value < 1) {
            logger.warn("The value set for `backOffFactor` needs to be great than than 1. The `backOffFactor`" +
                            " value is set to {}", 1.0);
            value = 1.0;
        }
        return value;
    }


    private InitEndpoint() {
    }
}
