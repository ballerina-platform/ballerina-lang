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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfig;
import org.wso2.transport.http.netty.contract.ServerConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "initEndpoint",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Service",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "epName", type = TypeKind.STRING),
                @Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration")},
        isPublic = true
)
public class InitEndpoint extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);

            // Creating server connector
            Struct serviceEndpointConfig = serviceEndpoint.getStructField(HttpConstants.SERVICE_ENDPOINT_CONFIG);
            ListenerConfiguration listenerConfiguration = getListerConfig(serviceEndpointConfig);
            ServerConnector httpServerConnector =
                    HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVER_CONNECTOR, httpServerConnector);

            //Adding service registries to native data
            WebSocketServicesRegistry webSocketServicesRegistry = new WebSocketServicesRegistry();
            HTTPServicesRegistry httpServicesRegistry = new HTTPServicesRegistry(webSocketServicesRegistry);
            serviceEndpoint.addNativeData(HttpConstants.HTTP_SERVICE_REGISTRY, httpServicesRegistry);
            serviceEndpoint.addNativeData(HttpConstants.WS_SERVICE_REGISTRY, webSocketServicesRegistry);

            context.setReturnValues((BValue) null);
        } catch (Throwable throwable) {
            BStruct errorStruct = HttpUtil.getHttpConnectorError(context, throwable);
            context.setReturnValues(errorStruct);
        }

    }


    private ListenerConfiguration getListerConfig(Struct endpointConfig) {
        String host = endpointConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_HOST);
        long port = endpointConfig.getIntField(HttpConstants.ENDPOINT_CONFIG_PORT);
        String keepAlive = endpointConfig.getEnumField(HttpConstants.ENDPOINT_CONFIG_KEEP_ALIVE);
        String transferEncoding = endpointConfig.getEnumField(HttpConstants.ENDPOINT_CONFIG_TRANSFER_ENCODING);
        String chunking = endpointConfig.getEnumField(HttpConstants.ENDPOINT_CONFIG_CHUNKING);
        Struct sslConfig = endpointConfig.getStructField(HttpConstants.ENDPOINT_CONFIG_SSL);
        String httpVersion = endpointConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_VERSION);
        Struct requestLimits = endpointConfig.getStructField(HttpConstants.ENDPOINT_REQUEST_LIMITS);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();

        if (host == null || host.isEmpty()) {
            listenerConfiguration.setHost(HttpConstants.HTTP_DEFAULT_HOST);
        } else {
            listenerConfiguration.setHost(host);
        }
        listenerConfiguration.setPort(Math.toIntExact(port));
        listenerConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAlive));

        // For the moment we don't have to pass it down to transport as we only support
        // chunking. Once we start supporting gzip, deflate, etc, we need to parse down the config.
        if ((!transferEncoding.isEmpty()) && !HttpConstants.ANN_CONFIG_ATTR_CHUNKING
                .equalsIgnoreCase(transferEncoding)) {
            throw new BallerinaConnectorException("Unsupported configuration found for Transfer-Encoding : "
                                                          + transferEncoding);
        }

        listenerConfiguration.setChunkConfig(HttpUtil.getChunkConfig(chunking));

        if (sslConfig != null) {
            return setSslConfig(sslConfig, listenerConfiguration);
        }

        // Set Request validation limits.
        if (requestLimits != null) {
            setRequestSizeValidationConfig(requestLimits, listenerConfiguration);
        }

        // Set HTTP version
        if (httpVersion != null) {
            listenerConfiguration.setVersion(httpVersion);
        }
        return listenerConfiguration;
    }

    private void setRequestSizeValidationConfig(Struct requestLimits, ListenerConfiguration listenerConfiguration) {
        long maxUriLength = requestLimits.getIntField(HttpConstants.REQUEST_LIMITS_MAXIMUM_URL_LENGTH);
        long maxHeaderSize = requestLimits.getIntField(HttpConstants.REQUEST_LIMITS_MAXIMUM_HEADER_SIZE);
        long maxEntityBodySize = requestLimits.getIntField(HttpConstants.REQUEST_LIMITS_MAXIMUM_ENTITY_BODY_SIZE);
        RequestSizeValidationConfig requestSizeValidationConfig = listenerConfiguration
                .getRequestSizeValidationConfig();

        if (maxUriLength != -1) {
            if (maxUriLength >= 0) {
                requestSizeValidationConfig.setMaxUriLength(Math.toIntExact(maxUriLength));
            } else {
                throw new BallerinaConnectorException("Invalid configuration found for maxUriLength : " + maxUriLength);
            }
        }

        if (maxHeaderSize != -1) {
            if (maxHeaderSize >= 0) {
                requestSizeValidationConfig.setMaxHeaderSize(Math.toIntExact(maxHeaderSize));
            } else {
                throw new BallerinaConnectorException(
                        "Invalid configuration found for maxHeaderSize : " + maxHeaderSize);
            }
        }

        if (maxEntityBodySize != -1) {
            if (maxEntityBodySize >= 0) {
                requestSizeValidationConfig.setMaxEntityBodySize(Math.toIntExact(maxEntityBodySize));
            } else {
                throw new BallerinaConnectorException(
                        "Invalid configuration found for maxEntityBodySize : " + maxEntityBodySize);
            }
        }
    }

    private ListenerConfiguration setSslConfig(Struct sslConfig, ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(HttpConstants.PROTOCOL_HTTPS);
        String keyStoreFile = sslConfig.getStringField(HttpConstants.SSL_CONFIG_KEY_STORE_FILE);
        String keyStorePassword = sslConfig.getStringField(HttpConstants.SSL_CONFIG_KEY_STORE_PASSWORD);
        String trustStoreFile = sslConfig.getStringField(HttpConstants.SSL_CONFIG_STRUST_STORE_FILE);
        String trustStorePassword = sslConfig.getStringField(HttpConstants.SSL_CONFIG_STRUST_STORE_PASSWORD);
        String sslVerifyClient = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_VERIFY_CLIENT);
        String certPassword = sslConfig.getStringField(HttpConstants.SSL_CONFIG_CERT_PASSWORD);
        String sslEnabledProtocols = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_ENABLED_PROTOCOLS);
        String cipher = sslConfig.getStringField(HttpConstants.SSL_CONFIG_CIPHERS);
        String sslProtocol = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_PROTOCOL);
        boolean validateCertificateEnabled = sslConfig.getBooleanField(HttpConstants.SSL_CONFIG_VALIDATE_CERT_ENABLED);
        long cacheSize = sslConfig.getIntField(HttpConstants.SSL_CONFIG_CACHE_SIZE);
        long cacheValidationPeriod = sslConfig.getIntField(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);

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
