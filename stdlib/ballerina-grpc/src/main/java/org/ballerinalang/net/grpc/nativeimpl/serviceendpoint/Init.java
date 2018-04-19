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

import io.netty.handler.ssl.SslContext;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.EndpointConstants;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.GrpcServicesBuilder;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.net.grpc.GrpcConstants.GRPC_DEFAULT_PORT;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_BUILDER;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_ENDPOINT_TYPE;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;

/**
 * Native function for initializing gRPC server endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ServiceEndpointConfiguration",
                        structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC)},
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {
    private static final ConfigRegistry configRegistry = ConfigRegistry.getInstance();
    
    @Override
    public void execute(Context context) {
        try {
            Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            Struct serviceEndpointConfig = serviceEndpoint.getStructField(EndpointConstants.ENDPOINT_CONFIG);
            ListenerConfiguration configuration = getListenerConfig(serviceEndpointConfig);
            io.grpc.ServerBuilder serverBuilder;
            if (configuration.getSSLConfig() != null) {
                SslContext sslCtx = new SSLHandlerFactory(configuration.getSSLConfig())
                        .createHttp2TLSContextForServer();
                serverBuilder = GrpcServicesBuilder.initService(configuration, sslCtx);
            } else {
                serverBuilder = GrpcServicesBuilder.initService(configuration, null);
            }
            serviceEndpoint.addNativeData(SERVICE_BUILDER, serverBuilder);
            context.setReturnValues();
        } catch (Throwable throwable) {
            BStruct err = getConnectorError(context, throwable);
            context.setError(err);
        }
    }
    
    private static BStruct getConnectorError(Context context, Throwable throwable) {
        return MessageUtils.getConnectorError(context, throwable);
    }
    
    private ListenerConfiguration getListenerConfig(Struct endpointConfig) {
        String host = endpointConfig.getStringField(GrpcConstants.ENDPOINT_CONFIG_HOST);
        long port = endpointConfig.getIntField(GrpcConstants.ENDPOINT_CONFIG_PORT);
        // TODO: 4/19/18 support implementation
//        String keepAlive = endpointConfig.getRefField(GrpcConstants.ENDPOINT_CONFIG_KEEP_ALIVE).getStringValue();
//        String transferEncoding =
//                endpointConfig.getRefField(GrpcConstants.ENDPOINT_CONFIG_TRANSFER_ENCODING).getStringValue();
//        Struct requestLimits = endpointConfig.getStructField(GrpcConstants.ENDPOINT_REQUEST_LIMITS);
        Struct sslConfig = endpointConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        
        if (host == null || host.trim().isEmpty()) {
            listenerConfiguration.setHost(
                    configRegistry.getConfigOrDefault("b7a.http.host", GrpcConstants.HTTP_DEFAULT_HOST));
        } else {
            listenerConfiguration.setHost(host);
        }
        
        if (port == GRPC_DEFAULT_PORT && configRegistry.contains("b7a.http.port")) {
            port = Long.parseLong(configRegistry.getAsString("b7a.http.port"));
        }
        listenerConfiguration.setPort(Math.toIntExact(port));
        
        if (sslConfig != null) {
            return setSslConfig(sslConfig, listenerConfiguration);
        }
        
        listenerConfiguration.setServerHeader(getServerName());
        
        return listenerConfiguration;
    }
    
    private ListenerConfiguration setSslConfig(Struct sslConfig, ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(GrpcConstants.PROTOCOL_HTTPS);
        Struct trustStore = sslConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_TRUST_STORE);
        Struct keyStore = sslConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_KEY_STORE);
        Struct protocols = sslConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_PROTOCOLS);
        Struct validateCert = sslConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_VALIDATE_CERT);
        Struct ocspStapling = sslConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_OCSP_STAPLING);
        
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringField(GrpcConstants.FILE_PATH);
            String keyStorePassword = keyStore.getStringField(GrpcConstants.PASSWORD);
            if (StringUtils.isBlank(keyStoreFile)) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException("Keystore location must be provided for secure connection");
            }
            if (StringUtils.isBlank(keyStorePassword)) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException("Keystore password value must be provided for secure connection");
            }
            listenerConfiguration.setKeyStoreFile(keyStoreFile);
            listenerConfiguration.setKeyStorePass(keyStorePassword);
        }
        String sslVerifyClient = sslConfig.getStringField(GrpcConstants.SSL_CONFIG_SSL_VERIFY_CLIENT);
        listenerConfiguration.setVerifyClient(sslVerifyClient);
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringField(GrpcConstants.FILE_PATH);
            String trustStorePassword = trustStore.getStringField(GrpcConstants.PASSWORD);
            if (StringUtils.isBlank(trustStoreFile) && StringUtils.isNotBlank(sslVerifyClient)) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Truststore location must be provided to enable Mutual SSL");
            }
            if (StringUtils.isBlank(trustStorePassword) && StringUtils.isNotBlank(sslVerifyClient)) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
            }
            listenerConfiguration.setTrustStoreFile(trustStoreFile);
            listenerConfiguration.setTrustStorePass(trustStorePassword);
        }
        List<Parameter> serverParamList = new ArrayList<>();
        Parameter serverParameters;
        if (protocols != null) {
            List<Value> sslEnabledProtocolsValueList = Arrays
                    .asList(protocols.getArrayField(GrpcConstants.ENABLED_PROTOCOLS));
            if (sslEnabledProtocolsValueList.size() > 0) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Value::getStringValue)
                        .collect(Collectors.joining(",", "", ""));
                serverParameters = new Parameter(GrpcConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS,
                        sslEnabledProtocols);
                serverParamList.add(serverParameters);
            }
            
            String sslProtocol = protocols.getStringField(GrpcConstants.PROTOCOL_VERSION);
            if (StringUtils.isNotBlank(sslProtocol)) {
                listenerConfiguration.setSSLProtocol(sslProtocol);
            }
        }
        
        List<Value> ciphersValueList = Arrays.asList(sslConfig.getArrayField(GrpcConstants.SSL_CONFIG_CIPHERS));
        if (ciphersValueList.size() > 0) {
            String ciphers = ciphersValueList.stream().map(Value::getStringValue)
                    .collect(Collectors.joining(",", "", ""));
            serverParameters = new Parameter(GrpcConstants.CIPHERS, ciphers);
            serverParamList.add(serverParameters);
        }
        if (validateCert != null) {
            boolean validateCertificateEnabled = validateCert.getBooleanField(GrpcConstants.ENABLE);
            long cacheSize = validateCert.getIntField(GrpcConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = validateCert.getIntField(GrpcConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
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
            boolean ocspStaplingEnabled = ocspStapling.getBooleanField(GrpcConstants.ENABLE);
            listenerConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            long cacheSize = ocspStapling.getIntField(GrpcConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = ocspStapling.getIntField(GrpcConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
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
                .getBooleanField(GrpcConstants.SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter enableSessionCreationParam = new Parameter(GrpcConstants.SSL_CONFIG_ENABLE_SESSION_CREATION,
                serverEnableSessionCreation);
        serverParamList.add(enableSessionCreationParam);
        if (!serverParamList.isEmpty()) {
            listenerConfiguration.setParameters(serverParamList);
        }
        
        listenerConfiguration
                .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
        
        return listenerConfiguration;
    }
    
    public static String getListenerInterface(String host, int port) {
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
