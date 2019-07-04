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

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * Util class for RabbitMQ Connection handling.
 *
 * @since 0.995.0
 */
public class ConnectionUtils {

    /**
     * Creates a RabbitMQ Connection using the given connection parameters.
     *
     * @param connectionConfig Parameters used to initialize the connection.
     * @return RabbitMQ Connection object.
     */
    public static Connection createConnection(MapValue<String, Object> connectionConfig) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();

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
                close(connection, Integer.parseInt(closeCode.toString()), closeMessage.toString(),
                        Integer.parseInt(timeout.toString()));
            } else if (validTimeout) {
                close(connection, Integer.parseInt(timeout.toString()));
            } else if (validCloseCode) {
                close(connection, Integer.parseInt(closeCode.toString()), closeMessage.toString());
            } else {
                close(connection);
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
            abortConnection(connection, Integer.parseInt(closeCode.toString()), closeMessage.toString(),
                    Integer.parseInt(timeout.toString()));
        } else if (validTimeout) {
            abortConnection(connection, Integer.parseInt(timeout.toString()));
        } else if (validCloseCode) {
            abortConnection(connection, Integer.parseInt(closeCode.toString()), closeMessage.toString());
        } else {
            abortConnection(connection);
        }
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @throws IOException If an I/O problem is encountered.
     */
    private static void close(Connection connection) throws IOException {
        connection.close();
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @throws IOException If an I/O problem is encountered.
     */
    private static void close(Connection connection, int closeCode, String closeMessage) throws IOException {
        connection.close(closeCode, closeMessage);
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @param timeout    Timeout (in milliseconds) for completing all the close-related operations, use -1 for infinity.
     * @throws IOException If an I/O problem is encountered.
     */
    private static void close(Connection connection, int timeout) throws IOException {
        connection.close(timeout);
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @throws IOException If an I/O problem is encountered.
     */
    private static void close(Connection connection, int closeCode, String closeMessage, int timeout)
            throws IOException {
        connection.close(closeCode, closeMessage, timeout);
    }

    /**
     * Aborts the connection.
     *
     * @param connection RabbitMQ Connection object.
     */
    private static void abortConnection(Connection connection) {
        connection.abort();
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     */
    private static void abortConnection(Connection connection, int closeCode, String closeMessage) {
        connection.abort(closeCode, closeMessage);
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @param timeout    Timeout (in milliseconds) for completing all the close-related operations, use -1 for infinity.
     */
    private static void abortConnection(Connection connection, int timeout) {
        connection.abort(timeout);
    }

    /**
     * Closes the connection.
     *
     * @param connection RabbitMQ Connection object.
     * @param timeout    Timeout (in milliseconds) for completing all the close-related operations, use -1 for infinity.
     */
    private static void abortConnection(Connection connection, int closeCode, String closeMessage,
                                        int timeout) {
        connection.abort(closeCode, closeMessage, timeout);
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
