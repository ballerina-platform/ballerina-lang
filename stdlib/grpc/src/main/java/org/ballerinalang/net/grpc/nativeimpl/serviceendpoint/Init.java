/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.serviceendpoint;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.config.ConfigRegistry;
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
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.contract.ServerConnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVER_CONNECTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_REGISTRY_BUILDER;
import static org.ballerinalang.net.http.HttpConstants.ENABLE;
import static org.ballerinalang.net.http.HttpConstants.ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_VALIDATE_CERT;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_VERSION;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_CACHE_SIZE;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_CIPHERS;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;

/**
 * Extern function for initializing gRPC server endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = SERVICE_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "config", type = TypeKind.RECORD, structType = "ServiceEndpointConfiguration",
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC)},
        isPublic = true
)
public class Init extends AbstractGrpcNativeFunction {
    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();
    
    @Override
    public void execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            BMap<String, BValue> endpointConfigStruct = (BMap<String, BValue>) context.getRefArgument(1);
            Struct serviceEndpointConfig = BLangConnectorSPIUtil.toStruct(endpointConfigStruct);
            ListenerConfiguration configuration = getListenerConfig(serviceEndpointConfig);
            ServerConnector httpServerConnector =
                    HttpConnectionManager.getInstance().createHttpServerConnector(configuration);

            ServicesRegistry.Builder grpcServicesRegistryBuilder = new ServicesRegistry.Builder();
            serviceEndpoint.addNativeData(SERVER_CONNECTOR, httpServerConnector);
            serviceEndpoint.addNativeData(SERVICE_REGISTRY_BUILDER, grpcServicesRegistryBuilder);
            context.setReturnValues();
        } catch (Exception ex) {
            BMap<String, BValue> errorStruct = MessageUtils.getConnectorError(context, ex);
            context.setReturnValues(errorStruct);
        }
    }
    
    private ListenerConfiguration getListenerConfig(Struct endpointConfig) {
        String host = endpointConfig.getStringField(GrpcConstants.ENDPOINT_CONFIG_HOST);
        long port = endpointConfig.getIntField(GrpcConstants.ENDPOINT_CONFIG_PORT);
        Struct sslConfig = endpointConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        
        if (host == null || host.trim().isEmpty()) {
            listenerConfiguration.setHost(
                    configRegistry.getConfigOrDefault("b7a.http.host", GrpcConstants.HTTP_DEFAULT_HOST));
        } else {
            listenerConfiguration.setHost(host);
        }

        if (port == 0) {
            throw new BallerinaConnectorException("Listener port is not defined!");
        }
        listenerConfiguration.setPort(Math.toIntExact(port));
        
        if (sslConfig != null) {
            setSslConfig(sslConfig, listenerConfiguration);
        }
        
        listenerConfiguration.setServerHeader(getServerName());
        listenerConfiguration.setVersion(String.valueOf(Constants.HTTP_2_0));

        return listenerConfiguration;
    }
    
    private void setSslConfig(Struct sslConfig, ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(GrpcConstants.PROTOCOL_HTTPS);
        Struct trustStore = sslConfig.getStructField(ENDPOINT_CONFIG_TRUST_STORE);
        Struct keyStore = sslConfig.getStructField(ENDPOINT_CONFIG_KEY_STORE);
        Struct protocols = sslConfig.getStructField(ENDPOINT_CONFIG_PROTOCOLS);
        Struct validateCert = sslConfig.getStructField(ENDPOINT_CONFIG_VALIDATE_CERT);
        Struct ocspStapling = sslConfig.getStructField(ENDPOINT_CONFIG_OCSP_STAPLING);
        String keyFile = sslConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_KEY);
        String certFile = sslConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_CERTIFICATE);
        String trustCerts = sslConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_TRUST_CERTIFICATES);
        String keyPassword = sslConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_KEY_PASSWORD);


        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw new BallerinaException("Cannot configure both keyStore and keyFile at the same time.");
        } else if (keyStore == null && (StringUtils.isBlank(keyFile) || StringUtils.isBlank(certFile))) {
            throw new BallerinaException("Either keystore or certificateKey and server certificates must be provided "
                    + "for secure connection");
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringField(HttpConstants.FILE_PATH);
            String keyStorePassword = keyStore.getStringField(HttpConstants.PASSWORD);
            listenerConfiguration.setKeyStoreFile(keyStoreFile);
            listenerConfiguration.setKeyStorePass(keyStorePassword);
        } else {
            listenerConfiguration.setServerKeyFile(keyFile);
            listenerConfiguration.setServerCertificates(certFile);
            if (keyPassword != null) {
                listenerConfiguration.setServerKeyPassword(keyPassword);
            }
        }
        String sslVerifyClient = sslConfig.getStringField(HttpConstants.SSL_CONFIG_SSL_VERIFY_CLIENT);
        listenerConfiguration.setVerifyClient(sslVerifyClient);
        if (trustStore == null && StringUtils.isNotBlank(sslVerifyClient) && StringUtils.isBlank(trustCerts)) {
            throw new BallerinaException(
                    "Truststore location or trustCertificates must be provided to enable Mutual SSL");
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringField(HttpConstants.FILE_PATH);
            String trustStorePassword = trustStore.getStringField(HttpConstants.PASSWORD);
            if (StringUtils.isBlank(trustStoreFile) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw new BallerinaException(
                        "Truststore location must be provided to enable Mutual SSL");
            }
            if (StringUtils.isBlank(trustStorePassword) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
            }
            listenerConfiguration.setTrustStoreFile(trustStoreFile);
            listenerConfiguration.setTrustStorePass(trustStorePassword);
        } else if (StringUtils.isNotBlank(trustCerts)) {
            listenerConfiguration.setServerTrustCertificates(trustCerts);
        }
        List<Parameter> serverParamList = new ArrayList<>();
        Parameter serverParameters;
        if (protocols != null) {
            List<Value> sslEnabledProtocolsValueList = Arrays
                    .asList(protocols.getArrayField(ENABLED_PROTOCOLS));
            if (!sslEnabledProtocolsValueList.isEmpty()) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Value::getStringValue)
                        .collect(Collectors.joining(",", "", ""));
                serverParameters = new Parameter(GrpcConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS,
                        sslEnabledProtocols);
                serverParamList.add(serverParameters);
            }
            
            String sslProtocol = protocols.getStringField(PROTOCOL_VERSION);
            if (StringUtils.isNotBlank(sslProtocol)) {
                listenerConfiguration.setSSLProtocol(sslProtocol);
            }
        }
        
        List<Value> ciphersValueList = Arrays.asList(sslConfig.getArrayField(SSL_CONFIG_CIPHERS));
        if (!ciphersValueList.isEmpty()) {
            String ciphers = ciphersValueList.stream().map(Value::getStringValue)
                    .collect(Collectors.joining(",", "", ""));
            serverParameters = new Parameter(GrpcConstants.CIPHERS, ciphers);
            serverParamList.add(serverParameters);
        }
        if (validateCert != null) {
            boolean validateCertificateEnabled = validateCert.getBooleanField(ENABLE);
            long cacheSize = validateCert.getIntField(SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = validateCert.getIntField(SSL_CONFIG_CACHE_VALIDITY_PERIOD);
            listenerConfiguration.setValidateCertEnabled(validateCertificateEnabled);
            if (validateCertificateEnabled) {
                if (cacheSize != 0) {
                    listenerConfiguration.setCacheSize(Math.toIntExact(cacheSize));
                }
                if (cacheValidationPeriod != 0) {
                    listenerConfiguration.setCacheValidityPeriod(Math.toIntExact(cacheValidationPeriod));
                }
            }
        }
        if (ocspStapling != null) {
            boolean ocspStaplingEnabled = ocspStapling.getBooleanField(ENABLE);
            listenerConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            long cacheSize = ocspStapling.getIntField(SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = ocspStapling.getIntField(SSL_CONFIG_CACHE_VALIDITY_PERIOD);
            listenerConfiguration.setValidateCertEnabled(ocspStaplingEnabled);
            if (ocspStaplingEnabled) {
                if (cacheSize != 0) {
                    listenerConfiguration.setCacheSize(Math.toIntExact(cacheSize));
                }
                if (cacheValidationPeriod != 0) {
                    listenerConfiguration.setCacheValidityPeriod(Math.toIntExact(cacheValidationPeriod));
                }
            }
        }
        listenerConfiguration.setTLSStoreType(GrpcConstants.PKCS_STORE_TYPE);
        String serverEnableSessionCreation = String.valueOf(sslConfig
                .getBooleanField(SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter enableSessionCreationParam = new Parameter(SSL_CONFIG_ENABLE_SESSION_CREATION,
                serverEnableSessionCreation);
        serverParamList.add(enableSessionCreationParam);
        if (!serverParamList.isEmpty()) {
            listenerConfiguration.setParameters(serverParamList);
        }
        
        listenerConfiguration
                .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
    }
    
    private static String getListenerInterface(String host, int port) {
        host = host != null ? host : "0.0.0.0";
        return host + ":" + port;
    }
    
    private String getServerName() {
        String userAgent;
        String version = System.getProperty(BALLERINA_VERSION);
        if (version != null) {
            userAgent = "ballerina/" + version;
        } else {
            userAgent = "ballerina";
        }
        return userAgent;
    }
}
