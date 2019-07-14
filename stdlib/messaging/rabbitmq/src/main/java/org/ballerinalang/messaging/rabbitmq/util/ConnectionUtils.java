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
    private static final Logger logger = LoggerFactory.getLogger(ConnectionUtils.class);

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
                logger.info("TLS enabled for the connection.");
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
            return connectionFactory.newConnection();
        } catch (IOException | TimeoutException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_CONNECTION_ERROR
                    + exception.getMessage(), exception);
        }
    }

    /**
     * Creates and retrieves the initialized SSLContext.
     *
     * @param secureSocket secureSocket record.
     * @return Initialized SSLContext.
     */
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
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "I/O error occurred.");
        } catch (CertificateException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "Certification error occurred.");
        } catch (UnrecoverableKeyException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "A key in the keystore cannot be recovered.");
        } catch (NoSuchAlgorithmException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "The particular cryptographic algorithm requested is not available in the environment.");
        } catch (KeyStoreException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "No provider supports a KeyStoreSpi implementation for this keystore type." +
                    exception.getLocalizedMessage());
        } catch (KeyManagementException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CREATE_SECURE_CONNECTION_ERROR +
                    "Error occurred in an operation with key management." +
                    exception.getLocalizedMessage());
        }
    }

    /**
     * Handles closing the given connection.
     *
     * @param connection   RabbitMQ Connection object.
     * @param timeout      Timeout (in milliseconds) for completing all the close-related
     *                     operations, use -1 for infinity.
     * @param closeCode    The close code (See under "Reply Codes" in the AMQP specification).
     * @param closeMessage A message indicating the reason for closing the connection.
     */
    public static void handleCloseConnection(Connection connection, Object closeCode, Object closeMessage,
                                             Object timeout) {
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
        } catch (IOException | ArithmeticException exception) {
            throw new RabbitMQConnectorException(RabbitMQConstants.CLOSE_CONNECTION_ERROR + exception.getMessage(),
                    exception);
        }
    }

    /**
     * Handles aborting the given connection.
     *
     * @param connection   RabbitMQ Connection object.
     * @param timeout      Timeout (in milliseconds) for completing all the close-related
     *                     operations, use -1 for infinity.
     * @param closeCode    The close code (See under "Reply Codes" in the AMQP specification).
     * @param closeMessage A message indicating the reason for closing the connection.
     */
    public static void handleAbortConnection(Connection connection, Object closeCode, Object closeMessage,
                                             Object timeout) {
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
    }

    /**
     * Checks if close was already called on the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @return True if the connection is already closed and false otherwise.
     */
    public static boolean isClosed(Connection connection) {
        boolean flag = false;
        if (connection == null || !connection.isOpen()) {
            flag = true;
        }
        return flag;
    }

    private ConnectionUtils() {
    }
}
