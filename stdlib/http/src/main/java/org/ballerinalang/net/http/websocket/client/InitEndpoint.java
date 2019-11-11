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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketClientConnectorConfig;

import java.net.URI;

/**
 * Initialize the WebSocket Client.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "initEndpoint",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = WebSocketConstants.WEBSOCKET_CLIENT,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        )
)
public class InitEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(InitEndpoint.class);
    private static final String STATEMENT_FOR_BACK_OF_FACTOR = "The decay's value set for the configuration needs " +
            "to be greater than -1. ";
    private static final String STATEMENT_FOR_MAX_INTERVAL = "The maxInterval's value set for the configuration" +
            " needs to be greater than -1. ";
    private static final String STATEMENT_FOR_MAX_ATTEPTS = "The maximum doReconnect attempt's value set for the" +
            " configuration needs to be greater than -1. ";
    private static final String STATEMENT_FOR_INTEVAL = "The interval's value set for the configuration needs to be " +
            "greater than -1. ";
    private static final String MAX_COUNT = "maxCount";
    private static final String INTERVAL = "intervalInMillis";
    private static final String BACK_OF_FACTOR = "backOffFactor";
    private static final String MAX_INTERVAL = "maxWaitIntervalInMillis";

    public static void initEndpoint(Strand strand, ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        if (WebSocketUtil.hasRetryConfig(webSocketClient)) {
            @SuppressWarnings(WebSocketConstants.UNCHECKED)
            MapValue<String, Object> retryConfig = (MapValue<String, Object>) clientEndpointConfig.getMapValue(
                    WebSocketConstants.RETRY_CONFIG);
            RetryContext retryConnectorConfig = new RetryContext();
            populateRetryConnectorConfig(retryConfig, retryConnectorConfig);
            webSocketClient.addNativeData(WebSocketConstants.RETRY_CONFIG, retryConnectorConfig);
        }
        String remoteUrl = webSocketClient.getStringValue(WebSocketConstants.CLIENT_URL_CONFIG);
        WebSocketService wsService = WebSocketUtil.validateAndCreateWebSocketService(clientEndpointConfig, strand);
        HttpWsConnectorFactory connectorFactory = HttpUtil.createHttpWsConnectionFactory();
        WebSocketClientConnectorConfig clientConnectorConfig = new WebSocketClientConnectorConfig(remoteUrl);
        String scheme = URI.create(remoteUrl).getScheme();
        WebSocketUtil.populateClientConnectorConfig(clientEndpointConfig, clientConnectorConfig, scheme);
        // Create the client connector
        WebSocketClientConnector clientConnector = connectorFactory.createWsClientConnector(clientConnectorConfig);
        WebSocketClientListener clientConnectorListener = new WebSocketClientListener();
        // Add client connector as a native data, when client is not as a failover client
        // Because when using one url  no need to create the client connector again
        webSocketClient.addNativeData(WebSocketConstants.CLIENT_CONNECTOR, clientConnector);
        webSocketClient.addNativeData(WebSocketConstants.CLIENT_LISTENER, clientConnectorListener);
        WebSocketUtil.establishWebSocketConnection(webSocketClient, wsService);
    }

    /**
     * Populate the retry config.
     *
     * @param retryConfig a retry config
     * @param retryConnectorConfig retry connector config
     */
    private static void populateRetryConnectorConfig(MapValue<String, Object> retryConfig,
                                                     RetryContext retryConnectorConfig) {
        setInterval(Integer.parseInt(retryConfig.get(INTERVAL).toString()), retryConnectorConfig);
        setBackOfFactor(Float.parseFloat(retryConfig.get(BACK_OF_FACTOR).toString()),
                retryConnectorConfig);
        setMaxInterval(Integer.parseInt(retryConfig.get(MAX_INTERVAL).toString()),
                retryConnectorConfig);
        setMaxAttempts(Integer.parseInt(retryConfig.get(MAX_COUNT).toString()),
                retryConnectorConfig);
    }

    /**
     * Set value of the interval in the retry config.
     *
     * @param interval retry interval
     * @param retryConnectorConfig retry connector config
     */
    private static void setInterval(int interval, RetryContext retryConnectorConfig) {
        if (interval < 0) {
            logger.warn("{} The interval[{}] value is set to 1000.", STATEMENT_FOR_INTEVAL, interval);
            interval = 1000;
        }
        retryConnectorConfig.setInterval(interval);
    }

    /**
     * Set value of the backOfFactor in the retry config.
     *
     * @param backOfFactor a rate of increase of the reconnect delay
     * @param retryConnectorConfig retry connector config
     */
    private static void setBackOfFactor(float backOfFactor, RetryContext retryConnectorConfig) {
        if (backOfFactor < 0) {
            logger.warn("{} The backOfFactor[{}] value is set to 1.0", STATEMENT_FOR_BACK_OF_FACTOR,
                    backOfFactor);
            backOfFactor = (float) 1.0;
        }
        retryConnectorConfig.setBackOfFactor(backOfFactor);
    }

    /**
     * Set value of the maxInterval in the retry config.
     *
     * @param maxInterval a maximum interval
     * @param retryConnectorConfig retry connector config
     */
    private static void setMaxInterval(int maxInterval, RetryContext retryConnectorConfig) {
        if (maxInterval < 0) {
            logger.warn("{} The maxInterval[{}] value is set to 30000", STATEMENT_FOR_MAX_INTERVAL,
                    maxInterval);
            maxInterval =  30000;
        }
        retryConnectorConfig.setMaxInterval(maxInterval);
    }

    /**
     * Set value of the maxAttempts in the retry config.
     *
     * @param maxAttempts a maximum number of retry attempts
     * @param retryConnectorConfig retry connector config
     */
    private static void setMaxAttempts(int maxAttempts, RetryContext retryConnectorConfig) {
        if (maxAttempts < 0) {
            logger.warn("{} The maxAttempts[{}] value is set to 0", STATEMENT_FOR_MAX_ATTEPTS,
                    maxAttempts);
            maxAttempts = 0;
        }
        retryConnectorConfig.setMaxAttempts(maxAttempts);
    }

    private InitEndpoint() {
    }
}
