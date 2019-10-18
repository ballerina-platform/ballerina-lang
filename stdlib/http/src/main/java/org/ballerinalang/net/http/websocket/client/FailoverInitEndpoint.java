/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketException;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Initializes the Failover WebSocket Client.
 *
 * @since 1.1.0
 */

@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "init",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        )
)
public class FailoverInitEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(FailoverInitEndpoint.class);

    public static void init(Strand strand, ObjectValue webSocketClient) throws URISyntaxException {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                WebSocketConstants.CLIENT_ENDPOINT_CONFIG);
        ArrayValue targets = clientEndpointConfig.getArrayValue(WebSocketConstants.TARGET_URLS);
        ArrayList<String> newTargetUrls = new ArrayList<>();
        int index = 0;
        // Checks whether the URL has a valid format or not.
        // If It isn't in the valid format, remove that from the URL set.
        for (int i = 0; i < targets.size(); i++) {
            URI uri = new URI(targets.get(i).toString());
            String scheme = uri.getScheme();
            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                logger.error(targets.get(i).toString() + " drop from the targets url" +
                        "because webSocket client supports only WS(S) scheme.");
            } else {
                newTargetUrls.add(index, targets.get(i).toString());
                index++;
            }
        }
        logger.debug("New targetUrls: " + newTargetUrls);
        if (newTargetUrls.size() == 0) {
            throw new WebSocketException("TargetUrls should have atleast one valid URL.");
        }
        // Creates the connector factory and sets it as the native data.
        webSocketClient.addNativeData(WebSocketConstants.CONNECTOR_FACTORY, HttpUtil.createHttpWsConnectionFactory());
        if (clientEndpointConfig.get(WebSocketConstants.RETRY_CONFIG) != null) {
            @SuppressWarnings(WebSocketConstants.UNCHECKED)
            MapValue<String, Object> retryConfig = (MapValue<String, Object>) clientEndpointConfig.
                    get(WebSocketConstants.RETRY_CONFIG);
            // Set the retry config values
            RetryContext retryConnectorConfig = new RetryContext();
            WebSocketUtil.populateRetryConnectorConfig(retryConfig, retryConnectorConfig);
            webSocketClient.addNativeData(WebSocketConstants.RETRY_CONFIG, retryConnectorConfig);
        }
        // Sets the failover config values.
        FailoverContext failoverConfig = new FailoverContext();
        WebSocketUtil.populateFailoverConnectorConfig(clientEndpointConfig, failoverConfig, newTargetUrls);
        webSocketClient.addNativeData(WebSocketConstants.FAILOVER_CONFIG, failoverConfig);
        // Calls the function with the first URL in the target URLs set.
        WebSocketUtil.establishInitialFailoverConnection(WebSocketUtil.
                        getWebSocketClientConnector(newTargetUrls.get(0), webSocketClient),
                webSocketClient, WebSocketUtil.getWebSocketService(clientEndpointConfig, strand));
    }

    private FailoverInitEndpoint() {
    }
}
