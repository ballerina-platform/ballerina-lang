/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.grpc;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.SslConfiguration;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.jvm.runtime.RuntimeConstants.BALLERINA_VERSION;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION;
import static org.ballerinalang.net.http.HttpConstants.ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_CERTIFICATE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_DISABLE_SSL;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_SESSION_TIMEOUT;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_CERTIFICATES;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_VALIDATE_CERT;
import static org.ballerinalang.net.http.HttpConstants.FILE_PATH;
import static org.ballerinalang.net.http.HttpConstants.LISTENER_CONFIGURATION;
import static org.ballerinalang.net.http.HttpConstants.PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.PKCS_STORE_TYPE;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTPS;
import static org.ballerinalang.net.http.HttpConstants.SERVER_NAME;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_SSL_VERIFY_CLIENT;
import static org.ballerinalang.net.http.HttpConstants.SSL_PROTOCOL_VERSION;

/**
 * Utility class providing utility methods for gRPC listener and client endpoint.
 *
 * @since 1.0.0
 */
public class GrpcUtil {

    private static final Logger log = LoggerFactory.getLogger(GrpcUtil.class);

    public static ConnectionManager getConnectionManager(MapValue<BString, Long> poolStruct) {
        ConnectionManager poolManager = (ConnectionManager) poolStruct.getNativeData(CONNECTION_MANAGER);
        if (poolManager == null) {
            synchronized (poolStruct) {
                if (poolStruct.getNativeData(CONNECTION_MANAGER) == null) {
                    PoolConfiguration userDefinedPool = new PoolConfiguration();
                    populatePoolingConfig(poolStruct, userDefinedPool);
                    poolManager = new ConnectionManager(userDefinedPool);
                    poolStruct.addNativeData(CONNECTION_MANAGER, poolManager);
                }
            }
        }
        return poolManager;
    }

    public static void populatePoolingConfig(MapValue<BString, Long> poolRecord, PoolConfiguration poolConfiguration) {
        long maxActiveConnections = poolRecord.get(HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS);
        poolConfiguration.setMaxActivePerPool(
                validateConfig(maxActiveConnections, HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS));

        long maxIdleConnections = poolRecord.get(HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS);
        poolConfiguration.setMaxIdlePerPool(
                validateConfig(maxIdleConnections, HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS));

        long waitTime = poolRecord.get(HttpConstants.CONNECTION_POOLING_WAIT_TIME);
        poolConfiguration.setMaxWaitTime(waitTime);

        long maxActiveStreamsPerConnection = poolRecord.get(CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION);
        poolConfiguration.setHttp2MaxActiveStreamsPerConnection(
                maxActiveStreamsPerConnection == -1 ? Integer.MAX_VALUE : validateConfig(maxActiveStreamsPerConnection,
                        CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION));
    }

    public static void populateSenderConfigurations(SenderConfiguration senderConfiguration,
                                                    MapValue<BString, Object> clientEndpointConfig, String scheme) {
        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);

        if (secureSocket != null) {
            populateSSLConfiguration(senderConfiguration, secureSocket);
        } else if (scheme.equals(PROTOCOL_HTTPS)) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("To enable https you need to" +
                            " configure secureSocket record")));
        }
        long timeoutMillis = clientEndpointConfig.getIntValue(HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT);
        if (timeoutMillis < 0) {
            senderConfiguration.setSocketIdleTimeout(0);
        } else {
            senderConfiguration.setSocketIdleTimeout(
                    validateConfig(timeoutMillis, HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT));
        }
    }

    /**
     * Populates SSL configuration instance with secure socket configuration.
     *
     * @param sslConfiguration  ssl configuration instance.
     * @param secureSocket    secure socket configuration.
     */
    public static void populateSSLConfiguration(SslConfiguration sslConfiguration,
            MapValue<BString, Object> secureSocket) {
        MapValue trustStore = secureSocket.getMapValue(ENDPOINT_CONFIG_TRUST_STORE);
        MapValue keyStore = secureSocket.getMapValue(ENDPOINT_CONFIG_KEY_STORE);
        MapValue protocols = secureSocket.getMapValue(ENDPOINT_CONFIG_PROTOCOLS);
        MapValue validateCert = secureSocket.getMapValue(ENDPOINT_CONFIG_VALIDATE_CERT);
        String keyFile = secureSocket.getStringValue(ENDPOINT_CONFIG_KEY).getValue();
        String certFile = secureSocket.getStringValue(ENDPOINT_CONFIG_CERTIFICATE).getValue();
        String trustCerts = secureSocket.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES).getValue();
        String keyPassword = secureSocket.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD).getValue();
        boolean disableSslValidation = secureSocket.getBooleanValue(ENDPOINT_CONFIG_DISABLE_SSL);
        List<Parameter> clientParams = new ArrayList<>();
        if (disableSslValidation) {
            sslConfiguration.disableSsl();
            return;
        }
        if (trustStore != null && StringUtils.isNotBlank(trustCerts)) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Cannot configure both " +
                            "trustStore and trustCerts at the same time.")));
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringValue(FILE_PATH).getValue();
            if (StringUtils.isNotBlank(trustStoreFile)) {
                sslConfiguration.setTrustStoreFile(trustStoreFile);
            }
            String trustStorePassword = trustStore.getStringValue(PASSWORD).getValue();
            if (StringUtils.isNotBlank(trustStorePassword)) {
                sslConfiguration.setTrustStorePass(trustStorePassword);
            }
        } else if (StringUtils.isNotBlank(trustCerts)) {
            sslConfiguration.setClientTrustCertificates(trustCerts);
        }
        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Cannot configure both " +
                            "keyStore and keyFile.")));
        } else if (StringUtils.isNotBlank(keyFile) && StringUtils.isBlank(certFile)) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Need to configure certFile " +
                            "containing client ssl certificates.")));
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringValue(FILE_PATH).getValue();
            if (StringUtils.isNotBlank(keyStoreFile)) {
                sslConfiguration.setKeyStoreFile(keyStoreFile);
            }
            String keyStorePassword = keyStore.getStringValue(PASSWORD).getValue();
            if (StringUtils.isNotBlank(keyStorePassword)) {
                sslConfiguration.setKeyStorePass(keyStorePassword);
            }
        } else if (StringUtils.isNotBlank(keyFile)) {
            sslConfiguration.setClientKeyFile(keyFile);
            sslConfiguration.setClientCertificates(certFile);
            if (StringUtils.isNotBlank(keyPassword)) {
                sslConfiguration.setClientKeyPassword(keyPassword);
            }
        }
        if (protocols != null) {
            List<String> sslEnabledProtocolsValueList = Arrays
                    .asList(protocols.getArrayValue(ENABLED_PROTOCOLS).getStringArray());
            if (!sslEnabledProtocolsValueList.isEmpty()) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream()
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientProtocols = new Parameter(ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                clientParams.add(clientProtocols);
            }

            String sslProtocol = protocols.getStringValue(SSL_PROTOCOL_VERSION).getValue();
            if (StringUtils.isNotBlank(sslProtocol)) {
                sslConfiguration.setSSLProtocol(sslProtocol);
            }
        }

        if (validateCert != null) {
            boolean validateCertEnabled = validateCert.getBooleanValue(HttpConstants.ENABLE);
            int cacheSize = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_SIZE).intValue();
            int cacheValidityPeriod = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD)
                    .intValue();
            sslConfiguration.setValidateCertEnabled(validateCertEnabled);
            if (cacheValidityPeriod != 0) {
                sslConfiguration.setCacheValidityPeriod(cacheValidityPeriod);
            }
            if (cacheSize != 0) {
                sslConfiguration.setCacheSize(cacheSize);
            }
        }
        boolean hostNameVerificationEnabled = secureSocket
                .getBooleanValue(HttpConstants.SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED);
        boolean ocspStaplingEnabled = secureSocket.getBooleanValue(HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING);
        sslConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
        sslConfiguration.setHostNameVerificationEnabled(hostNameVerificationEnabled);

        sslConfiguration
                .setSslSessionTimeOut((int) secureSocket.getDefaultableIntValue(ENDPOINT_CONFIG_SESSION_TIMEOUT));

        sslConfiguration.setSslHandshakeTimeOut(secureSocket.getDefaultableIntValue(ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT));

        Object[] cipherConfigs = secureSocket.getArrayValue(HttpConstants.SSL_CONFIG_CIPHERS).getStringArray();
        if (cipherConfigs != null) {
            List<Object> ciphersValueList = Arrays.asList(cipherConfigs);
            if (ciphersValueList.size() > 0) {
                String ciphers = ciphersValueList.stream().map(Object::toString)
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientCiphers = new Parameter(HttpConstants.CIPHERS, ciphers);
                clientParams.add(clientCiphers);
            }
        }
        String enableSessionCreation = String.valueOf(secureSocket
                .getBooleanValue(HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter clientEnableSessionCreation = new Parameter(
            HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION.getValue(), enableSessionCreation);
        clientParams.add(clientEnableSessionCreation);
        if (!clientParams.isEmpty()) {
            sslConfiguration.setParameters(clientParams);
        }
    }

    /**
     * Returns Listener configuration instance populated with endpoint config.
     *
     * @param port              listener port.
     * @param endpointConfig    listener endpoint configuration.
     * @return                  transport listener configuration instance.
     */
    public static ListenerConfiguration getListenerConfig(long port, MapValue<BString, Object> endpointConfig) {
        BString host = endpointConfig.getStringValue(HttpConstants.ENDPOINT_CONFIG_HOST);
        MapValue sslConfig = endpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        long idleTimeout = endpointConfig.getIntValue(HttpConstants.ENDPOINT_CONFIG_TIMEOUT);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();

        if (host == null || host.getValue().trim().isEmpty()) {
            listenerConfiguration.setHost(ConfigRegistry.getInstance().getConfigOrDefault("b7a.http.host",
                    HttpConstants.HTTP_DEFAULT_HOST));
        } else {
            listenerConfiguration.setHost(host.getValue());
        }

        if (port == 0) {
            throw new BallerinaConnectorException("Listener port is not defined!");
        }
        listenerConfiguration.setPort(Math.toIntExact(port));

        if (idleTimeout < 0) {
            throw new BallerinaConnectorException("Idle timeout cannot be negative. If you want to disable the " +
                    "timeout please use value 0");
        }
        listenerConfiguration.setSocketIdleTimeout(Math.toIntExact(idleTimeout));

        // Set HTTP version
        listenerConfiguration.setVersion(Constants.HTTP_2_0);

        if (endpointConfig.getType().getName().equalsIgnoreCase(LISTENER_CONFIGURATION)) {
            BString serverName = endpointConfig.getStringValue(SERVER_NAME);
            listenerConfiguration.setServerHeader(serverName != null ? serverName.getValue() : getServerName());
        } else {
            listenerConfiguration.setServerHeader(getServerName());
        }

        if (sslConfig != null) {
            return setSslConfig(sslConfig, listenerConfiguration);
        }

        listenerConfiguration.setPipeliningEnabled(true); //Pipelining is enabled all the time
        return listenerConfiguration;
    }

    private static String getServerName() {
        String userAgent;
        String version = System.getProperty(BALLERINA_VERSION);
        if (version != null) {
            userAgent = "ballerina/" + version;
        } else {
            userAgent = "ballerina";
        }
        return userAgent;
    }

    private static ListenerConfiguration setSslConfig(MapValue<BString, Object> sslConfig,
            ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(PROTOCOL_HTTPS);
        MapValue trustStore = sslConfig.getMapValue(ENDPOINT_CONFIG_TRUST_STORE);
        MapValue keyStore = sslConfig.getMapValue(ENDPOINT_CONFIG_KEY_STORE);
        MapValue protocols = sslConfig.getMapValue(ENDPOINT_CONFIG_PROTOCOLS);
        MapValue validateCert = sslConfig.getMapValue(ENDPOINT_CONFIG_VALIDATE_CERT);
        MapValue ocspStapling = sslConfig.getMapValue(ENDPOINT_CONFIG_OCSP_STAPLING);
        String keyFile = sslConfig.getStringValue(ENDPOINT_CONFIG_KEY) != null
                ? sslConfig.getStringValue(ENDPOINT_CONFIG_KEY).getValue() : null;
        String certFile = sslConfig.getStringValue(ENDPOINT_CONFIG_CERTIFICATE) != null 
                ? sslConfig.getStringValue(ENDPOINT_CONFIG_CERTIFICATE).getValue() : null;
        String trustCerts = sslConfig.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES) != null
                ? sslConfig.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES).getValue() : null;
        String keyPassword = sslConfig.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD) != null 
                ? sslConfig.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD).getValue() : null;

        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Cannot configure both " +
                            "keyStore and keyFile at the same time.")));
        } else if (keyStore == null && (StringUtils.isBlank(keyFile) || StringUtils.isBlank(certFile))) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Either keystore or " +
                            "certificateKey and server certificates must be provided for secure connection")));
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringValue(FILE_PATH) != null 
                    ? keyStore.getStringValue(FILE_PATH).getValue() : null;
            if (StringUtils.isBlank(keyStoreFile)) {
                throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Keystore file location " +
                                "must be provided for secure connection.")));
            }
            String keyStorePassword = keyStore.getStringValue(PASSWORD) != null 
                    ? keyStore.getStringValue(PASSWORD).getValue() : null;
            if (StringUtils.isBlank(keyStorePassword)) {
                throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Keystore password must " +
                                "be provided for secure connection")));
            }
            listenerConfiguration.setKeyStoreFile(keyStoreFile);
            listenerConfiguration.setKeyStorePass(keyStorePassword);
        } else {
            listenerConfiguration.setServerKeyFile(keyFile);
            listenerConfiguration.setServerCertificates(certFile);
            if (StringUtils.isNotBlank(keyPassword)) {
                listenerConfiguration.setServerKeyPassword(keyPassword);
            }
        }
        String sslVerifyClient = sslConfig.getStringValue(SSL_CONFIG_SSL_VERIFY_CLIENT) != null
                    ? sslConfig.getStringValue(SSL_CONFIG_SSL_VERIFY_CLIENT).getValue() : null;
        listenerConfiguration.setVerifyClient(sslVerifyClient);
        listenerConfiguration
                .setSslSessionTimeOut((int) sslConfig.getDefaultableIntValue(ENDPOINT_CONFIG_SESSION_TIMEOUT));
        listenerConfiguration
                .setSslHandshakeTimeOut(sslConfig.getDefaultableIntValue(ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT));
        if (trustStore == null && StringUtils.isNotBlank(sslVerifyClient) && StringUtils.isBlank(trustCerts)) {
            throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                    .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Truststore location or " +
                            "trustCertificates must be provided to enable Mutual SSL")));
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringValue(FILE_PATH) != null 
                        ? trustStore.getStringValue(FILE_PATH).getValue() : null;
            String trustStorePassword = trustStore.getStringValue(PASSWORD) != null 
                        ? trustStore.getStringValue(PASSWORD).getValue() : null;
            if (StringUtils.isBlank(trustStoreFile) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Truststore location " +
                                "must be provided to enable Mutual SSL")));
            }
            if (StringUtils.isBlank(trustStorePassword) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw MessageUtils.getConnectorError(new StatusRuntimeException(Status
                        .fromCode(Status.Code.INTERNAL.toStatus().getCode()).withDescription("Truststore password " +
                                "value must be provided to enable Mutual SSL")));
            }
            listenerConfiguration.setTrustStoreFile(trustStoreFile);
            listenerConfiguration.setTrustStorePass(trustStorePassword);
        } else if (StringUtils.isNotBlank(trustCerts)) {
            listenerConfiguration.setServerTrustCertificates(trustCerts);
        }
        List<Parameter> serverParamList = new ArrayList<>();
        Parameter serverParameters;
        if (protocols != null) {
            List<String> sslEnabledProtocolsValueList = Arrays.asList(
                    protocols.getArrayValue(ENABLED_PROTOCOLS).getStringArray());
            if (!sslEnabledProtocolsValueList.isEmpty()) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream()
                        .collect(Collectors.joining(",", "", ""));
                serverParameters = new Parameter(ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                serverParamList.add(serverParameters);
            }

            String sslProtocol = protocols.getStringValue(SSL_PROTOCOL_VERSION) != null
                        ? protocols.getStringValue(SSL_PROTOCOL_VERSION).getValue() : null;
            if (StringUtils.isNotBlank(sslProtocol)) {
                listenerConfiguration.setSSLProtocol(sslProtocol);
            }
        }

        List<String> ciphersValueList = Arrays.asList(
                sslConfig.getArrayValue(HttpConstants.SSL_CONFIG_CIPHERS).getStringArray());
        if (!ciphersValueList.isEmpty()) {
            String ciphers = ciphersValueList.stream().collect(Collectors.joining(",", "", ""));
            serverParameters = new Parameter(HttpConstants.CIPHERS, ciphers);
            serverParamList.add(serverParameters);
        }
        if (validateCert != null) {
            boolean validateCertificateEnabled = validateCert.getBooleanValue(HttpConstants.ENABLE);
            long cacheSize = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
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
            boolean ocspStaplingEnabled = ocspStapling.getBooleanValue(HttpConstants.ENABLE);
            listenerConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            long cacheSize = ocspStapling.getIntValue(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = ocspStapling.getIntValue(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
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
        listenerConfiguration.setTLSStoreType(PKCS_STORE_TYPE);
        String serverEnableSessionCreation = String
                .valueOf(sslConfig.getBooleanValue(SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter enableSessionCreationParam = new Parameter(SSL_CONFIG_ENABLE_SESSION_CREATION.getValue(),
                serverEnableSessionCreation);
        serverParamList.add(enableSessionCreationParam);
        if (!serverParamList.isEmpty()) {
            listenerConfiguration.setParameters(serverParamList);
        }

        listenerConfiguration
                .setId(HttpUtil.getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));

        return listenerConfiguration;
    }

    private static int validateConfig(long value, BString configName) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException e) {
            log.warn("The value set for the configuration needs to be less than {}. The " + configName.getValue() +
                    "value is set to {}", Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }
}
