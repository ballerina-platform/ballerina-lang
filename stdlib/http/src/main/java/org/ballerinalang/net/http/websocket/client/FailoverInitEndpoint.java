/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.websocket.client;

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.client.listener.ClientConnectorListener;
import org.ballerinalang.net.http.websocket.client.listener.FailoverConnectorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the Failover WebSocket Client.
 *
 * @since 1.2.0
 */
public class FailoverInitEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(FailoverInitEndpoint.class);
    private static final BString FAILOVER_INTERVAL = BStringUtils.fromString("failoverIntervalInMillis");

    public static void initEndpoint(BalEnv env, BObject failoverClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        BMap<BString, Object> clientEndpointConfig = (BMap<BString, Object>) failoverClient.getMapValue(
                WebSocketConstants.CLIENT_ENDPOINT_CONFIG);
        List<String> newTargetUrls = getValidUrls(clientEndpointConfig.getBArray(WebSocketConstants.TARGET_URLS));
        // Sets the failover config values.
        failoverClient.set(WebSocketConstants.CLIENT_URL_CONFIG, BStringUtils.fromString(newTargetUrls.get(0)));
        FailoverContext failoverContext = new FailoverContext();
        populateFailoverContext(clientEndpointConfig, failoverContext, newTargetUrls);
        failoverClient.addNativeData(WebSocketConstants.FAILOVER_CONTEXT, failoverContext);
        failoverClient.addNativeData(WebSocketConstants.CLIENT_LISTENER, new FailoverConnectorListener(
                new ClientConnectorListener()));
        InitEndpoint.initEndpoint(env, failoverClient);
    }

    /**
     * Populates the failover config.
     *
     * @param failoverConfig - a failover config
     * @param failoverClientConnectorConfig - a failover client connector config
     * @param targetUrls - target URLs
     */
    private static void populateFailoverContext(BMap<BString, Object> failoverConfig,
                                                FailoverContext failoverClientConnectorConfig,
                                                List<String> targetUrls) {
        failoverClientConnectorConfig.setFailoverInterval(WebSocketUtil.getIntValue(failoverConfig, FAILOVER_INTERVAL,
                1000));
        failoverClientConnectorConfig.setTargetUrls(targetUrls);
    }

    /**
     * Checks whether the URL has a valid format or not. If it isn't in the valid format, removes that from the URL set.
     *
     * @param targets - target URLs array
     * @return - validated target URLs array
     */
    private static List<String> getValidUrls(BArray targets) {
        List<String> newTargetUrls = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < targets.size(); i++) {
            String url = targets.get(i).toString();
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme();
                if (!WebSocketConstants.WS_SCHEME.equalsIgnoreCase(scheme) && !WebSocketConstants.WSS_SCHEME.
                        equalsIgnoreCase(scheme)) {
                    String name = targets.get(i).toString();
                    logger.error("{} drop from the targets url because webSocket client supports only WS(S) scheme.",
                            name);
                } else {
                    newTargetUrls.add(index, url);
                    index++;
                }
            } catch (URISyntaxException e) {
                logger.error("Error occurred when constructing a hierarchical URI from the " +
                        "given url[" + url + "].");
            }
        }
        if (newTargetUrls.isEmpty()) {
            throw WebSocketUtil.getWebSocketException("TargetUrls should have at least one valid URL.",
                    null, WebSocketConstants.ErrorCode.WsGenericError.errorCode(), null);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("New targetUrls: {}", newTargetUrls);
        }
        return newTargetUrls;
    }

    private FailoverInitEndpoint() {
    }
}
