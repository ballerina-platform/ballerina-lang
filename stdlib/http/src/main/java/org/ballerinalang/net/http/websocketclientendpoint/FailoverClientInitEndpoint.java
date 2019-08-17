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
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocketclientendpoint;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.ballerinalang.net.http.WebSocketConstants.CONNECTOR_FACTORY;
import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URLS;
import static org.ballerinalang.net.http.WebSocketUtil.getWebSocketService;
import static org.ballerinalang.net.http.WebSocketUtil.initialiseWebSocketFailoverConnection;
import static org.ballerinalang.net.http.WebSocketUtil.populateFailoverConnectorConfig;
import static org.ballerinalang.net.http.WebSocketUtil.populateRetryConnectorConfig;

/**
 * Initialize the failover WebSocket Client.
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
public class FailoverClientInitEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(FailoverClientInitEndpoint.class);

    public static void init(Strand strand, ObjectValue webSocketClient) throws URISyntaxException {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        ArrayValue targets = clientEndpointConfig.getArrayValue(TARGET_URLS);
        ArrayList<String> newTargetUrls = new ArrayList<>();
        int index = 0;
        // check whether url has valid format or not
        // if It isn't in the valid format, remove that from the url set
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
        logger.info("New targetUrls are " + newTargetUrls);
        if (newTargetUrls.size() == 0) {
            throw new WebSocketException("TargetUrls should have atleast one valid URL.");
        }
        // Create the connector factory and set it as the native data
        webSocketClient.addNativeData(CONNECTOR_FACTORY, HttpUtil.createHttpWsConnectionFactory());
        if (clientEndpointConfig.get(RETRY_CONFIG) != null) {
            @SuppressWarnings(WebSocketConstants.UNCHECKED)
            MapValue<String, Object> retryConfig = (MapValue<String, Object>) clientEndpointConfig.
                    get(RETRY_CONFIG);
            // Set the retry config values
            RetryContext retryConnectorConfig = new RetryContext();
            populateRetryConnectorConfig(retryConfig, retryConnectorConfig);
            webSocketClient.addNativeData(RETRY_CONFIG, retryConnectorConfig);
        }
        // Set the failover config values
        FailoverContext failoverClientConnectorConfig = new FailoverContext();
        populateFailoverConnectorConfig(clientEndpointConfig, failoverClientConnectorConfig, newTargetUrls);
        webSocketClient.addNativeData(FAILOVER_CONFIG, failoverClientConnectorConfig);
        // Call the function with first url in the target url set
        initialiseWebSocketFailoverConnection(newTargetUrls.get(0), webSocketClient,
                getWebSocketService(clientEndpointConfig, strand));
    }
}
