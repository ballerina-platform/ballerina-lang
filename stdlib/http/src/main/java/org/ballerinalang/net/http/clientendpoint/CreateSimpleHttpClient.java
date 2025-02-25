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

package org.ballerinalang.net.http.clientendpoint;

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_SERVICE_URI;
import static org.ballerinalang.net.http.HttpConstants.HTTP2_PRIOR_KNOWLEDGE;
import static org.ballerinalang.net.http.HttpUtil.getConnectionManager;
import static org.ballerinalang.net.http.HttpUtil.populateSenderConfigurations;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_2_0_VERSION;

/**
 * Initialization of client endpoint.
 *
 * @since 0.966
 */
public class CreateSimpleHttpClient {
    @SuppressWarnings("unchecked")
    public static void createSimpleHttpClient(ObjectValue httpClient,
                                              MapValue<String, Long> globalPoolConfig) {
        String urlString = httpClient.getStringValue(CLIENT_ENDPOINT_SERVICE_URI).replaceAll(HttpConstants.REGEX,
                HttpConstants.SINGLE_SLASH);
        httpClient.set(CLIENT_ENDPOINT_SERVICE_URI, urlString);
        MapValue<String, Object> clientEndpointConfig = (MapValue<String, Object>) httpClient.get(
                CLIENT_ENDPOINT_CONFIG);
        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();
        String scheme;
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw HttpUtil.createHttpError("malformed URL: " + urlString, HttpErrorType.GENERIC_CLIENT_ERROR);
        }
        scheme = url.getProtocol();
        Map<String, Object> properties =
                HttpConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setScheme(scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        String httpVersion = clientEndpointConfig.getStringValue(HttpConstants.CLIENT_EP_HTTP_VERSION);
        if (HTTP_2_0_VERSION.equals(httpVersion)) {
            MapValue<String, Object> http2Settings = (MapValue<String, Object>) clientEndpointConfig.
                    get(HttpConstants.HTTP2_SETTINGS);
            boolean http2PriorKnowledge = (boolean) http2Settings.get(HTTP2_PRIOR_KNOWLEDGE);
            senderConfiguration.setForceHttp2(http2PriorKnowledge);
        } else {
            MapValue<String, Object> http1Settings = (MapValue<String, Object>) clientEndpointConfig.get(
                    HttpConstants.HTTP1_SETTINGS);
            String chunking = http1Settings.getStringValue(HttpConstants.CLIENT_EP_CHUNKING);
            senderConfiguration.setChunkingConfig(HttpUtil.getChunkConfig(chunking));
            String keepAliveConfig = http1Settings.getStringValue(HttpConstants.CLIENT_EP_IS_KEEP_ALIVE);
            senderConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAliveConfig));
        }

        // Set Response validation limits.
        MapValue<String, Object> responseLimits = (MapValue<String, Object>) clientEndpointConfig.get(
                HttpConstants.RESPONSE_LIMITS);
        HttpUtil.setInboundMgsSizeValidationConfig(responseLimits.getIntValue(HttpConstants.MAX_STATUS_LINE_LENGTH),
                                                   responseLimits.getIntValue(HttpConstants.MAX_HEADER_SIZE),
                                                   responseLimits.getIntValue(HttpConstants.MAX_ENTITY_BODY_SIZE),
                                                   senderConfiguration.getMsgSizeValidationConfig());
        try {
            populateSenderConfigurations(senderConfiguration, clientEndpointConfig, scheme);
        } catch (RuntimeException e) {
            throw HttpUtil.createHttpError(e.getMessage(), HttpErrorType.GENERIC_CLIENT_ERROR);
        }
        ConnectionManager poolManager;
        MapValue<String, Long> userDefinedPoolConfig = (MapValue<String, Long>) clientEndpointConfig.get(
                HttpConstants.USER_DEFINED_POOL_CONFIG);

        if (userDefinedPoolConfig == null) {
            poolManager = getConnectionManager(globalPoolConfig);
        } else {
            poolManager = getConnectionManager(userDefinedPoolConfig);
        }

        try {
            HttpClientConnector httpClientConnector = HttpUtil.createHttpWsConnectionFactory()
                    .createHttpsClientConnector(properties, senderConfiguration, poolManager);
            httpClient.addNativeData(HttpConstants.CLIENT, httpClientConnector);
        } catch (Exception e) {
            throw HttpUtil.createHttpError(e.getMessage(), HttpErrorType.GENERIC_CLIENT_ERROR);
        }
    }
}
