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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.ProxyServerConfiguration;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.CALLER_ACTIONS;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;

/**
 * Initialization of client endpoint.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "createSimpleHttpClient",
        args = {@Argument(name = "uri", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.RECORD, structType = "ClientEndpointConfig")},
        isPublic = true
)
public class CreateSimpleHttpClient extends BlockingNativeCallableUnit {

    private static final int DEFAULT_MAX_REDIRECT_COUNT = 5;
    private HttpWsConnectorFactory httpConnectorFactory = HttpUtil.createHttpWsConnectionFactory();

    @Override
    public void execute(Context context) {
        BMap<String, BValue> configBStruct =
                (BMap<String, BValue>) context.getRefArgument(HttpConstants.CLIENT_ENDPOINT_CONFIG_INDEX);
        Struct clientEndpointConfig = BLangConnectorSPIUtil.toStruct(configBStruct);
        String urlString = context.getStringArgument(HttpConstants.CLIENT_ENDPOINT_URL_INDEX);
        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();
        String scheme;
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new BallerinaException("Malformed URL: " + urlString);
        }
        scheme = url.getProtocol();
        Map<String, Object> properties =
                HttpConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HttpConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        populateSenderConfigurationOptions(senderConfiguration, clientEndpointConfig);
        Struct connectionThrottling = clientEndpointConfig.getStructField(HttpConstants.
                CONNECTION_THROTTLING_STRUCT_REFERENCE);
        if (connectionThrottling != null) {
            long maxActiveConnections = connectionThrottling
                    .getIntField(HttpConstants.CONNECTION_THROTTLING_MAX_ACTIVE_CONNECTIONS);
            if (!isInteger(maxActiveConnections)) {
                throw new BallerinaConnectorException("invalid maxActiveConnections value: "
                        + maxActiveConnections);
            }
            senderConfiguration.getPoolConfiguration().setMaxActivePerPool((int) maxActiveConnections);

            long waitTime = connectionThrottling
                    .getIntField(HttpConstants.CONNECTION_THROTTLING_WAIT_TIME);
            senderConfiguration.getPoolConfiguration().setMaxWaitTime(waitTime);

            long maxActiveStreamsPerConnection = connectionThrottling.
                    getIntField(HttpConstants.CONNECTION_THROTTLING_MAX_ACTIVE_STREAMS_PER_CONNECTION);
            if (!isInteger(maxActiveStreamsPerConnection)) {
                throw new BallerinaConnectorException("invalid maxActiveStreamsPerConnection value: "
                                                      + maxActiveStreamsPerConnection);
            }
            senderConfiguration.getPoolConfiguration().setHttp2MaxActiveStreamsPerConnection(
                    maxActiveStreamsPerConnection == -1 ? Integer.MAX_VALUE : (int) maxActiveStreamsPerConnection);
        }
        HttpClientConnector httpClientConnector = httpConnectorFactory
                .createHttpClientConnector(properties, senderConfiguration);
        BMap<String, BValue> httpClient = BLangConnectorSPIUtil.createBStruct(context.getProgramFile(),
                HTTP_PACKAGE_PATH, CALLER_ACTIONS, urlString, clientEndpointConfig);
        httpClient.addNativeData(HttpConstants.CALLER_ACTIONS, httpClientConnector);
        httpClient.addNativeData(HttpConstants.CLIENT_ENDPOINT_CONFIG, clientEndpointConfig);
        context.setReturnValues(httpClient);
    }

    private void populateSenderConfigurationOptions(SenderConfiguration senderConfiguration, Struct
            clientEndpointConfig) {
        ProxyServerConfiguration proxyServerConfiguration = null;
        Struct secureSocket = clientEndpointConfig.getStructField(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);

        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(senderConfiguration, secureSocket);
        }
        Struct proxy = clientEndpointConfig.getStructField(HttpConstants.PROXY_STRUCT_REFERENCE);
        if (proxy != null) {
            String proxyHost = proxy.getStringField(HttpConstants.PROXY_HOST);
            int proxyPort = (int) proxy.getIntField(HttpConstants.PROXY_PORT);
            String proxyUserName = proxy.getStringField(HttpConstants.PROXY_USERNAME);
            String proxyPassword = proxy.getStringField(HttpConstants.PROXY_PASSWORD);
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

        String chunking = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_CHUNKING).getStringValue();
        senderConfiguration.setChunkingConfig(HttpUtil.getChunkConfig(chunking));

        long timeoutMillis = clientEndpointConfig.getIntField(HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT);
        if (timeoutMillis < 0 || !isInteger(timeoutMillis)) {
            throw new BallerinaConnectorException("invalid idle timeout: " + timeoutMillis);
        }
        senderConfiguration.setSocketIdleTimeout((int) timeoutMillis);

        String keepAliveConfig = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_IS_KEEP_ALIVE)
                .getStringValue();
        senderConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAliveConfig));

        String httpVersion = clientEndpointConfig.getStringField(HttpConstants.CLIENT_EP_HTTP_VERSION);
        if (httpVersion != null) {
            senderConfiguration.setHttpVersion(httpVersion);
        }
        String forwardedExtension = clientEndpointConfig.getStringField(HttpConstants.CLIENT_EP_FORWARDED);
        senderConfiguration.setForwardedExtensionConfig(HttpUtil.getForwardedExtensionConfig(forwardedExtension));
    }

    private boolean isInteger(long val) {
        return (int) val == val;
    }
}
