/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.rabbitmq.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConnectorException;
import org.ballerinalang.messaging.rabbitmq.RabbitMQConstants;
import org.ballerinalang.messaging.rabbitmq.RabbitMQUtils;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQMetricsUtil;
import org.ballerinalang.messaging.rabbitmq.observability.RabbitMQObservabilityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Util class for RabbitMQ Connection handling.
 *
 * @since 0.995.0
 */
public class ConnectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionUtils.class);

    /**
     * Creates a RabbitMQ Connection using the given connection parameters.
     *
     * @param connectionConfig Parameters used to initialize the connection.
     * @return RabbitMQ Connection object.
     */
    public static Connection createConnection(MapValue<String, Object> connectionConfig) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();

            // Enable TLS for the connection.
            MapValue secureSocket = connectionConfig.getMapValue(RabbitMQConstants.RABBITMQ_CONNECTION_SECURE_SOCKET);
            if (secureSocket != null) {
                SSLContext sslContext = getSSLContext(secureSocket);
                connectionFactory.useSslProtocol(sslContext);
                if (secureSocket.getBooleanValue(RabbitMQConstants.RABBITMQ_CONNECTION_VERIFY_HOST)) {
                    connectionFactory.enableHostnameVerification();
                }
                LOGGER.info("TLS enabled for the connection.");
            }

            String host = connectionConfig.getStringValue(RabbitMQConstants.RABBITMQ_CONNECTION_HOST);
            connectionFactory.setHost(host);

            int port = Math.toIntExact(connectionConfig.getIntValue(RabbitMQConstants.RABBITMQ_CONNECTION_PORT));
            connectionFactory.setPort(port);

            Object username = connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_USER);
            if (username != null) {
                connectionFactory.setUsername(username.toString());
            }
            Object pass = connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_PASS);
            if (pass != null) {
                connectionFactory.setPassword(pass.toString());
            }
            Object timeout = connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_TIMEOUT);
            if (timeout != null) {
                connectionFactory.setConnectionTimeout(Integer.parseInt(timeout.toString()));
            }
            Object handshakeTimeout = connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_HANDSHAKE_TIMEOUT);
            if (handshakeTimeout != null) {
                connectionFactory.setHandshakeTimeout(Integer.parseInt(handshakeTimeout.toString()));
            }
            Object shutdownTimeout = connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_SHUTDOWN_TIMEOUT);
            if (shutdownTimeout != null) {
                connectionFactory.setShutdownTimeout(Integer.parseInt(shutdownTimeout.toString()));
            }
            Object connectionHeartBeat = connectionConfig.get(RabbitMQConstants.RABBITMQ_CONNECTION_HEARTBEAT);
            if (connectionHeartBeat != null) {
                connectionFactory.setRequestedHeartbeat(Integer.parseInt(connectionHeartBeat.toString()));
            }
            Connection connection = connectionFactory.newConnection();
            RabbitMQMetricsUtil.reportNewConnection(connection);
            return connection;
        } catch (IOException | TimeoutException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_CONNECTION_ERROR
                    + exception.getMessage(), exception);
        }
    }

    private static SSLContext getSSLContext(MapValue secureSocket) {
        try {
            MapValue cryptoKeyStore = secureSocket.getMapValue(RabbitMQConstants.RABBITMQ_CONNECTION_KEYSTORE);
            MapValue cryptoTrustStore = secureSocket.getMapValue(RabbitMQConstants.RABBITMQ_CONNECTION_TRUSTORE);
            char[] keyPassphrase = cryptoKeyStore.getStringValue(RabbitMQConstants.KEY_STORE_PASS).toCharArray();
            String keyFilePath = cryptoKeyStore.getStringValue(RabbitMQConstants.KEY_STORE_PATH);
            char[] trustPassphrase = cryptoTrustStore.getStringValue(RabbitMQConstants.KEY_STORE_PASS).toCharArray();
            String trustFilePath = cryptoTrustStore.getStringValue(RabbitMQConstants.KEY_STORE_PATH);
            String tlsVersion = secureSocket.getStringValue(RabbitMQConstants.RABBITMQ_CONNECTION_TLS_VERSION);

            KeyStore keyStore = KeyStore.getInstance(RabbitMQConstants.KEY_STORE_TYPE);
            if (keyFilePath != null) {
                try (FileInputStream keyFileInputStream = new FileInputStream(keyFilePath)) {
                    keyStore.load(keyFileInputStream, keyPassphrase);
                }
            } else {
                RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
                throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                        "Path for the keystore is not found.");
            }
            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyPassphrase);

            KeyStore trustStore = KeyStore.getInstance(RabbitMQConstants.KEY_STORE_TYPE);
            if (trustFilePath != null) {
                try (FileInputStream trustFileInputStream = new FileInputStream(trustFilePath)) {
                    trustStore.load(trustFileInputStream, trustPassphrase);
                }
            } else {
                RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
                throw new RabbitMQConnectorException("Path for the truststore is not found.");
            }
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance(tlsVersion);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (FileNotFoundException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    exception.getLocalizedMessage());
        } catch (IOException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "I/O error occurred.");
        } catch (CertificateException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "Certification error occurred.");
        } catch (UnrecoverableKeyException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "A key in the keystore cannot be recovered.");
        } catch (NoSuchAlgorithmException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "The particular cryptographic algorithm requested is not available in the environment.");
        } catch (KeyStoreException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "No provider supports a KeyStoreSpi implementation for this keystore type." +
                    exception.getLocalizedMessage());
        } catch (KeyManagementException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION);
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "Error occurred in an operation with key management." +
                    exception.getLocalizedMessage());
        }
    }

    public static boolean isClosed(Connection connection) {
        return connection == null || !connection.isOpen();
    }

    public static Object handleCloseConnection(Object closeCode, Object closeMessage, Object timeout,
                                               Connection connection) {
        boolean validTimeout = timeout != null && RabbitMQUtils.checkIfInt(timeout);
        boolean validCloseCode = (closeCode != null && RabbitMQUtils.checkIfInt(closeCode)) &&
                (closeMessage != null && RabbitMQUtils.checkIfString(closeMessage));
        try {
            if (validTimeout && validCloseCode) {
                connection.close(Integer.parseInt(closeCode.toString()), closeMessage.toString(),
                        Integer.parseInt(timeout.toString()));
            } else if (validTimeout) {
                connection.close(Integer.parseInt(timeout.toString()));
            } else if (validCloseCode) {
                connection.close(Integer.parseInt(closeCode.toString()), closeMessage.toString());
            } else {
                connection.close();
            }
            RabbitMQMetricsUtil.reportConnectionClose(connection);
        } catch (IOException | ArithmeticException exception) {
            RabbitMQMetricsUtil.reportError(RabbitMQObservabilityConstants.ERROR_TYPE_CONNECTION_CLOSE);
            return RabbitMQUtils.returnErrorValue("Error occurred while closing the connection: "
                    + exception.getMessage());
        }
        return null;
    }

    public static void handleAbortConnection(Object closeCode, Object closeMessage, Object timeout,
                                             Connection connection) {
        boolean validTimeout = timeout != null && RabbitMQUtils.checkIfInt(timeout);
        boolean validCloseCode = (closeCode != null && RabbitMQUtils.checkIfInt(closeCode)) &&
                (closeMessage != null && RabbitMQUtils.checkIfString(closeMessage));
        if (validTimeout && validCloseCode) {
            connection.abort(Integer.parseInt(closeCode.toString()), closeMessage.toString(),
                    Integer.parseInt(timeout.toString()));
        } else if (validTimeout) {
            connection.abort(Integer.parseInt(timeout.toString()));
        } else if (validCloseCode) {
            connection.abort(Integer.parseInt(closeCode.toString()), closeMessage.toString());
        } else {
            connection.abort();
        }
        RabbitMQMetricsUtil.reportConnectionClose(connection);
    }

    private ConnectionUtils() {
    }
}
