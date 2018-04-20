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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.ssl.SSLHandlerFactory;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.SenderConfiguration;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.net.grpc.GrpcConstants.CHANNEL_KEY;
import static org.ballerinalang.net.grpc.GrpcConstants.CLIENT_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.DEFAULT_HOSTNAME;
import static org.ballerinalang.net.grpc.GrpcConstants.MAX_MESSAGE_SIZE;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;


/**
 * Native function for initializing gRPC client endpoint.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "init",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = CLIENT_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        args = {@Argument(name = "config", type = TypeKind.STRUCT, structType = "ClientEndpointConfig")},
        isPublic = true
)
public class Init extends BlockingNativeCallableUnit {
    
    @Override
    public void execute(Context context) {
        try {
            Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
            // Creating client endpoint with channel as native data.
            BStruct endpointConfigStruct = (BStruct) context.getRefArgument(1);
            Struct endpointConfig = BLangConnectorSPIUtil.toStruct(endpointConfigStruct);
            String urlString = endpointConfig.getStringField(GrpcConstants.CLIENT_ENDPOINT_URL);
            String scheme;
            URL url;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                throw new BallerinaException("Malformed URL: " + urlString);
            }
            scheme = url.getProtocol();
            SenderConfiguration configuration = populateSenderConfigurationOptions(endpointConfig, scheme);
            ManagedChannel channel;
            if (configuration.getSSLConfig() == null) {
                channel = ManagedChannelBuilder.forAddress(url.getHost(), url.getPort())
                        .usePlaintext(true)
                        .build();
            } else {
                SslContext sslContext = new SSLHandlerFactory(configuration.getSSLConfig())
                        .createHttp2TLSContextForClient();
                channel = NettyChannelBuilder
                        .forAddress(generateSocketAddress(url.getHost(), url.getPort()))
                        .flowControlWindow(65 * 1024)
                        .maxInboundMessageSize(MAX_MESSAGE_SIZE)
                        .sslContext(sslContext).build();
            }
            clientEndpoint.addNativeData(CHANNEL_KEY, channel);
            
        } catch (Throwable throwable) {
            BStruct errorStruct = MessageUtils.getConnectorError(context, throwable);
            context.setError(errorStruct);
        }
        
    }
    
    /**
     * Creates a new {@link InetSocketAddress} on localhost that overrides the host with.
     */
    private static InetSocketAddress generateSocketAddress(String host, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(DEFAULT_HOSTNAME);
            inetAddress = InetAddress.getByAddress(host, inetAddress.getAddress());
            return new InetSocketAddress(inetAddress, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    
    private SenderConfiguration populateSenderConfigurationOptions(Struct clientEndpointConfig, String scheme) {
        SenderConfiguration senderConfiguration = new SenderConfiguration();
        senderConfiguration.setScheme(scheme);
        Struct secureSocket = clientEndpointConfig.getStructField(GrpcConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            Struct trustStore = secureSocket.getStructField(GrpcConstants.ENDPOINT_CONFIG_TRUST_STORE);
            Struct keyStore = secureSocket.getStructField(GrpcConstants.ENDPOINT_CONFIG_KEY_STORE);
            Struct protocols = secureSocket.getStructField(GrpcConstants.ENDPOINT_CONFIG_PROTOCOLS);
            Struct validateCert = secureSocket.getStructField(GrpcConstants.ENDPOINT_CONFIG_VALIDATE_CERT);
            List<Parameter> clientParams = new ArrayList<>();
            if (trustStore != null) {
                String trustStoreFile = trustStore.getStringField(GrpcConstants.FILE_PATH);
                if (StringUtils.isNotBlank(trustStoreFile)) {
                    senderConfiguration.setTrustStoreFile(trustStoreFile);
                }
                String trustStorePassword = trustStore.getStringField(GrpcConstants.PASSWORD);
                if (StringUtils.isNotBlank(trustStorePassword)) {
                    senderConfiguration.setTrustStorePass(trustStorePassword);
                }
            }
            if (keyStore != null) {
                String keyStoreFile = keyStore.getStringField(GrpcConstants.FILE_PATH);
                if (StringUtils.isNotBlank(keyStoreFile)) {
                    senderConfiguration.setKeyStoreFile(keyStoreFile);
                }
                String keyStorePassword = keyStore.getStringField(GrpcConstants.PASSWORD);
                if (StringUtils.isNotBlank(keyStorePassword)) {
                    senderConfiguration.setKeyStorePassword(keyStorePassword);
                }
            }
            if (protocols != null) {
                List<Value> sslEnabledProtocolsValueList = Arrays
                        .asList(protocols.getArrayField(GrpcConstants.ENABLED_PROTOCOLS));
                if (sslEnabledProtocolsValueList.size() > 0) {
                    String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Value::getStringValue)
                            .collect(Collectors.joining(",", "", ""));
                    Parameter clientProtocols = new Parameter(GrpcConstants.SSL_ENABLED_PROTOCOLS,
                            sslEnabledProtocols);
                    clientParams.add(clientProtocols);
                }
                String sslProtocol = protocols.getStringField(GrpcConstants.PROTOCOL_VERSION);
                if (StringUtils.isNotBlank(sslProtocol)) {
                    senderConfiguration.setSSLProtocol(sslProtocol);
                }
            }
            
            if (validateCert != null) {
                boolean validateCertEnabled = validateCert.getBooleanField(GrpcConstants.ENABLE);
                int cacheSize = (int) validateCert.getIntField(GrpcConstants.SSL_CONFIG_CACHE_SIZE);
                int cacheValidityPeriod = (int) validateCert
                        .getIntField(GrpcConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
                senderConfiguration.setValidateCertEnabled(validateCertEnabled);
                if (cacheValidityPeriod != 0) {
                    senderConfiguration.setCacheValidityPeriod(cacheValidityPeriod);
                }
                if (cacheSize != 0) {
                    senderConfiguration.setCacheSize(cacheSize);
                }
            }
            boolean hostNameVerificationEnabled = secureSocket
                    .getBooleanField(GrpcConstants.SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED);
            boolean ocspStaplingEnabled = secureSocket.getBooleanField(GrpcConstants.ENDPOINT_CONFIG_OCSP_STAPLING);
            senderConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            senderConfiguration.setHostNameVerificationEnabled(hostNameVerificationEnabled);
            
            List<Value> ciphersValueList = Arrays
                    .asList(secureSocket.getArrayField(GrpcConstants.SSL_CONFIG_CIPHERS));
            if (ciphersValueList.size() > 0) {
                String ciphers = ciphersValueList.stream().map(Value::getStringValue)
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientCiphers = new Parameter(GrpcConstants.CIPHERS, ciphers);
                clientParams.add(clientCiphers);
            }
            String enableSessionCreation = String.valueOf(secureSocket
                    .getBooleanField(GrpcConstants.SSL_CONFIG_ENABLE_SESSION_CREATION));
            Parameter clientEnableSessionCreation = new Parameter(GrpcConstants.SSL_CONFIG_ENABLE_SESSION_CREATION,
                    enableSessionCreation);
            clientParams.add(clientEnableSessionCreation);
            if (!clientParams.isEmpty()) {
                senderConfiguration.setParameters(clientParams);
            }
        }
        // TODO: 4/20/18 Proxy support
        return senderConfiguration;
    }
    
}
