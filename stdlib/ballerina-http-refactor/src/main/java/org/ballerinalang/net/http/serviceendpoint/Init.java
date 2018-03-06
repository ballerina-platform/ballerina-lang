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

package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BEnumerator;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.ws.WebSocketConstants;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.contract.ServerConnector;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ServiceEndpoint",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration")},
        isPublic = true
)
public class Init extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            BStruct serviceEndpoint = (BStruct) getRefArgument(context, HttpConstants.SERVICE_ENDPOINT_INDEX);

            // Creating server connector
            BStruct serviceEndpointConfig =
                    (BStruct) serviceEndpoint.getRefField(HttpConstants.SERVICE_ENDPOINT_CONFIG_INDEX);
            ListenerConfiguration listenerConfiguration = getListerConfig(serviceEndpointConfig);
            ServerConnector httpServerConnector =
                    HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVER_CONNECTOR, httpServerConnector);

            //Adding service registries to native data
            WebSocketServicesRegistry webSocketServicesRegistry = new WebSocketServicesRegistry();
            HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(webSocketServicesRegistry);
            serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVICE_REGISTRY, httpServicesRegistry);
            serviceEndpoint.addNativeData(HttpConstants.WS_SERVICE_REGISTRY, webSocketServicesRegistry);

            return new BValue[]{null};
        } catch (Throwable throwable) {
            BStruct errorStruct = HttpUtil.getServerConnectorError(context, throwable);
            return new BValue[]{errorStruct};
        }

    }


    private ListenerConfiguration getListerConfig(BStruct endpointConfig) {
        String host = endpointConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_HOST_INDEX);
        long port = endpointConfig.getIntField(HttpConstants.ENDPOINT_CONFIG_PORT_INDEX);
        BEnumerator keepAlive =
                (BEnumerator) endpointConfig.getRefField(HttpConstants.ENDPOINT_CONFIG_KEEP_ALIVE_INDEX);
        String transferEncoding = endpointConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_TRANSFER_ENCODING_INDEX);
        BEnumerator chunking = (BEnumerator) endpointConfig.getRefField(HttpConstants.ENDPOINT_CONFIG_CHUNKING_INDEX);
        BStruct sslConfig = (BStruct) endpointConfig.getRefField(HttpConstants.ENDPOINT_CONFIG_SSL_INDEX);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();

        if (host == null) {
            listenerConfiguration.setHost(HttpConstants.HTTP_DEFAULT_HOST);
        } else {
            listenerConfiguration.setHost(host);
        }
        listenerConfiguration.setPort(Math.toIntExact(port));
        listenerConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAlive.stringValue()));

        // For the moment we don't have to pass it down to transport as we only support
        // chunking. Once we start supporting gzip, deflate, etc, we need to parse down the config.
        if (transferEncoding != null && !HttpConstants.ANN_CONFIG_ATTR_CHUNKING
                .equalsIgnoreCase(transferEncoding)) {
            throw new BallerinaConnectorException("Unsupported configuration found for Transfer-Encoding : "
                                                          + transferEncoding);
        }

        listenerConfiguration.setChunkConfig(HttpUtil.getChunkConfig(chunking.stringValue()));

        if (sslConfig != null) {
            return setSslConfig(sslConfig, listenerConfiguration);
        }
        return listenerConfiguration;
    }

    private ListenerConfiguration setSslConfig(BStruct sslConfig, ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(HttpConstants.PROTOCOL_HTTPS);
        String keyStoreFile = sslConfig.getStringField(HttpConstants.SSL_CONFIG_KEY_STORE_FILE_INDEX);
        String keyStorePassword = sslConfig.getStringField(HttpConstants.SSL_CONFIG_KEY_STORE_PASSWORD_INDEX);
        String trustStoreFile = sslConfig.getStringField(HttpConstants.SSL_CONFIG_STRUST_STORE_FILE_INDEX);
        String trustStorePassword = sslConfig.getStringField(HttpConstants.SSL_CONFIG_STRUST_STORE_PASSWORD_INDEX);
        String sslVerifyClient = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_VERIFY_CLIENT_INDEX);
        String certPassword = sslConfig.getStringField(HttpConstants.SSL_CONFIG_CERT_PASSWORD_INDEX);
        String sslEnabledProtocols = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_ENABLED_PROTOCOLS_INDEX);
        String cipher = sslConfig.getStringField(HttpConstants.SSL_CONFIG_CIPHERS_INDEX);
        String sslProtocol = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_PROTOCOL_INDEX);
        boolean validateCertificateEnabled =
                sslConfig.getBooleanField(HttpConstants.SSL_CONFIG_VALIDATE_CERT_ENABLED_INDEX) == 1;
        long cacheSize = sslConfig.getIntField(HttpConstants.SSL_CONFIG_CACHE_SIZE_INDEX);
        long cacheValidationPeriod = sslConfig.getIntField(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD_INDEX);

        if (keyStoreFile == null) {
            //TODO get from language pack, and add location
            throw new BallerinaConnectorException("Keystore location must be provided for secure connection");
        }
        if (keyStorePassword == null) {
            //TODO get from language pack, and add location
            throw new BallerinaConnectorException("Keystore password value must be provided for secure connection");
        }
        if (certPassword == null) {
            //TODO get from language pack, and add location
            throw new BallerinaConnectorException("Certificate password value must be provided for secure connection");
        }
        if ((trustStoreFile == null) && sslVerifyClient != null) {
            //TODO get from language pack, and add location
            throw new BallerinaException("Truststore location must be provided to enable Mutual SSL");
        }
        if ((trustStorePassword == null) && sslVerifyClient != null) {
            //TODO get from language pack, and add location
            throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
        }


        listenerConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);
        listenerConfiguration.setKeyStoreFile(keyStoreFile);
        listenerConfiguration.setKeyStorePass(keyStorePassword);
        listenerConfiguration.setCertPass(certPassword);

        listenerConfiguration.setVerifyClient(sslVerifyClient);
        listenerConfiguration.setTrustStoreFile(trustStoreFile);
        listenerConfiguration.setTrustStorePass(trustStorePassword);
        listenerConfiguration.setValidateCertEnabled(validateCertificateEnabled);
        if (validateCertificateEnabled) {
            listenerConfiguration.setCacheSize(Math.toIntExact(cacheSize));
            listenerConfiguration.setCacheValidityPeriod(Math.toIntExact(cacheValidationPeriod));
        }
        List<Parameter> serverParams = new ArrayList<>();
        Parameter serverCiphers;
        if (sslEnabledProtocols != null) {
            serverCiphers = new Parameter(HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
            serverParams.add(serverCiphers);
        }

        if (cipher != null) {
            serverCiphers = new Parameter(HttpConstants.ANN_CONFIG_ATTR_CIPHERS, cipher);
            serverParams.add(serverCiphers);
        }

        if (!serverParams.isEmpty()) {
            listenerConfiguration.setParameters(serverParams);
        }

        if (sslProtocol != null) {
            listenerConfiguration.setSSLProtocol(sslProtocol);
        }

        listenerConfiguration
                .setId(HttpUtil.getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));

        return listenerConfiguration;
    }
}
