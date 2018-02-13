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
package org.ballerinalang.net.http.actions;


import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.common.ProxyServerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.HttpWsConnectorFactoryImpl;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code Init} is the Init action implementation of the HTTP Client Connector.
 *
 * @since 0.94.0-M1
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "<init>",
        connectorName = Constants.CONNECTOR_NAME,
        args = {@Argument(name = "c", type = TypeKind.CONNECTOR)
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                          structPackage = "ballerina.net.http")
        })
public class Init extends AbstractHTTPAction {

    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int DEFAULT_MAX_REDIRECT_COUNT = 5;

    private HttpWsConnectorFactory httpConnectorFactory = new HttpWsConnectorFactoryImpl();

    @Override
    public ConnectorFuture execute(Context context) {
        BConnector connector = (BConnector) getRefArgument(context, 0);
        String url = connector.getStringField(0);
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
            connector.setStringField(0, url);
        }

        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();

        String scheme;
        if (url.startsWith("http://")) {
            scheme = Constants.PROTOCOL_HTTP;
        } else if (url.startsWith("https://")) {
            scheme = Constants.PROTOCOL_HTTPS;
        } else {
            throw new BallerinaException("malformed URL: " + url);
        }

        Map<String, Object> properties =
                HTTPConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HTTPConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(Constants.PKCS_STORE_TYPE);

        BStruct options = (BStruct) connector.getRefField(Constants.OPTIONS_STRUCT_INDEX);
        if (options != null) {
            populateSenderConfigurationOptions(senderConfiguration, options);
            long maxActiveConnections = options.getIntField(Constants.MAX_ACTIVE_CONNECTIONS_INDEX);
            if (!isInteger(maxActiveConnections)) {
                throw new BallerinaConnectorException("invalid maxActiveConnections value: " + maxActiveConnections);
            }
            properties.put(Constants.MAX_ACTIVE_CONNECTIONS_PER_POOL, (int) maxActiveConnections);
        }

        HttpClientConnector httpClientConnector =
                httpConnectorFactory.createHttpClientConnector(properties, senderConfiguration);
        connector.setNativeData(Constants.CONNECTOR_NAME, httpClientConnector);

        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        ballerinaFuture.notifySuccess();

        return ballerinaFuture;
    }

    private void populateSenderConfigurationOptions(SenderConfiguration senderConfiguration, BStruct options) {
        //TODO Define default values until we get Anonymous struct (issues #3635)
        ProxyServerConfiguration proxyServerConfiguration = null;
        int followRedirect = FALSE;
        int maxRedirectCount = DEFAULT_MAX_REDIRECT_COUNT;
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
            boolean certificateRevocationVerifier =
                    ssl.getBooleanField(Constants.CERTIFICATE_REVOCATION_VERIFIER_INDEX) == TRUE;
            int cacheSize = (int) ssl.getIntField(Constants.CACHE_SIZE_INDEX);
            int cacheDelay = (int) ssl.getIntField(Constants.CACHE_DELAY_INDEX);

            if (certificateRevocationVerifier && cacheSize == 0) {
                throw new BallerinaConnectorException(
                        "cache size must be provided in order to enable certificate revocation verifier");
            }

            if (certificateRevocationVerifier && cacheDelay == 0) {
                throw new BallerinaConnectorException(
                        "cache delay must be provided in order to enable certificate revocation verifier");
            }

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
                senderConfiguration.setSSLProtocol(sslProtocol);
            }
            if (!clientParams.isEmpty()) {
                senderConfiguration.setParameters(clientParams);
            }
            if (certificateRevocationVerifier) {
                senderConfiguration.setCertificateRevocationVerifier(certificateRevocationVerifier);
                senderConfiguration.setCacheSize(cacheSize);
                senderConfiguration.setCacheDelay(cacheDelay);
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

        senderConfiguration.setFollowRedirect(followRedirect == TRUE);
        senderConfiguration.setMaxRedirectCount(maxRedirectCount);

        // For the moment we don't have to pass it down to transport as we only support
        // chunking. Once we start supporting gzip, deflate, etc, we need to parse down the config.
        String transferEncoding = options.getStringField(Constants.TRANSFER_ENCODING);
        if (transferEncoding != null && !Constants.ANN_CONFIG_ATTR_CHUNKING.equalsIgnoreCase(transferEncoding)) {
            throw new BallerinaConnectorException("Unsupported configuration found for Transfer-Encoding : "
                                                          + transferEncoding);
        }

        String chunking = options.getStringField(Constants.ENABLE_CHUNKING_INDEX);
        senderConfiguration.setChunkingConfig(HttpUtil.getChunkConfig(chunking));

        long endpointTimeout = options.getIntField(Constants.ENDPOINT_TIMEOUT_STRUCT_INDEX);
        if (endpointTimeout < 0 || !isInteger(endpointTimeout)) {
            throw new BallerinaConnectorException("invalid idle timeout: " + endpointTimeout);
        }
        senderConfiguration.setSocketIdleTimeout((int) endpointTimeout);

        boolean isKeepAlive = options.getBooleanField(Constants.IS_KEEP_ALIVE_INDEX) == TRUE;
        senderConfiguration.setKeepAlive(isKeepAlive);

        String httpVersion = options.getStringField(Constants.HTTP_VERSION_STRUCT_INDEX);
        senderConfiguration.setHttpVersion(httpVersion);
    }

    private boolean isInteger(long val) {
        return (int) val == val;
    }
}
