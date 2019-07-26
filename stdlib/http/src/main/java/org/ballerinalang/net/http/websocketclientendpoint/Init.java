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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ballerinalang.net.http.WebSocketConstants.FAILOVER_INTEVAL;
import static org.ballerinalang.net.http.WebSocketConstants.IS_CONNECTION_LOST;
import static org.ballerinalang.net.http.WebSocketConstants.IS_CONNECTION_MADE;
import static org.ballerinalang.net.http.WebSocketConstants.RECONNECT_ATTEMPTS;
import static org.ballerinalang.net.http.WebSocketConstants.RETRY_CONFIG;
import static org.ballerinalang.net.http.WebSocketConstants.SUB_TARGET_URLS_INDEX;
import static org.ballerinalang.net.http.WebSocketConstants.TARGET_URL_INDEX;
import static org.ballerinalang.net.http.WebSocketUtil.checkParameter;
import static org.ballerinalang.net.http.WebSocketUtil.getIntegerValue;
import static org.ballerinalang.net.http.WebSocketUtil.getWebSocketService;
import static org.ballerinalang.net.http.WebSocketUtil.initialiseWebSocketConnection;

/**
 * Initialize the failover WebSocket Client.
 *
 */

@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "init",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = WebSocketConstants.FAILOVER_WEBSOCKET_CLIENT,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        ),
        args = {@Argument(name = "config", type = TypeKind.RECORD,
                structType = "FailoverWebSocketClientEndpointConfig")},
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {

    private static final Logger logger = LoggerFactory.getLogger(Init.class);

    @Override
    public void execute(Context context) {
    }

    public static void init(Strand strand, ObjectValue webSocketClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) webSocketClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        ArrayValue targets = clientEndpointConfig.getArrayValue(WebSocketConstants.TARGETS_URLS);
        int failoverInterval =  getIntegerValue(Long.valueOf(clientEndpointConfig.get(FAILOVER_INTEVAL).
                toString()));
        if (targets.size() == 0) {
           throw new WebSocketException("TargetUrls should have atleast one URL");
        }
        if (failoverInterval < 0) {
            logger.warn("The maxInterval's value set for the configuration needs to be " +
                    "greater than -1. The " + failoverInterval + "value is set to 1.0");
            webSocketClient.getMapValue(HttpConstants.CLIENT_ENDPOINT_CONFIG).put(FAILOVER_INTEVAL, 1000);
        }
        if (clientEndpointConfig.get(RETRY_CONFIG) != null) {
            webSocketClient.addNativeData(RECONNECT_ATTEMPTS, 0);
            checkParameter(webSocketClient);
        }
        webSocketClient.addNativeData(TARGET_URL_INDEX, 0);
        webSocketClient.addNativeData(IS_CONNECTION_LOST, false);
        webSocketClient.addNativeData(SUB_TARGET_URLS_INDEX, 0);
        webSocketClient.addNativeData(IS_CONNECTION_MADE, false);
        initialiseWebSocketConnection(targets.getString(0), webSocketClient,
                getWebSocketService(clientEndpointConfig, strand));
    }
}
