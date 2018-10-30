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
package org.ballerinalang.net.grpc.nativeimpl.clientendpoint;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.message.HttpConnectorUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ACTIONS;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.http.HttpConstants.ENABLE;
import static org.ballerinalang.net.http.HttpConstants.ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_CERTIFICATE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_CERTIFICATES;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_VALIDATE_CERT;
import static org.ballerinalang.net.http.HttpConstants.FILE_PATH;
import static org.ballerinalang.net.http.HttpConstants.PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_VERSION;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_CACHE_SIZE;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_CIPHERS;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED;

/**
 * Extern function for initializing gRPC client endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CLIENT_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "config", type = TypeKind.RECORD, structType = "ClientEndpointConfig")},
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {

    private HttpWsConnectorFactory httpConnectorFactory = MessageUtils.createHttpWsConnectionFactory();

    @Override
    public void execute(Context context) {
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        // Creating client endpoint with channel as native data.
        BMap<String, BValue> endpointConfigStruct = (BMap<String, BValue>) context.getRefArgument(1);
        Struct endpointConfig = BLangConnectorSPIUtil.toStruct(endpointConfigStruct);
        String urlString = endpointConfig.getStringField(GrpcConstants.CLIENT_ENDPOINT_URL);
        HttpConnectionManager connectionManager = HttpConnectionManager.getInstance();
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new BallerinaConnectorException("Malformed URL: " + urlString);
        }

        String scheme = url.getProtocol();
        Map<String, Object> properties =
                HttpConnectorUtil.getTransportProperties(connectionManager.getTransportConfig());
        SenderConfiguration senderConfiguration =
                HttpConnectorUtil.getSenderConfiguration(connectionManager.getTransportConfig(), scheme);

        if (connectionManager.isHTTPTraceLoggerEnabled()) {
            senderConfiguration.setHttpTraceLogEnabled(true);
        }
        senderConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);

        senderConfiguration = populateSenderConfigurationOptions(endpointConfig, scheme);
        senderConfiguration.setHttpVersion(String.valueOf(Constants.HTTP_2_0));
        senderConfiguration.setForceHttp2(true);
        HttpClientConnector clientConnector = httpConnectorFactory.createHttpClientConnector(properties,
                senderConfiguration);

        clientEndpoint.addNativeData(CALLER_ACTIONS, clientConnector);
        clientEndpoint.addNativeData(CLIENT_ENDPOINT_CONFIG, endpointConfig);

    }

    private SenderConfiguration populateSenderConfigurationOptions(Struct clientEndpointConfig, String scheme) {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setScheme(scheme);
        Struct secureSocket = clientEndpointConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            Struct trustStore = secureSocket.getStructField(ENDPOINT_CONFIG_TRUST_STORE);
            Struct keyStore = secureSocket.getStructField(ENDPOINT_CONFIG_KEY_STORE);
            Struct protocols = secureSocket.getStructField(ENDPOINT_CONFIG_PROTOCOLS);
            Struct validateCert = secureSocket.getStructField(ENDPOINT_CONFIG_VALIDATE_CERT);
            String keyFile = secureSocket.getStringField(ENDPOINT_CONFIG_KEY);
            String certFile = secureSocket.getStringField(ENDPOINT_CONFIG_CERTIFICATE);
            String trustCerts = secureSocket.getStringField(ENDPOINT_CONFIG_TRUST_CERTIFICATES);
            String keyPassword = secureSocket.getStringField(ENDPOINT_CONFIG_KEY_PASSWORD);
            List<Parameter> clientParams = new ArrayList<>();
            if (trustStore != null && StringUtils.isNotBlank(trustCerts)) {
                throw new BallerinaException("Cannot configure both trustStore and trustCerts at the same time.");
            }
            if (trustStore != null) {
                String trustStoreFile = trustStore.getStringField(FILE_PATH);
                if (StringUtils.isNotBlank(trustStoreFile)) {
                    senderConfiguration.setTrustStoreFile(trustStoreFile);
                }
                String trustStorePassword = trustStore.getStringField(PASSWORD);
                if (StringUtils.isNotBlank(trustStorePassword)) {
                    senderConfiguration.setTrustStorePass(trustStorePassword);
                }
            } else if (StringUtils.isNotBlank(trustCerts)) {
                senderConfiguration.setClientTrustCertificates(trustCerts);
            }
            if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
                throw new BallerinaException("Cannot configure both keyStore and keyFile.");
            } else if (StringUtils.isNotBlank(keyFile) && StringUtils.isBlank(certFile)) {
                throw new BallerinaException("Need to configure certFile containing client ssl certificates.");
            }
            if (keyStore != null) {
                String keyStoreFile = keyStore.getStringField(FILE_PATH);
                if (StringUtils.isNotBlank(keyStoreFile)) {
                    senderConfiguration.setKeyStoreFile(keyStoreFile);
                }
                String keyStorePassword = keyStore.getStringField(PASSWORD);
                if (StringUtils.isNotBlank(keyStorePassword)) {
                    senderConfiguration.setKeyStorePass(keyStorePassword);
                }
            } else if (StringUtils.isNotBlank(keyFile)) {
                senderConfiguration.setClientKeyFile(keyFile);
                senderConfiguration.setClientCertificates(certFile);
                if (StringUtils.isNotBlank(keyPassword)) {
                    senderConfiguration.setClientKeyPassword(keyPassword);
                }
            }
            senderConfiguration.setTLSStoreType(GrpcConstants.PKCS_STORE_TYPE);
            if (protocols != null) {
                List<Value> sslEnabledProtocolsValueList = Arrays
                        .asList(protocols.getArrayField(ENABLED_PROTOCOLS));
                if (!sslEnabledProtocolsValueList.isEmpty()) {
                    String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Value::getStringValue)
                            .collect(Collectors.joining(",", "", ""));
                    Parameter clientProtocols = new Parameter(GrpcConstants.SSL_ENABLED_PROTOCOLS,
                            sslEnabledProtocols);
                    clientParams.add(clientProtocols);
                }
                String sslProtocol = protocols.getStringField(PROTOCOL_VERSION);
                if (StringUtils.isNotBlank(sslProtocol)) {
                    senderConfiguration.setSSLProtocol(sslProtocol);
                }
            }

            if (validateCert != null) {
                boolean validateCertEnabled = validateCert.getBooleanField(ENABLE);
                int cacheSize = (int) validateCert.getIntField(SSL_CONFIG_CACHE_SIZE);
                int cacheValidityPeriod = (int) validateCert
                        .getIntField(SSL_CONFIG_CACHE_VALIDITY_PERIOD);
                senderConfiguration.setValidateCertEnabled(validateCertEnabled);
                if (cacheValidityPeriod != 0) {
                    senderConfiguration.setCacheValidityPeriod(cacheValidityPeriod);
                }
                if (cacheSize != 0) {
                    senderConfiguration.setCacheSize(cacheSize);
                }
            }
            boolean hostNameVerificationEnabled = secureSocket
                    .getBooleanField(SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED);
            boolean ocspStaplingEnabled = secureSocket.getBooleanField(ENDPOINT_CONFIG_OCSP_STAPLING);
            senderConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            senderConfiguration.setHostNameVerificationEnabled(hostNameVerificationEnabled);

            List<Value> ciphersValueList = Arrays
                    .asList(secureSocket.getArrayField(SSL_CONFIG_CIPHERS));
            if (!ciphersValueList.isEmpty()) {
                String ciphers = ciphersValueList.stream().map(Value::getStringValue)
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientCiphers = new Parameter(GrpcConstants.CIPHERS, ciphers);
                clientParams.add(clientCiphers);
            }
            String enableSessionCreation = String.valueOf(secureSocket
                    .getBooleanField(SSL_CONFIG_ENABLE_SESSION_CREATION));
            Parameter clientEnableSessionCreation = new Parameter(SSL_CONFIG_ENABLE_SESSION_CREATION,
                    enableSessionCreation);
            clientParams.add(clientEnableSessionCreation);
            if (!clientParams.isEmpty()) {
                senderConfiguration.setParameters(clientParams);
            }
        }
        return senderConfiguration;
    }
}
