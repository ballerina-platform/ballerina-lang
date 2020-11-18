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
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.client.listener.ClientConnectorListener;
import org.ballerinalang.net.http.websocket.client.listener.RetryConnectorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes the Retry WebSocket Client.
 *
 * @since 1.2.0
 */
public class RetryInitEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(RetryInitEndpoint.class);
    private static final BString INTERVAL_IN_MILLIS = BStringUtils.fromString("intervalInMillis");
    private static final BString MAX_WAIT_INTERVAL = BStringUtils.fromString("maxWaitIntervalInMillis");
    private static final BString MAX_COUNT = BStringUtils.fromString("maxCount");
    private static final BString BACK_OF_FACTOR = BStringUtils.fromString("backOffFactor");

    public static void initEndpoint(BalEnv env, BObject retryClient) {
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        BMap<BString, Object> clientEndpointConfig = (BMap<BString, Object>) retryClient.getMapValue(
                HttpConstants.CLIENT_ENDPOINT_CONFIG);
        @SuppressWarnings(WebSocketConstants.UNCHECKED)
        BMap<BString, Object> retryConfig = (BMap<BString, Object>) clientEndpointConfig.getMapValue(
                WebSocketConstants.RETRY_CONTEXT);
        RetryContext retryConnectorConfig = new RetryContext();
        populateRetryConnectorConfig(retryConfig, retryConnectorConfig);
        retryClient.addNativeData(WebSocketConstants.RETRY_CONTEXT.getValue(), retryConnectorConfig);
        retryClient.addNativeData(WebSocketConstants.CLIENT_LISTENER, new RetryConnectorListener(
                new ClientConnectorListener()));
        InitEndpoint.initEndpoint(env, retryClient);
    }

    /**
     * Populate the retry config.
     *
     * @param retryConfig - the retry config
     * @param retryConnectorConfig - the retry connector config
     */
    private static void populateRetryConnectorConfig(BMap<BString, Object> retryConfig,
                                                     RetryContext retryConnectorConfig) {
        retryConnectorConfig.setInterval(WebSocketUtil.getIntValue(retryConfig, INTERVAL_IN_MILLIS, 1000));
        retryConnectorConfig.setBackOfFactor(getDoubleValue(retryConfig));
        retryConnectorConfig.setMaxInterval(WebSocketUtil.getIntValue(retryConfig, MAX_WAIT_INTERVAL, 30000));
        retryConnectorConfig.setMaxAttempts(WebSocketUtil.getIntValue(retryConfig, MAX_COUNT, 0));
    }

    private static Double getDoubleValue(BMap<BString, Object> configs) {
        double value = Math.toRadians(configs.getFloatValue(BACK_OF_FACTOR));
        if (value < 1) {
            logger.warn("The value set for `backOffFactor` needs to be great than than 1. The `backOffFactor`" +
                    " value is set to {}", 1.0);
            value = 1.0;
        }
        return value;
    }

    private RetryInitEndpoint() {
    }
}
